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

    public _Bug() {
        super();
    }

    public static abstract class _BugClazz extends er.extensions.ERXGenericRecord.ERXGenericRecordClazz {

        public NSArray bugsFiledRecentlyWithDateUser(EOEditingContext ec, Object date, Object user) {
            NSMutableDictionary _dict = new NSMutableDictionary(4);
            
            if(date != null) _dict.setObjectForKey( date, "date");
            if(user != null) _dict.setObjectForKey( user, "user");
            return EOUtilities.objectsWithFetchSpecificationAndBindings(ec, "Bug", "bugsFiledRecently", _dict);
        }

        public NSArray bugsInBuildWithTargetRelease(EOEditingContext ec, Object targetRelease) {
            NSMutableDictionary _dict = new NSMutableDictionary(4);
            
            if(targetRelease != null) _dict.setObjectForKey( targetRelease, "targetRelease");
            return EOUtilities.objectsWithFetchSpecificationAndBindings(ec, "Bug", "bugsInBuild", _dict);
        }

        public NSArray bugsOwnedWithUser(EOEditingContext ec, Object user) {
            NSMutableDictionary _dict = new NSMutableDictionary(4);
            
            if(user != null) _dict.setObjectForKey( user, "user");
            return EOUtilities.objectsWithFetchSpecificationAndBindings(ec, "Bug", "bugsOwned", _dict);
        }

        public NSArray unreadBugsWithUser(EOEditingContext ec, Object user) {
            NSMutableDictionary _dict = new NSMutableDictionary(4);
            
            if(user != null) _dict.setObjectForKey( user, "user");
            return EOUtilities.objectsWithFetchSpecificationAndBindings(ec, "Bug", "unreadBugs", _dict);
        }

    }


    public String subject() {
        return (String)storedValueForKey("subject");
    }
    public void setSubject(String aValue) {
        takeStoredValueForKey(aValue, "subject");
    }

    public String read() {
        return (String)storedValueForKey("read");
    }
    public void setRead(String aValue) {
        takeStoredValueForKey(aValue, "read");
    }

    public NSTimestamp dateSubmitted() {
        return (NSTimestamp)storedValueForKey("dateSubmitted");
    }
    public void setDateSubmitted(NSTimestamp aValue) {
        takeStoredValueForKey(aValue, "dateSubmitted");
    }

    public NSTimestamp dateModified() {
        return (NSTimestamp)storedValueForKey("dateModified");
    }
    public void setDateModified(NSTimestamp aValue) {
        takeStoredValueForKey(aValue, "dateModified");
    }

    public Number featureRequest() {
        return (Number)storedValueForKey("featureRequest");
    }
    public void setFeatureRequest(Number aValue) {
        takeStoredValueForKey(aValue, "featureRequest");
    }

    public String textDescription() {
        return (String)storedValueForKey("textDescription");
    }
    public void setTextDescription(String aValue) {
        takeStoredValueForKey(aValue, "textDescription");
    }

    public Number bugid() {
        return (Number)storedValueForKey("bugid");
    }
    public void setBugid(Number aValue) {
        takeStoredValueForKey(aValue, "bugid");
    }

    public er.bugtracker.Priority priority() {
        return (er.bugtracker.Priority)storedValueForKey("priority");
    }

    public void setPriority(er.bugtracker.Priority aValue) {
        takeStoredValueForKey(aValue, "priority");
    }
    public void addToBothSidesOfPriority(er.bugtracker.Priority object) {
        addObjectToBothSidesOfRelationshipWithKey(object, "priority");
    }
    public void removeFromBothSidesOfPriority(er.bugtracker.Priority object) {
        removeObjectFromBothSidesOfRelationshipWithKey(object, "priority");
    }


    public er.bugtracker.Component component() {
        return (er.bugtracker.Component)storedValueForKey("component");
    }

    public void setComponent(er.bugtracker.Component aValue) {
        takeStoredValueForKey(aValue, "component");
    }
    public void addToBothSidesOfComponent(er.bugtracker.Component object) {
        addObjectToBothSidesOfRelationshipWithKey(object, "component");
    }
    public void removeFromBothSidesOfComponent(er.bugtracker.Component object) {
        removeObjectFromBothSidesOfRelationshipWithKey(object, "component");
    }


    public er.bugtracker.People owner() {
        return (er.bugtracker.People)storedValueForKey("owner");
    }

    public void setOwner(er.bugtracker.People aValue) {
        takeStoredValueForKey(aValue, "owner");
    }
    public void addToBothSidesOfOwner(er.bugtracker.People object) {
        addObjectToBothSidesOfRelationshipWithKey(object, "owner");
    }
    public void removeFromBothSidesOfOwner(er.bugtracker.People object) {
        removeObjectFromBothSidesOfRelationshipWithKey(object, "owner");
    }


    public er.bugtracker.State state() {
        return (er.bugtracker.State)storedValueForKey("state");
    }

    public void setState(er.bugtracker.State aValue) {
        takeStoredValueForKey(aValue, "state");
    }
    public void addToBothSidesOfState(er.bugtracker.State object) {
        addObjectToBothSidesOfRelationshipWithKey(object, "state");
    }
    public void removeFromBothSidesOfState(er.bugtracker.State object) {
        removeObjectFromBothSidesOfRelationshipWithKey(object, "state");
    }


    public er.bugtracker.People originator() {
        return (er.bugtracker.People)storedValueForKey("originator");
    }

    public void setOriginator(er.bugtracker.People aValue) {
        takeStoredValueForKey(aValue, "originator");
    }
    public void addToBothSidesOfOriginator(er.bugtracker.People object) {
        addObjectToBothSidesOfRelationshipWithKey(object, "originator");
    }
    public void removeFromBothSidesOfOriginator(er.bugtracker.People object) {
        removeObjectFromBothSidesOfRelationshipWithKey(object, "originator");
    }


    public er.bugtracker.Release targetRelease() {
        return (er.bugtracker.Release)storedValueForKey("targetRelease");
    }

    public void setTargetRelease(er.bugtracker.Release aValue) {
        takeStoredValueForKey(aValue, "targetRelease");
    }
    public void addToBothSidesOfTargetRelease(er.bugtracker.Release object) {
        addObjectToBothSidesOfRelationshipWithKey(object, "targetRelease");
    }
    public void removeFromBothSidesOfTargetRelease(er.bugtracker.Release object) {
        removeObjectFromBothSidesOfRelationshipWithKey(object, "targetRelease");
    }


    public er.bugtracker.People previousOwner() {
        return (er.bugtracker.People)storedValueForKey("previousOwner");
    }

    public void setPreviousOwner(er.bugtracker.People aValue) {
        takeStoredValueForKey(aValue, "previousOwner");
    }
    public void addToBothSidesOfPreviousOwner(er.bugtracker.People object) {
        addObjectToBothSidesOfRelationshipWithKey(object, "previousOwner");
    }
    public void removeFromBothSidesOfPreviousOwner(er.bugtracker.People object) {
        removeObjectFromBothSidesOfRelationshipWithKey(object, "previousOwner");
    }


    public NSArray testItems() {
        return (NSArray)storedValueForKey("testItems");
    }
    public void setTestItems(NSMutableArray aValue) {
        takeStoredValueForKey(aValue, "testItems");
    }
    public void addToTestItems(er.bugtracker.TestItem object) {
        NSMutableArray array = (NSMutableArray)testItems();

        willChange();
        array.addObject(object);
    }
    public void removeFromTestItems(er.bugtracker.TestItem object) {
        NSMutableArray array = (NSMutableArray)testItems();

        willChange();
        array.removeObject(object);
    }
    public void addToBothSidesOfTestItems(er.bugtracker.TestItem object) {
        addObjectToBothSidesOfRelationshipWithKey(object, "testItems");
    }
    public void removeFromBothSidesOfTestItems(er.bugtracker.TestItem object) {
        removeObjectFromBothSidesOfRelationshipWithKey(object, "testItems");
    }

}
