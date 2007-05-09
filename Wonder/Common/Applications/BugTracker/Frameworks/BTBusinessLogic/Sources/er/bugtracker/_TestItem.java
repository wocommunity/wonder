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


    public TestItemState state() {
        return (TestItemState)storedValueForKey("state");
    }

    public void setState(TestItemState aValue) {
        takeStoredValueForKey(aValue, "state");
    }
    public void addToBothSidesOfState(TestItemState object) {
        addObjectToBothSidesOfRelationshipWithKey(object, "state");
    }
    public void removeFromBothSidesOfState(TestItemState object) {
        removeObjectFromBothSidesOfRelationshipWithKey(object, "state");
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


    public NSArray requirements() {
        return (NSArray)storedValueForKey("requirements");
    }
    public void setRequirements(NSMutableArray aValue) {
        takeStoredValueForKey(aValue, "requirements");
    }
    public void addToRequirements(Requirement object) {
        NSMutableArray array = (NSMutableArray)requirements();

        willChange();
        array.addObject(object);
    }
    public void removeFromRequirements(Requirement object) {
        NSMutableArray array = (NSMutableArray)requirements();

        willChange();
        array.removeObject(object);
    }
    public void addToBothSidesOfRequirements(Requirement object) {
        addObjectToBothSidesOfRelationshipWithKey(object, "requirements");
    }
    public void removeFromBothSidesOfRequirements(Requirement object) {
        removeObjectFromBothSidesOfRelationshipWithKey(object, "requirements");
    }


    public NSArray bugs() {
        return (NSArray)storedValueForKey("bugs");
    }
    public void setBugs(NSMutableArray aValue) {
        takeStoredValueForKey(aValue, "bugs");
    }
    public void addToBugs(Bug object) {
        NSMutableArray array = (NSMutableArray)bugs();

        willChange();
        array.addObject(object);
    }
    public void removeFromBugs(Bug object) {
        NSMutableArray array = (NSMutableArray)bugs();

        willChange();
        array.removeObject(object);
    }
    public void addToBothSidesOfBugs(Bug object) {
        addObjectToBothSidesOfRelationshipWithKey(object, "bugs");
    }
    public void removeFromBothSidesOfBugs(Bug object) {
        removeObjectFromBothSidesOfRelationshipWithKey(object, "bugs");
    }

}
