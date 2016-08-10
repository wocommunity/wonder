package er.validexample;
// User.java
// Created on Sun Jun 03 17:00:06 US/Central 2001 by Apple EOModeler Version 5.0

import com.gammastream.validity.GSVGenericRecord;
public class User extends GSVGenericRecord {

    public User() {
        super();
    }

/*
    // If you implement the following constructor EOF will use it to
    // create your objects, otherwise it will use the default
    // constructor. For maximum performance, you should only
    // implement this constructor if you depend on the arguments.
    public User(EOEditingContext context, EOClassDescription classDesc, EOGlobalID gid) {
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

    public String username() {
        return (String)storedValueForKey("username");
    }

    public void setUsername(String value) {
        takeStoredValueForKey(value, "username");
    }

    public String password() {
        return (String)storedValueForKey("password");
    }

    public void setPassword(String value) {
        takeStoredValueForKey(value, "password");
    }

    public String firstName() {
        return (String)storedValueForKey("firstName");
    }

    public void setFirstName(String value) {
        takeStoredValueForKey(value, "firstName");
    }

    public String lastName() {
        return (String)storedValueForKey("lastName");
    }

    public void setLastName(String value) {
        takeStoredValueForKey(value, "lastName");
    }

    public Number number() {
        return (Number)storedValueForKey("number");
    }

    public void setNumber(Number value) {
        takeStoredValueForKey(value, "number");
    }
}
