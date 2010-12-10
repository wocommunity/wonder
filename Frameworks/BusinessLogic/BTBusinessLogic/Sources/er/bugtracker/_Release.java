// _Release.java
// 
// Created by eogenerator
// DO NOT EDIT.  Make changes to Release.java instead.
package er.bugtracker;

import com.webobjects.eoaccess.EOUtilities;
import com.webobjects.eocontrol.EOEditingContext;
import com.webobjects.foundation.NSArray;
import er.extensions.eof.ERXGenericRecord;

public abstract class _Release extends ERXGenericRecord {

    public _Release() {
        super();
    }

    public static abstract class _ReleaseClazz extends ERXGenericRecord.ERXGenericRecordClazz {

        public NSArray fetchAll(EOEditingContext ec) {
            return EOUtilities.objectsWithFetchSpecificationAndBindings(ec, "Release", "FetchAll", null);
        }

    }


    public String name() {
        return (String)storedValueForKey("name");
    }
    public void setName(String aValue) {
        takeStoredValueForKey(aValue, "name");
    }

    public Number isOpen() {
        return (Number)storedValueForKey("isOpen");
    }
    public void setIsOpen(Number aValue) {
        takeStoredValueForKey(aValue, "isOpen");
    }
}
