// _RequirementType.java
// 
// Created by eogenerator
// DO NOT EDIT.  Make changes to RequirementType.java instead.
package er.bugtracker;
import com.webobjects.foundation.*;
import com.webobjects.eocontrol.*;
import com.webobjects.eoaccess.*;
import er.extensions.*;
import java.util.*;
import java.math.BigDecimal;

public abstract class _RequirementType extends ERXGenericRecord {

    public _RequirementType() {
        super();
    }

    public static abstract class _RequirementTypeClazz extends er.extensions.ERXGenericRecord.ERXGenericRecordClazz {

        public NSArray fetchAll(EOEditingContext ec) {
            return EOUtilities.objectsWithFetchSpecificationAndBindings(ec, "RequirementType", "FetchAll", null);
        }

    }


    public String typeDescription() {
        return (String)storedValueForKey("typeDescription");
    }
    public void setTypeDescription(String aValue) {
        takeStoredValueForKey(aValue, "typeDescription");
    }
}
