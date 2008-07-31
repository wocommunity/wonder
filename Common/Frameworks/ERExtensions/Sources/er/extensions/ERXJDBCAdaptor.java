package er.extensions;

import java.lang.reflect.Field;
import java.sql.Connection;
import java.util.Enumeration;

import org.apache.log4j.Logger;

import com.webobjects.eoaccess.EOAdaptor;
import com.webobjects.eoaccess.EOAdaptorChannel;
import com.webobjects.eoaccess.EOAdaptorContext;
import com.webobjects.eoaccess.EOAttribute;
import com.webobjects.eoaccess.EOEntity;
import com.webobjects.eoaccess.EOGeneralAdaptorException;
import com.webobjects.eocontrol.EOFetchSpecification;
import com.webobjects.foundation.NSArray;
import com.webobjects.foundation.NSDictionary;
import com.webobjects.foundation.NSMutableArray;
import com.webobjects.foundation.NSMutableDictionary;
import com.webobjects.jdbcadaptor.ERXJDBCColumn;
import com.webobjects.jdbcadaptor.JDBCAdaptor;
import com.webobjects.jdbcadaptor.JDBCAdaptorException;
import com.webobjects.jdbcadaptor.JDBCChannel;
import com.webobjects.jdbcadaptor.JDBCContext;
import com.webobjects.jdbcadaptor.JDBCPlugIn;

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

	public static final Logger log = Logger.getLogger(ERXJDBCAdaptor.class);

	public static final String USE_CONNECTION_BROKER_KEY = "er.extensions.ERXJDBCAdaptor.useConnectionBroker";

	public static final String CLASS_NAME_KEY = "er.extensions.ERXJDBCAdaptor.className";

	private static Boolean switchReadWrite = null;
	private static Boolean useConnectionBroker = null;

	private static boolean switchReadWrite() {
		if (switchReadWrite == null) {
			switchReadWrite = "false".equals(ERXSystem.getProperty("er.extensions.ERXJDBCAdaptor.switchReadWrite", "false")) ? Boolean.FALSE : Boolean.TRUE;
		}
		return switchReadWrite.booleanValue();
	}

	/**
	 * Returns whether the connection broker is active.
	 * 
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
	 * 
	 */
	public static class Channel extends JDBCChannel {

		public Channel(JDBCContext jdbccontext) {
			super(jdbccontext);
			try {
				Field field = JDBCChannel.class.getDeclaredField("_inputColumn");
				field.setAccessible(true);
				field.set(this, new ERXJDBCColumn(this));
			}
			catch (Exception e) {
				System.err.println(e);
				e.printStackTrace();
				System.exit(1);
			}
		}

		public void setAttributesToFetch(NSArray attributes) {
			_attributes = attributes;
			int j;
			if (_attributes == null || (j = _attributes.count()) == 0)
				return;
			ERXJDBCColumn columns[] = new ERXJDBCColumn[j];
			for (int i = 0; i < j; i++)
				columns[i] = new ERXJDBCColumn((EOAttribute) _attributes.objectAtIndex(i), this, i + 1, _resultSet);

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
		public void selectAttributes(NSArray array, EOFetchSpecification fetchspecification, boolean lock, EOEntity entity) {
			boolean mode = setReadOnly(!lock);
			super.selectAttributes(array, fetchspecification, lock, entity);
			setReadOnly(mode);
		}

		/**
		 * Overridden to post a notification when the operations were performed.
		 */
		public void performAdaptorOperations(NSArray ops) {
			super.performAdaptorOperations(ops);
			ERXAdaptorOperationWrapper.adaptorOperationsDidPerform(ops);
		}

		private JDBCPlugIn _plugIn() {
			JDBCAdaptor jdbcadaptor = (JDBCAdaptor) adaptorContext().adaptor();
			return jdbcadaptor.plugIn();
		}

		private static NSMutableDictionary pkCache = new NSMutableDictionary();
		private int defaultBatchSize = ERXProperties.intForKeyWithDefault("er.extensions.ERXPrimaryKeyBatchSize", -1);
		
		/**
		 * Batch-fetches new primary keys. Set the property
		 * <code> er.extensions.ERXPrimaryKeyBatchSize</code> to a number
		 * larger than 0. Also, you can fine-tune the size by adding a key
		 * <code>ERXPrimaryKeyBatchSize</code> to your model or entity user
		 * info.
		 */
		public NSArray primaryKeysForNewRowsWithEntity(int cnt, EOEntity entity) {
			if (defaultBatchSize > 0) {
				synchronized (pkCache) {
					String key = entity.primaryKeyRootName();
					NSMutableArray pks = (NSMutableArray)pkCache.objectForKey(key);
					if (pks == null) {
						pks = new NSMutableArray();
						pkCache.setObjectForKey(pks, key);
					}
					if (pks.count() < cnt) {
						Object batchSize = (String) (entity.userInfo() != null ? entity.userInfo().objectForKey("ERXPrimaryKeyBatchSize") : null);
						if (batchSize == null) {
							batchSize = (String) (entity.model().userInfo() != null ? entity.model().userInfo().objectForKey("ERXPrimaryKeyBatchSize") : null);
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
					for (Enumeration pksEnum = pks.objectEnumerator(); pksEnum.hasMoreElements() && --cnt >= 0;) {
						Object pk = (Object) pksEnum.nextElement();
						batch.addObject(pk);
						pks.removeObject(pk);
					}
					return batch;
				}
			}
			return _plugIn().newPrimaryKeys(cnt, entity, this);
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

		private void checkoutConnection() {
			if (useConnectionBroker()) {
				if (_jdbcConnection == null) {
					_jdbcConnection = ((ERXJDBCAdaptor) adaptor()).checkoutConnection();
				}
			}
		}

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

		protected JDBCChannel _cachedAdaptorChannel() {
			if (_cachedChannel == null) {
				_cachedChannel = createJDBCChannel();
			}
			return _cachedChannel;
		}

		public EOAdaptorChannel createAdaptorChannel() {
			if (_cachedChannel != null) {
				JDBCChannel jdbcchannel = _cachedChannel;
				_cachedChannel = null;
				return jdbcchannel;
			}
			else {
				return createJDBCChannel();
			}
		}

		public void disconnect() throws JDBCAdaptorException {
			freeConnection();
			super.disconnect();
		}

		public void beginTransaction() {
			checkoutConnection();
			super.beginTransaction();
		}

		public void transactionDidCommit() {
			super.transactionDidCommit();
			freeConnection();
		}

		public void transactionDidRollback() {
			super.transactionDidRollback();
			freeConnection();
		}
	}

	public ERXJDBCAdaptor(String s) {
		super(s);
	}

	protected JDBCContext _cachedAdaptorContext() {
		if (_cachedContext == null) {
			_cachedContext = createJDBCContext();
		}
		return _cachedContext;
	}

	protected NSDictionary jdbcInfo() {
		boolean closeCachedContext = (_cachedContext == null && _jdbcInfo == null);
		NSDictionary jdbcInfo = super.jdbcInfo();
		if (closeCachedContext && _cachedContext != null) {
			_cachedContext.disconnect();
			_cachedContext = null;
		}
		return jdbcInfo;
	}

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
