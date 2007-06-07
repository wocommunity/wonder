// _Bug.java
// 
// Created by eogenerator
// DO NOT EDIT.  Make changes to Bug.java instead.
package er.bugtracker;
import com.webobjects.foundation.*;
import com.webobjects.eocontrol.*;
import com.webobjects.eoaccess.*;
import er.extensions.*;
import java.util.*;
import java.math.BigDecimal;

public abstract class _Bug extends ERXGenericRecord {

    public static final String ENTITY = "Bug";

    public interface Key  {
        public static final String TYPE = "type";
        public static final String TEST_ITEMS = "testItems";
        public static final String TARGET_RELEASE = "targetRelease";
        public static final String SUBJECT = "subject";
        public static final String STATE = "state";
        public static final String PRIORITY = "priority";
        public static final String PREVIOUS_OWNER = "previousOwner";
        public static final String OWNER = "owner";
        public static final String ORIGINATOR = "originator";
        public static final String IS_READ = "isRead";
        public static final String IS_FEATURE_REQUEST = "isFeatureRequest";
        public static final String DATE_SUBMITTED = "dateSubmitted";
        public static final String DATE_MODIFIED = "dateModified";
        public static final String COMPONENT = "component";
        public static final String COMMENTS = "comments";  
    }

    public static abstract class _BugClazz extends ERXGenericRecord.ERXGenericRecordClazz {
 

        public NSArray objectsForBugsFiledRecently(EOEditingContext context, NSTimestamp dateBinding, er.bugtracker.People userBinding) {
            EOFetchSpecification spec = EOFetchSpecification.fetchSpecificationNamed("bugsFiledRecently", "Bug");

            NSMutableDictionary bindings = new NSMutableDictionary();

            if (dateBinding != null)
                bindings.setObjectForKey(dateBinding, "date");
            if (userBinding != null)
                bindings.setObjectForKey(userBinding, "user");
            spec = spec.fetchSpecificationWithQualifierBindings(bindings);

            return context.objectsWithFetchSpecification(spec);
        }

        public NSArray objectsForBugsInBuild(EOEditingContext context, er.bugtracker.Release targetReleaseBinding) {
            EOFetchSpecification spec = EOFetchSpecification.fetchSpecificationNamed("bugsInBuild", "Bug");

            NSMutableDictionary bindings = new NSMutableDictionary();

            if (targetReleaseBinding != null)
                bindings.setObjectForKey(targetReleaseBinding, "targetRelease");
            spec = spec.fetchSpecificationWithQualifierBindings(bindings);

            return context.objectsWithFetchSpecification(spec);
        }

        public NSArray objectsForBugsOwned(EOEditingContext context, er.bugtracker.People userBinding) {
            EOFetchSpecification spec = EOFetchSpecification.fetchSpecificationNamed("bugsOwned", "Bug");

            NSMutableDictionary bindings = new NSMutableDictionary();

            if (userBinding != null)
                bindings.setObjectForKey(userBinding, "user");
            spec = spec.fetchSpecificationWithQualifierBindings(bindings);

            return context.objectsWithFetchSpecification(spec);
        }

        public NSArray objectsForUnreadBugs(EOEditingContext context, er.bugtracker.People userBinding) {
            EOFetchSpecification spec = EOFetchSpecification.fetchSpecificationNamed("unreadBugs", "Bug");

            NSMutableDictionary bindings = new NSMutableDictionary();

            if (userBinding != null)
                bindings.setObjectForKey(userBinding, "user");
            spec = spec.fetchSpecificationWithQualifierBindings(bindings);

            return context.objectsWithFetchSpecification(spec);
        }

    }


    public NSTimestamp dateModified() {
        return (NSTimestamp)storedValueForKey(Key.DATE_MODIFIED);
    }
    public void setDateModified(NSTimestamp aValue) {
        takeStoredValueForKey(aValue, Key.DATE_MODIFIED);
    }

    public NSTimestamp dateSubmitted() {
        return (NSTimestamp)storedValueForKey(Key.DATE_SUBMITTED);
    }
    public void setDateSubmitted(NSTimestamp aValue) {
        takeStoredValueForKey(aValue, Key.DATE_SUBMITTED);
    }

    public boolean isFeatureRequest() {
        return ((Boolean)storedValueForKey(Key.IS_FEATURE_REQUEST)).booleanValue();
    }
    public void setIsFeatureRequest(boolean aValue) {
        takeStoredValueForKey((aValue ? Boolean.TRUE : Boolean.FALSE), Key.IS_FEATURE_REQUEST);
    }

    public boolean isRead() {
        return ((Boolean)storedValueForKey(Key.IS_READ)).booleanValue();
    }
    public void setIsRead(boolean aValue) {
        takeStoredValueForKey((aValue ? Boolean.TRUE : Boolean.FALSE), Key.IS_READ);
    }

    public er.bugtracker.State state() {
        er.bugtracker.State value = (er.bugtracker.State)storedValueForKey(Key.STATE);
        return value;
    }
    public void setState(er.bugtracker.State aValue) {
        takeStoredValueForKey(aValue, Key.STATE);
    }

    public String subject() {
        return (String)storedValueForKey(Key.SUBJECT);
    }
    public void setSubject(String aValue) {
        takeStoredValueForKey(aValue, Key.SUBJECT);
    }

    public String type() {
        return (String)storedValueForKey(Key.TYPE);
    }
    public void setType(String aValue) {
        takeStoredValueForKey(aValue, Key.TYPE);
    }

    public er.bugtracker.Component component() {
        return (er.bugtracker.Component)storedValueForKey(Key.COMPONENT);
    }
    public void setComponent(er.bugtracker.Component object) {
        takeStoredValueForKey(object, Key.COMPONENT);
    }


    public er.bugtracker.People originator() {
        return (er.bugtracker.People)storedValueForKey(Key.ORIGINATOR);
    }
    public void setOriginator(er.bugtracker.People object) {
        takeStoredValueForKey(object, Key.ORIGINATOR);
    }


    public er.bugtracker.People owner() {
        return (er.bugtracker.People)storedValueForKey(Key.OWNER);
    }
    public void setOwner(er.bugtracker.People object) {
        takeStoredValueForKey(object, Key.OWNER);
    }


    public er.bugtracker.People previousOwner() {
        return (er.bugtracker.People)storedValueForKey(Key.PREVIOUS_OWNER);
    }
    public void setPreviousOwner(er.bugtracker.People object) {
        takeStoredValueForKey(object, Key.PREVIOUS_OWNER);
    }


    public er.bugtracker.Priority priority() {
        return (er.bugtracker.Priority)storedValueForKey(Key.PRIORITY);
    }
    public void setPriority(er.bugtracker.Priority object) {
        takeStoredValueForKey(object, Key.PRIORITY);
    }


    public er.bugtracker.Release targetRelease() {
        return (er.bugtracker.Release)storedValueForKey(Key.TARGET_RELEASE);
    }
    public void setTargetRelease(er.bugtracker.Release object) {
        takeStoredValueForKey(object, Key.TARGET_RELEASE);
    }


    public NSArray comments() {
        return (NSArray)storedValueForKey(Key.COMMENTS);
    }
    public void addToComments(er.bugtracker.Comment object) {
        includeObjectIntoPropertyWithKey(object, Key.COMMENTS);
    }
    public void removeFromComments(er.bugtracker.Comment object) {
        excludeObjectFromPropertyWithKey(object, Key.COMMENTS);
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
