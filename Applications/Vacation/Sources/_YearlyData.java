// YearlyData.java
// Created on Mon Nov 04 14:52:02 US/Pacific 2002 by Apple EOModeler Version 5.0

import com.webobjects.foundation.*;
import com.webobjects.eocontrol.*;
import java.math.BigDecimal;
import java.util.*;

public class _YearlyData extends EOGenericRecord {

    public _YearlyData() {
        super();
    }

/*
    // If you implement the following constructor EOF will use it to
    // create your objects, otherwise it will use the default
    // constructor. For maximum performance, you should only
    // implement this constructor if you depend on the arguments.
    public YearlyData(EOEditingContext context, EOClassDescription classDesc, EOGlobalID gid) {
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

    public Number vacationCarryOver() {
        return (Number)storedValueForKey("vacationCarryOver");
    }

    public void setVacationCarryOver(Number value) {
        takeStoredValueForKey(value, "vacationCarryOver");
    }

    public Number vacationEntitled() {
        return (Number)storedValueForKey("vacationEntitled");
    }

    public void setVacationEntitled(Number value) {
        takeStoredValueForKey(value, "vacationEntitled");
    }

    public Number year() {
        return (Number)storedValueForKey("year");
    }

    public void setYear(Number value) {
        takeStoredValueForKey(value, "year");
    }

    public Person person() {
        return (Person)storedValueForKey("person");
    }

    public void setPerson(Person value) {
        takeStoredValueForKey(value, "person");
    }
}
