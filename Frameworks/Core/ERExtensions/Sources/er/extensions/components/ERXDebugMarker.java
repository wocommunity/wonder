/*
 * Copyright (C) NetStruxr, Inc. All rights reserved.
 *
 * This software is published under the terms of the NetStruxr
 * Public Software License version 0.5, a copy of which has been
 * included with this distribution in the LICENSE.NPL file.  */
package er.extensions.components;

import org.apache.log4j.Logger;

import com.webobjects.appserver.WOComponent;
import com.webobjects.appserver.WOContext;
import com.webobjects.appserver.WOSession;
import com.webobjects.eocontrol.EOEditingContext;
import com.webobjects.eocontrol.EOEnterpriseObject;

import er.extensions.eof.ERXEnterpriseObject;

/**
 * Given an object displays a link to show information about the editing context of that object.*<br />
 * 
 * @binding object
 */

public class ERXDebugMarker extends WOComponent {

    /** logging support */
    public static final Logger log = Logger.getLogger(ERXDebugMarker.class);

    public ERXDebugMarker(WOContext aContext) {
        super(aContext);
    }    

    public static interface DebugPageProvider {
        WOComponent debugPageForObject(EOEnterpriseObject o, WOSession s);
    }
    
    public boolean isStateless() { return true; }
    public boolean synchronizesVariablesWithBindings() { return false; }

    private DebugPageProvider _debugPageProvider;
    public DebugPageProvider debugPageProvider() {
        if (_debugPageProvider==null) {
            _debugPageProvider= (DebugPageProvider)valueForBinding("debugPageProvider");
        }
        return _debugPageProvider;
    }
    
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
        _debugPageProvider=null;
    }

    public boolean disabled() { return object()==null; }
    
    public WOComponent debug() {
        WOComponent result=null;
        //if (log.isDebugEnabled()) log.debug("Object = "+object());
        if (object() instanceof EOEditingContext) {
            result=pageWithName("ERXEditingContextInspector");
            result.takeValueForKey(object(),"object");
            result.takeValueForKey(debugPageProvider(),"debugPageProvider");
        } else if (object() instanceof EOEnterpriseObject) {
            result=debugPageForObject((EOEnterpriseObject)object(),session());
            if(result != null) {
                result.takeValueForKey(object(),"object");
            } else if(object() instanceof ERXEnterpriseObject) {
                log.info("Object: " + ((ERXEnterpriseObject)object()).toLongString());
            } else {
                log.info("Object: " + object());
            }
        }
        return result;
    }

    public WOComponent debugPageForObject(EOEnterpriseObject o, WOSession s) {
        if(debugPageProvider() != null) {
            return debugPageProvider().debugPageForObject(o, s);
        }
        // Don't want the dependency on D2W. Not sure what the best solution is
        //return (WOComponent)D2W.factory().inspectPageForEntityNamed(o.entityName(),s);
        return null;
    }
    
}
