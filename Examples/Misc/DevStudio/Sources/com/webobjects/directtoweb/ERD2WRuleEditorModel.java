//
// RuleEditorModel.java
// Project RuleEditor
//
// Created by ak on Fri Jun 21 2002
//
package com.webobjects.directtoweb;
import java.io.File;
import java.util.Enumeration;
import java.util.Vector;

import com.webobjects.eocontrol.EOKeyValueUnarchiver;
import com.webobjects.foundation.NSArray;
import com.webobjects.foundation.NSDictionary;
import com.webobjects.foundation.NSForwardException;
import com.webobjects.foundation.NSLog;
import com.webobjects.foundation.NSMutableDictionary;

public class ERD2WRuleEditorModel extends D2WModel {
    
    public ERD2WRuleEditorModel(File file) {
        super(new EOKeyValueUnarchiver(ERD2WRuleEditorModel._dictionaryFromFile(file)));

    }

    public NSArray publicRules() {
        return rules();
    }

    public void setPublicRules(NSArray rules) {
        setRules(rules);
    }

    public Enumeration publicTasks() {
        return tasks();
    }

    public Vector publicDynamicPages() {
        return dynamicPages();
    }

    protected static NSDictionary _dictionaryFromFile(File file) {
        NSDictionary model = null;
        try {
            model = Services.dictionaryFromFile(file);
            NSArray rules = (NSArray)model.objectForKey("rules");
            Enumeration e = rules.objectEnumerator();
            while(e.hasMoreElements()) {
                NSMutableDictionary dict = (NSMutableDictionary)e.nextElement();
                if("com.webobjects.directtoweb.Rule".equals(dict.objectForKey("class"))) {
                    dict.setObjectForKey("ERD2WExtendedRule", "class");
                }
            }
        } catch (Throwable throwable) {
            NSLog.err.appendln("****** DirectToWeb: Problem reading file "
                               + file + " reason:" + throwable);
            if (NSLog.debugLoggingAllowedForLevelAndGroups(1, 40L)) {
                NSLog.err.appendln("STACKTRACE:");
                NSLog.err.appendln(throwable);
            }
            throw NSForwardException._runtimeExceptionForThrowable(throwable);
        }
        return model;
    }
}
