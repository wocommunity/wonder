// _State.java
// 
// Created by eogenerator
// DO NOT EDIT.  Make changes to State.java instead.
package er.bugtracker;

import com.webobjects.eoaccess.EOUtilities;
import com.webobjects.eocontrol.EOEditingContext;
import com.webobjects.foundation.NSArray;
import er.extensions.eof.ERXGenericRecord;

public abstract class _State extends ERXGenericRecord {

    public _State() {
        super();
    }

    public static abstract class _StateClazz extends ERXGenericRecord.ERXGenericRecordClazz {

        public NSArray fetchAll(EOEditingContext ec) {
            return EOUtilities.objectsWithFetchSpecificationAndBindings(ec, "State", "FetchAll", null);
        }

    }


    public String textDescription() {
        return (String)storedValueForKey("textDescription");
    }
    public void setTextDescription(String aValue) {
        takeStoredValueForKey(aValue, "textDescription");
    }

    public Number sortOrder() {
        return (Number)storedValueForKey("sortOrder");
    }
    public void setSortOrder(Number aValue) {
        takeStoredValueForKey(aValue, "sortOrder");
    }
}
