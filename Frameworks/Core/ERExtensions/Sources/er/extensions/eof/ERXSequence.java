package er.extensions.eof;

import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

import org.apache.log4j.Logger;

import com.webobjects.eoaccess.EOAdaptor;
import com.webobjects.eoaccess.EOEntity;
import com.webobjects.eoaccess.EOModel;
import com.webobjects.eoaccess.EOSQLExpression;
import com.webobjects.eoaccess.EOSQLExpressionFactory;
import com.webobjects.eocontrol.EOEditingContext;
import com.webobjects.eocontrol.EOQualifier;
import com.webobjects.foundation.NSForwardException;

import er.extensions.foundation.ERXProperties;
import er.extensions.jdbc.ERXJDBCConnectionBroker;
import er.extensions.jdbc.ERXSQLHelper;

/**
 * Simple sequence class. MT safe, but not multi instance safe (this is implemented by subclasses)
 * @author ak
 *
 */
public class ERXSequence {
	
	protected Logger log;
	
	private final String _name;
	protected long _lastValue;
	protected long _maxValue;
	
	public ERXSequence(String name) {
		_name = name;
		log = Logger.getLogger(name);
	}

	public ERXSequence(String name, long initialValue) {
		this(name);
		_lastValue = initialValue;
	}

	public String name() {
		return _name;
	}

	public long nextValue() {
		return nextValue(1L);
	}
	
	protected long maxValue() {
		return _maxValue;
	}
	
	public long nextValue(long increment) {
		synchronized (this) {
			long diff = (lastValue() + increment) - maxValue();
			if(diff > 0) {
				long nextIncrement = increment();
				_maxValue = increasedMaxValue(diff > nextIncrement ? diff : nextIncrement);
			}
			_lastValue += increment;
			return lastValue();
		}
	}

	protected long lastValue() {
		return _lastValue;
	}

	protected long increasedMaxValue(long increment) {
		return maxValue() + increment;
	}

	protected long increment() {
		return ERXProperties.longForKeyWithDefault(name() + ".Increment", 
				ERXProperties.longForKeyWithDefault("er.extensions.ERXSequence.Increment", 10L));
	}

	/**
	 * NativeDatabaseSequence uses ERXSQLHelper.getNextValFromSequenceNamed to
	 * generate a sequence value using your database's native sequence generation scheme.  This
	 * will fail if ERXSQLHelper.getNextValFromSequenceNamed is not implemented for your database.
	 * This is also currently limited to only incrementing 1 at a time.
	 * 
	 * @author mschrag
	 */
	public static class NativeDatabaseSequence extends ERXSequence {
		private EOEditingContext _editingContext;
		private String _modelName;
		
		public NativeDatabaseSequence(EOEditingContext editingContext, String modelName, String name) {
			super(name);
		    _editingContext=editingContext;
		    _modelName=modelName;
		}
		
		@Override
		public long nextValue(long increment) {
			if (increment != 1) {
				throw new IllegalArgumentException("NativeDatabaseSequence only supports incrementing 1 at a time.");
			}
			return ERXSQLHelper.newSQLHelper(_editingContext, _modelName).getNextValFromSequenceNamed(_editingContext, _modelName, name()).longValue();
		}
		
		@Override
		protected long increment() {
			return 1L;
		}
	}
	
	/**
	 * Multi-instance-safe subclass of ERXSequence. Creates a table 
	 * erx_sequence_table to store the values. 
	 * @author ak
	 *
	 */
	public static class DatabaseSequence extends ERXSequence {

		private static final String ERX_SEQUENCE_TABLE = EOSQLExpression.sqlStringForString(ERXProperties.stringForKeyWithDefault("er.extensions.ERXSequence.TableName", "erx_sequence_table"));
		private static final String VALUE_COLUMN_NAME = EOSQLExpression.sqlStringForString("value_");
		private static final String NAME_COLUMN_NAME = EOSQLExpression.sqlStringForString("name_");

		private ERXJDBCConnectionBroker _broker;
		private EOSQLExpressionFactory _factory;

