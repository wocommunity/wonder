/*
 * Copyright (C) NetStruxr, Inc. All rights reserved.
 *
 * This software is published under the terms of the NetStruxr
 * Public Software License version 0.5, a copy of which has been
 * included with this distribution in the LICENSE.NPL file.  */

/* WOOgnl.java created by max on Fri 28-Sep-2001 */
package ognl.webobjects;

import java.util.Enumeration;
import java.util.Hashtable;

import ognl.ClassResolver;
import ognl.Ognl;
import ognl.OgnlException;
import ognl.OgnlRuntime;
import ognl.helperfunction.WOHelperFunctionHTMLParser;
import ognl.helperfunction.WOHelperFunctionHTMLTemplateParser;
import ognl.helperfunction.compatibility.WOMiddleManParser;

import org.apache.log4j.Logger;

import com.webobjects.appserver.WOApplication;
import com.webobjects.appserver.WOAssociation;
import com.webobjects.foundation.NSArray;
import com.webobjects.foundation.NSDictionary;
import com.webobjects.foundation.NSMutableArray;
import com.webobjects.foundation.NSMutableDictionary;
import com.webobjects.foundation.NSNotification;
import com.webobjects.foundation.NSNotificationCenter;
import com.webobjects.foundation.NSSelector;
import com.webobjects.foundation.NSSet;

public class WOOgnl {
	public static Logger log = Logger.getLogger(WOOgnl.class);

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
        	WOOgnl.log.error("Failed to configure WOOgnl.", e);
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
        if (_factory == null) {
            _factory = new WOOgnl();
        }
        return _factory;
    }
    public static void setFactory(WOOgnl factory) { _factory = factory; }
    
    public ClassResolver classResolver() { return NSClassResolver.sharedInstance(); }

    public String ognlBindingFlag() { return DefaultWOOgnlBindingFlag; }

    public Hashtable newDefaultContext() {
        Hashtable h = new Hashtable();
        if (classResolver() != null) {
            h.put("classResolver", classResolver());
        }
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
    	    WOMiddleManParser.setWOHTMLTemplateParserClassName("ognl.helperfunction.WOHelperFunctionHTMLTemplateParser");
    	    if ("true".equalsIgnoreCase(System.getProperty("ognl.inlineBindings"))) {
    	    	WOHelperFunctionHTMLTemplateParser.setAllowInlineBindings(true);
    	    }
    	    if ("true".equalsIgnoreCase(System.getProperty("ognl.parseStandardTags"))) {
    	    	WOHelperFunctionHTMLParser.setParseStandardTags(true);
    	    }
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
                        if (keys.count() == 1) {
                            associations.setObjectForKey(ognlAssociation, keys.lastObject());
                        }else {
                            for (Enumeration ee = keys.objectEnumerator(); ee.hasMoreElements();) {
                                associations.setObjectForKey(ognlAssociation, e.nextElement());    
                            }
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
            throw new RuntimeException("Failed to get value '" + expression + "' on " + obj, ex);
        }
        return value;
    } 
    
    public void setValue(String expression, Object obj, Object value) {
        try {
            Ognl.setValue(expression, newDefaultContext(), obj, value);
        } catch (OgnlException ex) {
            throw new RuntimeException("Failed to set value '" + expression + "' on " + obj, ex);
        }
    } 
    
}	
