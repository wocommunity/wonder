package er.extensions.eof;

import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.Enumeration;
import java.util.Hashtable;
import java.util.Map;
import java.util.Stack;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.webobjects.eoaccess.EOAdaptorChannel;
import com.webobjects.eoaccess.EODatabaseContext;
import com.webobjects.eoaccess.EOEntity;
import com.webobjects.eoaccess.EOModelGroup;
import com.webobjects.eocontrol.EOEditingContext;
import com.webobjects.eocontrol.EOEnterpriseObject;
import com.webobjects.eocontrol.EOObjectStoreCoordinator;
import com.webobjects.foundation.NSArray;
import com.webobjects.foundation.NSDictionary;
import com.webobjects.foundation.NSForwardException;

import er.extensions.foundation.ERXProperties;
import er.extensions.foundation.ERXSystem;
import er.extensions.foundation.ERXValueUtilities;
import er.extensions.jdbc.ERXJDBCConnectionBroker;

/**
 * Automatically generates Long primary keys for entities. Features a cache which reduces
 * database roundtrips as well as optionally encoding Entity type in PK value.
 * <h3>Usage:</h3>
 * override either the ERXGeneratesPrimaryKey interface like this:
 * <pre><code>
 * private NSDictionary _primaryKeyDictionary = null;
 * 
 * public NSDictionary primaryKeyDictionary(boolean inTransaction) {
 * 		if (_primaryKeyDictionary == null) {
 *			_primaryKeyDictionary = ERXLongPrimaryKeyFactory.primaryKeyDictionary(this);
 * 		}
 * 		return _primaryKeyDictionary;
 * }
 * </code></pre>
 * or manually call<br>
 * <code>ERXLongPrimaryKeyFactory.primaryKeyDictionary(EOEnterpriseObject eo);</code>
 * the necessary database table is generated on the fly.
 * <h3>Encoding Entity in PK values</h3>
 * If the system property <code>ERXIntegerPrimaryKeyFactory.encodeEntityInPkValue</code> is
 * set to <code>true</code> then the last 6 bits from the 64 bit primary key is used to
 * encode the Subentity in the pk value. This speeds up inheritance with multiple tables. In
 * order to support this you must add an entry to the userInfo from the Subentities:
 * <p>
 * <code>key=entityCode</code><br>
 * <code>value= %lt;%lt; an unique integer, no longer than 6 bit - 1</code>
 * 
 * @author david@cluster9.com
 */
public class ERXLongPrimaryKeyFactory {

	private static final int       CODE_LENGTH  = 6;
	private static final int		HOST_CODE_LENGTH = 10;
	private static final String    HOST_CODE_KEY = "er.extensions.ERXLongPrimaryKeyFactory.hostCode";

	private static final Logger log = LoggerFactory.getLogger(ERXLongPrimaryKeyFactory.class);
	private static long            MAX_PK_VALUE = (long) Math.pow(2, 48);
	private  Boolean         encodeEntityInPkValue;
	private  Boolean         encodeHostInPkValue;
	private  Integer 		hostCode;
	private  Map<String, Stack> pkCache      = new Hashtable();
	
	private  Integer increaseBy;
	
