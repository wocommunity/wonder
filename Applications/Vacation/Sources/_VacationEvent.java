// VacationEvent.java
// Created on Wed Jan 22 09:44:27 US/Pacific 2003 by Apple EOModeler Version 5.0

import com.webobjects.foundation.*;
import com.webobjects.eocontrol.*;
import java.math.BigDecimal;
import java.util.*;

public class _VacationEvent extends EOGenericRecord {

    public _VacationEvent() {
        super();
    }

/*
    // If you implement the following constructor EOF will use it to
    // create your objects, otherwise it will use the default
    // constructor. For maximum performance, you should only
    // implement this constructor if you depend on the arguments.
    public VacationEvent(EOEditingContext context, EOClassDescription classDesc, EOGlobalID gid) {
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

    public String comment() {
        return (String)storedValueForKey("comment");
    }

    public void setComment(String value) {
        takeStoredValueForKey(value, "comment");
    }

    public NSTimestamp fromDate() {
        return (NSTimestamp)storedValueForKey("fromDate");
    }

    public void setFromDate(NSTimestamp value) {
        takeStoredValueForKey(value, "fromDate");
    }

    public NSTimestamp toDate() {
        return (NSTimestamp)storedValueForKey("toDate");
    }

    public void setToDate(NSTimestamp value) {
        takeStoredValueForKey(value, "toDate");
    }

    public Number totalTime() {
        return (Number)storedValueForKey("totalTime");
    }

    public void setTotalTime(Number value) {
        takeStoredValueForKey(value, "totalTime");
    }

    public String type() {
        return (String)storedValueForKey("type");
    }

    public void setType(String value) {
        takeStoredValueForKey(value, "type");
    }

    public Number archived() {
        return (Number)storedValueForKey("archived");
    }

    public void setArchived(Number value) {
        takeStoredValueForKey(value, "archived");
    }

    public String fromPeriod() {
        return (String)storedValueForKey("fromPeriod");
    }

    public void setFromPeriod(String value) {
        takeStoredValueForKey(value, "fromPeriod");
    }

    public String toPeriod() {
        return (String)storedValueForKey("toPeriod");
    }

    public void setToPeriod(String value) {
        takeStoredValueForKey(value, "toPeriod");
    }

    public Person person() {
        return (Person)storedValueForKey("person");
    }

    public void setPerson(Person value) {
        takeStoredValueForKey(value, "person");
    }
}