		public DatabaseSequence(EOEditingContext ec, String modelName, String name, long initialValue) {
			super(name, initialValue);
			EOModel model = ERXEOAccessUtilities.modelGroup(ec).modelNamed(modelName);
			_broker = ERXJDBCConnectionBroker.connectionBrokerForModel(model);
    		_factory = new EOSQLExpressionFactory(EOAdaptor.adaptorWithModel(model));
			_lastValue = increasedMaxValue(0);
			_maxValue = _lastValue;
		}

		public DatabaseSequence(EOEditingContext ec, String modelName, String name) {
			this(ec, modelName, name, ERXProperties.longForKeyWithDefault(name + ".InitalValue", 100000L));
		}

		protected ERXJDBCConnectionBroker broker() {
			return _broker;
		}
		
		protected long selectAndUpdateValue(Connection con, long increment) throws SQLException {
			long pk;
			EOSQLExpression selectExpression = _factory.expressionForEntity(null);

			String columnList = VALUE_COLUMN_NAME;
			String tableList = ERX_SEQUENCE_TABLE;
			String whereSelector = selectExpression.sqlStringForSelector(EOQualifier.QualifierOperatorEqual, name());
			String whereClause = NAME_COLUMN_NAME + " " + whereSelector + " '" + name() + "'";
			String lockClause = selectExpression.lockClause();

			String selectStatement = selectExpression.assembleSelectStatementWithAttributes(null, true, null, null, "SELECT ", columnList , tableList, whereClause, null, null, lockClause);
			ResultSet resultSet = con.createStatement().executeQuery(selectStatement);
    		
    		boolean hasNext = resultSet.next();
    		if (hasNext) {
    			String incrementString = EOSQLExpression.sqlStringForNumber(Long.valueOf(increment));
    			String updateValueColumn = VALUE_COLUMN_NAME + " = " + VALUE_COLUMN_NAME + " + " + incrementString;
				String updateList = updateValueColumn;
    		
    			pk = resultSet.getLong(VALUE_COLUMN_NAME);
				String updateStatement = _factory.expressionForEntity(null).assembleUpdateStatementWithRow(null, null, tableList, updateList, whereClause);
    			con.createStatement().executeUpdate(updateStatement);
    		} else {
    			pk = createRow(con, increment);
    		}
    		return pk;
		}

		protected long createRow(Connection con, long increment) throws SQLException {
			String incrementString = EOSQLExpression.sqlStringForNumber(Long.valueOf(increment));
			String tableList = ERX_SEQUENCE_TABLE;
			String columnList = NAME_COLUMN_NAME + "," + VALUE_COLUMN_NAME;
			String valueList = "'" + name() + "'," + incrementString;
			String insertStatement = _factory.expressionForEntity(null).assembleInsertStatementWithRow(null, tableList, columnList, valueList);
			con.createStatement().executeUpdate(insertStatement);
			return 0L;
		}

		protected void createTable(Connection con) throws SQLException {
			String nameColumnClause = NAME_COLUMN_NAME + " VARCHAR(100) NOT NULL";
			String valueColumnClause = VALUE_COLUMN_NAME + " INT";
			String createTableStatement = "CREATE TABLE " + ERX_SEQUENCE_TABLE + " (" + nameColumnClause + ", " + valueColumnClause + ")";
			String alterTableStatement = "ALTER TABLE " + ERX_SEQUENCE_TABLE + " ADD PRIMARY KEY (" + NAME_COLUMN_NAME + ")";// NOT
			// DEFERRABLE
			// INITIALLY
			// IMMEDIATE");
			con.createStatement().executeUpdate(createTableStatement);
			con.createStatement().executeUpdate(alterTableStatement);
		}
		
