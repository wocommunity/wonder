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

    public interface Key  {
        public static final String TYPE_DESCRIPTION = "typeDescription";  
    }

    public static abstract class _RequirementTypeClazz extends ERXGenericRecord.ERXGenericRecordClazz {

        public NSArray objectsForFetchAll(EOEditingContext context) {
            EOFetchSpecification spec = EOFetchSpecification.fetchSpecificationNamed("FetchAll", "RequirementType");

            return context.objectsWithFetchSpecification(spec);
        }

    }


    public String typeDescription() {
        return (String)storedValueForKey(Key.TYPE_DESCRIPTION);
    }
    public void setTypeDescription(String aValue) {
        takeStoredValueForKey(aValue, Key.TYPE_DESCRIPTION);
    }
}
