package com.webobjects.jdbcadaptor;

import java.io.IOException;
import java.io.InputStream;
import java.util.Enumeration;

import com.webobjects.eoaccess.EOAttribute;
import com.webobjects.eoaccess.EOEntity;
import com.webobjects.eoaccess.EOSynchronizationFactory;
import com.webobjects.foundation.NSArray;
import com.webobjects.foundation.NSBundle;
import com.webobjects.foundation.NSData;
import com.webobjects.foundation.NSDictionary;
import com.webobjects.foundation.NSForwardException;
import com.webobjects.foundation.NSLog;
import com.webobjects.foundation.NSMutableArray;
import com.webobjects.foundation.NSPropertyListSerialization;

/**
 * WO runtime plugin with support for Postgresql.
 *
 * @author ak
 * @author giorgio_v
 */
@SuppressWarnings({ "rawtypes", "unchecked", "deprecation" })
public class DB2PlugIn extends JDBCPlugIn {
  private static final String QUERY_STRING_USE_BUNDLED_JDBC_INFO = "useBundledJdbcInfo";
  
  static {
      setPlugInNameForSubprotocol(DB2PlugIn.class.getName(), "db2");
  }
    
  /**
   * Designated constructor.
   */
  public DB2PlugIn(JDBCAdaptor adaptor) {
    super(adaptor);
  }

  /**
   * Name of the driver.
   */
  @Override
  public String defaultDriverName() {
      return "com.ibm.db2.jcc.DB2Driver";
  }

  /**
   * Name of the database.
   */
  @Override
  public String databaseProductName() {
    return "DB2";
  }

  /**
   * <P>WebObjects 5.4's version of JDBCAdaptor will use this
   * in order to assemble the name of the prototype to use when
   * it loads models.</P>
   * @return the name of the plugin.
   */
  @Override
  public String name() {
    return "DB2";
  }

  /**
   * <P>This method returns true if the connection URL for the
   * database has a special flag on it which indicates to the
   * system that the jdbcInfo which has been bundled into the
   * plugin is acceptable to use in place of actually going to
   * the database and getting it.
   */
  protected boolean shouldUseBundledJdbcInfo() {
    boolean shouldUseBundledJdbcInfo = false;
    String url = connectionURL();
    if (url != null) {
      shouldUseBundledJdbcInfo = url.toLowerCase().matches(".*(\\?|\\?.*&)" + DB2PlugIn.QUERY_STRING_USE_BUNDLED_JDBC_INFO.toLowerCase() + "=(true|yes)(\\&|$)");
    }
    return shouldUseBundledJdbcInfo;
  }

  /**
   * <P>This is usually extracted from the the database using
   * JDBC, but this is really inconvenient for users who are
   * trying to generate SQL at some.  A specific version of the
   * data has been written into the property list of the
   * framework and this can be used as a hard-coded equivalent.
   * </P> 
   */
  @Override
  public NSDictionary jdbcInfo() {
    // you can swap this code out to write the property list out in order
    // to get a fresh copy of the JDBCInfo.plist.
//    try {
//      String jdbcInfoS = NSPropertyListSerialization.stringFromPropertyList(super.jdbcInfo());
//      FileOutputStream fos = new FileOutputStream("/tmp/JDBCInfo.plist");
//      fos.write(jdbcInfoS.getBytes());
//      fos.close();
//    }
//    catch(Exception e) {
//      throw new IllegalStateException("problem writing JDBCInfo.plist",e);
//    }

    NSDictionary jdbcInfo;
    // have a look at the JDBC connection URL to see if the flag has been set to
    // specify that the hard-coded jdbcInfo information should be used.
    if(shouldUseBundledJdbcInfo()) {
      if(NSLog.debugLoggingAllowedForLevel(NSLog.DebugLevelDetailed)) {
        NSLog.debug.appendln("Loading jdbcInfo from JDBCInfo.plist as opposed to using the JDBCPlugIn default implementation.");
      }
      
      InputStream jdbcInfoStream = NSBundle.bundleForClass(getClass()).inputStreamForResourcePath("JDBCInfo.plist");
      if (jdbcInfoStream == null) {
        throw new IllegalStateException("Unable to find 'JDBCInfo.plist' in this plugin jar.");
      }

      try {
        jdbcInfo = (NSDictionary) NSPropertyListSerialization.propertyListFromData(new NSData(jdbcInfoStream, 2048), "US-ASCII");
      }
      catch (IOException e) {
        throw new RuntimeException("Failed to load 'JDBCInfo.plist' from this plugin jar.", e);
      } finally {
    	  try { jdbcInfoStream.close(); } catch (IOException e) {}
      }
    }
    else {
      jdbcInfo = super.jdbcInfo();
    }
    return jdbcInfo;
  }

