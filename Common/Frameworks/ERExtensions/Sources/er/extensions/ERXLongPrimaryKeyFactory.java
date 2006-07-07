package er.extensions;

import java.sql.*;
import java.util.*;

import org.apache.log4j.Logger;

import com.webobjects.eoaccess.*;
import com.webobjects.eocontrol.*;
import com.webobjects.foundation.*;

/**
 * @author david@cluster9.com<br/> <br/> Automatically generates Long primary
 *         keys for entities. Features a cache which reduces database roundtrips
 *         as well as optionally encoding Entity type in PK value.<br/> <br/>
 *         usage:<br/> <br/> override either the ERXGeneratesPrimaryKey
 *         interface like this:<br/> <code><pre>
 * private NSDictionary _primaryKeyDictionary = null;
 * 
 *                                                    public NSDictionary primaryKeyDictionary(boolean inTransaction) {
 *                                                        if (_primaryKeyDictionary == null) {
 *                                                            _primaryKeyDictionary = ERXLongPrimaryKeyFactory.primaryKeyDictionary(this);
 *                                                        }
 *                                                        return _primaryKeyDictionary;
 *                                                    }
 * </pre>
 * </code><br/> or manually call<br/>
 *         <code>ERXLongPrimaryKeyFactory.primaryKeyDictionary(EOEnterpriseObject eo);</code><br/>
 *         <br/> the necessary database table is generated on the fly.<br/>
 *         <br/> <b>Encoding Entity in PK values</b><br/> If the system
 *         property
 *         <code>ERXIntegerPrimaryKeyFactory.encodeEntityInPkValue</code> is
 *         set to <code>true</code> then the last 6 bits from the 64 bit
 *         primary key is used to encode the Subentity in the pk value. This
 *         speeds up inheritance with multiple tables. In order to support this
 *         you must add an entry to the userInfo from the Subentities:<br/>
 *         <br/> <code>key=entityCode</code><br/>
 *         <code>value= %lt;%lt; an unique integer, no longer than 6 bit - 1</code><br/>
 * 
 */
public class ERXLongPrimaryKeyFactory {

    public static final int       CODE_LENGTH  = 6;
    public static final int		HOST_CODE_LENGTH = 10;
    public static final String    HOST_CODE_KEY = "er.extensions.ERXLongPrimaryKeyFactory.hostCode";
    
    public static final Logger log          = Logger.getLogger(ERXLongPrimaryKeyFactory.class);
    public static long            MAX_PK_VALUE = (long) Math.pow(2, 48);
    public static Boolean         encodeEntityInPkValue;
    public static Boolean         encodeHostInPkValue;
    public static Integer 		hostCode;
    private static Hashtable      pkCache      = new Hashtable();
    private static int            increaseBy   = 0;

    private static Long getNextPkValueForEntity(String ename) {
        Long pk = cachedPkValue(ename);
        if (encodeHostInPkValue()) {
            long l = pk.longValue();
            if (l > MAX_PK_VALUE) { throw new IllegalStateException("max PK value reached for entity " + ename + " cannot continue!");

            }

            // we are assuming 64 bit int values
            // and we are using the last 10 bits for
            // hostCode
            long realPk = l << HOST_CODE_LENGTH;
            // now add the hostCode
            realPk = realPk | hostCode();
            if (log.isDebugEnabled()) {
                log.debug("new pk value for "+ename+"("+((ERXModelGroup) ERXApplication.erxApplication().defaultModelGroup()).entityCode(ename)+"), db value = "+pk+", new value = "+realPk);
            }
            pk = new Long(realPk);
        }
        if (encodeEntityInPkValue()) {
            long l = pk.longValue();
            if (l > MAX_PK_VALUE) { throw new IllegalStateException("max PK value reached for entity " + ename + " cannot continue!");

            }

            // we are assuming 64 bit int values
            // and we are using the last 6 bits for
            // entity encoding
            long realPk = l << CODE_LENGTH;
            // now add the entity code
            realPk = realPk | ((ERXModelGroup) ERXApplication.erxApplication().defaultModelGroup()).entityCode(ename);
            if (log.isDebugEnabled()) {
                log.debug("new pk value for "+ename+"("+((ERXModelGroup) ERXApplication.erxApplication().defaultModelGroup()).entityCode(ename)+"), db value = "+pk+", new value = "+realPk);
            }
            pk = new Long(realPk);
        }
        return pk;
    }

