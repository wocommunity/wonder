package er.extensions;

import java.sql.*;
import java.util.*;

import org.apache.log4j.*;

import com.webobjects.eoaccess.*;
import com.webobjects.eocontrol.*;
import com.webobjects.foundation.*;

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
			long diff = (_lastValue + increment) - _maxValue;
			if(diff > 0) {
				long nextIncrement = increment();
				_maxValue = increasedMaxValue(diff > nextIncrement ? diff : nextIncrement);
			}
			_lastValue += increment;
			return _lastValue;
		}
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
		private EOModel _model;
		
		public DatabaseSequence(EOEditingContext ec, String modelName, String name) {
			super(name);
			_model = ERXEOAccessUtilities.modelGroup(ec).modelNamed(modelName);
		}

		protected EOModel model() {
			return _model;
		}
		
		protected long increasedMaxValue(long increment) {
	        String where = "where name_ = '"+ name() +"'";

	        ERXJDBCConnectionBroker broker = ERXJDBCConnectionBroker.connectionBrokerForModel(model());
			Connection con = broker.getConnection();
	        try {
	            try {
	                con.setAutoCommit(false);
	                con.setReadOnly(false);
	            } catch (SQLException e) {
	                log.error(e, e);
	            }

	            for(int tries = 0; tries < 5; tries++) {
	            	try {
	            		ResultSet resultSet = con.createStatement().executeQuery("select value_ from erx_sequence_table " + where + " for update");
	            		boolean hasNext = resultSet.next();
	            		long pk;
	            		if (hasNext) {
	            			pk = resultSet.getLong("value_");
	               			// now execute the update
	            			con.createStatement().executeUpdate("update erx_sequence_table set value_ = value_ +" + increment + " " + where);
	            		} else {
	            			pk = 0;
	            			con.createStatement().executeUpdate("insert into erx_sequence_table (name_, value_) values ('" + name() + "', " + increment + ")");
	            		}
	            		if(_lastValue == 0) {
	            			_lastValue = pk;
	            		}
	            		pk += increment;
	            		con.commit();
	            		return pk;
	            	} catch(SQLException ex) {
	            		String s = ex.getMessage().toLowerCase();
	            		boolean creationError = (s.indexOf("error code 116") != -1); // frontbase?
	               		creationError |= (s.indexOf("erx_sequence_table") != -1 && s.indexOf("does not exist") != -1); // postgres ?
	               		creationError |= s.indexOf("ora-00942") != -1; // oracle
	               		creationError |= s.indexOf("doesn't exist") != -1; // mysql
	               		if (creationError) {
	               			try {
	               				con.rollback();
	               				log.info("creating pk table");
	               				con.createStatement().executeUpdate("create table erx_sequence_table (name_ varchar(100) not null, value_ int)");
	               				con.createStatement().executeUpdate("alter table erx_sequence_table add primary key (name_)");// NOT
	               				// DEFERRABLE
	               				// INITIALLY
	               				// IMMEDIATE");
	               			} catch (SQLException ee) {
	               				throw new NSForwardException(ee, "could not create erx_sequence_table");
	               			}
	               		} else {
	            			throw new NSForwardException(ex, "Error fetching sequence: " + name());
	            		}
	            	}
	            }
	        } finally {
	        	broker.freeConnection(con);
	        }
	        throw new IllegalStateException("Couldn't get sequence: " + name());
		}
	};
	
	private static final Map cache = Collections.synchronizedMap(new HashMap());
	
	public static ERXSequence sequenceWithName(String name) {
		return (ERXSequence) cache.get(name);
	}
	
	public static ERXSequence createSequenceWithName(String name) {
		ERXSequence sequence = new ERXSequence(name);
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
