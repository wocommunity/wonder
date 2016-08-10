// DO NOT EDIT.  Make changes to er.persistentsessionstorage.model.ERSessionInfo.java instead.
package er.persistentsessionstorage.model.eogen;

import com.webobjects.eoaccess.*;
import com.webobjects.eocontrol.*;
import com.webobjects.foundation.*;
import java.math.*;
import java.util.*;
import org.apache.log4j.Logger;

import er.extensions.eof.*;
import er.extensions.foundation.*;


@SuppressWarnings("all")
public abstract class _ERSessionInfo extends  ERXGenericRecord {
  public static final String ENTITY_NAME = "ERSessionInfo";

  // Attributes
  public static final ERXKey<NSTimestamp> EXPIRATION_DATE = new ERXKey<NSTimestamp>("expirationDate");
  public static final String EXPIRATION_DATE_KEY = EXPIRATION_DATE.key();
  public static final ERXKey<Integer> INT_LOCK = new ERXKey<Integer>("intLock");
  public static final String INT_LOCK_KEY = INT_LOCK.key();
  public static final ERXKey<NSData> SESSION_DATA = new ERXKey<NSData>("sessionData");
  public static final String SESSION_DATA_KEY = SESSION_DATA.key();
  public static final ERXKey<String> SESSION_ID = new ERXKey<String>("sessionID");
  public static final String SESSION_ID_KEY = SESSION_ID.key();

  // Relationships

  public static class _ERSessionInfoClazz<T extends er.persistentsessionstorage.model.ERSessionInfo> extends ERXGenericRecord.ERXGenericRecordClazz<T> {
    /* more clazz methods here */
  }

  private static final Logger LOG = Logger.getLogger(_ERSessionInfo.class);

  public er.persistentsessionstorage.model.ERSessionInfo.ERSessionInfoClazz clazz() {
    return er.persistentsessionstorage.model.ERSessionInfo.clazz;
  }
  
  public NSTimestamp expirationDate() {
    return (NSTimestamp) storedValueForKey(_ERSessionInfo.EXPIRATION_DATE_KEY);
  }

  public void setExpirationDate(NSTimestamp value) {
    if (_ERSessionInfo.LOG.isDebugEnabled()) {
    	_ERSessionInfo.LOG.debug( "updating expirationDate from " + expirationDate() + " to " + value);
    }
    takeStoredValueForKey(value, _ERSessionInfo.EXPIRATION_DATE_KEY);
  }

  public Integer intLock() {
    return (Integer) storedValueForKey(_ERSessionInfo.INT_LOCK_KEY);
  }

  public void setIntLock(Integer value) {
    if (_ERSessionInfo.LOG.isDebugEnabled()) {
    	_ERSessionInfo.LOG.debug( "updating intLock from " + intLock() + " to " + value);
    }
    takeStoredValueForKey(value, _ERSessionInfo.INT_LOCK_KEY);
  }

  public NSData sessionData() {
    return (NSData) storedValueForKey(_ERSessionInfo.SESSION_DATA_KEY);
  }

  public void setSessionData(NSData value) {
    if (_ERSessionInfo.LOG.isDebugEnabled()) {
    	_ERSessionInfo.LOG.debug( "updating sessionData from " + sessionData() + " to " + value);
    }
    takeStoredValueForKey(value, _ERSessionInfo.SESSION_DATA_KEY);
  }

  public String sessionID() {
    return (String) storedValueForKey(_ERSessionInfo.SESSION_ID_KEY);
  }

  public void setSessionID(String value) {
    if (_ERSessionInfo.LOG.isDebugEnabled()) {
    	_ERSessionInfo.LOG.debug( "updating sessionID from " + sessionID() + " to " + value);
    }
    takeStoredValueForKey(value, _ERSessionInfo.SESSION_ID_KEY);
  }


}
