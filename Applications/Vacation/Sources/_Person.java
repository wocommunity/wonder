// Person.java
// Created on Wed Feb 26 02:53:55 US/Pacific 2003 by Apple EOModeler Version 5.0

import com.webobjects.foundation.*;
import com.webobjects.eocontrol.*;
import java.math.BigDecimal;
import java.util.*;

public class _Person extends EOGenericRecord {

    public _Person() {
        super();
    }

/*
    // If you implement the following constructor EOF will use it to
    // create your objects, otherwise it will use the default
    // constructor. For maximum performance, you should only
    // implement this constructor if you depend on the arguments.
    public Person(EOEditingContext context, EOClassDescription classDesc, EOGlobalID gid) {
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

    public String password() {
        return (String)storedValueForKey("password");
    }

    public void setPassword(String value) {
        takeStoredValueForKey(value, "password");
    }

    public String type() {
        return (String)storedValueForKey("type");
    }

    public void setType(String value) {
        takeStoredValueForKey(value, "type");
    }

    public String email() {
        return (String)storedValueForKey("email");
    }

    public void setEmail(String value) {
        takeStoredValueForKey(value, "email");
    }

    public String userID() {
        return (String)storedValueForKey("userID");
    }

    public void setUserID(String value) {
        takeStoredValueForKey(value, "userID");
    }

    public String notes() {
        return (String)storedValueForKey("notes");
    }

    public void setNotes(String value) {
        takeStoredValueForKey(value, "notes");
    }

    public Group group() {
        return (Group)storedValueForKey("group");
    }

    public void setGroup(Group value) {
        takeStoredValueForKey(value, "group");
    }

    public NSArray dates() {
        return (NSArray)storedValueForKey("dates");
    }

    public void setDates(NSMutableArray value) {
        takeStoredValueForKey(value, "dates");
    }

    public void addToDates(VacationEvent object) {
        NSMutableArray array = (NSMutableArray)dates();

        willChange();
        array.addObject(object);
    }

    public void removeFromDates(VacationEvent object) {
        NSMutableArray array = (NSMutableArray)dates();

        willChange();
        array.removeObject(object);
    }

    public NSArray yearlyData() {
        return (NSArray)storedValueForKey("yearlyData");
    }

    public void setYearlyData(NSMutableArray value) {
        takeStoredValueForKey(value, "yearlyData");
    }

    public void addToYearlyData(YearlyData object) {
        NSMutableArray array = (NSMutableArray)yearlyData();

        willChange();
        array.addObject(object);
    }

    public void removeFromYearlyData(YearlyData object) {
        NSMutableArray array = (NSMutableArray)yearlyData();

        willChange();
        array.removeObject(object);
    }
}
