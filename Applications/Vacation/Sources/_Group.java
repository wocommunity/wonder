// Group.java
// Created on Wed Jun 04 14:01:58 Europe/London 2003 by Apple EOModeler Version 5.0

import com.webobjects.foundation.*;
import com.webobjects.eocontrol.*;
import java.math.BigDecimal;
import java.util.*;

public class _Group extends EOGenericRecord {

    public _Group() {
        super();
    }

/*
    // If you implement the following constructor EOF will use it to
    // create your objects, otherwise it will use the default
    // constructor. For maximum performance, you should only
    // implement this constructor if you depend on the arguments.
    public Group(EOEditingContext context, EOClassDescription classDesc, EOGlobalID gid) {
        super(context, classDesc, gid);
    }

    // If you add instance variables to store property values you
    // should add empty implementions of the Serialization methods
    // to avoid unnecessary overhead (the properties will be
    // serialized for you in the superclass).
    private void writeObject(java.io.ObjectOutputStream out) throws java.io.IOException {
    }

    private void readObject(java.io.ObjectInputStream in) throws java.io.IOException, java.lang.ClassNotFoundException {
    }
*/

    public String name() {
        return (String)storedValueForKey("name");
    }

    public void setName(String value) {
        takeStoredValueForKey(value, "name");
    }

    public Group parentGroup() {
        return (Group)storedValueForKey("parentGroup");
    }

    public void setParentGroup(Group value) {
        takeStoredValueForKey(value, "parentGroup");
    }

    public NSArray persons() {
        return (NSArray)storedValueForKey("persons");
    }

    public void setPersons(NSMutableArray value) {
        takeStoredValueForKey(value, "persons");
    }

    public void addToPersons(Person object) {
        NSMutableArray array = (NSMutableArray)persons();

        willChange();
        array.addObject(object);
    }

    public void removeFromPersons(Person object) {
        NSMutableArray array = (NSMutableArray)persons();

        willChange();
        array.removeObject(object);
    }

    public NSArray subGroups() {
        return (NSArray)storedValueForKey("subGroups");
    }

    public void setSubGroups(NSMutableArray value) {
        takeStoredValueForKey(value, "subGroups");
    }

    public void addToSubGroups(Group object) {
        NSMutableArray array = (NSMutableArray)subGroups();

        willChange();
        array.addObject(object);
    }

    public void removeFromSubGroups(Group object) {
        NSMutableArray array = (NSMutableArray)subGroups();

        willChange();
        array.removeObject(object);
    }
}
