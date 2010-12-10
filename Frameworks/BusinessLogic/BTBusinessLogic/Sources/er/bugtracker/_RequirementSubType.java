// _RequirementSubType.java
// 
// Created by eogenerator
// DO NOT EDIT.  Make changes to RequirementSubType.java instead.
package er.bugtracker;

import com.webobjects.eoaccess.EOUtilities;
import com.webobjects.eocontrol.EOEditingContext;
import com.webobjects.foundation.NSArray;
import er.extensions.eof.ERXGenericRecord;

public abstract class _RequirementSubType extends ERXGenericRecord {

    public _RequirementSubType() {
        super();
    }

    public static abstract class _RequirementSubTypeClazz extends ERXGenericRecord.ERXGenericRecordClazz {

        public NSArray fetchAll(EOEditingContext ec) {
            return EOUtilities.objectsWithFetchSpecificationAndBindings(ec, "RequirementSubType", "FetchAll", null);
        }

    }


    public String subTypeDescription() {
        return (String)storedValueForKey("subTypeDescription");
    }
    public void setSubTypeDescription(String aValue) {
        takeStoredValueForKey(aValue, "subTypeDescription");
    }
}
