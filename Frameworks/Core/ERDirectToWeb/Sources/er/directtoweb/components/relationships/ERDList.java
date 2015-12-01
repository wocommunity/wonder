/*
 * Copyright (C) NetStruxr, Inc. All rights reserved.
 *
 * This software is published under the terms of the NetStruxr
 * Public Software License version 0.5, a copy of which has been
 * included with this distribution in the LICENSE.NPL file.  */
package er.directtoweb.components.relationships;

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

import er.directtoweb.components.ERDCustomEditComponent;
import er.extensions.eof.ERXEOControlUtilities;
import er.extensions.foundation.ERXValueUtilities;

/**
 * Used to edit a toMany relationship by allowing the user to pick the eos that belong in the relationship.
 * 
 * @binding object
 * @binding key
 * @binding emptyListMessage
 * @binding listPageConfiguration
 * @binding list
 * @d2wKey createButtonComponentName
 * @d2wKey useNestedEditingContext
 */
public class ERDList extends ERDCustomEditComponent {
	/**
	 * Do I need to update serialVersionUID?
	 * See section 5.6 <cite>Type Changes Affecting Serialization</cite> on page 51 of the 
	 * <a href="http://java.sun.com/j2se/1.4/pdf/serial-spec.pdf">Java Object Serialization Spec</a>
	 */
	private static final long serialVersionUID = 1L;

    /* logging support */
    static final Logger log = Logger.getLogger(ERDList.class);
    
    protected NSArray list;

    public ERDList(WOContext context) { super(context); }

    @Override
    public boolean synchronizesVariablesWithBindings() { return false; }

    @Override
    public void reset() {
        list = null;
        super.reset();
    }

    public NSDictionary settings() {
        String pc = d2wContext().dynamicPage();
        if(pc != null) {
            return new NSDictionary(pc, "parentPageConfiguration");
        }
        return null;
    }
    
    public WOComponent createObjectAction() {
    	WOComponent nextPage = context().page();
    	// if creationDelegate binding is provided then just call the delegate with the right params.
    	// delegate is responsible for making sure the right page is returned after creating hte object.
    	if (useCreationDelegate()) {
    		EOEnterpriseObject obj = masterObjectKeyPathForCreationDelegate();
    		// defaults to object(), if masterObjectKeyPath is not provided
    		obj = (obj == null) ? object() : obj;
    		String relationshipName = relationshipName();
    		// defaults to key(), if the relationshipName binding is not provided
    		relationshipName = relationshipName == null ? key () : relationshipName;
    		nextPage = createObjectDelegate().create(obj, (String) valueForBinding ("destinationEntityName"), relationshipName);
    	}
    	else {
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
    
    @Override
    public Object valueForKey(String key) {
        Object o = super.valueForKey(key);
        if (key.indexOf("emptyListMessage")!=-1) {
            log.debug("key = emptyListMessage, value = "+o);
        } 
        return o;
    }
    @Override
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
    
    
    /**
     * Interface that all createObjectDelegate classes should implement
     */
    public static interface CreateObjectDelegate {
     
        /**
         * method used to create a newInstance of
         * <code>destinationEntityNameM</code>.
         * <p>
         * Note: this delegate is responsible for making sure that the object is created properly and 
         * the right page is returned.
         * 
         * @param parentObject
         *            object to which the newly created object should be linked
         * @param destinationEntityName
         *            entity name of the destination in the list that we are
         *            creating
         * @param relationshipNameToAddTo
         *            relationship name that should be used to link the newly
         *            created instance to the <code>parentObject</code>
         * @return {@link WOComponent} editPage for the destination entity (usually...)
         */
        public WOComponent create (Object parentObject, String destinationEntityName, String relationshipNameToAddTo);
    }

    /**
     * @return {@link CreateObjectDelegate} instance from the rule
     *         file/bindings. this is responsible for creating the object and
     *         displaying the appropriate page.
     *         <p>
     *         Note: if this is binding is present, then the usual method of
     *         figuring out what to create from
     *         <code>destinationEntityName</code> and <code>object</code>
     *         binding doesn't happen.
     */
    public CreateObjectDelegate createObjectDelegate () {
        Object obj = valueForBinding ("createObjectDelegate");
        CreateObjectDelegate delegate = new DefaultCreateObjectDelegate ();
        if (obj != null && obj instanceof CreateObjectDelegate) {
            delegate = (CreateObjectDelegate) obj;
        }

        return delegate;
    }

    /**
     * @return true, if <code>createObjectDelegate</code> resolved to a valid
     *         object. false, otherwise
     */
    public Boolean hasCreationDelegate () {
        return createObjectDelegate () != null;
    }

    /**
     * @return {@link Boolean} if true, createObjectAction method will use
     *         delegate (either the one that is provided or the default one.
     */
    public Boolean useCreationDelegate () {
        return ERXValueUtilities.booleanValue (valueForBinding ("useCreationDelegate"));
    }

    /**
     * @return the keyPath to be used to compute the master object used by
     *         {@link CreateObjectDelegate}. whatever this keypath resolves to
     *         will be used to attach the newly created object.
     */
    public EOEnterpriseObject masterObjectKeyPathForCreationDelegate () {
        return (EOEnterpriseObject) valueForBinding ("masterObjectKeyPathForCreationDelegate");
    }

    /**
     * @return {@link String} relationshipName to connect <code>masterObj</code>
     *         and the newly created object. this will be used to add the newly
     *         createdObjects to both sides of relationship.
     */
    public String relationshipName () {
        return (String) valueForBinding ("relationshipNameForCreationDelegate");
    }
    
    /**
     * Default creation delegate class. if useCreationDelegate binding is true
     * and no valid createObjectDelegate is present, then this class will be
     * used.
     * 
     * @author santoash
     */
    public class DefaultCreateObjectDelegate implements CreateObjectDelegate {
        /**
         * {@inheritDoc}
         */
        public WOComponent create (Object parentObject, String destinationEntityName, String relationshipNameToAddTo) {
            if (! (parentObject instanceof EOEnterpriseObject)) {
                throw new IllegalArgumentException ("only instances of EOEnterpriseObject can be handled by DefaultCreateObjectDelegate.");
            }
            
            EOEnterpriseObject masterObj = (EOEnterpriseObject) parentObject;
            EOEnterpriseObject newEO = ERXEOControlUtilities.createAndAddObjectToRelationship (masterObj.editingContext (), masterObj, relationshipNameToAddTo, destinationEntityName, null);
            
            EditPageInterface epi = D2W.factory ().editPageForEntityNamed (destinationEntityName, session ());
            epi.setObject (newEO);
            
            return (WOComponent) epi;
        }
    }
}
