/*
 * Copyright (C) NetStruxr, Inc. All rights reserved.
 *
 * This software is published under the terms of the NetStruxr 
 * Public Software License version 0.5, a copy of which has been
 * included with this distribution in the LICENSE.NPL file.  */
package er.directtoweb;

import com.webobjects.foundation.*;
import com.webobjects.eocontrol.*;
import com.webobjects.eoaccess.*;
import com.webobjects.appserver.*;
import com.webobjects.directtoweb.*;
import org.apache.log4j.Category;

// now that we use reflection to fire methods, there's no reason to have a lot a assignment classes
public class ERDDefaultsAssignment extends ERDAssignment {

    public static Object decodeWithKeyValueUnarchiver(EOKeyValueUnarchiver eokeyvalueunarchiver)  {
        return new ERDDefaultsAssignment(eokeyvalueunarchiver);
    }
    
    //////////////////////////////////////////////  log4j category  //////////////////////////////////////////
    public final static Category cat = Category.getInstance("er.directtoweb.ERDefaultsAssignment");

    public ERDDefaultsAssignment (EOKeyValueUnarchiver u) { super(u); }
    public ERDDefaultsAssignment (String key, Object value) { super(key,value); }

    public static final NSArray _DEPENDENT_KEYS=new NSArray(new String[] { "smartAttribute"});
    public NSArray dependentKeys(String keyPath) { return _DEPENDENT_KEYS; }
    
    public String keyForMethodLookup(D2WContext c) { return (String)value(); }

    public int attributeWidthAsInt(D2WContext c) {
        EOAttribute a = (EOAttribute)c.valueForKey("smartAttribute");
        return a!=null ? a.width() : 0;
    }

    public int smartDefaultAttributeWidthAsInt(D2WContext c) {
        int i=attributeWidthAsInt(c);
        return i<50 ? ( i==0 ? 20 : i ) : 50;        
    }

    public Object smartDefaultAttributeWidth(D2WContext c) {
        return String.valueOf(smartDefaultAttributeWidthAsInt(c));
    }

    public Object smartDefaultRows(D2WContext c) {
        int i = attributeWidthAsInt(c);
        int j = smartDefaultAttributeWidthAsInt(c);
        int k = j == 0 ? i : (int)((double)(i / j) + 0.5D);
        if(k > 8) k = 8;
        return String.valueOf(k);
    }
}
