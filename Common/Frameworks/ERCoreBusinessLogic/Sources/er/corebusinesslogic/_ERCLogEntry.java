// _ERCLogEntry.java
// 
// Created by eogenerator
// DO NOT EDIT.  Make changes to ERCLogEntry.java instead.
package er.corebusinesslogic;
import com.webobjects.foundation.*;

import er.extensions.*;

public abstract class _ERCLogEntry extends ERXGenericRecord {

    public _ERCLogEntry() {
        super();
    }

    public static abstract class _ERCLogEntryClazz extends er.extensions.ERXGenericRecord.ERXGenericRecordClazz {

    }


    public NSTimestamp created() {
        return (NSTimestamp)storedValueForKey("created");
    }
    public void setCreated(NSTimestamp aValue) {
        takeStoredValueForKey(aValue, "created");
    }

    public Number userID() {
        return (Number)storedValueForKey("userID");
    }
    public void setUserID(Number aValue) {
        takeStoredValueForKey(aValue, "userID");
    }

    public String text() {
        return (String)storedValueForKey("text");
    }
    public void setText(String aValue) {
        takeStoredValueForKey(aValue, "text");
    }
}
