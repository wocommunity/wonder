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
import com.webobjects.directtoweb.*;
import java.util.*;

public class ERDTabSectionsContentsAssignment extends ERDAssignment {

    public static Object decodeWithKeyValueUnarchiver(EOKeyValueUnarchiver eokeyvalueunarchiver)  {
        return new ERDTabSectionsContentsAssignment(eokeyvalueunarchiver);
    }
    
    public ERDTabSectionsContentsAssignment (EOKeyValueUnarchiver u) { super(u); }
    public ERDTabSectionsContentsAssignment (String key, Object value) { super(key,value); }

    public static final NSArray _DEPENDENT_KEYS=new NSArray(new String[] {});
    public NSArray dependentKeys(String keyPath) { return _DEPENDENT_KEYS; }

    public Object tabSectionsContents(D2WContext context) {
        NSMutableArray tabSectionsContents=new NSMutableArray();
        for (Enumeration e= ((NSArray)value(context)).objectEnumerator(); e.hasMoreElements();) {
            NSArray tab=(NSArray)e.nextElement();
            ERD2WContainer c=new ERD2WContainer();
            c.name=(String)tab.objectAtIndex(0);
            c.keys=new NSMutableArray();
            Object testObject=tab.objectAtIndex(1);
            if (testObject instanceof NSArray) { // format #2
                for (int i=1; i<tab.count(); i++) {
                    NSArray sectionArray=(NSArray)tab.objectAtIndex(i);
                    ERD2WContainer section=new ERD2WContainer();
                    section.name=(String)sectionArray.objectAtIndex(0);
                    section.keys=new NSMutableArray(sectionArray);
                    section.keys.removeObjectAtIndex(0);
                    c.keys.addObject(section);
                }
            } else { // format #1
                ERD2WContainer fakeTab=new ERD2WContainer();
                fakeTab.name="";
                fakeTab.keys=new NSMutableArray(tab);
                fakeTab.keys.removeObjectAtIndex(0);
                c.keys.addObject(fakeTab);
            }
            tabSectionsContents.addObject(c);
        }
        return tabSectionsContents;
    }    
}