	private Long getNextPkValueForEntity(String ename) {
		Long pk = cachedPkValue(ename);
		if (encodeHostInPkValue()) {
			long l = pk.longValue();
			if (l > MAX_PK_VALUE) { 
				throw new IllegalStateException("max PK value reached for entity " + ename + " cannot continue!");
			}

			// we are assuming 64 bit int values
			// and we are using the last 10 bits for
			// hostCode
			long realPk = l << HOST_CODE_LENGTH;
			// now add the hostCode
			realPk = realPk | hostCode();
			if (log.isDebugEnabled()) {
				log.debug("new pk value for {}({}), db value = {}, new value = {}", ename, ((ERXModelGroup) EOModelGroup.defaultGroup()).entityCode(ename), pk, realPk);
			}
			pk = Long.valueOf(realPk);
		}
		if (encodeEntityInPkValue()) {
			long l = pk.longValue();
			if (l > MAX_PK_VALUE) { 
				throw new IllegalStateException("max PK value reached for entity " + ename + " cannot continue!");
			}

			// we are assuming 64 bit int values
			// and we are using the last 6 bits for
			// entity encoding
			long realPk = l << CODE_LENGTH;
			// now add the entity code
			realPk = realPk | ((ERXModelGroup) EOModelGroup.defaultGroup()).entityCode(ename);
			if (log.isDebugEnabled()) {
				log.debug("new pk value for {}({}), db value = {}, new value = {}", ename, ((ERXModelGroup) EOModelGroup.defaultGroup()).entityCode(ename), pk, realPk);
			}
			pk = Long.valueOf(realPk);
		}
		return pk;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see com.webobjects.eoaccess.EOModelGroup.Delegate#subEntityForEntity(com.webobjects.eoaccess.EOEntity,
	 *      com.webobjects.foundation.NSDictionary)
	 */
	public EOEntity subEntityForEntity(EOEntity entity, NSDictionary pkDict) {
		if (encodeEntityInPkValue()) {
			//get the code, we assume that the pkDict contains only one pk value!
			NSArray values = pkDict.allValues();
			if (values.count() > 1) throw new IllegalArgumentException("subEntityForEntity in its default implementation"+
			" works only with single pk long values " + entity.name()+  " has " +  pkDict);
			long pkValueWithCode;
			try {
				Number n = (Number) values.objectAtIndex(0);
				pkValueWithCode = n.longValue();
			} catch (ClassCastException e) {
				throw new IllegalArgumentException("subEntityForEntity in its default implementation"+
						" works only with single pk long values, expected a java.lang.Number but got a "+values.objectAtIndex(0));
			}
			long entityCode = pkValueWithCode & ((1 << ERXLongPrimaryKeyFactory.CODE_LENGTH) - 1);
			if (entityCode == 0) return null;
			for (Enumeration subEntities = entity.subEntities().objectEnumerator(); subEntities.hasMoreElements();) {
				EOEntity subEntity = (EOEntity) subEntities.nextElement();
				if (((ERXModelGroup) EOModelGroup.defaultGroup()).entityCode(subEntity) == entityCode) {
					return subEntity;
				}
			}
		}
		return null;
	}


	private int hostCode() {
		if (hostCode == null) {
			hostCode = Integer.valueOf(ERXSystem.getProperty(HOST_CODE_KEY));
		}
		return hostCode.intValue();
	}
	private boolean encodeEntityInPkValue() {
		if (encodeEntityInPkValue == null) {
			boolean b = ERXValueUtilities.booleanValueWithDefault(System.getProperty("er.extensions.ERXLongPrimaryKeyFactory.encodeEntityInPkValue"),
					false);
			encodeEntityInPkValue = b ? Boolean.TRUE : Boolean.FALSE;
		}
		return encodeEntityInPkValue.booleanValue();
	}
	private boolean encodeHostInPkValue() {
		if (encodeHostInPkValue == null) {
			boolean b = ERXValueUtilities.booleanValueWithDefault(System.getProperty("er.extensions.ERXLongPrimaryKeyFactory.encodeHostInPkValue"),
					false);
			encodeHostInPkValue = b ? Boolean.TRUE : Boolean.FALSE;
		}
		return encodeHostInPkValue.booleanValue();
	}

	public synchronized static Object primaryKeyValue(String entityName) {
		return factory().primaryKeyDictionary(entityName).objectEnumerator().nextElement();
	}

	public synchronized static NSDictionary primaryKeyDictionary(EOEnterpriseObject eo) {
		String entityName = eo.entityName();
		return factory().primaryKeyDictionary(entityName);
	}

	private static ERXLongPrimaryKeyFactory _factory;

	private static ERXLongPrimaryKeyFactory factory() {
		if(_factory == null) {
			_factory = new ERXLongPrimaryKeyFactory();
			if(_factory.encodeEntityInPkValue()) {
				EOModelGroup.defaultGroup().setDelegate(_factory);
			}
		}
		return _factory;
	}