  /**
   * Returns a "pure java" synchronization factory.
   * Useful for testing purposes.
   */
  @Override
  public EOSynchronizationFactory createSynchronizationFactory() {
    try {
      return new DB2SynchronizationFactory(adaptor());
    }
    catch (Exception e) {
      throw new NSForwardException(e, "Couldn't create synchronization factory");
    }
  }

  /**                                                                                                                                                         
   * Expression class to create. We have custom code, so we need our own class.                                                                               
   */
  @Override
  public Class defaultExpressionClass() {
    return DB2Expression.class;
  }

  /** 
   * Overrides the parent implementation to provide a more efficient mechanism for generating primary keys,
   * while generating the primary key support on the fly.
   *
   * @param count the batch size
   * @param entity the entity requesting primary keys
   * @param channel open JDBCChannel
   * @return NSArray of NSDictionary where each dictionary corresponds to a unique  primary key value
   */
  @Override
  public NSArray newPrimaryKeys(int count, EOEntity entity, JDBCChannel channel) {
    if (isPrimaryKeyGenerationNotSupported(entity)) {
      return null;
    }
    
    EOAttribute attribute = entity.primaryKeyAttributes().lastObject();
    String attrName = attribute.name();
    boolean isIntType = "i".equals(attribute.valueType());

    NSMutableArray results = new NSMutableArray(count);
    String sequenceName = sequenceNameForEntity(entity);
    DB2Expression expression = new DB2Expression(entity);
    
    boolean succeeded = false;
    for (int tries = 0; !succeeded && tries < 2; tries++) {
      while (results.count() < count) {
        try {
          StringBuilder sql = new StringBuilder();
          sql.append("SELECT next value for ");
          sql.append(sequenceName);
          sql.append(" AS KEY from sysibm.sysdummy1");
          expression.setStatement(sql.toString());
          channel.evaluateExpression(expression);
          try {
            NSDictionary row;
            while ((row = channel.fetchRow()) != null) {
              Enumeration pksEnum = row.allValues().objectEnumerator();
              while (pksEnum.hasMoreElements()) {
                Number pkObj = (Number)pksEnum.nextElement();
                Number pk;
                if (isIntType) {
                  pk = Integer.valueOf(pkObj.intValue());
                }
                else {
                  pk = Long.valueOf(pkObj.longValue());
                }
                results.addObject(new NSDictionary(pk, attrName));
              }            
            }
          }
          finally {
            channel.cancelFetch();
          }
          succeeded = true;
        }
        catch (JDBCAdaptorException ex) {
        	throw ex;
        }
      }
    }
    
    if (results.count() != count) {
      throw new IllegalStateException("Unable to generate primary keys from the sequence for " + entity + ".");
    }
    
    return results;
  }
  
  /**
   * Utility method that returns the name of the sequence associated
   * with <code>entity</code>.  Non Static so we can reuse code.
   *
   * @param entity    the entity
   * @return  the name of the sequence
   */
  protected String sequenceNameForEntity(EOEntity entity) {

    return _sequenceNameForEntity(entity);
  }


  /**
   * Utility method that returns the name of the sequence associated
   * with <code>entity</code>
   *
   * @param entity    the entity
   * @return  the name of the sequence
   */
  protected static String _sequenceNameForEntity(EOEntity entity) {

    return entity.primaryKeyRootName() + "_seq";
  }

  /**
   * Checks whether primary key generation can be supported for <code>entity</code>
   *
   * @param entity    the entity to be checked
   * @return  yes/no
   */
  protected boolean isPrimaryKeyGenerationNotSupported(EOEntity entity) {
    return entity.primaryKeyAttributes().count() > 1 || entity.primaryKeyAttributes().lastObject().adaptorValueType() != EOAttribute.AdaptorNumberType;
  }

}
