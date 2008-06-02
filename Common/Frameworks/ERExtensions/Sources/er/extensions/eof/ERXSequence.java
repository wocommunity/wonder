package er.extensions.eof;

import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

import org.apache.log4j.Logger;

import com.webobjects.eoaccess.EOAttribute;
import com.webobjects.eoaccess.EOEntity;
import com.webobjects.eoaccess.EOModel;
import com.webobjects.eocontrol.EOEditingContext;
import com.webobjects.foundation.NSForwardException;

import er.extensions.foundation.ERXProperties;
import er.extensions.jdbc.ERXJDBCConnectionBroker;

/**
 * Simple sequence class. MT safe, but not multi instance safe (this is implemented by subclasses)
 * @author ak
 *
 */
public class ERXSequence {
	
	protected Logger log;
	
	private final String _name;
	protected long _lastValue;
	private long _maxValue;
	
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
				ERXProperties.longForKeyWithDefault(getClass().getName() + ".Increment", 10L));
	}
	
	/**
	 * Multi-instance-safe subclass of ERXSequence. Creates a table 
	 * erx_sequence_table to store the values. 
	 * @author ak
	 *
	 */
	public static class DatabaseSequence extends ERXSequence {

		private ERXJDBCConnectionBroker _broker;

		public DatabaseSequence(EOEditingContext ec, String modelName, String name, long initialValue) {
			super(name, initialValue);
			EOModel model = ERXEOAccessUtilities.modelGroup(ec).modelNamed(modelName);
			_broker = ERXJDBCConnectionBroker.connectionBrokerForModel(model);
		}

		public DatabaseSequence(EOEditingContext ec, String modelName, String name) {
			this(ec, modelName, name, ERXProperties.longForKeyWithDefault(name + ".InitalValue", 100000L));
		}

		protected ERXJDBCConnectionBroker broker() {
			return _broker;
		}
		
		protected long selectAndUpdateValue(Connection con, long increment) throws SQLException {
			long pk;
			String where = "where name_ = '"+ name() +"'";
    		ResultSet resultSet = con.createStatement().executeQuery("select value_ from erx_sequence_table " + where + " for update");
    		boolean hasNext = resultSet.next();
    		if (hasNext) {
    			pk = resultSet.getLong("value_");
    			con.createStatement().executeUpdate("update erx_sequence_table set value_ = value_ +" + increment + " " + where);
    		} else {
    			pk = createRow(con, increment);
    		}
    		return pk;
		}

		protected long createRow(Connection con, long increment) throws SQLException {
			con.createStatement().executeUpdate("insert into erx_sequence_table (name_, value_) values ('" + name() + "', " + increment + ")");
			return 0L;
		}

		protected void createTable(Connection con) throws SQLException {
			con.createStatement().executeUpdate("create table erx_sequence_table (name_ varchar(100) not null, value_ int)");
			con.createStatement().executeUpdate("alter table erx_sequence_table add primary key (name_)");// NOT
			// DEFERRABLE
			// INITIALLY
			// IMMEDIATE");
		}
		
		protected long increasedMaxValue(long increment) {
	        
	        Connection con = broker().getConnection();
	        try {
	            try {
	                con.setAutoCommit(false);
	                con.setReadOnly(false);
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
    		creationError |= (s.indexOf("error code 116") != -1); // frontbase?
			creationError |= (s.indexOf("erx_sequence_table") != -1 && s.indexOf("does not exist") != -1); // postgres ?
			creationError |= s.indexOf("ora-00942") != -1; // oracle
			creationError |= s.indexOf("doesn't exist") != -1; // mysql
			creationError |= (s.indexOf("erx_sequence_table") != -1 && s.indexOf("not found.") != -1); // sybase
			return creationError;
		}
	}
	
	public static class PrimaryKeySequence extends DatabaseSequence {
		private String _entityName;
		
		public PrimaryKeySequence(EOEditingContext ec, String modelName, String entityName) {
			super(ec, modelName, entityName + "_pk_seq");
			_entityName = entityName;
		}
		
		protected long createRow(Connection con, long increment) throws SQLException {
			EOEntity entity = ERXEOAccessUtilities.rootEntityForEntityNamed(_entityName);
			String tableName = entity.externalName();
			String colName = ((EOAttribute)entity.primaryKeyAttributes().lastObject()).columnName();
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
