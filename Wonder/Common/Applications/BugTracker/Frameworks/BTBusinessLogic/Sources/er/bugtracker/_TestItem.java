// _TestItem.java
// 
// Created by eogenerator
// DO NOT EDIT.  Make changes to TestItem.java instead.
package er.bugtracker;
import com.webobjects.foundation.*;
import com.webobjects.eocontrol.*;
import com.webobjects.eoaccess.*;
import er.extensions.*;
import java.util.*;
import java.math.BigDecimal;

public abstract class _TestItem extends ERXGenericRecord {

    public static final String ENTITY = "TestItem";

    public interface Key  {
        public static final String TITLE = "title";
        public static final String TEXT_DESCRIPTION = "textDescription";
        public static final String STATE = "state";
        public static final String REQUIREMENTS = "requirements";
        public static final String OWNER = "owner";
        public static final String ID = "id";
        public static final String DATE_CREATED = "dateCreated";
        public static final String CONTROLLED = "controlled";
        public static final String COMPONENT = "component";
        public static final String COMMENTS = "comments";
        public static final String BUGS = "bugs";  
    }

    public static abstract class _TestItemClazz extends ERXGenericRecord.ERXGenericRecordClazz {
 

        public NSArray objectsForUnclosedTestItems(EOEditingContext context, er.bugtracker.People userBinding) {
            EOFetchSpecification spec = EOFetchSpecification.fetchSpecificationNamed("unclosedTestItems", "TestItem");

            NSMutableDictionary bindings = new NSMutableDictionary();

            if (userBinding != null)
                bindings.setObjectForKey(userBinding, "user");
            spec = spec.fetchSpecificationWithQualifierBindings(bindings);

            return context.objectsWithFetchSpecification(spec);
        }

    }


    public String comments() {
        return (String)storedValueForKey(Key.COMMENTS);
    }
    public void setComments(String aValue) {
        takeStoredValueForKey(aValue, Key.COMMENTS);
    }

    public String controlled() {
        return (String)storedValueForKey(Key.CONTROLLED);
    }
    public void setControlled(String aValue) {
        takeStoredValueForKey(aValue, Key.CONTROLLED);
    }

    public NSTimestamp dateCreated() {
        return (NSTimestamp)storedValueForKey(Key.DATE_CREATED);
    }
    public void setDateCreated(NSTimestamp aValue) {
        takeStoredValueForKey(aValue, Key.DATE_CREATED);
    }

    public Number id() {
        return (Number)storedValueForKey(Key.ID);
    }
    public void setId(Number aValue) {
        takeStoredValueForKey(aValue, Key.ID);
    }

    public String textDescription() {
        return (String)storedValueForKey(Key.TEXT_DESCRIPTION);
    }
    public void setTextDescription(String aValue) {
        takeStoredValueForKey(aValue, Key.TEXT_DESCRIPTION);
    }

    public String title() {
        return (String)storedValueForKey(Key.TITLE);
    }
    public void setTitle(String aValue) {
        takeStoredValueForKey(aValue, Key.TITLE);
    }

    public er.bugtracker.Component component() {
        return (er.bugtracker.Component)storedValueForKey(Key.COMPONENT);
    }
    public void setComponent(er.bugtracker.Component object) {
        takeStoredValueForKey(object, Key.COMPONENT);
    }


    public er.bugtracker.People owner() {
        return (er.bugtracker.People)storedValueForKey(Key.OWNER);
    }
    public void setOwner(er.bugtracker.People object) {
        takeStoredValueForKey(object, Key.OWNER);
    }


    public er.bugtracker.TestItemState state() {
        return (er.bugtracker.TestItemState)storedValueForKey(Key.STATE);
    }
    public void setState(er.bugtracker.TestItemState object) {
        takeStoredValueForKey(object, Key.STATE);
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
