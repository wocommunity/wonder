/*
 * Copyright (C) NetStruxr, Inc. All rights reserved.
 *
 * This software is published under the terms of the NetStruxr
 * Public Software License version 0.5, a copy of which has been
 * included with this distribution in the LICENSE.NPL file.  */
package er.directtoweb;

import com.webobjects.appserver.WOContext;
import com.webobjects.appserver.WORequest;
import com.webobjects.appserver.WOResponse;
import com.webobjects.foundation.NSArray;
import com.webobjects.foundation.NSDictionary;

//    This component will remap keys, useful if you want to have the ability to have two sets of keys for a page
//	Look in the Comparision template for how this component can be used.
/**
 * Useful for remapping keys if say you want to compare two different objects in a compare list.<br />
 * 
 */

public class ERD2WKeyMapper extends ERD2WStatelessComponent {

    public ERD2WKeyMapper(WOContext context) { super(context); }
    
    public void reset() {
        super.reset();
        _mappedKey=null;
    }
    
    String _mappedKey;
    public String mappedKey() {
        if (_mappedKey==null) {
            NSDictionary mapping=(NSDictionary)d2wContext().valueForKey("keyMappingsForComparisonObject");
            _mappedKey = (String) (mapping!=null ? mapping.objectForKey(propertyKey()) : null);
        }
        return _mappedKey;
    }

    public boolean renderMe() {
        String mk=mappedKey();
        if (mk==null && propertyKey()!=null && propertyKey().indexOf(".")!=-1) {
            // if the key is not mentioned we render it EXCEPT if it is a key path
            // and the first atom of that kp is not rendering (in which case the keypath we know will fail
            NSArray components=NSArray.componentsSeparatedByString(propertyKey(),".");
            NSDictionary mapping=(NSDictionary)d2wContext().valueForKey("keyMappingsForComparisonObject");
            String mappingForFirstAtom=(String)mapping.objectForKey(components.objectAtIndex(0));
            if (mappingForFirstAtom!=null && mappingForFirstAtom.length()==0) mk="";
        }
        return (mk==null || mk.length()>0) && propertyKey()!=null;
    }
    
    public void appendToResponse(WOResponse r, WOContext c) {
        if (renderMe()) {
            String originalKey=propertyKey();
            if (mappedKey()!=null) d2wContext().takeValueForKey(mappedKey(),"propertyKey");
            super.appendToResponse(r,c);
            d2wContext().takeValueForKey(originalKey,"propertyKey");
        }
    }

    public void takeValuesFromRequest(WORequest q, WOContext c) {
        // no form in here -- so do nothing -- do not call super
    }
}
