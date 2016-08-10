/*
 * Copyright (C) NetStruxr, Inc. All rights reserved.
 *
 * This software is published under the terms of the NetStruxr
 * Public Software License version 0.5, a copy of which has been
 * included with this distribution in the LICENSE.NPL file.
 */
package er.extensions.components;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.webobjects.appserver.WOComponent;
import com.webobjects.appserver.WOContext;
import com.webobjects.appserver.WOSession;
import com.webobjects.eocontrol.EOEditingContext;
import com.webobjects.eocontrol.EOEnterpriseObject;

import er.extensions.eof.ERXEnterpriseObject;

/**
 * Given an object displays a link to show information about the editing context of that object.
 * 
 * @binding object An EOEditingContext or an EOEnterpriseObject object 
 * @binding debugPageProvider Page to display for showing up details about a EOEnterpriseObject
 */
public class ERXDebugMarker extends WOComponent {
	/**
	 * Do I need to update serialVersionUID?
	 * See section 5.6 <cite>Type Changes Affecting Serialization</cite> on page 51 of the 
	 * <a href="http://java.sun.com/j2se/1.4/pdf/serial-spec.pdf">Java Object Serialization Spec</a>
	 */
	private static final long serialVersionUID = 1L;

    private static final Logger log = LoggerFactory.getLogger(ERXDebugMarker.class);

    public ERXDebugMarker(WOContext aContext) {
        super(aContext);
    }    

    public static interface DebugPageProvider {
        WOComponent debugPageForObject(EOEnterpriseObject o, WOSession s);
    }

    @Override
    public boolean isStateless() { return true; }

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

    @Override
    public void reset() {
        super.reset();
        _object=null;
        _debugPageProvider=null;
    }

    public boolean disabled() { return object()==null; }
    
    public WOComponent debug() {
        WOComponent result=null;
        //log.debug("Object = {}", object());
        if (object() instanceof EOEditingContext) {
            result=pageWithName("ERXEditingContextInspector");
            result.takeValueForKey(object(),"object");
            result.takeValueForKey(debugPageProvider(),"debugPageProvider");
        } else if (object() instanceof EOEnterpriseObject) {
            result=debugPageForObject((EOEnterpriseObject)object(),session());
            if(result != null) {
                result.takeValueForKey(object(),"object");
            } else if(object() instanceof ERXEnterpriseObject) {
                log.info("Object: {}", ((ERXEnterpriseObject)object()).toLongString());
            } else {
                log.info("Object: {}", object());
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
