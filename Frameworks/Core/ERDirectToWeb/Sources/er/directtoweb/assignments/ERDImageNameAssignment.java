/*
 * Copyright (C) NetStruxr, Inc. All rights reserved.
 *
 * This software is published under the terms of the NetStruxr
 * Public Software License version 0.5, a copy of which has been
 * included with this distribution in the LICENSE.NPL file.  */
package er.directtoweb.assignments;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.webobjects.directtoweb.D2WContext;
import com.webobjects.eocontrol.EOKeyValueUnarchiver;
import com.webobjects.foundation.NSArray;

/**
 * Default way of generating image references for tabs and sections.
 */

public class ERDImageNameAssignment extends ERDAssignment implements ERDLocalizableAssignmentInterface {
	/**
	 * Do I need to update serialVersionUID?
	 * See section 5.6 <cite>Type Changes Affecting Serialization</cite> on page 51 of the 
	 * <a href="http://java.sun.com/j2se/1.4/pdf/serial-spec.pdf">Java Object Serialization Spec</a>
	 */
	private static final long serialVersionUID = 1L;

    /** logging support */
    public static final Logger log = LoggerFactory.getLogger("er.directtoweb.rules.ERDImageNameAssignment");

    /** holds the array of keys this assignment depends upon */
    public static final NSArray _DEPENDENT_KEYS=new NSArray(new String[] { "baseImageDirectory", "sectionKey", "tabKey"});

    /**
     * Static constructor required by the EOKeyValueUnarchiver
     * interface. If this isn't implemented then the default
     * behavior is to construct the first super class that does
     * implement this method. Very lame.
     * @param eokeyvalueunarchiver to be unarchived
     * @return decoded assignment of this class
     */
    public static Object decodeWithKeyValueUnarchiver(EOKeyValueUnarchiver eokeyvalueunarchiver)  {
        return new ERDImageNameAssignment(eokeyvalueunarchiver);
    }
        
    /** 
     * Public constructor
     * @param u key-value unarchiver used when unarchiving
     *		from rule files. 
     */
    public ERDImageNameAssignment (EOKeyValueUnarchiver u) { super(u); }
    
    /** 
     * Public constructor
     * @param key context key
     * @param value of the assignment
     */
    public ERDImageNameAssignment (String key, Object value) { super(key,value); }

    /**
     * Implementation of the {@link er.directtoweb.assignments.ERDComputingAssignmentInterface}. This
     * assignment depends upon the context keys: "baseImageDirectory", "sectionKey",
     * and "tabKey". This array of keys is used when constructing the 
     * significant keys for the passed in keyPath.
     * @param keyPath to compute significant keys for. 
     * @return array of context keys this assignment depends upon.
     */
    public NSArray dependentKeys(String keyPath) { return _DEPENDENT_KEYS; }

    public String imageNameForKey(String key, String baseImageDirectory, String prefix) {
        String imageName = null;
        if (key != null && baseImageDirectory != null) {
            String name =(NSArray.componentsSeparatedByString(key," ")).componentsJoinedByString("");
            imageName = baseImageDirectory + prefix + name + ".gif";
        }
        return imageName;
    }
    
    public Object sectionImageName(D2WContext c) {
        String sectionKey = (String)(c.valueForKey("sectionKey"));
        String baseImageDirectory = (String)(c.valueForKey("baseImageDirectory"));
        if (sectionKey == null)
            log.warn("SectionKey is null for pageConfiguration: " + c.valueForKey("pageConfiguration"));
        if (baseImageDirectory == null)
            log.warn("BaseImageDirectory is null for sectionKey: " + sectionKey + " and pageConfiguration: " +
                     c.valueForKey("pageConfiguration"));        
        return imageNameForKey(sectionKey, baseImageDirectory, "section");
    }

    public Object tabImageName(D2WContext c) {
        String tabKey = (String)(c.valueForKey("tabKey"));
        String baseImageDirectory = (String)(c.valueForKey("baseImageDirectory"));
        if (tabKey == null)
            log.warn("TabKey is null for pageConfiguration: " + c.valueForKey("pageConfiguration"));
        if (baseImageDirectory == null)
            log.warn("BaseImageDirectory is null for tabKey: " + tabKey + " and pageConfiguration: " +
                     c.valueForKey("pageConfiguration"));
        return imageNameForKey(tabKey, baseImageDirectory, "tab");
    }
}
