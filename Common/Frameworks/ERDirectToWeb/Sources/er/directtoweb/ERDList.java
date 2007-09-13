/*
 * Copyright (C) NetStruxr, Inc. All rights reserved.
 *
 * This software is published under the terms of the NetStruxr
 * Public Software License version 0.5, a copy of which has been
 * included with this distribution in the LICENSE.NPL file.  */
package er.directtoweb;

import org.apache.log4j.Logger;

import com.webobjects.appserver.WOComponent;
import com.webobjects.appserver.WOContext;
import com.webobjects.directtoweb.D2W;
import com.webobjects.directtoweb.EditPageInterface;
import com.webobjects.directtoweb.EditRelationshipPageInterface;
import com.webobjects.eocontrol.EOEditingContext;
import com.webobjects.eocontrol.EOEnterpriseObject;
import com.webobjects.foundation.NSArray;
import com.webobjects.foundation.NSDictionary;

import er.extensions.ERXEOControlUtilities;
import er.extensions.ERXValueUtilities;

/**
 * Used to edit a toMany relationship by allowing the user to pick the eos that belong in the relationship.<br />
 *
 * @binding object
 * @binding key
 * @binding emptyListMessage
 * @binding listPageConfiguration
 * @binding list
 */

public class ERDList extends ERDCustomEditComponent {

    /* logging support */
    static final Logger log = Logger.getLogger(ERDList.class);

    protected NSArray list;

    public ERDList(WOContext context) { super(context); }

    public boolean synchronizesVariablesWithBindings() { return false; }

    public void reset() {
        list = null;
        super.reset();
    }

    public NSDictionary settings() {
        if (null == d2wContext()) { return null; }
        String pc = d2wContext().dynamicPage();
        if (pc != null) {
            return new NSDictionary(pc, "parentPageConfiguration");
        }
        return null;
    }

    public WOComponent createObjectAction() {
    	WOComponent nextPage = context().page();
    	String editRelationshipConfigurationName = (String)valueForBinding("editRelationshipConfigurationName");
    	if(editRelationshipConfigurationName != null && editRelationshipConfigurationName.length() > 0) {
    		nextPage = D2W.factory().pageForConfigurationNamed(editRelationshipConfigurationName, session());
    		if(nextPage instanceof EditRelationshipPageInterface) {
    			EditRelationshipPageInterface epi = (EditRelationshipPageInterface)nextPage;
    			epi.setMasterObjectAndRelationshipKey(object(), key());
    			epi.setNextPage(context().page());
    		} else if(nextPage instanceof EditPageInterface) {
    	    	Object value = d2wContext().valueForKey("useNestedEditingContext");
    	    	boolean createNestedContext = ERXValueUtilities.booleanValue(value);
    	    	EOEnterpriseObject object = ERXEOControlUtilities.editableInstanceOfObject(object(), createNestedContext);
	 			EOEditingContext ec = object.editingContext();
    			ec.lock();
    			try {
    				EOEnterpriseObject eo = ERXEOControlUtilities.createAndAddObjectToRelationship(ec, object, key(), (String)valueForBinding("destinationEntityName"), null);
    				EditPageInterface epi = (EditPageInterface)nextPage;
    				epi.setObject(eo);
    				epi.setNextPage(context().page());
    			} finally {
    				ec.unlock();
    			}
    		}
    	} else {
    		ERXEOControlUtilities.createAndAddObjectToRelationship(object().editingContext(), object(), key(), (String)valueForBinding("destinationEntityName"), null);
    	}
    	return nextPage;
    }
    // we will get asked quite a lot of times, so caching is in order

    public NSArray list() {
        if (list == null) {
            try {
                if (hasBinding("list")) {
                    list = (NSArray)valueForBinding("list");
                } else {
                    list = (NSArray)objectKeyPathValue();
                }
            } catch(java.lang.ClassCastException ex) {
                // (ak) This happens quite often when you haven't set up all display keys...
                // the statement makes this more easy to debug
                log.error(ex + " while getting " + key() + " of " + object());
            }
            if (list == null)
                list = NSArray.EmptyArray;
        }
        return list;
    }

    // This is fine because we only use the D2WList if we have at least one element in the list.
    public boolean erD2WListOmitCenterTag() {
        return hasBinding("erD2WListOmitCenterTag") ? booleanValueForBinding("erD2WListOmitCenterTag") : false;
    }

    public Object valueForKey(String key) {
        Object o = super.valueForKey(key);
        if (key.indexOf("emptyListMessage")!=-1) {
            log.debug("key = emptyListMessage, value = "+o);
        }
        return o;
    }
    public Object valueForBinding(String key) {
        Object o = super.valueForBinding(key);
        if (key.indexOf("emptyListMessage")!=-1) {
            log.debug("key = emptyListMessage, value = "+o);
        }
        return o;
    }
    public String emptyListMessage() {
        log.info("asked for emptyListMessage");
        return "nix";
    }

}
