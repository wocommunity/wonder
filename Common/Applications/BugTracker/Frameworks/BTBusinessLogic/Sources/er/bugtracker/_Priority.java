// _Priority.java
// 
// Created by eogenerator
// DO NOT EDIT.  Make changes to Priority.java instead.
package er.bugtracker;
import com.webobjects.foundation.*;
import com.webobjects.eocontrol.*;
import com.webobjects.eoaccess.*;
import er.extensions.*;
import java.util.*;
import java.math.BigDecimal;

public abstract class _Priority extends ERXGenericRecord {

    public static final String ENTITY = "Priority";

    public interface Key  {
        public static final String TEXT_DESCRIPTION = "textDescription";
        public static final String SORT_ORDER = "sortOrder";  
    }

    public static abstract class _PriorityClazz extends ERXGenericRecord.ERXGenericRecordClazz {
    
    	public Priority createPriority(EOEditingContext editingContext, Number sortOrder, String textDescription) {
	   		Priority eo = (Priority)EOUtilities.createAndInsertInstance(editingContext, Priority.ENTITY);
	    	eo.setSortOrder(sortOrder);
	    	eo.setTextDescription(textDescription);
	    	return eo;
 		}


        public NSArray objectsForFetchAll(EOEditingContext context) {
            EOFetchSpecification spec = EOFetchSpecification.fetchSpecificationNamed("FetchAll", "Priority");

            return context.objectsWithFetchSpecification(spec);
        }

    }


    public Number sortOrder() {
        return (Number)storedValueForKey(Key.SORT_ORDER);
    }
    public void setSortOrder(Number aValue) {
        takeStoredValueForKey(aValue, Key.SORT_ORDER);
    }

    public String textDescription() {
        return (String)storedValueForKey(Key.TEXT_DESCRIPTION);
    }
    public void setTextDescription(String aValue) {
        takeStoredValueForKey(aValue, Key.TEXT_DESCRIPTION);
    }
}