	private NSDictionary primaryKeyDictionary(String entityName) {
		EOEntity entity = EOModelGroup.defaultGroup().entityNamed(entityName);
		while (entity.parentEntity() != null) {
			entity = entity.parentEntity();
		}
		entityName = entity.name();
		if(entity.primaryKeyAttributeNames().count() != 1) {
			throw new IllegalArgumentException("Can handle only entities with one PK: " + entityName + " has " + entity.primaryKeyAttributeNames());
		}

		Long pk = getNextPkValueForEntity(entityName);
		String pkName = entity.primaryKeyAttributeNames().objectAtIndex(0);
		return new NSDictionary(new Object[] { pk}, new Object[] { pkName});
	}

	/**
	 * returns a new primary key for the specified entity.
	 * 
	 * @param entityName
	 *            the entity name for which this method should return a new
	 *            primary key
	 * @param count
	 *            the number of times the method should try to get a value from
	 *            the database if something went wrong (a deadlock in the db for
	 *            example -> high traffic with multiple instances)
	 * @param increasePkBy
	 *            if > 1 then the value in the database is increased by this
	 *            factor. This is useful to 'get' 10000 pk values at once for
	 *            caching. Removes a lot of db roundtrips.
	 * @return a new pk values for the specified entity.
	 */
	private Long getNextPkValueForEntityIncreaseBy(String entityName, int count, int increasePkBy) {
		if (increasePkBy < 1) increasePkBy = 1;

		String where = "where eoentity_name = '" + entityName + "'";
		if(false) {
			// AK: this should actually be the correct way...
			EOEditingContext ec = ERXEC.newEditingContext();
			ec.lock();
			try {
				EODatabaseContext dbc = ERXEOAccessUtilities.databaseContextForEntityNamed((EOObjectStoreCoordinator) ec.rootObjectStore(), entityName);
				dbc.lock();
				try {
					EOEntity entity = ERXEOAccessUtilities.entityNamed(ec, entityName);
					EOAdaptorChannel channel = (EOAdaptorChannel) dbc.adaptorContext().channels().lastObject();
					NSArray result = channel.primaryKeysForNewRowsWithEntity(increasePkBy, entity);
					return (Long) ((NSDictionary) result.lastObject()).allValues().lastObject();
				} finally {
					dbc.unlock();
				}
			} finally {
				ec.unlock();
			}
		} else {
			ERXJDBCConnectionBroker broker = ERXJDBCConnectionBroker.connectionBrokerForEntityNamed(entityName);
			Connection con = broker.getConnection();
			try {
				try {
					con.setAutoCommit(false);
					con.setReadOnly(false);
				} catch (SQLException e) {
					log.error("Database error.", e);
				}

				for(int tries = 0; tries < count; tries++) {
					try {
						ResultSet resultSet = con.createStatement().executeQuery("select pk_value from pk_table " + where);
						con.commit();

						boolean hasNext = resultSet.next();
						long pk = 1;
						if (hasNext) {
							pk = resultSet.getLong("pk_value");
							// now execute the update
							con.createStatement().executeUpdate("update pk_table set pk_value = " + (pk+increasePkBy) + " " + where);
						} else {
							pk = maxIdFromTable(entityName);
							// first time, we need to set i up
							con.createStatement().executeUpdate("insert into pk_table (eoentity_name, pk_value) values ('" + entityName + "', " + (pk+increasePkBy) + ")");
						}
						con.commit();
						return Long.valueOf(pk);
					} catch(SQLException ex) {
						String s = ex.getMessage().toLowerCase();
						boolean creationError = (s.indexOf("error code 116") != -1); // frontbase?
						creationError |= (s.indexOf("pk_table") != -1 && s.indexOf("does not exist") != -1); // postgres ?
						creationError |= s.indexOf("ora-00942") != -1; // oracle
						if (creationError) {
							try {
								con.rollback();
								log.info("creating pk table");
								con.createStatement().executeUpdate("create table pk_table (eoentity_name varchar(100) not null, pk_value integer)");
								con.createStatement().executeUpdate("alter table pk_table add primary key (eoentity_name)");// NOT
								// DEFERRABLE
								// INITIALLY
								// IMMEDIATE");
								con.commit();
							} catch (SQLException ee) {
								throw new NSForwardException(ee, "could not create pk table");
							}
						} else {
							throw new NSForwardException(ex, "Error fetching PK");
						}
					}
				}
			} finally {
				broker.freeConnection(con);
			}
		}
		throw new IllegalStateException("Couldn't get PK");
	}

