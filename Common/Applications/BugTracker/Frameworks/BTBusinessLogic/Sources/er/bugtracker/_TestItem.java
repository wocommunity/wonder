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

    public _TestItem() {
        super();
    }

    public static abstract class _TestItemClazz extends er.extensions.ERXGenericRecord.ERXGenericRecordClazz {

        public NSArray unclosedTestItemsWithUser(EOEditingContext ec, Object user) {
            NSMutableDictionary _dict = new NSMutableDictionary(1);
            
            if(user != null) _dict.setObjectForKey( user, "user");
            return EOUtilities.objectsWithFetchSpecificationAndBindings(ec, "TestItem", "unclosedTestItems", _dict);
        }

    }


    public String controlled() {
        return (String)storedValueForKey("controlled");
    }
    public void setControlled(String aValue) {
        takeStoredValueForKey(aValue, "controlled");
    }

    public String textDescription() {
        return (String)storedValueForKey("textDescription");
    }
    public void setTextDescription(String aValue) {
        takeStoredValueForKey(aValue, "textDescription");
    }

    public NSTimestamp dateCreated() {
        return (NSTimestamp)storedValueForKey("dateCreated");
    }
    public void setDateCreated(NSTimestamp aValue) {
        takeStoredValueForKey(aValue, "dateCreated");
    }

    public String comments() {
        return (String)storedValueForKey("comments");
    }
    public void setComments(String aValue) {
        takeStoredValueForKey(aValue, "comments");
    }

    public String title() {
        return (String)storedValueForKey("title");
    }
    public void setTitle(String aValue) {
        takeStoredValueForKey(aValue, "title");
    }

    public Number id() {
        return (Number)storedValueForKey("id");
    }
    public void setId(Number aValue) {
        takeStoredValueForKey(aValue, "id");
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


    public er.bugtracker.TestItemState state() {
        return (er.bugtracker.TestItemState)storedValueForKey("state");
    }

    public void setState(er.bugtracker.TestItemState aValue) {
        takeStoredValueForKey(aValue, "state");
    }
    public void addToBothSidesOfState(er.bugtracker.TestItemState object) {
        addObjectToBothSidesOfRelationshipWithKey(object, "state");
    }
    public void removeFromBothSidesOfState(er.bugtracker.TestItemState object) {
        removeObjectFromBothSidesOfRelationshipWithKey(object, "state");
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


    public NSArray requirements() {
        return (NSArray)storedValueForKey("requirements");
    }
    public void setRequirements(NSMutableArray aValue) {
        takeStoredValueForKey(aValue, "requirements");
    }
    public void addToRequirements(er.bugtracker.Requirement object) {
        NSMutableArray array = (NSMutableArray)requirements();

        willChange();
        array.addObject(object);
    }
    public void removeFromRequirements(er.bugtracker.Requirement object) {
        NSMutableArray array = (NSMutableArray)requirements();

        willChange();
        array.removeObject(object);
    }
    public void addToBothSidesOfRequirements(er.bugtracker.Requirement object) {
        addObjectToBothSidesOfRelationshipWithKey(object, "requirements");
    }
    public void removeFromBothSidesOfRequirements(er.bugtracker.Requirement object) {
        removeObjectFromBothSidesOfRelationshipWithKey(object, "requirements");
    }


    public NSArray bugs() {
        return (NSArray)storedValueForKey("bugs");
    }
    public void setBugs(NSMutableArray aValue) {
        takeStoredValueForKey(aValue, "bugs");
    }
    public void addToBugs(er.bugtracker.Bug object) {
        NSMutableArray array = (NSMutableArray)bugs();

        willChange();
        array.addObject(object);
    }
    public void removeFromBugs(er.bugtracker.Bug object) {
        NSMutableArray array = (NSMutableArray)bugs();

        willChange();
        array.removeObject(object);
    }
    public void addToBothSidesOfBugs(er.bugtracker.Bug object) {
        addObjectToBothSidesOfRelationshipWithKey(object, "bugs");
    }
    public void removeFromBothSidesOfBugs(er.bugtracker.Bug object) {
        removeObjectFromBothSidesOfRelationshipWithKey(object, "bugs");
    }

}
