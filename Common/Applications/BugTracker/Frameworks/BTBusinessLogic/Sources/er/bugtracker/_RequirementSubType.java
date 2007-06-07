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

    public static final String ENTITY = "RequirementSubType";

    public interface Key  {
        public static final String SUB_TYPE_DESCRIPTION = "subTypeDescription";  
    }

    public static abstract class _RequirementSubTypeClazz extends ERXGenericRecord.ERXGenericRecordClazz {
    
    	public RequirementSubType createRequirementSubType(EOEditingContext editingContext, String subTypeDescription) {
	   		RequirementSubType eo = (RequirementSubType)EOUtilities.createAndInsertInstance(editingContext, RequirementSubType.ENTITY);
	    	eo.setSubTypeDescription(subTypeDescription);
	    	return eo;
 		}


        public NSArray objectsForFetchAll(EOEditingContext context) {
            EOFetchSpecification spec = EOFetchSpecification.fetchSpecificationNamed("FetchAll", "RequirementSubType");

            return context.objectsWithFetchSpecification(spec);
        }

    }


    public String subTypeDescription() {
        return (String)storedValueForKey(Key.SUB_TYPE_DESCRIPTION);
    }
    public void setSubTypeDescription(String aValue) {
        takeStoredValueForKey(aValue, Key.SUB_TYPE_DESCRIPTION);
    }
}
