// Account.java
// Created on Thu Jun 21 14:07:14 Europe/Amsterdam 2001 by Apple EOModeler Version 5.0
package org.svgobjects.examples.ezforms;

import com.webobjects.foundation.*;
import com.webobjects.eocontrol.*;

public class Account extends EOGenericRecord {
    public static NSArray Types = new NSArray(new String[]{"Checking", "Savings"});

    /*
    * custom accessors
    */
    public boolean isCheckingAccount() {
	String type = (String) valueForKey("type");
        return (type != null) ? type.equals("Checking"): false;
    }
}
