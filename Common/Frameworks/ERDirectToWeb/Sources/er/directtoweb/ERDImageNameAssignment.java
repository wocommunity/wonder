/*
 * Copyright (C) NetStruxr, Inc. All rights reserved.
 *
 * This software is published under the terms of the NetStruxr
 * Public Software License version 0.5, a copy of which has been
 * included with this distribution in the LICENSE.NPL file.  */

/* ERImageNameAssignment.java created by max on Fri 15-Dec-2000 */
package er.directtoweb;

import com.webobjects.directtoweb.*;
import com.webobjects.foundation.*;
import com.webobjects.eocontrol.*;
import com.webobjects.eoaccess.*;
import com.webobjects.appserver.*;
import org.apache.log4j.*;

public class ERDImageNameAssignment extends ERDAssignment {

    public static Object decodeWithKeyValueUnarchiver(EOKeyValueUnarchiver eokeyvalueunarchiver)  {
        return new ERDImageNameAssignment(eokeyvalueunarchiver);
    }
    
    ///////////////////////////  log4j category  ///////////////////////////
    Category cat = Category.getInstance("er.directtoweb.rules.ERDImageNameAssignment");
    ////////////////////////////////////////////////////////////////////////
    
    public ERDImageNameAssignment (EOKeyValueUnarchiver u) { super(u); }
    public ERDImageNameAssignment (String key, Object value) { super(key,value); }

    public static final NSArray _DEPENDENT_KEYS=new NSArray(new String[] { "baseImageDirectory", "sectionKey", "tabKey"});
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
            cat.warn("SectionKey is null for pageConfiguration: " + c.valueForKey("pageConfiguration"));
        if (baseImageDirectory == null)
            cat.warn("BaseImageDirectory is null for sectionKey: " + sectionKey + " and pageConfiguration: " +
                     c.valueForKey("pageConfiguration"));        
        return imageNameForKey(sectionKey, baseImageDirectory, "section");
    }

}
