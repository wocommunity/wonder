// Setting.java
// Created on Mon Nov 04 14:51:45 US/Pacific 2002 by Apple EOModeler Version 5.0

import com.webobjects.foundation.*;
import com.webobjects.eocontrol.*;
import java.math.BigDecimal;
import java.util.*;

public class _Setting extends EOGenericRecord {

    public _Setting() {
        super();
    }

/*
    // If you implement the following constructor EOF will use it to
    // create your objects, otherwise it will use the default
    // constructor. For maximum performance, you should only
    // implement this constructor if you depend on the arguments.
    public Setting(EOEditingContext context, EOClassDescription classDesc, EOGlobalID gid) {
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

    public Number uniqueID() {
        return (Number)storedValueForKey("uniqueID");
    }

    public void setUniqueID(Number value) {
        takeStoredValueForKey(value, "uniqueID");
    }

    public NSTimestamp archiveDate() {
        return (NSTimestamp)storedValueForKey("archiveDate");
    }

    public void setArchiveDate(NSTimestamp value) {
        takeStoredValueForKey(value, "archiveDate");
    }
}
