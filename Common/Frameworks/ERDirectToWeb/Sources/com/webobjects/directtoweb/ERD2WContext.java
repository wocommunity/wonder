/*
 * Created on 26.08.2004
 *
 * TODO To change the template for this generated file go to
 * Window - Preferences - Java - Code Style - Code Templates
 */
package com.webobjects.directtoweb;

import sun.misc.*;

import com.webobjects.appserver.*;
import com.webobjects.eoaccess.*;
import com.webobjects.foundation.*;


/**
 * @author david
 *
 * TODO To change the template for this generated type comment go to
 * Window - Preferences - Java - Code Style - Code Templates
 */
public class ERD2WContext extends D2WContext {

    public static NSMutableDictionary customAttributes = new NSMutableDictionary();
    public static NSMutableDictionary customNullAttributes = new NSMutableDictionary();
    
    /**
     * 
     */
    public ERD2WContext() {
        super();
    }

    /**
     * @param arg0
     */
    public ERD2WContext(WOSession arg0) {
        super(arg0);
    }

    /**
     * @param arg0
     */
    public ERD2WContext(D2WContext arg0) {
        super(arg0);
    }

    EOAttribute customAttribute(String s, EOEntity eoentity) {
        Perf p = Perf.getPerf();
        double freq = p.highResFrequency();
        double s2 = p.highResCounter();
        String s1 = eoentity.name() + "." + s;
        EOAttribute eoattribute = (EOAttribute) customAttributes.objectForKey(s1);
        if (eoattribute == null && customNullAttributes.objectForKey(s1) == null) {
            Class class1 = D2WUtils.dataTypeForCustomKeyAndEntity(s, eoentity);
            if (class1 != null) {
                eoattribute = new EOAttribute();
                eoattribute.setName(s);
                eoattribute.setClassName(class1.getName());
                customAttributes.setObjectForKey(eoattribute, s1);
            } else {
                // this should be cached, too
                // can save up to 100 millis and more for complex pages
                customNullAttributes.setObjectForKey(s1, s1);
            }
        }
        double e = p.highResCounter();
        System.out.println(""+((e - s2)*1000.0/freq));
        return eoattribute;
    }

    
}
