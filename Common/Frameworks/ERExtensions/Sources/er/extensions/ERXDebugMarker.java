/*
 * Copyright (C) NetStruxr, Inc. All rights reserved.
 *
 * This software is published under the terms of the NetStruxr
 * Public Software License version 0.5, a copy of which has been
 * included with this distribution in the LICENSE.NPL file.  */
package er.extensions;

import com.webobjects.foundation.*;
import com.webobjects.appserver.*;
import com.webobjects.eocontrol.*;
import com.webobjects.eoaccess.*;
import org.apache.log4j.Category;

public class ERXDebugMarker extends WOComponent {

    //////////////////////////////////////  log4j category  /////////////////////////////////////
    public static final Category cat = Category.getInstance(ERXDebugMarker.class);

    public ERXDebugMarker(WOContext aContext) {
        super(aContext);
    }    
    
    public boolean isStateless() { return true; }
    public boolean synchronizesVariablesWithBindings() { return false; }

    private Object _object;
    public Object object() {
        if (_object==null) {
            _object=valueForBinding("object");
        }
        return _object;
    }
    public void reset() {
        super.reset();
        _object=null;
    }

    public boolean disabled() { return object()==null; }
    
    public WOComponent debug() {
        WOComponent result=null;
        //if (cat.isDebugEnabled()) cat.debug("Object = "+object());
        if (object() instanceof EOEditingContext) {
            result=pageWithName("ERXEditingContextInspector");
            result.takeValueForKey(object(),"object");
        } else if (object() instanceof EOEnterpriseObject) {
            result=debugPageForObject((EOEnterpriseObject)object(),session());
            if(result != null) {
                result.takeValueForKey(object(),"object");
            } else if(object() instanceof ERXGenericRecord) {
                cat.info("Object: " + ((ERXGenericRecord)object()).toLongString());
            } else {
                cat.info("Object: " + object());
            }
        }
        return result;
    }

    public static WOComponent debugPageForObject(EOEnterpriseObject o, WOSession s) {
        // Don't want the dependency on D2W. Not sure what the best solution is
        //return (WOComponent)D2W.factory().inspectPageForEntityNamed(o.entityName(),s);
        return null;
    }
    
}
