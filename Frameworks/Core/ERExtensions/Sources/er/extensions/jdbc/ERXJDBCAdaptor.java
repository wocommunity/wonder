package er.extensions.jdbc;

import java.lang.reflect.Constructor;
import java.lang.reflect.Field;
import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.Iterator;

import com.webobjects.eoaccess.EOAdaptor;
import com.webobjects.eoaccess.EOAdaptorChannel;
import com.webobjects.eoaccess.EOAdaptorContext;
import com.webobjects.eoaccess.EOAttribute;
import com.webobjects.eoaccess.EOEntity;
import com.webobjects.eoaccess.EOGeneralAdaptorException;
import com.webobjects.eoaccess.EOSQLExpression;
import com.webobjects.eoaccess.EOStoredProcedure;
import com.webobjects.eocontrol.EOFetchSpecification;
import com.webobjects.eocontrol.EOQualifier;
import com.webobjects.foundation.NSArray;
import com.webobjects.foundation.NSDictionary;
import com.webobjects.foundation.NSForwardException;
import com.webobjects.foundation.NSMutableArray;
import com.webobjects.foundation.NSMutableDictionary;
import com.webobjects.foundation._NSUtilities;
import com.webobjects.jdbcadaptor.ERXJDBCColumn;
import com.webobjects.jdbcadaptor.JDBCAdaptor;
import com.webobjects.jdbcadaptor.JDBCAdaptorException;
import com.webobjects.jdbcadaptor.JDBCChannel;
import com.webobjects.jdbcadaptor.JDBCContext;
import com.webobjects.jdbcadaptor.JDBCPlugIn;

import er.extensions.eof.ERXAdaptorOperationWrapper;
import er.extensions.foundation.ERXKeyValueCodingUtilities;
import er.extensions.foundation.ERXPatcher;
import er.extensions.foundation.ERXProperties;
import er.extensions.foundation.ERXSystem;
import er.extensions.foundation.ERXValueUtilities;

/**
 * Subclass of the JDBC adaptor and accompanying classes that supports
 * connection pooling and posting of adaptor operation notifications. Will get
 * patched into the runtime via the usual class name magic if the property
 * <code>er.extensions.ERXJDBCAdaptor.className</code> is set to this class's
 * name or another subclass of JDBCAdaptor. The connection pooling will be
 * enabled if the system property
 * <code>er.extensions.ERXJDBCAdaptor.useConnectionBroker</code> is set.
 * 
 * @author ak
 * 
 */
public class ERXJDBCAdaptor extends JDBCAdaptor {

	public static interface ConnectionBroker {
		public void freeConnection(Connection conn);

		public Connection getConnection();
	}

	public static final String USE_CONNECTION_BROKER_KEY = "er.extensions.ERXJDBCAdaptor.useConnectionBroker";

	public static final String CLASS_NAME_KEY = "er.extensions.ERXJDBCAdaptor.className";

	private static Boolean switchReadWrite = null;
	private static Boolean useConnectionBroker = null;

	static boolean switchReadWrite() {
		if (switchReadWrite == null) {
			switchReadWrite = "false".equals(ERXSystem.getProperty("er.extensions.ERXJDBCAdaptor.switchReadWrite", "false")) ? Boolean.FALSE : Boolean.TRUE;
		}
		return switchReadWrite.booleanValue();
	}

	/**
	 * Returns whether the connection broker is active.
	 * 
	 * @return <code>true</code> if connection broker is active
	 */
	public static boolean useConnectionBroker() {
		if (useConnectionBroker == null) {
			useConnectionBroker = ERXProperties.booleanForKeyWithDefault(USE_CONNECTION_BROKER_KEY, false) ? Boolean.TRUE : Boolean.FALSE;
		}
		return useConnectionBroker.booleanValue();
	}

	public static void registerJDBCAdaptor() {
		String className = ERXProperties.stringForKey(CLASS_NAME_KEY);
		if (className != null) {
			Class c = ERXPatcher.classForName(className);
			if (c == null) {
				throw new IllegalStateException("Can't find class: " + className);
			}
			ERXPatcher.setClassForName(c, JDBCAdaptor.class.getName());
		}
	}

	/**
	 * Channel subclass to support notification posting.
	 * 
	 * @author ak
	 */
	public static class Channel extends JDBCChannel {

		public static final String COLUMN_CLASS_NAME_KEY = "er.extensions.ERXJDBCAdaptor.columnClassName";
		
