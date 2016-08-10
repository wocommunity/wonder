/*
 * Copyright (C) NetStruxr, Inc. All rights reserved.
 *
 * This software is published under the terms of the NetStruxr
 * Public Software License version 0.5, a copy of which has been
 * included with this distribution in the LICENSE.NPL file.  */
package er.directtoweb;

import java.io.Serializable;

import com.webobjects.foundation.NSArray;
import com.webobjects.foundation.NSMutableArray;

/**
 * Used to hold pieces of a tabsSectionsContents, holds either sections or tabs.
 */

public class ERD2WContainer implements Serializable {
	/**
	 * Do I need to update serialVersionUID?
	 * See section 5.6 <cite>Type Changes Affecting Serialization</cite> on page 51 of the 
	 * <a href="http://java.sun.com/j2se/1.4/pdf/serial-spec.pdf">Java Object Serialization Spec</a>
	 */
	private static final long serialVersionUID = 1L;

    public String name;
    public String displayName;
    public NSMutableArray keys;
    
    public ERD2WContainer() {}
    
    public ERD2WContainer(String newName) {
        name = newName;
        keys = new NSMutableArray();
    }
    
    public ERD2WContainer(String newName, NSArray newKeys) {
        name = newName;
        keys = new NSMutableArray(newKeys);
    }

    @Override
    public String toString() {
        StringBuilder sb = new StringBuilder();
        if (displayName != null && displayName.trim().length() > 0) {
            sb.append(displayName).append(": ");
        } else if (name != null && name.trim().length() > 0) {
            sb.append(name).append(": ");
        }
        sb.append(keys);
        return sb.toString();
    }
    
    public boolean equals(Object something) {
        boolean equals = true;
        if (something == null) {
            equals = false;
        }
        if (equals && !getClass().equals(something.getClass())) {
            equals = false;
        }
        if (equals) {
            ERD2WContainer other = (ERD2WContainer) something;
            // verify name equality
            if (name == null && other.name != null) {
                equals = false;
            }
            if (equals && name != null && !name.equals(other.name)) {
                equals = false;
            }
            // we don't verify display name and keys equality
        }
        return equals;
    }
    
}