    /**
     * @return
     */
    public synchronized static int hostCode() {
        if (hostCode == null) {
            hostCode = new Integer(ERXSystem.getProperty(HOST_CODE_KEY));
        }
        return hostCode.intValue();
    }
    public synchronized static boolean encodeEntityInPkValue() {
        if (encodeEntityInPkValue == null) {
            boolean b = ERXValueUtilities.booleanValueWithDefault(System.getProperty("er.extensions.ERXLongPrimaryKeyFactory.encodeEntityInPkValue"),
                    false);
            encodeEntityInPkValue = b ? Boolean.TRUE : Boolean.FALSE;
        }
        return encodeEntityInPkValue.booleanValue();
    }
    public synchronized static boolean encodeHostInPkValue() {
        if (encodeHostInPkValue == null) {
            boolean b = ERXValueUtilities.booleanValueWithDefault(System.getProperty("er.extensions.ERXLongPrimaryKeyFactory.encodeHostInPkValue"),
                    false);
            encodeHostInPkValue = b ? Boolean.TRUE : Boolean.FALSE;
        }
        return encodeHostInPkValue.booleanValue();
    }

    public static Object primaryKeyValue(EOEnterpriseObject eo) {
        return primaryKeyDictionary(eo).objectEnumerator().nextElement();
    }

    public static Object primaryKeyValue(String entityName) {
        return primaryKeyDictionary(entityName).objectEnumerator().nextElement();
    }

    public static NSDictionary primaryKeyDictionary(EOEnterpriseObject eo) {
        String entityName = eo.entityName();
        return primaryKeyDictionary(entityName);
    }

    public static synchronized NSDictionary primaryKeyDictionary(String entityName) {
        EOEntity entity = EOModelGroup.defaultGroup().entityNamed(entityName);
        while (entity.parentEntity() != null) {
            entity = entity.parentEntity();
        }
        entityName = entity.name();

        Long pk = getNextPkValueForEntity(entityName);
        String pkName = (String) entity.primaryKeyAttributeNames().objectAtIndex(0);
        return new NSDictionary(new Object[] { pk}, new Object[] { pkName});
    }

    /**
     * returns a new primary key for the specified entity.
     * 
     * @param ename,
     *            the entity name for which this method should return a new
     *            primary key
     * @param count,
     *            the number of times the method should try to get a value from
     *            the database if something went wrong (a deadlock in the db for
     *            example -> high traffic with multiple instances)
     * @param increaseBy,
     *            if > 1 then the value in the database is increased by this
     *            factor. This is usefull to 'get' 10000 pk values at once for
     *            caching. Removes a lot of db roundtrips.
     * @return a new pk values for the specified entity.
     */
    private static Long getNextPkValueForEntityIncreaseBy(String ename, int count, int _increaseBy) {
        if (_increaseBy < 1) _increaseBy = 1;

        Connection con = ERXJDBCConnectionBroker.connectionBrokerForEntityNamed(ename).getConnection();
        try {
            con.setAutoCommit(false);
            con.setReadOnly(false);
        } catch (SQLException e) {
            log.error(e, e);
        }

        String where = "where eoentity_name = '" + ename + "'";

        try {
            ResultSet resultSet = con.createStatement().executeQuery("select pk_value from pk_table " + where);
            con.commit();

            boolean hastNext = resultSet.next();
            int pk = 1;
            if (hastNext) {
                pk = resultSet.getInt("pk_value");
                pk += _increaseBy;
                // now execute the update
                con.createStatement().executeUpdate("update pk_table set pk_value = " + pk + " " + where);
            } else {
                // first time, we need to set i up
                con.createStatement().executeUpdate(
                        "insert into pk_table (eoentity_name, pk_value) values ('" + ename + "', " + maxIdFromTable(ename) + ")");
                pk = maxIdFromTable(ename) + 1;
            }
            con.commit();
            return new Long(pk);
        } catch (SQLException e) {
            String s = e.getMessage().toLowerCase();
            if ((s.indexOf("error code 116") != -1) || (s.indexOf("pk_table") != -1 && s.indexOf("does not exist") != -1) || 
                    // this is for Oracle
                    s.indexOf("ora-00942") != -1) {
                try {
                    con.rollback();
                    log.info("creating pk table");
                    con.createStatement().executeUpdate("create table pk_table (eoentity_name varchar(100) not null, pk_value integer)");
                    con.createStatement().executeUpdate("alter table pk_table add primary key (eoentity_name)");// NOT
                    // DEFERRABLE
                    // INITIALLY
                    // IMMEDIATE");
                    con.commit();

                    ResultSet resultSet = con.createStatement().executeQuery("select pk_value from pk_table " + where);
                    con.commit();

                    boolean hastNext = resultSet.next();
                    int pk = 1;
                    if (hastNext) {
                        pk = resultSet.getInt("pk_value");
                        pk++;
                        // now execute the update
                        con.createStatement().executeUpdate("update pk_table set pk_value = " + pk + " " + where);
                    } else {
                        // first time, we need to set i up
                        con.createStatement().executeUpdate(
                                "insert into pk_table (eoentity_name, pk_value) values ('" + ename + "', " + pk + ")");
                    }
                    con.commit();
                    return new Long(pk);
                } catch (SQLException ee) {
                    ee.printStackTrace();
                    throw new RuntimeException("could not create pk table");
                } finally {
                    ERXJDBCConnectionBroker.connectionBrokerForEntityNamed(ename).freeConnection(con);
                }
            }
            if (count < 10) {
                log.error("could not get primkey, trying again " + count + ", error was " + e.getMessage());
                return getNextPkValueForEntity(ename);
            }
            log.error(e, e);
            throw new RuntimeException("could not get primkey, original message was " + e.getMessage());

        } finally {
            ERXJDBCConnectionBroker.connectionBrokerForEntityNamed(ename).freeConnection(con);
        }
    }

