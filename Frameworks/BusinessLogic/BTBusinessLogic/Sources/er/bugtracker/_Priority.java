// _Priority.java
// 
// Created by eogenerator
// DO NOT EDIT.  Make changes to Priority.java instead.
package er.bugtracker;

import com.webobjects.eoaccess.EOUtilities;
import com.webobjects.eocontrol.EOEditingContext;
import com.webobjects.foundation.NSArray;
import er.extensions.eof.ERXGenericRecord;

public abstract class _Priority extends ERXGenericRecord {

    public _Priority() {
        super();
    }

    public static abstract class _PriorityClazz extends ERXGenericRecord.ERXGenericRecordClazz {

        public NSArray fetchAll(EOEditingContext ec) {
            return EOUtilities.objectsWithFetchSpecificationAndBindings(ec, "Priority", "FetchAll", null);
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
