// _Framework.java
// 
// Created by eogenerator
// DO NOT EDIT.  Make changes to Framework.java instead.
package er.bugtracker;
import com.webobjects.foundation.*;
import com.webobjects.eocontrol.*;
import com.webobjects.eoaccess.*;
import er.extensions.*;
import java.util.*;
import java.math.BigDecimal;

public abstract class _Framework extends ERXGenericRecord {

    public static final String ENTITY = "Framework";

    public interface Key  {
        public static final String OWNER = "owner";
        public static final String OWNED_SINCE = "ownedSince";
        public static final String ORDERING = "ordering";
        public static final String NAME = "name";  
    }

    public static abstract class _FrameworkClazz extends ERXGenericRecord.ERXGenericRecordClazz {
 

        public NSArray objectsForOrderedFrameworks(EOEditingContext context) {
            EOFetchSpecification spec = EOFetchSpecification.fetchSpecificationNamed("orderedFrameworks", "Framework");

            return context.objectsWithFetchSpecification(spec);
        }

    }


    public String name() {
        return (String)storedValueForKey(Key.NAME);
    }
    public void setName(String aValue) {
        takeStoredValueForKey(aValue, Key.NAME);
    }

    public Number ordering() {
        return (Number)storedValueForKey(Key.ORDERING);
    }
    public void setOrdering(Number aValue) {
        takeStoredValueForKey(aValue, Key.ORDERING);
    }

    public NSTimestamp ownedSince() {
        return (NSTimestamp)storedValueForKey(Key.OWNED_SINCE);
    }
    public void setOwnedSince(NSTimestamp aValue) {
        takeStoredValueForKey(aValue, Key.OWNED_SINCE);
    }

    public er.bugtracker.People owner() {
        return (er.bugtracker.People)storedValueForKey(Key.OWNER);
    }
    public void setOwner(er.bugtracker.People object) {
        takeStoredValueForKey(object, Key.OWNER);
    }

}