    /**
     * retrieves the maxValue from id from the specified entity
     * 
     * @param ename
     * @param pk
     * @return
     */
    private static int maxIdFromTable(String ename) {
        EOEntity entity = EOModelGroup.defaultGroup().entityNamed(ename);
        if (entity == null) throw new NullPointerException("could not find an entity named " + ename);

        String tableName = entity.externalName();

        String sql = "select max(id) from " + tableName;

        Connection con = ERXJDBCConnectionBroker.connectionBrokerForEntityNamed(ename).getConnection();
        ResultSet resultSet;
        try {
            resultSet = con.createStatement().executeQuery(sql);
            con.commit();

            boolean hastNext = resultSet.next();
            if (hastNext) {
                int v = resultSet.getInt(1);
                if (log.isDebugEnabled())
                    log.debug("received max id from table " + tableName + ", setting value in PK_TABLE to " + v);

                return v;
            }
            throw new IllegalStateException("could not get value from " + sql);

        } catch (SQLException e) {
            log.error("could not call database with sql " + sql, e);
            throw new IllegalStateException("could not get value from " + sql);
        } finally {
            ERXJDBCConnectionBroker.connectionBrokerForEntityNamed(ename).freeConnection(con);
        }
    }

    /**
     * Returns a new integer based PkValue for the specified entity. If the
     * cache is empty it is refilled again.
     * 
     * @param ename,
     *            the entity name for which this method should return a new
     *            primary key
     * 
     * @return a new Integer based primary key for the specified entity.
     */
    private static Long cachedPkValue(String ename) {
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
     * @param ename,
     *            the name of the entity for which this method should return the
     *            Stack
     * @return the Stack with primary key values for the specified entity.
     */
    private static Stack cacheStack(String ename) {
        Stack s = (Stack) pkCache.get(ename);
        if (s == null) {
            s = new Stack();
            pkCache.put(ename, s);
        }
        return s;
    }

    /**
     * creates 1000 primary key values for the specified entity and updates the
     * database
     * 
     * @param s,
     *            the stack into which the pk values should be inserted
     * @param ename,
     *            the entity name for which the pk values should be generated
     */
    private static void fillPkCache(Stack s, String ename) {
        Long pkValueStart = getNextPkValueForEntityIncreaseBy(ename, 10, increaseBy());
        long value = pkValueStart.longValue();
        log.debug("filling pkCache for " + ename + ", starting at " + value);
        for (int i = increaseBy(); i-- > 1;) {
            s.push(new Long(i + value));
        }
    }

    private static int increaseBy() {
        if (increaseBy == 0) {
            increaseBy = 1000;
        }
        return increaseBy;
    }

}