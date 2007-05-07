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

    public Boolean featureRequest() {
        return (Boolean)storedValueForKey("featureRequest");
    }
    public void setFeatureRequest(Boolean aValue) {
        takeStoredValueForKey(aValue, "featureRequest");
    }

    public String textDescription() {
        return (String)storedValueForKey("textDescription");
    }
    public void setTextDescription(String aValue) {
        takeStoredValueForKey(aValue, "textDescription");
    }

    public Number bugid() {
        return (Number)storedValueForKey("id");
    }
    public void setBugid(Number aValue) {
        takeStoredValueForKey(aValue, "id");
    }

    public Priority priority() {
        return (Priority)storedValueForKey("priority");
    }

    public void setPriority(Priority aValue) {
        takeStoredValueForKey(aValue, "priority");
    }
    public void addToBothSidesOfPriority(Priority object) {
        addObjectToBothSidesOfRelationshipWithKey(object, "priority");
    }
    public void removeFromBothSidesOfPriority(Priority object) {
        removeObjectFromBothSidesOfRelationshipWithKey(object, "priority");
    }


    public Component component() {
        return (Component)storedValueForKey("component");
    }

    public void setComponent(Component aValue) {
        takeStoredValueForKey(aValue, "component");
    }
    public void addToBothSidesOfComponent(Component object) {
        addObjectToBothSidesOfRelationshipWithKey(object, "component");
    }
    public void removeFromBothSidesOfComponent(Component object) {
        removeObjectFromBothSidesOfRelationshipWithKey(object, "component");
    }


    public People owner() {
        return (People)storedValueForKey("owner");
    }

    public void setOwner(People aValue) {
        takeStoredValueForKey(aValue, "owner");
    }
    public void addToBothSidesOfOwner(People object) {
        addObjectToBothSidesOfRelationshipWithKey(object, "owner");
    }
    public void removeFromBothSidesOfOwner(People object) {
        removeObjectFromBothSidesOfRelationshipWithKey(object, "owner");
    }


    public State state() {
        return (State)storedValueForKey("state");
    }

    public void setState(State aValue) {
        takeStoredValueForKey(aValue, "state");
    }
    public void addToBothSidesOfState(State object) {
        addObjectToBothSidesOfRelationshipWithKey(object, "state");
    }
    public void removeFromBothSidesOfState(State object) {
        removeObjectFromBothSidesOfRelationshipWithKey(object, "state");
    }


    public People originator() {
        return (People)storedValueForKey("originator");
    }

    public void setOriginator(People aValue) {
        takeStoredValueForKey(aValue, "originator");
    }
    public void addToBothSidesOfOriginator(People object) {
        addObjectToBothSidesOfRelationshipWithKey(object, "originator");
    }
    public void removeFromBothSidesOfOriginator(People object) {
        removeObjectFromBothSidesOfRelationshipWithKey(object, "originator");
    }


    public Release targetRelease() {
        return (Release)storedValueForKey("targetRelease");
    }

    public void setTargetRelease(Release aValue) {
        takeStoredValueForKey(aValue, "targetRelease");
    }
    public void addToBothSidesOfTargetRelease(Release object) {
        addObjectToBothSidesOfRelationshipWithKey(object, "targetRelease");
    }
    public void removeFromBothSidesOfTargetRelease(Release object) {
        removeObjectFromBothSidesOfRelationshipWithKey(object, "targetRelease");
    }


    public People previousOwner() {
        return (People)storedValueForKey("previousOwner");
    }

    public void setPreviousOwner(People aValue) {
        takeStoredValueForKey(aValue, "previousOwner");
    }
    public void addToBothSidesOfPreviousOwner(People object) {
        addObjectToBothSidesOfRelationshipWithKey(object, "previousOwner");
    }
    public void removeFromBothSidesOfPreviousOwner(People object) {
        removeObjectFromBothSidesOfRelationshipWithKey(object, "previousOwner");
    }


    public NSArray testItems() {
        return (NSArray)storedValueForKey("testItems");
    }
    public void setTestItems(NSMutableArray aValue) {
        takeStoredValueForKey(aValue, "testItems");
    }
    public void addToTestItems(TestItem object) {
        NSMutableArray array = (NSMutableArray)testItems();

        willChange();
        array.addObject(object);
    }
    public void removeFromTestItems(TestItem object) {
        NSMutableArray array = (NSMutableArray)testItems();

        willChange();
        array.removeObject(object);
    }
    public void addToBothSidesOfTestItems(TestItem object) {
        addObjectToBothSidesOfRelationshipWithKey(object, "testItems");
    }
    public void removeFromBothSidesOfTestItems(TestItem object) {
        removeObjectFromBothSidesOfRelationshipWithKey(object, "testItems");
    }

}
