/*
 * Copyright (C) NetStruxr, Inc. All rights reserved.
 *
 * This software is published under the terms of the NetStruxr
 * Public Software License version 0.5, a copy of which has been
 * included with this distribution in the LICENSE.NPL file.  */
package er.bugtracker;

import java.util.Enumeration;

import com.webobjects.eoaccess.EOUtilities;
import com.webobjects.eocontrol.EOEditingContext;
import com.webobjects.eocontrol.EOEnterpriseObject;
import com.webobjects.eocontrol.EOSortOrdering;
import com.webobjects.foundation.NSArray;
import com.webobjects.foundation.NSKeyValueCoding;
import com.webobjects.foundation.NSMutableArray;
import com.webobjects.foundation.NSValidation;

import er.extensions.ERXEC;

public class Component extends _Component {

    // not sharing Component because we want to retain the ability to modify components from the app
    // cacheing this list however

    // FIXME not MT-safe until synchronized
    private static EOEditingContext _editingContext;
    private static NSMutableArray _cachedComponents;
    public static NSArray orderedComponents(EOEditingContext ec) {
        NSMutableArray result=new NSMutableArray();
        if (_cachedComponents==null) {
            _editingContext=ERXEC.newEditingContext();
            _cachedComponents=new NSMutableArray();
            addChildrenOfComponentToArray(null,_cachedComponents,_editingContext);
        }
        for (Enumeration e=_cachedComponents.objectEnumerator(); e.hasMoreElements();) {
            result.addObject(EOUtilities.localInstanceOfObject(ec,(EOEnterpriseObject)e.nextElement()));
        }
        return result;
    }

    private final static NSArray DESCRIPTION_SORT=
        new NSArray(EOSortOrdering.sortOrderingWithKey("textDescription",
                                                       EOSortOrdering.CompareAscending));
                                                                                            
    public static void addChildrenOfComponentToArray(Component c, NSMutableArray a, EOEditingContext ec) {
        NSArray children=c!=null ?
            (NSArray)c.valueForKey("children") :
            EOUtilities.objectsMatchingKeyAndValue(ec,"Component","parent", NSKeyValueCoding.NullValue);
        children=EOSortOrdering.sortedArrayUsingKeyOrderArray(children,
                                                              DESCRIPTION_SORT);
        for (Enumeration e=children.objectEnumerator();e.hasMoreElements();) {
            Component child=(Component)e.nextElement();
            a.addObject(child);
            addChildrenOfComponentToArray(child,a,ec);
        }                   
    }

    public int level() { return level(0); }
    public int level(int safe) {
        if (safe>10) return -1;
        Component parent=(Component)valueForKey("parent");
        return parent==null ? 0 : 1+parent.level(safe+1);
    }

    public String indentedDescription() {
        int level=level();
        StringBuffer sb=new StringBuffer();
        if (level==-1)
            sb.append("***");
        else
            for (int i=0;i<level();i++) sb.append("&nbsp;&nbsp;&nbsp;&nbsp;");
        sb.append(valueForKey("textDescription"));
        return sb.toString();
    }


    public Object validateParent(Component newParent) {
        if (!okToSetParent(this,newParent))
            throw new NSValidation.ValidationException("Sorry: the parent-child relationship you are setting would create a cycle");
        return null;
    }

    public boolean okToSetParent(Component child,
                                 Component parent) {
        return parent==null ? true : okToSetParent(child,(Component)parent.valueForKey("parent"));
    }

    // called automatically by ERXGenericRecord
    public void flushCaches() {
        _cachedComponents=null;
    }
    
}