		private static Class columnClass;

		/**
		 * The class of the JDBCColumn. It must subclass ERXJDBCColumn and provide 
		 * implementations for the same two constructors as ERXJDBCColumn. It is set
		 * using the property <code>er.extensions.ERXJDBCAdaptor.columnClassName</code>
		 * If no value is set, then the default class is ERXJDBCColumn.
		 * 
		 * @return The ERXJDBCColumn subclass
		 */
		public static Class columnClass() {
			if(columnClass == null) {
				String className = ERXProperties.stringForKey(COLUMN_CLASS_NAME_KEY);
				if(className != null && className.length() > 0) {
					columnClass = _NSUtilities.classWithName(className);
				} else {
					columnClass = ERXJDBCColumn.class;
				}
			}
			return columnClass;
		}
		
		public static ERXJDBCColumn newERXJDBCColumn(Channel channel) {
			try {
				Constructor<? extends ERXJDBCColumn> cstr = columnClass().getDeclaredConstructor(Channel.class);
				return cstr.newInstance(channel);
			} catch(Exception e) {
				throw NSForwardException._runtimeExceptionForThrowable(e);
			}
		}
		
		public static ERXJDBCColumn newERXJDBCColumn(EOAttribute attribute, JDBCChannel channel, int column, ResultSet rs) {
			try {
				Constructor<? extends ERXJDBCColumn> cstr = columnClass().getDeclaredConstructor(EOAttribute.class, JDBCChannel.class, Integer.TYPE, ResultSet.class);
				return cstr.newInstance(attribute, channel, column, rs);
			} catch(Exception e) {
				throw NSForwardException._runtimeExceptionForThrowable(e);
			}
		}
		
		public Channel(JDBCContext jdbccontext) {
			super(jdbccontext);
			try {
				Field field = JDBCChannel.class.getDeclaredField("_inputColumn");
				field.setAccessible(true);
				field.set(this, newERXJDBCColumn(this));
			}
			catch (Exception e) {
				System.err.println(e);
				e.printStackTrace();
				System.exit(1);
			}
		}

		@Override
		public void setAttributesToFetch(NSArray<EOAttribute> attributes) {
			_attributes = attributes;
			int j;
			if (_attributes == null || (j = _attributes.count()) == 0)
				return;
			ERXJDBCColumn columns[] = new ERXJDBCColumn[j];
			for (int i = 0; i < j; i++)
				columns[i] = newERXJDBCColumn(_attributes.objectAtIndex(i), this, i + 1, _resultSet);

			_selectedColumns = new NSArray(columns);
		}

		private boolean setReadOnly(boolean mode) {
			boolean old = false;
			if (switchReadWrite()) {
				try {
					Connection connection = ((JDBCContext) adaptorContext()).connection();
					if (connection != null) {
						old = connection.isReadOnly();
						connection.setReadOnly(mode);
					}
					else {
						throw new EOGeneralAdaptorException("Can't switch connection mode to " + mode + ", the connection is null");
					}
				}
				catch (java.sql.SQLException e) {
					throw new EOGeneralAdaptorException("Can't switch connection mode to " + mode, new NSDictionary(e, "originalException"));
				}
			}
			return old;
		}

		/**
		 * Overridden to switch the connection to read-only while selecting.
		 */
		@Override
		public void selectAttributes(NSArray array, EOFetchSpecification fetchspecification, boolean lock, EOEntity entity) {
			boolean mode = setReadOnly(!lock);
			super.selectAttributes(array, fetchspecification, lock, entity);
			setReadOnly(mode);
		}

		/**
		 * Overridden to post a notification when the operations were performed.
		 */
		@Override
		public void performAdaptorOperations(NSArray ops) {
			super.performAdaptorOperations(ops);
			ERXAdaptorOperationWrapper.adaptorOperationsDidPerform(ops);
		}

		private JDBCPlugIn _plugIn() {
			JDBCAdaptor jdbcadaptor = (JDBCAdaptor) adaptorContext().adaptor();
			return jdbcadaptor.plugIn();
		}

		private static NSMutableDictionary<String, NSMutableArray> pkCache = new NSMutableDictionary<String, NSMutableArray>();
		private int defaultBatchSize = ERXProperties.intForKeyWithDefault("er.extensions.ERXPrimaryKeyBatchSize", -1);
		
