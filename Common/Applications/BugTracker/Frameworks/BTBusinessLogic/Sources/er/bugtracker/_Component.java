// _Component.java
// 
// Created by eogenerator
// DO NOT EDIT.  Make changes to Component.java instead.
package er.bugtracker;

import com.webobjects.foundation.NSArray;
import com.webobjects.foundation.NSMutableArray;
import er.extensions.eof.ERXGenericRecord;

public abstract class _Component extends ERXGenericRecord {

    public _Component() {
        super();
    }

    public static abstract class _ComponentClazz extends ERXGenericRecord.ERXGenericRecordClazz {

    }


    public String textDescription() {
        return (String)storedValueForKey("textDescription");
    }
    public void setTextDescription(String aValue) {
        takeStoredValueForKey(aValue, "textDescription");
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


    public Component parent() {
        return (Component)storedValueForKey("parent");
    }

    public void setParent(Component aValue) {
        takeStoredValueForKey(aValue, "parent");
    }
    public void addToBothSidesOfParent(Component object) {
        addObjectToBothSidesOfRelationshipWithKey(object, "parent");
    }
    public void removeFromBothSidesOfParent(Component object) {
        removeObjectFromBothSidesOfRelationshipWithKey(object, "parent");
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


    public NSArray children() {
        return (NSArray)storedValueForKey("children");
    }
    public void setChildren(NSMutableArray aValue) {
        takeStoredValueForKey(aValue, "children");
    }
    public void addToChildren(Component object) {
        NSMutableArray array = (NSMutableArray)children();

        willChange();
        array.addObject(object);
    }
    public void removeFromChildren(Component object) {
        NSMutableArray array = (NSMutableArray)children();

        willChange();
        array.removeObject(object);
    }
    public void addToBothSidesOfChildren(Component object) {
        addObjectToBothSidesOfRelationshipWithKey(object, "children");
    }
    public void removeFromBothSidesOfChildren(Component object) {
        removeObjectFromBothSidesOfRelationshipWithKey(object, "children");
    }

}
