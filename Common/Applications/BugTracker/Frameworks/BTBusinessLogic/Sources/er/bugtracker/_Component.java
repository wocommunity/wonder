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

    public _Component() {
        super();
    }

    public static abstract class _ComponentClazz extends er.extensions.ERXGenericRecord.ERXGenericRecordClazz {

    }


    public String textDescription() {
        return (String)storedValueForKey("textDescription");
    }
    public void setTextDescription(String aValue) {
        takeStoredValueForKey(aValue, "textDescription");
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


    public er.bugtracker.Component parent() {
        return (er.bugtracker.Component)storedValueForKey("parent");
    }

    public void setParent(er.bugtracker.Component aValue) {
        takeStoredValueForKey(aValue, "parent");
    }
    public void addToBothSidesOfParent(er.bugtracker.Component object) {
        addObjectToBothSidesOfRelationshipWithKey(object, "parent");
    }
    public void removeFromBothSidesOfParent(er.bugtracker.Component object) {
        removeObjectFromBothSidesOfRelationshipWithKey(object, "parent");
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


    public NSArray children() {
        return (NSArray)storedValueForKey("children");
    }
    public void setChildren(NSMutableArray aValue) {
        takeStoredValueForKey(aValue, "children");
    }
    public void addToChildren(er.bugtracker.Component object) {
        NSMutableArray array = (NSMutableArray)children();

        willChange();
        array.addObject(object);
    }
    public void removeFromChildren(er.bugtracker.Component object) {
        NSMutableArray array = (NSMutableArray)children();

        willChange();
        array.removeObject(object);
    }
    public void addToBothSidesOfChildren(er.bugtracker.Component object) {
        addObjectToBothSidesOfRelationshipWithKey(object, "children");
    }
    public void removeFromBothSidesOfChildren(er.bugtracker.Component object) {
        removeObjectFromBothSidesOfRelationshipWithKey(object, "children");
    }

}