		/**
		 * Batch-fetches new primary keys. Set the property
		 * <code> er.extensions.ERXPrimaryKeyBatchSize</code> to a number
		 * larger than 0. Also, you can fine-tune the size by adding a key
		 * <code>ERXPrimaryKeyBatchSize</code> to your model or entity user
		 * info.
		 */
		@Override
		public NSArray primaryKeysForNewRowsWithEntity(int cnt, EOEntity entity) {
			if (defaultBatchSize > 0) {
				synchronized (pkCache) {
					String key = entity.primaryKeyRootName();
					NSMutableArray pks = pkCache.objectForKey(key);
					if (pks == null) {
						pks = new NSMutableArray();
						pkCache.setObjectForKey(pks, key);
					}
					if (pks.count() < cnt) {
						Object batchSize = (entity.userInfo() != null ? entity.userInfo().objectForKey("ERXPrimaryKeyBatchSize") : null);
						if (batchSize == null) {
							batchSize = (entity.model().userInfo() != null ? entity.model().userInfo().objectForKey("ERXPrimaryKeyBatchSize") : null);
						}
						if (batchSize == null) {
							batchSize = ERXProperties.stringForKey("er.extensions.ERXPrimaryKeyBatchSize");
						}
						int size = defaultBatchSize;
						if (batchSize != null) {
							size = ERXValueUtilities.intValue(batchSize);
						}
						pks.addObjectsFromArray(_plugIn().newPrimaryKeys(size + cnt, entity, this));
					}
					NSMutableArray batch = new NSMutableArray();
					for (Iterator iterator = pks.iterator(); iterator.hasNext() && --cnt >= 0;) {
						Object pk = iterator.next();
						batch.addObject(pk);
						iterator.remove();
					}
					return batch;
				}
			}
			return _plugIn().newPrimaryKeys(cnt, entity, this);
		}
		
		private void cleanup() {
			Boolean value = (Boolean) ERXKeyValueCodingUtilities.privateValueForKey(this, "_beganTransaction");
			if (value) {
				try {
					_context.rollbackTransaction();
				}
				catch (JDBCAdaptorException ex) {
					ERXKeyValueCodingUtilities.takePrivateValueForKey(this, Boolean.FALSE, "_beganTransaction");
					throw ex;
				}
			}
		}
		
		/**
		 * Overridden to clean up after a transaction fails.
		 */
		@Override
		public void evaluateExpression(EOSQLExpression eosqlexpression) {
			try {
				super.evaluateExpression(eosqlexpression);
			}
			catch (JDBCAdaptorException ex) {
				cleanup();
				throw ex;
			}
		}
		
		/**
		 * Overridden to clean up after a transaction fails.
		 */
		@Override
		public void executeStoredProcedure(EOStoredProcedure eostoredprocedure, NSDictionary nsdictionary) {
			try {
				super.executeStoredProcedure(eostoredprocedure, nsdictionary);
			}
			catch (JDBCAdaptorException ex) {
				cleanup();
				throw ex;
			}
		}
		
		/**
		 * Overridden to clean up after a transaction fails.
		 */
		@Override
		public int deleteRowsDescribedByQualifier(EOQualifier eoqualifier, EOEntity eoentity) {
			try {
				return super.deleteRowsDescribedByQualifier(eoqualifier, eoentity);
			}
			catch (JDBCAdaptorException ex) {
				cleanup();
				throw ex;
			}
		}

		/**
		 * Overridden to clea up after a transaction fails.
		 */
		@Override
		public int updateValuesInRowsDescribedByQualifier(NSDictionary nsdictionary, EOQualifier eoqualifier, EOEntity eoentity) {
			try {
				return super.updateValuesInRowsDescribedByQualifier(nsdictionary, eoqualifier, eoentity);
			}
			catch (JDBCAdaptorException ex) {
				cleanup();
				throw ex;
			}
		}
	}

	/**
	 * Context subclass that uses connection pooling.
	 * 
	 * @author ak
	 */
	public static class Context extends JDBCContext {

		public Context(EOAdaptor eoadaptor) {
			super(eoadaptor);
		}

		private void freeConnection() {
			if (useConnectionBroker()) {
				if (_jdbcConnection != null) {
					((ERXJDBCAdaptor) adaptor()).freeConnection(_jdbcConnection);
					_jdbcConnection = null;
				}
			}
		}

