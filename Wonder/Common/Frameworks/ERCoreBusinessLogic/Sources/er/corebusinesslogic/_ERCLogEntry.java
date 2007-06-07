// _ERCLogEntry.java
// 
// Created by eogenerator
// DO NOT EDIT.  Make changes to ERCLogEntry.java instead.
package er.corebusinesslogic;
import com.webobjects.foundation.*;
import com.webobjects.eocontrol.*;
import com.webobjects.eoaccess.*;
import er.extensions.*;
import java.util.*;
import java.math.BigDecimal;

public abstract class _ERCLogEntry extends ERXGenericRecord {

    public static final String ENTITY = "ERCLogEntry";

    public interface Key  {
        public static final String USER_ID = "userID";
        public static final String TEXT = "text";
        public static final String CREATED = "created";  
    }

    public static abstract class _ERCLogEntryClazz extends ERXGenericRecord.ERXGenericRecordClazz {
 

    }


    public NSTimestamp created() {
        return (NSTimestamp)storedValueForKey(Key.CREATED);
    }
    public void setCreated(NSTimestamp aValue) {
        takeStoredValueForKey(aValue, Key.CREATED);
    }

    public String text() {
        return (String)storedValueForKey(Key.TEXT);
    }
    public void setText(String aValue) {
        takeStoredValueForKey(aValue, Key.TEXT);
    }

    public Number userID() {
        return (Number)storedValueForKey(Key.USER_ID);
    }
    public void setUserID(Number aValue) {
        takeStoredValueForKey(aValue, Key.USER_ID);
    }
}
