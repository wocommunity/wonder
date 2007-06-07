// _TestItemState.java
// 
// Created by eogenerator
// DO NOT EDIT.  Make changes to TestItemState.java instead.
package er.bugtracker;
import com.webobjects.foundation.*;
import com.webobjects.eocontrol.*;
import com.webobjects.eoaccess.*;
import er.extensions.*;
import java.util.*;
import java.math.BigDecimal;

public abstract class _TestItemState extends ERXGenericRecord {

    public static final String ENTITY = "TestItemState";

    public interface Key  {
        public static final String SORT_ORDER = "sortOrder";
        public static final String NAME = "name";  
    }

    public static abstract class _TestItemStateClazz extends ERXGenericRecord.ERXGenericRecordClazz {
    
    	public TestItemState createTestItemState(EOEditingContext editingContext, String name, Number sortOrder) {
	   		TestItemState eo = (TestItemState)EOUtilities.createAndInsertInstance(editingContext, TestItemState.ENTITY);
	    	eo.setName(name);
	    	eo.setSortOrder(sortOrder);
	    	return eo;
 		}


        public NSArray objectsForFetchAll(EOEditingContext context) {
            EOFetchSpecification spec = EOFetchSpecification.fetchSpecificationNamed("FetchAll", "TestItemState");

            return context.objectsWithFetchSpecification(spec);
        }

    }


    public String name() {
        return (String)storedValueForKey(Key.NAME);
    }
    public void setName(String aValue) {
        takeStoredValueForKey(aValue, Key.NAME);
    }

    public Number sortOrder() {
        return (Number)storedValueForKey(Key.SORT_ORDER);
    }
    public void setSortOrder(Number aValue) {
        takeStoredValueForKey(aValue, Key.SORT_ORDER);
    }
}
