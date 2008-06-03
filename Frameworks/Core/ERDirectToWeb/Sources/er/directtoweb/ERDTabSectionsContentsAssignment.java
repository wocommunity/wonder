/*
 * Copyright (C) NetStruxr, Inc. All rights reserved.
 *
 * This software is published under the terms of the NetStruxr
 * Public Software License version 0.5, a copy of which has been
 * included with this distribution in the LICENSE.NPL file.  */
package er.directtoweb;

import java.util.Enumeration;

import com.webobjects.directtoweb.D2WContext;
import com.webobjects.eocontrol.EOKeyValueUnarchiver;
import com.webobjects.foundation.NSArray;
import com.webobjects.foundation.NSMutableArray;

/**
 * Assignment used to construct and cache the 
 * tab sections containers used with tab insepct
 * pages. See {@link ERD2WTabInspectPage} for 
 * information on the formats of a tab section. 
 */
// FIXME: Should subclass Assignment instead
public class ERDTabSectionsContentsAssignment extends ERDAssignment {

    /**
     * Static constructor required by the EOKeyValueUnarchiver
     * interface. If this isn't implemented then the default
     * behavior is to construct the first super class that does
     * implement this method. Very lame.
     * @param eokeyvalueunarchiver to be unarchived
     * @return decoded assignment of this class
     */
    public static Object decodeWithKeyValueUnarchiver(EOKeyValueUnarchiver eokeyvalueunarchiver)  {
        return new ERDTabSectionsContentsAssignment(eokeyvalueunarchiver);
    }
    
    /** 
     * Public constructor
     * @param u key-value unarchiver used when unarchiving
     *		from rule files. 
     */
    public ERDTabSectionsContentsAssignment (EOKeyValueUnarchiver u) { super(u); }
    
    /** 
     * Public constructor
     * @param key context key
     * @param value of the assignment
     */
    public ERDTabSectionsContentsAssignment (String key, Object value) { super(key,value); }

    /**
     * Implementation of the {@link ERDComputingAssignmentInterface}. This
     * assignment is not dependent on any context keys.
     * @return empty array.
     */
    public NSArray dependentKeys(String keyPath) { return NSArray.EmptyArray; }

    /**
     * Called when firing this assignment with the key-path:
     * <b>tabSectionsContents</b>. Constructs an array of 
     * {@link ERD2WContainer}s representing each tab and
     * optionally another array of containers representing 
     * each section. See {@link ERD2WTabInspectPage} for the
     * exact format of the tabs and sections.
     * @return array of containers for each tab
     */
    public Object tabSectionsContents(D2WContext context) {
        NSMutableArray tabSectionsContents=new NSMutableArray();
        for (Enumeration e= ((NSArray)value()).objectEnumerator(); e.hasMoreElements();) {
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