		/**
		 * Re-implemented to fix: http://www.mail-archive.com/dspace-tech@lists.sourceforge.net/msg06063.html.
		 * We could also use the delegate, but where would be the fun in that?
		 */
		@Override
		public void rollbackTransaction() {
			if (!hasOpenTransaction()) {
				return;
			}
			if (((Number)ERXKeyValueCodingUtilities.privateValueForKey(this, "_fetchesInProgress")).intValue() > 0) {
				throw new JDBCAdaptorException("Cannot rollbackTransaction() while a fetch is in progress", null);
			}
			if (_delegateRespondsTo_shouldRollback && !_delegate.booleanPerform("adaptorContextShouldRollback", this))
				return;
			try {
				if (_connectionSupportTransaction) {
					// AK: only roll back if the connection isn't closed.
					if(!_jdbcConnection.isClosed()) {
						_jdbcConnection.rollback();
					}
				}
			}
			catch (SQLException sqlexception) {
				throw new JDBCAdaptorException(sqlexception);
			}
			transactionDidRollback();
			if (_delegateRespondsTo_didRollback) {
				_delegate.perform("adaptorContextDidRollback", this);
			}
		}

		private void checkoutConnection() {
			if (useConnectionBroker()) {
				if (_jdbcConnection == null) {
					_jdbcConnection = ((ERXJDBCAdaptor) adaptor()).checkoutConnection();
				}
			}
		}

		@Override
		public boolean connect() throws JDBCAdaptorException {
			boolean connected = false;
			if (useConnectionBroker()) {
				checkoutConnection();
				connected = _jdbcConnection != null;
			}
			else {
				connected = super.connect();
			}
			return connected;
		}

		protected JDBCChannel createJDBCChannel() {
			return new Channel(this);
		}

		@Override
		protected JDBCChannel _cachedAdaptorChannel() {
			if (_cachedChannel == null) {
				_cachedChannel = createJDBCChannel();
			}
			return _cachedChannel;
		}

		@Override
		public EOAdaptorChannel createAdaptorChannel() {
			if (_cachedChannel != null) {
				JDBCChannel jdbcchannel = _cachedChannel;
				_cachedChannel = null;
				return jdbcchannel;
			}
			return createJDBCChannel();
		}

		@Override
		public void disconnect() throws JDBCAdaptorException {
			freeConnection();
			super.disconnect();
		}

		@Override
		public void beginTransaction() {
			checkoutConnection();
			super.beginTransaction();
		}

		@Override
		public void transactionDidCommit() {
			super.transactionDidCommit();
			freeConnection();
		}

		@Override
		public void transactionDidRollback() {
			super.transactionDidRollback();
			freeConnection();
		}
	}

	public ERXJDBCAdaptor(String name) {
		super(name);
	}

	@Override
	protected JDBCContext _cachedAdaptorContext() {
		if (_cachedContext == null) {
			_cachedContext = createJDBCContext();
		}
		return _cachedContext;
	}

	@Override
	protected NSDictionary jdbcInfo() {
		boolean closeCachedContext = (_cachedContext == null && _jdbcInfo == null);
		NSDictionary jdbcInfo = super.jdbcInfo();
		if (closeCachedContext && _cachedContext != null) {
			_cachedContext.disconnect();
			_cachedContext = null;
		}
		return jdbcInfo;
	}

	@Override
	protected NSDictionary typeInfo() {
		boolean closeCachedContext = (_cachedContext == null && _jdbcInfo == null);
		NSDictionary typeInfo = super.typeInfo();
		if (closeCachedContext && _cachedContext != null) {
			_cachedContext.disconnect();
			_cachedContext = null;
		}
		return typeInfo;
	}

	public Context createJDBCContext() {
		Context context = new Context(this);
		return context;
	}

	@Override
	public EOAdaptorContext createAdaptorContext() {
		EOAdaptorContext context;
		if (_cachedContext != null) {
			context = _cachedContext;
			_cachedContext = null;
		}
		else {
			context = createJDBCContext();
		}
		return context;
	}

	protected Connection checkoutConnection() {
		Connection c = connectionBroker().getConnection();
		return c;
	}

	private ConnectionBroker connectionBroker() {
		return ERXJDBCConnectionBroker.connectionBrokerForAdaptor(this);
	}

	protected void freeConnection(Connection connection) {
		connectionBroker().freeConnection(connection);
	}
}
