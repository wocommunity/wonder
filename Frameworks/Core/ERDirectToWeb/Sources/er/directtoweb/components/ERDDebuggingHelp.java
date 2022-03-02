/*
 * Copyright (C) NetStruxr, Inc. All rights reserved.
 *
 * This software is published under the terms of the NetStruxr
 * Public Software License version 0.5, a copy of which has been
 * included with this distribution in the LICENSE.NPL file.  */
package er.directtoweb.components;

import com.webobjects.appserver.WOComponent;
import com.webobjects.appserver.WOContext;
import com.webobjects.appserver.WOSession;
import com.webobjects.directtoweb.D2W;
import com.webobjects.directtoweb.D2WContext;
import com.webobjects.eocontrol.EOEditingContext;
import com.webobjects.eocontrol.EOEnterpriseObject;
import com.webobjects.foundation.NSDictionary;
import com.webobjects.foundation.NSMutableDictionary;

import er.directtoweb.ERD2WContextDictionary;
import er.directtoweb.ERDirectToWeb;
import er.directtoweb.pages.ERD2WPage;
import er.extensions.components.ERXDebugMarker;
import er.extensions.foundation.ERXValueUtilities;

/**
 * Little help component useful for debugging.
 * 
 * @binding d2wContext
 * @binding condition default=Boolean
 * @d2wKey pageConfiguration
 * @d2wKey subTask
 * @d2wKey pageName
 * @d2wKey parentPageConfiguration
 * @d2wKey task
 * @d2wKey tabKey
 * @d2wKey entity
 * @d2wKey contextDictionary
 */
public class ERDDebuggingHelp extends WOComponent implements ERXDebugMarker.DebugPageProvider {
	/**
	 * Do I need to update serialVersionUID?
	 * See section 5.6 <cite>Type Changes Affecting Serialization</cite> on page 51 of the 
	 * <a href="http://java.sun.com/j2se/1.4/pdf/serial-spec.pdf">Java Object Serialization Spec</a>
	 */
	private static final long serialVersionUID = 1L;

	protected NSDictionary _contextDictionary;
	public String currentKey;
	
    public ERDDebuggingHelp(WOContext context) { super(context); }

    @Override
    public boolean synchronizesVariablesWithBindings() { return false; }
    
    public boolean showHelp() {
        return (session() != null && ERDirectToWeb.d2wDebuggingEnabled(session())) || ERXValueUtilities.booleanValue(valueForBinding("condition"));
    }
    public boolean d2wComponentNameDebuggingEnabled() {
        return ERDirectToWeb.d2wComponentNameDebuggingEnabled(session());
    }
    public WOComponent toggleComponentNameDebugging() {
        ERDirectToWeb.setD2wComponentNameDebuggingEnabled(session(),
                                                          !ERDirectToWeb.d2wComponentNameDebuggingEnabled(session()));
        return null;
    }

    public String key;
    protected EOEditingContext editingContext;
    protected boolean didSearchEditingContext;
    
    public EOEditingContext editingContext() {
    	if(editingContext == null && !didSearchEditingContext) {
    		WOComponent parent = parent();
    		while(parent != null && editingContext == null) {
    			if(parent instanceof ERD2WPage) {
    				editingContext = ((ERD2WPage)parent).editingContext();
    			}
    			parent = parent.parent();
    		}
    		didSearchEditingContext = true;
    	}
    	return editingContext;
    }

    protected WOComponent showEditingContext(EOEditingContext ec) {
        WOComponent nextPage = pageWithName("ERXEditingContextInspector");
        nextPage.takeValueForKey(ec, "object");
        nextPage.takeValueForKey(this, "debugPageProvider");
        return nextPage;
    }
    public WOComponent debugPageForObject(EOEnterpriseObject o, WOSession s) {
        WOComponent page = (WOComponent)D2W.factory().inspectPageForEntityNamed(o.entityName(),s);
        page.takeValueForKey(o, "object");
        return page;
    }
    
    public WOComponent showEditingContext() {
        return showEditingContext(editingContext());
    }

    public WOComponent showDefaultEditingContext() {
        return showEditingContext(session().defaultEditingContext());
    }

    public boolean hasEditingContext() {
    	return editingContext() != null;
    }
    
    public D2WContext d2wContext() {
    	return (D2WContext)parent().valueForKey("d2wContext");
    }
    
    public Object debugValueForKey() {
        if(key != null && !"".equals(key))
                return d2wContext().valueForKeyPath(key);
        return null;
    }
    
    public void toggleRuleTracing() {
        ERDirectToWeb.configureTraceRuleFiring();
    }
    
    public String ruleTracingState() {
        return ERDirectToWeb.trace.isDebugEnabled() ? "off" : "on";
    }
    
    public Object currentValue() {
        return contextDictionary().valueForKey(currentKey);
    }

    public NSDictionary contextDictionary() {
        if(_contextDictionary == null) {
            _contextDictionary = (NSDictionary)d2wContext().valueForKey("contextDictionary");
            if(_contextDictionary == null) {
                ERD2WContextDictionary dict = new ERD2WContextDictionary(d2wContext().dynamicPage(), null, null);
                _contextDictionary = dict.dictionary();
                d2wContext().takeValueForKey(_contextDictionary, "contextDictionary");
            }
        }
        return _contextDictionary;
    }
    
    public NSDictionary contextDictionaryForPage() {
    	NSMutableDictionary dict = contextDictionary().mutableClone();
    	dict.removeObjectForKey("componentLevelKeys");
        return dict;
    }
    
}