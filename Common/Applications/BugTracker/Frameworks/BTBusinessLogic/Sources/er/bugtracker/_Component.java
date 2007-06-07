// _Component.java
// 
// Created by eogenerator
// DO NOT EDIT.  Make changes to Component.java instead.
package er.bugtracker;
import com.webobjects.foundation.*;
import com.webobjects.eocontrol.*;
import com.webobjects.eoaccess.*;
import er.extensions.*;
import java.util.*;
import java.math.BigDecimal;

public abstract class _Component extends ERXGenericRecord {

    public static final String ENTITY = "Component";

    public interface Key  {
        public static final String TEXT_DESCRIPTION = "textDescription";
        public static final String TEST_ITEMS = "testItems";
        public static final String REQUIREMENTS = "requirements";
        public static final String PARENT = "parent";
        public static final String OWNER = "owner";
        public static final String CHILDREN = "children";
        public static final String BUGS = "bugs";  
    }

    public static abstract class _ComponentClazz extends ERXGenericRecord.ERXGenericRecordClazz {
 

    }


    public String textDescription() {
        return (String)storedValueForKey(Key.TEXT_DESCRIPTION);
    }
    public void setTextDescription(String aValue) {
        takeStoredValueForKey(aValue, Key.TEXT_DESCRIPTION);
    }

    public er.bugtracker.People owner() {
        return (er.bugtracker.People)storedValueForKey(Key.OWNER);
    }
    public void setOwner(er.bugtracker.People object) {
        takeStoredValueForKey(object, Key.OWNER);
    }


    public er.bugtracker.Component parent() {
        return (er.bugtracker.Component)storedValueForKey(Key.PARENT);
    }
    public void setParent(er.bugtracker.Component object) {
        takeStoredValueForKey(object, Key.PARENT);
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


    public NSArray children() {
        return (NSArray)storedValueForKey(Key.CHILDREN);
    }
    public void addToChildren(er.bugtracker.Component object) {
        includeObjectIntoPropertyWithKey(object, Key.CHILDREN);
    }
    public void removeFromChildren(er.bugtracker.Component object) {
        excludeObjectFromPropertyWithKey(object, Key.CHILDREN);
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


    public NSArray testItems() {
        return (NSArray)storedValueForKey(Key.TEST_ITEMS);
    }
    public void addToTestItems(er.bugtracker.TestItem object) {
        includeObjectIntoPropertyWithKey(object, Key.TEST_ITEMS);
    }
    public void removeFromTestItems(er.bugtracker.TestItem object) {
        excludeObjectFromPropertyWithKey(object, Key.TEST_ITEMS);
    }

}