	/**
	 * Retrieves the maxValue from id from the specified entity. If 
	 * hosts and entities are encoded, then these values are stripped
	 * first
	 * 
	 * @param ename
	 */
	private long maxIdFromTable(String ename) {
		EOEntity entity = EOModelGroup.defaultGroup().entityNamed(ename);
		if (entity == null) throw new NullPointerException("could not find an entity named " + ename);
		String tableName = entity.externalName();
		String colName = entity.primaryKeyAttributes().lastObject().columnName();
		String sql = "select max(" + colName + ") from " + tableName;

		ERXJDBCConnectionBroker broker = ERXJDBCConnectionBroker.connectionBrokerForEntityNamed(ename);
		Connection con = broker.getConnection();
		ResultSet resultSet;
		try {
			resultSet = con.createStatement().executeQuery(sql);
			con.commit();

			boolean hasNext = resultSet.next();
			long v = 1l;
			if (hasNext) {
				v = resultSet.getLong(1);
				log.debug("received max id from table {}, setting value in PK_TABLE to {}", tableName, v);
				if(encodeEntityInPkValue()) {
					v = v >> CODE_LENGTH;
				}
				if(encodeHostInPkValue()) {
					v = v >> HOST_CODE_LENGTH;
				}
			}
			return v + 1;

		} catch (SQLException e) {
			log.error("could not call database with sql {}", sql, e);
			throw new IllegalStateException("could not get value from " + sql);
		} finally {
			broker.freeConnection(con);
		}
	}

	/**
	 * Returns a new integer based PkValue for the specified entity. If the
	 * cache is empty it is refilled again.
	 * 
	 * @param ename
	 *            the entity name for which this method should return a new
	 *            primary key
	 * 
	 * @return a new Integer based primary key for the specified entity.
	 */
	private Long cachedPkValue(String ename) {
		Stack s = cacheStack(ename);
		if (s.empty()) {
			synchronized (s) {
				if (s.empty()) {
					fillPkCache(s, ename);
				}
			}
		}
		Long pkValue = (Long) s.pop();
		return pkValue;
	}

	/**
	 * looks in the cache hashtable if there is already an Stack for the
	 * specified entity name. If there is no Stack a new Stack object will be
	 * created.
	 * 
	 * @param ename
	 *            the name of the entity for which this method should return the
	 *            Stack
	 * @return the Stack with primary key values for the specified entity.
	 */
	private Stack cacheStack(String ename) {
		Stack s = pkCache.get(ename);
		if (s == null) {
			s = new Stack();
			pkCache.put(ename, s);
		}
		return s;
	}

	/**
	 * creates x primary key values for the specified entity and updates the
	 * database, where x is the number specified in increaseBy
	 * 
	 * @param s
	 *            the stack into which the pk values should be inserted
	 * @param ename
	 *            the entity name for which the pk values should be generated
	 */
	private void fillPkCache(Stack s, String ename) {
		Long pkValueStart = getNextPkValueForEntityIncreaseBy(ename, 10, increaseBy());
		long value = pkValueStart.longValue();
		log.debug("filling pkCache for {}, starting at {}", ename, value);
		for (int i = increaseBy(); i > 0;  i--) {
			s.push(Long.valueOf(i + value));
		}
	}

	/**
	 * The amount of cached keys, set the property
	 * <code>er.extensions.ERXLongPrimaryKeyFactory.increaseBy</code> to the
	 * interval you want to use.
	 * 
	 * @return the interval to use for cached keys
	 */
	private int increaseBy() {
		if (increaseBy == null) {
			increaseBy = Integer.valueOf(ERXProperties.intForKeyWithDefault("er.extensions.ERXLongPrimaryKeyFactory.increaseBy", 1000));
		}
		return increaseBy.intValue();
	}

}