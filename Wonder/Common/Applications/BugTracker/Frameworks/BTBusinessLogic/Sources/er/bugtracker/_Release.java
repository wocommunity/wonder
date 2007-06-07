// _Release.java
// 
// Created by eogenerator
// DO NOT EDIT.  Make changes to Release.java instead.
package er.bugtracker;
import com.webobjects.foundation.*;
import com.webobjects.eocontrol.*;
import com.webobjects.eoaccess.*;
import er.extensions.*;
import java.util.*;
import java.math.BigDecimal;

public abstract class _Release extends ERXGenericRecord {

    public static final String ENTITY = "Release";

    public interface Key  {
        public static final String REQUIREMENTS = "requirements";
        public static final String NAME = "name";
        public static final String IS_OPEN = "isOpen";
        public static final String BUGS = "bugs";  
    }

    public static abstract class _ReleaseClazz extends ERXGenericRecord.ERXGenericRecordClazz {
 

        public NSArray objectsForFetchAll(EOEditingContext context) {
            EOFetchSpecification spec = EOFetchSpecification.fetchSpecificationNamed("FetchAll", "Release");

            return context.objectsWithFetchSpecification(spec);
        }

    }


    public boolean isOpen() {
        return ((Boolean)storedValueForKey(Key.IS_OPEN)).booleanValue();
    }
    public void setIsOpen(boolean aValue) {
        takeStoredValueForKey((aValue ? Boolean.TRUE : Boolean.FALSE), Key.IS_OPEN);
    }

    public String name() {
        return (String)storedValueForKey(Key.NAME);
    }
    public void setName(String aValue) {
        takeStoredValueForKey(aValue, Key.NAME);
    }

    public NSArray bugs() {
        return (NSArray)storedValueForKey(Key.BUGS);
    }
    public void addToBugs(er.bugtracker.Bug object) {
        includeObjectIntoPropertyWithKey(object, Key.BUGS);
    }
    public void removeFromBugs(er.bugtracker.Bug object) {
        excludeObjectFromPropertyWithKey(object, Key.BUGS);
    }


    public NSArray requirements() {
        return (NSArray)storedValueForKey(Key.REQUIREMENTS);
    }
    public void addToRequirements(er.bugtracker.Requirement object) {
        includeObjectIntoPropertyWithKey(object, Key.REQUIREMENTS);
    }
    public void removeFromRequirements(er.bugtracker.Requirement object) {
        excludeObjectFromPropertyWithKey(object, Key.REQUIREMENTS);
    }

}
