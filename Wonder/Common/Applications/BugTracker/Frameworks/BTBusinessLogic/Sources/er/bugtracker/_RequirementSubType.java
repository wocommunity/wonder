// _RequirementSubType.java
// 
// Created by eogenerator
// DO NOT EDIT.  Make changes to RequirementSubType.java instead.
package er.bugtracker;
import com.webobjects.foundation.*;
import com.webobjects.eocontrol.*;
import com.webobjects.eoaccess.*;
import er.extensions.*;
import java.util.*;
import java.math.BigDecimal;

public abstract class _RequirementSubType extends ERXGenericRecord {

    public _RequirementSubType() {
        super();
    }

    public static abstract class _RequirementSubTypeClazz extends er.extensions.ERXGenericRecord.ERXGenericRecordClazz {

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