		@Override
		protected long increasedMaxValue(long increment) {
	        
	        Connection con = broker().getConnection();
	        try {
	            try {
	            	if(con.getTransactionIsolation() != 0) {
	            		con.setAutoCommit(false);
	            		con.setReadOnly(false);
	            	}
	            } catch (SQLException e) {
	                log.error(e, e);
	            }

	            for(int tries = 0; tries < 5; tries++) {
	            	try {
	            		long lastValue = selectAndUpdateValue(con, increment);
	            		
	            		if(_lastValue == 0L) {
	            			_lastValue = lastValue;
	            		}
	            		lastValue += increment;
	            		con.commit();
	            		return lastValue;
	            	} catch(SQLException ex) {
	            		if (isCreationError(ex)) {
	               			try {
	               				con.rollback();
	               				createTable(con);
	               			} catch (SQLException ee) {
	               				throw new NSForwardException(ee, "could not create erx_sequence_table");
	               			}
	               		} else {
	            			throw new NSForwardException(ex, "Error fetching sequence: " + name());
	            		}
	            	}
	            }
	        } finally {
	        	broker().freeConnection(con);
	        }
	        throw new IllegalStateException("Couldn't get sequence: " + name());
		}

		protected boolean isCreationError(SQLException ex) {
			String s = ex.getMessage().toLowerCase();
    		boolean creationError = false;
    		String tableNameLastComponent = DatabaseSequence.ERX_SEQUENCE_TABLE.substring(DatabaseSequence.ERX_SEQUENCE_TABLE.lastIndexOf('.')+1);
    		creationError |= (s.indexOf("error 116") != -1); // frontbase?
			creationError |= (s.indexOf(tableNameLastComponent) != -1 && s.indexOf("does not exist") != -1); // postgres ?
			creationError |= s.indexOf("ora-00942") != -1; // oracle
			creationError |= s.indexOf("doesn't exist") != -1; // mysql
			creationError |= (s.indexOf(tableNameLastComponent) != -1 && s.indexOf("not found.") != -1); // sybase
			return creationError;
		}
	}
	
	public static class PrimaryKeySequence extends DatabaseSequence {
		private String _entityName;
		
		public PrimaryKeySequence(EOEditingContext ec, String modelName, String entityName) {
			super(ec, modelName, entityName + "_pk_seq");
			_entityName = entityName;
		}
		
		@Override
		protected long createRow(Connection con, long increment) throws SQLException {
			EOEntity entity = ERXEOAccessUtilities.rootEntityForEntityNamed(_entityName);
			String tableName = entity.externalName();
			String colName = entity.primaryKeyAttributes().lastObject().columnName();
			String sql = "select max(" + colName + ") from " + tableName;

			ResultSet resultSet;
			resultSet = con.createStatement().executeQuery(sql);
			con.commit();

			boolean hasNext = resultSet.next();
			long v = 0L;
			if (hasNext) {
				v = resultSet.getLong(1);
				v = fixMaxIdValue(v);
				/*if (log.isDebugEnabled())
					log.debug("received max id from table " + tableName + ", setting value in PK_TABLE to " + v);
					                if(encodeEntityInPkValue()) {
	                	v = v >> CODE_LENGTH;
	                }
	                if(encodeHostInPkValue()) {
	                	v = v >> HOST_CODE_LENGTH;
	                }*/
			}
			super.createRow(con, v+increment);
			return v;
		}

		protected long fixMaxIdValue(long v) {
			return v;
		}

	}
	
	private static final Map cache = Collections.synchronizedMap(new HashMap());
	
	public static ERXSequence sequenceWithName(String name) {
		return (ERXSequence) cache.get(name);
	}
	
	public static ERXSequence createSequenceWithName(String name, long initialValue) {
		ERXSequence sequence = new ERXSequence(name, initialValue);
		cache.put(name, sequence);
		return sequence;
	}
	
	public static ERXSequence createDatabaseSequenceWithName(EOEditingContext ec, String modelName, String name) {
		ERXSequence sequence = new DatabaseSequence(ec, modelName, name);
		cache.put(name, sequence);
		return sequence;
	}
	
	public static void registerSequenceWithName(ERXSequence sequence, String name) {
		cache.put(name, sequence);
	}
}
