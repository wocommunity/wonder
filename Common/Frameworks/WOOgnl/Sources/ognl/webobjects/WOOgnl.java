/*
 * Copyright (C) NetStruxr, Inc. All rights reserved.
 *
 * This software is published under the terms of the NetStruxr
 * Public Software License version 0.5, a copy of which has been
 * included with this distribution in the LICENSE.NPL file.  */

/* WOOgnl.java created by max on Fri 28-Sep-2001 */
package ognl.webobjects;

import java.util.*;

import ognl.*;

import com.webobjects.appserver.*;
import com.webobjects.appserver._private.*;
import com.webobjects.foundation.*;

public class WOOgnl {

    public static final String DefaultWOOgnlBindingFlag = "~";

    protected static NSMutableArray _retainerArray = new NSMutableArray();
    static {
        try {
            Observer o = new Observer();
            _retainerArray.addObject(o);
            NSNotificationCenter.defaultCenter().addObserver(o,
                                                             new NSSelector("configureWOOgnl", new Class[]
                                                                            { com.webobjects.foundation.NSNotification.class }),
                                                             WOApplication.ApplicationWillFinishLaunchingNotification,
                                                             null);
        } catch (Exception e) {
            NSLog.err.appendln("Exception: " + e.getMessage());
        }
    }

    public static class Observer {
        public void configureWOOgnl(NSNotification n) {
            WOOgnl.factory().configureWOForOgnl();
            NSNotificationCenter.defaultCenter().removeObserver(this);
            _retainerArray.removeObject(this);
        }
    }

    protected static WOOgnl _factory;
    public static WOOgnl factory() {
        if (_factory == null)
            _factory = new WOOgnl();
        return _factory;
    }
    public static void setFactory(WOOgnl factory) { _factory = factory; }
    
    public ClassResolver classResolver() { return NSClassResolver.sharedInstance(); }

    public String ognlBindingFlag() { return DefaultWOOgnlBindingFlag; }

    public Hashtable newDefaultContext() {
        Hashtable h = new Hashtable();
        if (classResolver() != null)
            h.put("classResolver", classResolver());
        return h;
    }
    
    public void configureWOForOgnl() {
        // Configure runtime.
        // Configure foundation classes.
        OgnlRuntime.setPropertyAccessor(Object.class, new NSObjectPropertyAccessor());
        OgnlRuntime.setPropertyAccessor(NSArray.class, new NSArrayPropertyAccessor());
        OgnlRuntime.setPropertyAccessor(NSDictionary.class, new NSDictionaryPropertyAccessor());

        NSFoundationElementsAccessor e = new NSFoundationElementsAccessor();
        OgnlRuntime.setElementsAccessor(NSArray.class, e);
        OgnlRuntime.setElementsAccessor(NSDictionary.class, e);
        OgnlRuntime.setElementsAccessor(NSSet.class, e);
        // Register template parser
        if (!"false".equals(System.getProperty("ognl.active"))) {
            WOParser.setWOHTMLTemplateParserClassName("ognl.webobjects.WOOgnlHTMLTemplateParser");
        }
    }

    public void convertOgnlConstantAssociations(NSMutableDictionary associations) {
        for (Enumeration e = associations.objectEnumerator(); e.hasMoreElements();) {
            WOAssociation association = (WOAssociation)e.nextElement();
            if (association.isValueConstant()) {
                Object constantValue = association.valueInComponent(null);
                if (constantValue != null && constantValue instanceof String && ((String)constantValue).startsWith(ognlBindingFlag())) {
                    String ognlExpression = ((String)constantValue).substring(ognlBindingFlag().length(),
                                                                              ((String)constantValue).length());
                    if (ognlExpression.length() > 0) {
                        WOOgnlAssociation ognlAssociation = new WOOgnlAssociation(ognlExpression);
                        NSArray keys = associations.allKeysForObject(association);
                        //if (log.isDebugEnabled())
                        //    log.debug("Constructing Ognl association for binding key(s): "
                        //              + (keys.count() == 1 ? keys.lastObject() : keys) + " expression: " + ognlExpression);
                        if (keys.count() == 1)
                            associations.setObjectForKey(ognlAssociation, keys.lastObject());
                        else {
                            for (Enumeration ee = keys.objectEnumerator(); ee.hasMoreElements();)
                                associations.setObjectForKey(ognlAssociation, e.nextElement());                                
                        }
                    }
                }
            }
        }
    }

    public Object getValue(String expression, Object obj) {
        Object value = null;
        try {
            value = Ognl.getValue(expression, newDefaultContext(), obj);
        } catch (OgnlException ex) {
            throw new IllegalStateException(ex.getMessage());
        }
        return value;
    } 
    
    public void setValue(String expression, Object obj, Object value) {
        try {
            Ognl.setValue(expression, newDefaultContext(), obj, value);
        } catch (OgnlException ex) {
            throw new IllegalStateException(ex.getMessage());
        }
    } 
    
}	
