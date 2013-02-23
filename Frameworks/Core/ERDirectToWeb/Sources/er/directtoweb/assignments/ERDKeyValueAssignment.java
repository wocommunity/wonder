/*
 * Copyright (C) NetStruxr, Inc. All rights reserved.
 *
 * This software is published under the terms of the NetStruxr
 * Public Software License version 0.5, a copy of which has been
 * included with this distribution in the LICENSE.NPL file.  */
package er.directtoweb.assignments;

import com.webobjects.directtoweb.Assignment;
import com.webobjects.directtoweb.D2WContext;
import com.webobjects.eocontrol.EOKeyValueUnarchiver;

/**
 * Piece of crap. This assignment works around the 
 * fact that KeyValueAssignment isn't public (and should be).
 * Use this assignment as a drop in replacement for 
 * KeyValueAssignment. Note that this assignment is not a
 * delayed assignment and as such the value returned by this
 * assignment will be cached the first time this assignment is
 * fired. To have a key value assignment that does not cache 
 * the value returned the first time have a look at
 * {@link er.directtoweb.assignments.delayed.ERDDelayedKeyValueAssignment}.
 */
public class ERDKeyValueAssignment extends Assignment {
	/**
	 * Do I need to update serialVersionUID?
	 * See section 5.6 <cite>Type Changes Affecting Serialization</cite> on page 51 of the 
	 * <a href="http://java.sun.com/j2se/1.4/pdf/serial-spec.pdf">Java Object Serialization Spec</a>
	 */
	private static final long serialVersionUID = 1L;

    /**
     * Static constructor required by the EOKeyValueUnarchiver
     * interface. If this isn't implemented then the default
     * behavior is to construct the first super class that does
     * implement this method. Very lame.
     * @param eokeyvalueunarchiver to be unarchived
     * @return decoded assignment of this class
     */
    // ENHANCEME: Could maintain a weak hash map cache based on the key and 
    //		  value() of the assignment.
    public static Object decodeWithKeyValueUnarchiver(EOKeyValueUnarchiver eokeyvalueunarchiver) {
        return new ERDKeyValueAssignment(eokeyvalueunarchiver);
    }    
    
    /**
     * Public constructor
     * @param s context key
     * @param s1 value to be invoke on the context when firing.
     */
    public ERDKeyValueAssignment(String s, String s1) {
        super(s, s1);
    }

    /** 
     * Public constructor
     * @param eokeyvalueunarchiver key-value unarchiver used when unarchiving
     *		from rule files. 
     */
    public ERDKeyValueAssignment(EOKeyValueUnarchiver eokeyvalueunarchiver) {
        super(eokeyvalueunarchiver);
    }

    /**
     * Fires the assignment. In this case this method calls
     * <code>valueForKeyPath</code> on the passed in context
     * using the value() as the key.
     * @param d2wcontext current context
     * @return result of resolving the key path off of the 
     * 		context. 
     */
    @Override
    public Object fire(D2WContext d2wcontext) {
        return d2wcontext.valueForKeyPath((String)value());
    }
}
