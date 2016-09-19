/*
 * Copyright (C) NetStruxr, Inc. All rights reserved.
 *
 * This software is published under the terms of the NetStruxr
 * Public Software License version 0.5, a copy of which has been
 * included with this distribution in the LICENSE.NPL file.  */
package er.directtoweb.pages;

import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.io.Serializable;
import java.lang.reflect.Constructor;
import java.lang.reflect.InvocationTargetException;
import java.util.Enumeration;
import java.util.NoSuchElementException;

import org.apache.commons.lang3.ObjectUtils;
import org.apache.log4j.Logger;
import org.apache.log4j.NDC;

import com.webobjects.appserver.WOActionResults;
import com.webobjects.appserver.WOApplication;
import com.webobjects.appserver.WOComponent;
import com.webobjects.appserver.WOContext;
import com.webobjects.appserver.WORequest;
import com.webobjects.appserver.WOResponse;
import com.webobjects.directtoweb.D2WContext;
import com.webobjects.directtoweb.D2WModel;
import com.webobjects.directtoweb.D2WPage;
import com.webobjects.directtoweb.InspectPageInterface;
import com.webobjects.directtoweb.NextPageDelegate;
import com.webobjects.eocontrol.EODataSource;
import com.webobjects.eocontrol.EOEditingContext;
import com.webobjects.eocontrol.EOEnterpriseObject;
import com.webobjects.foundation.NSArray;
import com.webobjects.foundation.NSDictionary;
import com.webobjects.foundation.NSForwardException;
import com.webobjects.foundation.NSKeyValueCoding;
import com.webobjects.foundation.NSMutableArray;
import com.webobjects.foundation.NSMutableDictionary;
import com.webobjects.foundation.NSMutableSet;
import com.webobjects.foundation.NSTimestamp;
import com.webobjects.foundation._NSUtilities;

import er.directtoweb.ERD2WContainer;
import er.directtoweb.ERDirectToWeb;
import er.directtoweb.delegates.ERDBranchDelegate;
import er.directtoweb.delegates.ERDBranchDelegateInterface;
import er.directtoweb.delegates.ERDBranchInterface;
import er.directtoweb.interfaces.ERDUserInfoInterface;
import er.extensions.ERXExtensions;
import er.extensions.appserver.ERXComponentActionRedirector;
import er.extensions.components.ERXClickToOpenSupport;
import er.extensions.components.ERXComponentUtilities;
import er.extensions.eof.ERXGuardedObjectInterface;
import er.extensions.foundation.ERXStringUtilities;
import er.extensions.foundation.ERXValueUtilities;
import er.extensions.localization.ERXLocalizer;
import er.extensions.statistics.ERXStats;
import er.extensions.validation.ERXExceptionHolder;
import er.extensions.validation.ERXValidation;
import er.extensions.validation.ERXValidationException;

/**
 * Common superclass for all ERD2W templates (except ERD2WEditRelationshipPage).
 * Has tons of extra functionality:
 * <ul>
 * <li>Debugging support.<br>
 * Special handlers add extra info in the request-response loop</li>
 * <li>Workflow extensions.<br>
 * If your NextPageDelegate is a {@link ERDBranchDelegate}, then all of the
 * code for actions can be handled in your delegate.</li>
 * <li>Display key extensions. We support tab and sectioned pages via the
 * d2wContext array.</li>
 * </ul>
 * In the case of a non-tab page, we expect d2wContext.sectionsContents to
 * return one of the three following formats: (( section1, key1, key2, key4 ), (
 * section2, key76, key 5, ..) .. ) OR with the sections enclosed in "()" - this
 * is most useful with the WebAssistant ( "(section1)", key1, key2, key3,
 * "(section2)", key3, key4, key5... ) OR with normal displayPropertyKeys array
 * in fact if sectionContents isn't found then it will look for
 * displayPropertyKeys ( key1, key2, key3, ... )
 * 
 * In the case of a TAB page, we expect d2wContext.tabSectionsContents to return
 * one of the two following formats: ( ( tab1, key1, key2, key4 ), ( tab2,
 * key76, key 5, ..) .. ) OR with sections ( ( tab1, ( section1, key1, key2 ..),
 * (section3, key4, key..) ), ... ) OR with the alternate syntax, which is most
 * useful with the WebAssistant ( "[tab1]", "(section1)", key1, key2, ...
 * "[tab2]", "(section3)", key4, key..... )
 * @d2wKey object
 * @d2wKey localContext
 * @d2wKey keyPathsWithValidationExceptions
 * @d2wKey shouldPropagateExceptions
 * @d2wKey shouldCollectionValidationExceptions
 * @d2wKey shouldSetFailedValidationValue
 * @d2wKey errorMessages
 * @d2wKey componentName
 * @d2wKey customComponentName
 * @d2wKey propertyKey
 * @d2wKey sectionKey
 * @d2wKey sectionContents
 * @d2wKey tabKey
 * @d2wKey displayNameForTabKey
 * @d2wKey isEntityEditable
 * @d2wKey pageConfiguration
 * @d2wKey displayPropertyKeys
 * @d2wKey tabSectionsContents
 * @d2wKey displayVariant
 * @d2wKey displayNameForEntity
 * @d2wKey nextPageDelegate
 * @d2wKey pageController
 * @d2wKey pageWrapperName
 * @d2wKey inlineStyle
 */
public abstract class ERD2WPage extends D2WPage implements ERXExceptionHolder, ERDUserInfoInterface, ERXComponentActionRedirector.Restorable, ERDBranchInterface {
	/**
	 * Do I need to update serialVersionUID?
	 * See section 5.6 <cite>Type Changes Affecting Serialization</cite> on page 51 of the 
	 * <a href="http://java.sun.com/j2se/1.4/pdf/serial-spec.pdf">Java Object Serialization Spec</a>
	 */
	private static final long serialVersionUID = 1L;

    /** interface for all the keys used in this pages code */
    public static interface Keys {
        public static final String object = "object";

        public static final String localContext = "localContext";

        public static final String d2wContext = "d2wContext";

        public static final String keyPathsWithValidationExceptions = "keyPathsWithValidationExceptions";

        public static final String shouldPropagateExceptions = "shouldPropagateExceptions";

        public static final String shouldCollectValidationExceptions = "shouldCollectValidationExceptions";

        public static final String shouldSetFailedValidationValue = "shouldSetFailedValidationValue";

        public static final String errorMessages = "errorMessages";

        public static final String componentName = "componentName";

        public static final String customComponentName = "customComponentName";

        public static final String pageConfiguration = "pageConfiguration";

        public static final String propertyKey = "propertyKey";

        public static final String sectionKey = "sectionKey";

        public static final String sectionsContents = "sectionsContents";

        public static final String tabKey = "tabKey";

        public static final String tabIndex = "tabIndex";
        
        public static final String tabCount = "tabCount";

        public static final String displayNameForTabKey = "displayNameForTabKey";

        public static final String displayPropertyKeys = "displayPropertyKeys";

        public static final String tabSectionsContents = "tabSectionsContents";

        public static final String alternateKeyInfo = "alternateKeyInfo";
        
        public static final String displayVariant = "displayVariant";
        
        public static final String clickToOpenEnabled = "clickToOpenEnabled";

		// The propertyKey whose form widget gets the focus upon loading an edit page.
		public static final String firstResponderKey = "firstResponderKey";
        
    }

    /** logging support */
    public final static Logger log = Logger.getLogger(ERD2WPage.class);

    public static final Logger validationLog = Logger.getLogger("er.directtoweb.validation.ERD2WPage");

    private String _statsKeyPrefix;

    /**
     * Default public constructor.
     * 
     * @param c
     *            current context.
     */
    public ERD2WPage(WOContext c) {
        super(c);
    }

    /**
     * Overridden to lock the page's editingContext, if there is any present.
     */
	@Override
    public void awake() {
        super.awake();
        if (_context != null) {
            _context.lock();
        }
    }
    
    /**
     * Returns whether or not click-to-open should be enabled for this component.  By
     * default this returns ERXClickToOpenSupport.isEnabled().
     * 
     * @param response the response
     * @param context the context
     * @return whether or not click-to-open is enabled for this component
     */
    public boolean clickToOpenEnabled(WOResponse response, WOContext context) {
        return ERXValueUtilities.booleanValueWithDefault(d2wContext().valueForKey(Keys.clickToOpenEnabled), ERXClickToOpenSupport.isEnabled());
    }
    
    /**
     * Utility method to get a value from the user prefs.
     * 
     * @param key
     */
    protected Object userPreferencesValueForKey(String key) {
        Object result = null;
        NSKeyValueCoding userPreferences = (NSKeyValueCoding) d2wContext().valueForKey("userPreferences");
        if (userPreferences != null) {
            result = userPreferences.valueForKey(key);
        }
        return result;
    }

    /**
     * Utility method to get a value for the current page configuration from the
     * user prefs.
     * 
     * @param key
     */
    protected Object userPreferencesValueForPageConfigurationKey(String key) {
        key = ERXExtensions.userPreferencesKeyFromContext(key, d2wContext());
        return userPreferencesValueForKey(key);
    }

    /**
     * Overridden to unlock the page's editingContext, if there is any present.
     */
    @Override
    public void sleep() {
        if (_context != null) {
            _context.unlock();
        }
        // Make sure the property key is cleared out.  In some embedded page configurations, invoking an action in the embedded component 
        // interrupts the repetition over the property keys, preventing the nullification of the value at the end of the repetition.  This causes
        // weird stuff to happen.
        d2wContext().takeValueForKey(null, "propertyKey");
        super.sleep();
    }

    /**
     * Sets the page's editingContext, automatically locking/unlocking it.
     * 
     * @param newEditingContext
     *            new EOEditingContext
     */
    public void setEditingContext(EOEditingContext newEditingContext) {
        if (newEditingContext != _context) {
            if (_context != null) {
                _context.unlock();
            }
            _context = newEditingContext;
            if (_context != null) {
                _context.lock();
            }
        }
    }

    public EOEditingContext editingContext() {
        return _context;
    }
    
    /**
     * Returns true if the EC has "real" changes (processRecentChanges was called)
	 */
    public boolean hasActualChanges() {
        EOEditingContext ec = editingContext();
        boolean hasChanges = ec.hasChanges();
        if(hasChanges) {
            hasChanges  = ec.insertedObjects().count() > 0;
            hasChanges |= ec.updatedObjects().count() > 0 && ((NSArray)ec.updatedObjects().valueForKeyPath("changesFromCommittedSnapshot.allValues.@flatten")).count() > 0;
            hasChanges |= ec.deletedObjects().count() > 0;
        }
        return hasChanges;
    }

    /**
     * Implementation of the {@link er.extensions.appserver.ERXComponentActionRedirector.Restorable}
     * interface. This implementation creates an URL with the name of the
     * current pageConfiguration as a direct action, which assumes a
     * {@link er.directtoweb.ERD2WDirectAction} as the default direct action. Subclasses need
     * to implement more sensible behaviour.
     * 
     * @return url for the current page
     */
    public String urlForCurrentState() {
        return context().directActionURLForActionNamed(d2wContext().dynamicPage(), null).replaceAll("&amp;", "&");
    }

    /** {@link EOEditingContext} for the current object */
    protected EOEditingContext _context;

    /** Implementation of the {@link InspectPageInterface} */
	@Override
    public void setObject(EOEnterpriseObject eo) {
        setEditingContext((eo != null) ? eo.editingContext() : null);

        /*
         * Storing the EO in the D2WComponent field prevents serialization. The
         * ec must be serialized before the EO. So we store the value in the
         * context instead.
         * 
         * also, for SmartAssignment
         */
        d2wContext().takeValueForKey(eo, Keys.object);
    }
    
    /**
     * Return the object from the d2wContext.
     */
	@Override
    public EOEnterpriseObject object() {
        return (EOEnterpriseObject) d2wContext().valueForKey(Keys.object);
    }

	@Override
    public void setDataSource(EODataSource eodatasource) {
        setEditingContext(eodatasource != null ? eodatasource.editingContext() : null);
        super.setDataSource(eodatasource);
    }

    /** Can be used to get this instance into KVC */
    public final WOComponent self() {
        return this;
    }

    /**
     * {@link D2WContext} for this page. Checks if there is a "d2wContext"
     * binding, too.
     * 
     * @return d2wContext
     */
	@Override
    public D2WContext d2wContext() {
        if (super.d2wContext() == null) {
            if (hasBinding(Keys.localContext)) {
                setLocalContext((D2WContext) valueForBinding(Keys.localContext));
            }
        }
        return super.d2wContext();
    }

    /** Key-Value-Coding needs this method. It should not be called */
    public void setD2wContext(D2WContext newValue) {
    }

    /** Sets the d2wContext for this page */
	@Override
    public void setLocalContext(D2WContext newValue) {
        if (ObjectUtils.notEqual(newValue, _localContext)) {
            // HACK ALERT: this next line is made necessary by the
            // brain-damageness of
            // D2WComponent.setLocalContext, which holds on to the first non
            // null value it gets.
            // I swear if I could get my hands on the person who did that.. :-)
            _localContext = newValue;
            log.debug("SetLocalContext: " + newValue);
        }
        super.setLocalContext(newValue);
        if (newValue != null)
            newValue.takeValueForKey(keyPathsWithValidationExceptions, Keys.keyPathsWithValidationExceptions);
        else
            log.warn("D2WContext was null!");
    }

    // **************************************************************************
    // Error handling extensions
    // **************************************************************************

    protected NSMutableDictionary errorMessages = new NSMutableDictionary();

    protected NSMutableArray errorKeyOrder = new NSMutableArray();

    protected NSMutableArray<String> keyPathsWithValidationExceptions = new NSMutableArray<>();

    protected String errorMessage = "";

    protected ValidationDelegate validationDelegate;
    
    protected boolean validationDelegateInited;

    public NSMutableDictionary errorMessages() {
        return errorMessages;
    }

    public void setErrorMessages(NSMutableDictionary value) {
        errorMessages = value;
    }

    public String errorMessage() {
        return errorMessage;
    }

    public void setErrorMessage(String message) {
        errorMessage = message == null ? "" : message;
    }

    public boolean hasErrors() {
        return (errorMessages != null && errorMessages.count() > 0) || (errorMessage != null && errorMessage.trim().length() > 0);
    }

    public NSArray errorKeyOrder() {
        return errorKeyOrder;
    }

    /**
     * Should exceptions be propagated through to the parent page. If false, the
     * validation errors are not shown at all.
     */
    public boolean shouldPropagateExceptions() {
        return ERXValueUtilities.booleanValue(d2wContext().valueForKey(Keys.shouldPropagateExceptions));
    }

    /** Should exceptions also be handled here or only handled by the parent. */
    public boolean shouldCollectValidationExceptions() {
        return ERXValueUtilities.booleanValue(d2wContext().valueForKey(Keys.shouldCollectValidationExceptions));
    }

    /**
     * Clears all of the collected validation exceptions. Implementation of the
     * {@link ERXExceptionHolder} interface.
     */
    public void clearValidationFailed() {
        errorMessage = null;
        errorMessages.removeAllObjects();
        errorKeyOrder.removeAllObjects();
        keyPathsWithValidationExceptions.removeAllObjects();
        if(validationDelegate() != null) {
        	validationDelegate().clearValidationFailed();
        }
    }

    /**
     * Should incorrect values still be set into the EO. If not set, then the
     * user must re-enter them.
     */
    public boolean shouldSetFailedValidationValue() {
        return ERXValueUtilities.booleanValue(d2wContext().valueForKey(Keys.shouldSetFailedValidationValue));
    }

    /** Used to hold a cleaned-up validation key and message. */
    private NSMutableDictionary<String,String> _temp = new NSMutableDictionary<>();

    /** Handles validation errors. */
	@Override
    public void validationFailedWithException(Throwable e, Object value, String keyPath) {
        if (validationLog.isDebugEnabled()) {
            validationLog.debug("Validation failed with exception: " + e + " value: " + value + " keyPath: " + keyPath);
        }
        if (shouldCollectValidationExceptions()) {
        	if(validationDelegate() != null) {
        		validationDelegate().validationFailedWithException(e, value, keyPath);
        		return;
        	}
            if (e instanceof ERXValidationException) {
                ERXValidationException erv = (ERXValidationException) e;

                // DT: if we are using the ERXValidation dictionary in the
                // EOModel to define validation rules AND
                // if we are using keyPaths like person.firstname instead of
                // firstname because we have something like:
                // user <<-> person and are editing an user instance then
                // without this fix here the ERD2WPropertyKey
                // would not recognize that 'his' value failed.
                if ("value".equals(keyPath)) {
                    keyPath = "" + d2wContext().propertyKey();
                }
                erv.setContext(d2wContext());
                if (d2wContext().propertyKey() != null) {
                	if(!errorKeyOrder.containsObject(d2wContext().displayNameForProperty())) {
                		errorKeyOrder.addObject(d2wContext().displayNameForProperty());
                	}
                	errorMessages.setObjectForKey(erv.getMessage(), d2wContext().displayNameForProperty());
                    // DT: the propertyKey from the validationException is
                    // important because keyPath might only be
                    // saveChangesExceptionKey
                    // which is not enough
                    String key = erv.propertyKey();
                    if (key == null) {
                        key = d2wContext().propertyKey();
                    }
                    keyPathsWithValidationExceptions.addObject(key);
                    if (erv.eoObject() != null && erv.propertyKey() != null && shouldSetFailedValidationValue()) {
                        try {
                            erv.eoObject().takeValueForKeyPath(value, erv.propertyKey());
                        } catch (NSKeyValueCoding.UnknownKeyException ex) {
                            // AK: as we could have custom components that have
                            // non-existant keys
                            // we of course can't push a value, so we discard
                            // the resulting exception
                        } catch (NoSuchElementException ex) {
                            // AK: as we could have custom components that have
                            // non-existant keys
                            // we of course can't push a value, so we discard
                            // the resulting exception
                        }
                    }
                }
                if(("saveChangesExceptionKey".equals(keyPath) || "queryExceptionKey".equals(keyPath)) && erv.propertyKey() != null) { 
                	// AK: this is for combined keys like company,taxIdentifier
                	keyPathsWithValidationExceptions.addObjectsFromArray(NSArray.componentsSeparatedByString( erv.propertyKey(), ","));
                }
            } else {
                _temp.removeAllObjects();
                ERXValidation.validationFailedWithException(e, value, keyPath, _temp, propertyKey(), ERXLocalizer.currentLocalizer(), d2wContext().entity(),
                        shouldSetFailedValidationValue());
                errorKeyOrder.addObjectsFromArray(_temp.allKeys());
                errorMessages.addEntriesFromDictionary(_temp);
            }
            d2wContext().takeValueForKey(errorMessages, Keys.errorMessages);
            if (keyPath != null) {
                // this is set when you have multiple keys failing
                // your keyPath should look like "foo,bar.baz"
                if (keyPath.indexOf(",") > 0) {
                    keyPathsWithValidationExceptions.addObjectsFromArray(NSArray.componentsSeparatedByString(keyPath, ","));
                } else {
                    keyPathsWithValidationExceptions.addObject(keyPath);
                }
            }
        } else if (parent() != null && shouldPropagateExceptions()) {
            parent().validationFailedWithException(e, value, keyPath);
        }
    }

    public ValidationDelegate validationDelegate() {
    	if(!validationDelegateInited && _localContext != null && shouldCollectValidationExceptions()) {
    		// initialize validation delegate
    		String delegateClassName = (String)_localContext.valueForKey("validationDelegateClassName");
    		if(delegateClassName != null) {
	    		try {
	    			Class<? extends ValidationDelegate> delegateClass = 
	    					_NSUtilities.classWithName(delegateClassName);
	    			if(delegateClass != null) {
	    				Constructor<? extends ValidationDelegate> constructor = 
	    						delegateClass.getConstructor(ERD2WPage.class);
	    				validationDelegate = constructor.newInstance(this);
	    			}    			
	    		} catch (NoSuchMethodException e) {
	    			throw NSForwardException._runtimeExceptionForThrowable(e);
	    		} catch (IllegalArgumentException e) {
					throw NSForwardException._runtimeExceptionForThrowable(e);
				} catch (InstantiationException e) {
					throw NSForwardException._runtimeExceptionForThrowable(e);
				} catch (IllegalAccessException e) {
					throw NSForwardException._runtimeExceptionForThrowable(e);
				} catch (InvocationTargetException e) {
					throw NSForwardException._runtimeExceptionForThrowable(e);
				}
    		}
    		validationDelegateInited = true;
    	}
    	return validationDelegate;
    }
    
    public void setValidationDelegate(ValidationDelegate delegate) {
    	validationDelegate = delegate;
    }

    public static abstract class ValidationDelegate implements Serializable {
    	/**
    	 * Do I need to update serialVersionUID?
    	 * See section 5.6 <cite>Type Changes Affecting Serialization</cite> on page 51 of the 
    	 * <a href="http://java.sun.com/j2se/1.4/pdf/serial-spec.pdf">Java Object Serialization Spec</a>
    	 */
    	private static final long serialVersionUID = 1L;

   	protected final ERD2WPage _page;
    	
    	public ValidationDelegate(ERD2WPage page) {
    		_page = page;
    	}
    	
    	protected NSMutableDictionary errorMessages() {
    		return _page.errorMessages;
    	}
    	
    	protected NSMutableArray errorKeyOrder() {
    		return _page.errorKeyOrder;
    	}
    	
    	protected String errorMessage() {
    		return _page.errorMessage;
    	}
    	
    	protected void setErrorMessage(String errorMessage) {
    		_page.setErrorMessage(errorMessage);
    	}
    	
        public abstract boolean hasValidationExceptionForPropertyKey();
        public abstract void validationFailedWithException(Throwable e, Object value, String keyPath);
        public abstract void clearValidationFailed();
        public abstract String errorMessageForPropertyKey();
    }
    
    /**
     * @return the validation exception message for the current property key
     */
    public String errorMessageForPropertyKey() {
    	if(validationDelegate() != null) {
    		return validationDelegate().errorMessageForPropertyKey();
    	}
        return propertyKey() != null && keyPathsWithValidationExceptions.containsObject(propertyKey())?
        		(String) errorMessages().objectForKey(propertyKey()):null;
    }


    /** Checks if the current object can be edited. */
    public boolean isObjectEditable() {
        boolean result = !isEntityReadOnly();
        Object o = object();
        if (o instanceof ERXGuardedObjectInterface) {
            result = result && ((ERXGuardedObjectInterface) o).canUpdate();
        }
        return result;
    }

    /** Checks if the current object can be deleted. */
    public boolean isObjectDeleteable() {
        boolean result = !isEntityReadOnly();
        Object o = object();
        if (o instanceof ERXGuardedObjectInterface) {
            result = result && ((ERXGuardedObjectInterface) o).canDelete();
        }
        return result;
    }

    /** Checks if the current object can be viewed. */
    public boolean isObjectInspectable() {
        return true;
    }

    /**
     * True if the entity is read only. Returns
     * <code>!(isEntityEditable()) </code>
     */
	@Override
    public boolean isEntityReadOnly() {
        return !isEntityEditable();
    }

    /**
     * If the key <code>isEntityEditable</code> is set, then this value is
     * used, otherwise the value from the super implementation, which checks if
     * the entity is not in the list of <code>readOnlyEntityNames</code>.
     * 
     */
    public boolean isEntityEditable() {
        return ERXValueUtilities.booleanValueWithDefault(d2wContext().valueForKey("isEntityEditable"), !super.isEntityReadOnly());
    }

    /**
     * Checks if there is a validation exception in the D2WContext for the
     * current property key.
     */
    public boolean hasValidationExceptionForPropertyKey() {
    	if(validationDelegate() != null) {
    		return validationDelegate().hasValidationExceptionForPropertyKey();
    	}
        return d2wContext().propertyKey() != null && keyPathsWithValidationExceptions.count() != 0 ? keyPathsWithValidationExceptions.containsObject(d2wContext().propertyKey())
                : false;
    }

    // **************************************************************************
    // Debugging extensions
    // **************************************************************************

    /** Holds the user info. */
    protected NSMutableDictionary _userInfo = new NSMutableDictionary();

    /** Implementation of the {@link ERDUserInfoInterface} */
    public NSMutableDictionary userInfo() {
        return _userInfo;
    }

    /** Checks if basic debugging is on */
    public boolean d2wDebuggingEnabled() {
        return ERDirectToWeb.d2wDebuggingEnabled(session());
    }

    /** Checks is component names should be shown. */
    public boolean d2wComponentNameDebuggingEnabled() {
        return ERDirectToWeb.d2wComponentNameDebuggingEnabled(session());
    }

    /**
     * Helper to return the actual current component name, even when wrapped in
     * a custom component.
     */
    public String d2wCurrentComponentName() {
        String name = (String) d2wContext().valueForKey(Keys.componentName);
        if (name.indexOf("CustomComponent") >= 0) {
            name = (String) d2wContext().valueForKey(Keys.customComponentName);
        }
        return name;
    }

    /**
     * This will allow d2w pages to be listed on a per configuration basis in
     * stats collecting.
     */
	@Override
    public String descriptionForResponse(WOResponse aResponse, WOContext aContext) {
        String descriptionForResponse = (String) d2wContext().valueForKey(Keys.pageConfiguration);
        /*
         * if (descriptionForResponse == null) log.info("Unable to find
         * pageConfiguration in d2wContext: " + d2wContext());
         */
        return descriptionForResponse != null ? descriptionForResponse : super.descriptionForResponse(aResponse, aContext);
    }

    /**
     * Overridden from the parent for better logging. Also clears validation
     * errors
     */
	@Override
    public void takeValuesFromRequest(WORequest r, WOContext c) {
        // Need to make sure that we have a clean plate, every time
        clearValidationFailed();
        NDC.push("Page: " + getClass().getName() + (d2wContext() != null ? (" - Configuration: " + d2wContext().valueForKey(Keys.pageConfiguration)) : ""));
        try {
            super.takeValuesFromRequest(r, c);
        } finally {
            NDC.pop();
        }
    }

    /** Overridden from the parent for better logging. */
	@Override
    public WOActionResults invokeAction(WORequest r, WOContext c) {
        WOActionResults result = null;
        NDC.push("Page: " + getClass().getName() + (d2wContext() != null ? (" - Configuration: " + d2wContext().valueForKey(Keys.pageConfiguration)) : ""));
        try {
            result = super.invokeAction(r, c);
        } finally {
            NDC.pop();
        }
        return result;
    }

    protected static final NSMutableSet<String> _allConfigurations = new NSMutableSet<>();

    /**
     * Collects the names of all page configurations as you walk through your
     * application.
     * 
     */
    public static NSArray<String> allConfigurationNames() {
        synchronized (_allConfigurations) {
            return _allConfigurations.allObjects();
        }
    }

    /**
     * Overridden from the parent for better logging. Reports exceptions in the
     * console for easier debugging.
     */
	@Override
    public void appendToResponse(WOResponse response, WOContext context) {
    	String info = "(" + d2wContext().dynamicPage() + ")";
    	// String info = "(" + getClass().getName() + (d2wContext() != null ? ("/" + d2wContext().valueForKey(Keys.pageConfiguration)) : "") + ")";
        NDC.push(info);
        if (d2wContext() != null && !WOApplication.application().isCachingEnabled()) {
            synchronized (_allConfigurations) {
                if (d2wContext().dynamicPage() != null) {
                    _allConfigurations.addObject(d2wContext().dynamicPage());
                }
                // log.info("" + NSPropertyListSerialization.stringFromPropertyList(_allConfigurations));
            }
        }
        
        boolean clickToOpenEnabled = clickToOpenEnabled(response, context); 
        ERXClickToOpenSupport.preProcessResponse(response, context, clickToOpenEnabled);
        super.appendToResponse(response, context);
        ERXClickToOpenSupport.postProcessResponse(getClass(), response, context, clickToOpenEnabled);

        NDC.pop();
    }

    // **************************************************************************
    // Workflow extensions (Branches)
    // **************************************************************************

    /** holds the chosen branch */
    protected NSDictionary _branch;

    /**
     * Cover method for getting the choosen branch.
     * 
     * @return user choosen branch.
     */
    public NSDictionary branch() {
        return _branch;
    }

    /**
     * Sets the user chosen branch.
     * 
     * @param branch
     *            chosen by user.
     */
    public void setBranch(NSDictionary branch) {
        _branch = branch;
        // Propagate the branchName to the D2WContext.
        Object branchValue = _branch != null ? _branch.valueForKey(ERDBranchDelegate.BRANCH_NAME) : NSKeyValueCoding.NullValue;
        d2wContext().takeValueForKey(branchValue, ERDBranchDelegate.BRANCH_NAME);
    }

    /**
     * Implementation of the {@link ERDBranchDelegate ERDBranchDelegate}. Gets
     * the user selected branch name.
     * 
     * @return user selected branch name.
     */
    // ENHANCEME: Should be localized
    public String branchName() {
        if (branch() != null) {
            return (String) branch().valueForKey(ERDBranchDelegate.BRANCH_NAME);
        }
        return null;
    }

    /**
     * Calculates the branch choices for the current page. This method is just a
     * cover for calling the method <code>branchChoicesForContext</code> on
     * the current {@link er.directtoweb.delegates.ERDBranchDelegate ERDBranchDelegate}.
     * 
     * @return array of branch choices
     */
    public NSArray branchChoices() {
        NSArray branchChoices = null;
        if (nextPageDelegate() != null && nextPageDelegate() instanceof ERDBranchDelegateInterface) {
            branchChoices = ((ERDBranchDelegateInterface) nextPageDelegate()).branchChoicesForContext(d2wContext());
        } else if (pageController() != null && pageController() instanceof ERDBranchDelegateInterface) {
            branchChoices = pageController().branchChoicesForContext(d2wContext());
        } else {
            branchChoices = NSArray.EmptyArray;
            //log.error("Attempting to call branchChoices on a page with a delegate: " + nextPageDelegate() + " that doesn't support the ERDBranchDelegateInterface!");
        }
        return branchChoices;
    }

    /**
     * Determines if this message page should display branch choices.
     * 
     * @return if the current delegate supports branch choices.
     */
    public boolean hasBranchChoices() {
        return nextPageDelegate() != null && nextPageDelegate() instanceof ERDBranchDelegateInterface;
    }

    // **************************************************************************
    // Display property key extensions (Sections)
    // **************************************************************************

    /** Holds the current section of display keys. */
    private ERD2WContainer _currentSection;

    /** The current section of display keys. */
    public ERD2WContainer currentSection() {
        return _currentSection;
    }

    /** Sets the current section of display keys. */
    public void setCurrentSection(ERD2WContainer value) {
        _currentSection = value;
        if (value != null) {
            d2wContext().takeValueForKey(value.name, Keys.sectionKey);
            // we can fire rules from the WebAssistant when we push it the
            // -remangled sectionName into the context
            d2wContext().takeValueForKey("(" + value.name + ")", Keys.propertyKey);
            if (log.isDebugEnabled())
                log.debug("Setting sectionKey: " + value.name);
        }
    }

    /**
     * The display keys for the current section. You bind to this method.
     * 
     * @return array of {@link er.directtoweb.ERD2WContainer} holding the keys for the current
     *         section
     */
    public NSArray currentSectionKeys() {
        if (log.isDebugEnabled())
            log.debug("currentSectionKeys()");
        NSArray keys = (NSArray) d2wContext().valueForKey(Keys.alternateKeyInfo);
        if (log.isDebugEnabled())
            log.debug("currentSectionKeys (from alternateKeyInfo):" + keys);
        keys = keys == null ? (NSArray) currentSection().keys : keys;
        if (log.isDebugEnabled())
            log.debug("Setting sectionKey and keys: " + _currentSection.name + keys);
        return keys;
    }

    /** Holds the section info */
    private NSMutableArray _sectionsContents;

    /**
     * The array of sections. You bind to this method.
     * 
     * @return array of arrays of {@link er.directtoweb.ERD2WContainer} holding the keys.
     */
    public NSArray sectionsContents() {
        if (_sectionsContents == null) {
            NSArray sectionsContentsFromRule = (NSArray) d2wContext().valueForKey(Keys.sectionsContents);
            if (sectionsContentsFromRule == null) {
                sectionsContentsFromRule = (NSArray) d2wContext().valueForKey(Keys.displayPropertyKeys);
            }
            if (sectionsContentsFromRule == null)
                throw new RuntimeException("Could not find sectionsContents or displayPropertyKeys in " + d2wContext());
            _sectionsContents = ERDirectToWeb.convertedPropertyKeyArray(sectionsContentsFromRule, '(', ')');

        }
        return _sectionsContents;
    }

    // **************************************************************************
    // Display property key extensions (Tabs)
    // **************************************************************************

    /** Holds the array of {@link er.directtoweb.ERD2WContainer} defining the tabs. */
    private NSArray _tabSectionsContents;

    /**
     * Returns the array of {@link er.directtoweb.ERD2WContainer} defining the tabs. A tab is a
     * key and an array of sections
     */
    public NSArray<ERD2WContainer> tabSectionsContents() {
        if (_tabSectionsContents == null) {
            NSArray tabSectionContentsFromRule = (NSArray) d2wContext().valueForKey("tabSectionsContents");
            if (tabSectionContentsFromRule == null)
                tabSectionContentsFromRule = (NSArray) d2wContext().valueForKey(Keys.displayPropertyKeys);

            if (tabSectionContentsFromRule == null)
                throw new RuntimeException("Could not find tabSectionsContents in " + d2wContext());

            String statsKey = makeStatsKey("ComputeTabSectionsContents");
            ERXStats.markStart("D2W", statsKey);
            _tabSectionsContents = tabSectionsContentsFromRuleResult(tabSectionContentsFromRule);
            ERXStats.markEnd("D2W", statsKey);
            
            // Once calculated we then determine any displayNameForTabKey
            String currentTabKey = (String) d2wContext().valueForKey(Keys.tabKey);
            for (Enumeration e = _tabSectionsContents.objectEnumerator(); e.hasMoreElements();) {
                ERD2WContainer c = (ERD2WContainer) e.nextElement();
                if (c.name.length() > 0) {
                    d2wContext().takeValueForKey(c.name, Keys.tabKey);
                    c.displayName = (String) d2wContext().valueForKey(Keys.displayNameForTabKey);
                }
                if (c.displayName == null)
                    c.displayName = c.name;
            }
            d2wContext().takeValueForKey(currentTabKey, Keys.tabKey);
        }
        return _tabSectionsContents;
    }
    
    /**
     * If you switch the context out from under a wizard page it will hold onto the keys in the tab
     * sections and blow up the next time you use it if the entity has changed. This allows you to 
     * clear the array so it rebuilds.
     */
    protected void clearTabSectionsContents() {
    	_tabSectionsContents = null;
    	_currentTab = null;
    }

    /** Dummy denoting to sections. */
    private final static NSArray _NO_SECTIONS = new NSArray("");

    /** Returns the sections on the current tab. */
    public NSArray sectionsForCurrentTab() {
        return currentTab() != null ? currentTab().keys : _NO_SECTIONS;
    }

    /** Holds the current tab. */
    private ERD2WContainer _currentTab;

    /** Returns the {@link er.directtoweb.ERD2WContainer} defining the current tab. */
    public ERD2WContainer currentTab() {
        String tabName = (String) d2wContext().valueForKey(Keys.tabKey); 
        if (_currentTab == null && !ERXStringUtilities.stringIsNullOrEmpty(tabName)) {
           for (ERD2WContainer aTab : tabSectionsContents()) {
               if (tabName.equals(aTab.name)) {
                   setCurrentTab(aTab);
               }
           }
        }
        if (_currentTab == null) {
            _currentTab = tabSectionsContents().objectAtIndex(0);
        }
        return _currentTab;
    }

    /** Sets the current tab. */
    public void setCurrentTab(ERD2WContainer value) {
        _currentTab = value;
        if (value != null && value.name != null && !value.name.equals("")) {
        	NSArray<ERD2WContainer> tabs = tabSectionsContents();
        	Integer count = Integer.valueOf(tabs.count());
        	Integer index = Integer.valueOf(tabs.indexOf(value));
            d2wContext().takeValueForKey(value.name, Keys.tabKey);
            d2wContext().takeValueForKey(count, Keys.tabCount);
            d2wContext().takeValueForKey(index, Keys.tabIndex);
            if (log.isDebugEnabled()) {
                log.debug("Setting tabKey: " + value.name);
                log.debug("Setting tabCount: " + count);
            	log.debug("Setting tabIndex: " + index);
            }
        }
    }

    /** Helper method to calculate the tab key array */
    protected static NSArray tabSectionsContentsFromRuleResult(NSArray tabSectionContentsFromRule) {
        NSMutableArray tabSectionsContents = new NSMutableArray();

        if (tabSectionContentsFromRule.count() > 0) {
            Object firstValue = tabSectionContentsFromRule.objectAtIndex(0);
            if (firstValue instanceof NSArray) {
                for (Enumeration e = tabSectionContentsFromRule.objectEnumerator(); e.hasMoreElements();) {
                    NSArray tab = (NSArray) e.nextElement();
                    ERD2WContainer c = new ERD2WContainer();
                    c.name = (String) tab.objectAtIndex(0);
                    c.keys = new NSMutableArray();
                    Object testObject = tab.objectAtIndex(1);
                    if (testObject instanceof NSArray) { // format #2
                        for (int i = 1; i < tab.count(); i++) {
                            NSArray sectionArray = (NSArray) tab.objectAtIndex(i);
                            ERD2WContainer section = new ERD2WContainer();
                            section.name = (String) sectionArray.objectAtIndex(0);
                            section.keys = new NSMutableArray(sectionArray);
                            section.keys.removeObjectAtIndex(0);
                            c.keys.addObject(section);
                        }
                    } else { // format #1
                        ERD2WContainer fakeTab = new ERD2WContainer();
                        fakeTab.name = "";
                        fakeTab.keys = new NSMutableArray(tab);
                        fakeTab.keys.removeObjectAtIndex(0);
                        c.keys.addObject(fakeTab);
                    }
                    tabSectionsContents.addObject(c);
                }
            } else if (firstValue instanceof String) {
                tabSectionsContents = ERDirectToWeb.convertedPropertyKeyArray(tabSectionContentsFromRule, '[', ']');
                for (Enumeration e = tabSectionsContents.objectEnumerator(); e.hasMoreElements();) {
                    ERD2WContainer tab = (ERD2WContainer) e.nextElement();
                    if(tab.displayName == null) {
                    	tab.displayName = "Main";
                    }
                    if(tab.name.length() == 0) {
                    	tab.name = "Main";
                    }
                    tab.keys = ERDirectToWeb.convertedPropertyKeyArray(tab.keys, '(', ')');
                }
            }
        }
        return tabSectionsContents;
    }

    // (ak) these actually belong to CompactEdit and PrinterFriendlyInspect
    // moved them here to avoid too much subclassing
    public boolean isEmbedded() {
        return ERXComponentUtilities.booleanValueForBinding(this, "isEmbedded", false);
    }

    /**
     * Gets the <code>displayVariant</code> for the current property key.  The intention is that the display variant
     * allows variation in the display method of property keys without needing different, slightly varying,
     * <code>displayPropertyKeys</code> or <code>tabSectionsContents</code> rules.  Template support has been added for
     * the <code>omit</code> and <code>blank</code> variants.  One could imagine others, such as <code>collapsed</code>,
     * <code>ajax</code>, etc.
     * @return the display variant, if specified
     */
    public String displayVariant() {
        return (String)d2wContext().valueForKey(Keys.displayVariant);
    }

    /**
     * Determines if display of the current property key should be <code>omitted</code>.
     * @return true if key should be omitted
     */
    public boolean isKeyOmitted() {
        return "omit".equals(displayVariant());
    }

    /*
     * // FIXME: Should be dynamic public String pageTitle() { return "NetStruxr -
     * "+d2wContext().valueForKey("displayNameForEntity")+" View"; }
     */
    public NSTimestamp now() {
        return new NSTimestamp();
    }

    protected WOComponent _nextPage;

    protected NextPageDelegate _nextPageDelegate;

	@Override
    public WOComponent nextPage() {
        return _nextPage;
    }

	@Override
    public void setNextPage(WOComponent wocomponent) {
        _nextPage = wocomponent;
    }

    /**
     * Checks if the delegate is present and can be invoked, then returns the
     * page from it.
     * 
     */
    protected WOComponent nextPageFromDelegate() {
        WOComponent result = null;
        NextPageDelegate delegate = nextPageDelegate();
        if (delegate != null) {
            if (!((delegate instanceof ERDBranchDelegate) && (branchName() == null))) {
                // AK CHECKME: we assume here, because nextPage() in
                // ERDBranchDelegate is final,
                // we can't do something reasonable when none of the branch
                // buttons was selected.
                // This allows us to throw a branch delegate at any page, even
                // when no branch was taken
                result = delegate.nextPage(this);
            }
        }
        return result;
    }

    /**
     * Returns the page's {@link NextPageDelegate NextPageDelegate},
     * if any, checking for a "nextPageDelegate" binding if no delegate
     * has been explicitly set.
     * @return The page's next page delegate.
     */
    @Override
    public NextPageDelegate nextPageDelegate() {
        if (_nextPageDelegate == null) {
            _nextPageDelegate = (NextPageDelegate) d2wContext().valueForKey("nextPageDelegate");
        }
        return _nextPageDelegate;
    }

    @Override
    public void setNextPageDelegate(NextPageDelegate nextpagedelegate) {
        _nextPageDelegate = nextpagedelegate;
    }

    /**
     * Holds the page controller for this page.
     */
    protected ERDBranchDelegateInterface _pageController;

    /**
     * Returns the pageController for this page. If there is none given yet,
     * tries to create one by querying the key "pageController" from the
     * d2wContext. The most convenient way to set and use a pageController is
     * via the rule system:<pre><code>
     *  100: (entity.name='WebSite') and (task = 'list') =&gt; pageController = &quot;ListWebSiteController&quot; [er.directtoweb.ERDDelayedObjectCreationAssignment]
     *  100: (entity.name='WebSite') =&gt; actions = {left = (editAction, controllerAction);}
     *  100: (propertyKey = 'controllerAction') =&gt; componentName = &quot;ERDControllerButton&quot;
     * </code></pre> Then ListWebSiteController would be:<pre><code>
     * public class ListWebSiteController extends ERDBranchDelegate {
     * 
     *     private WOComponent _sender;
     * 
     *     private WOComponent sender() {
     *         return _sender;
     *     }
     * 
     *     private void setSender(WOComponent aSender) {
     *         _sender = aSender;
     *     }
     * 
     *     private D2WContext d2wContext() {
     *         return (D2WContext) sender().valueForKey(&quot;d2wContext&quot;);
     *     }
     * 
     *     private EOEnterpriseObject object() {
     *         return (EOEnterpriseObject) d2wContext().valueForKey(&quot;object&quot;);
     *     }
     * 
     *     // first action, show up as &quot;Copy Web Site&quot;
     *     public WOComponent copyWebSite(WOComponent sender) {
     *        setSender(sender);
     *        WOComponent result = ....
     *        return result;
     *     }
     * 
     *     // second action, show up as &quot;Delete Web Site&quot;
     *     public WOComponent deleteWebSite(WOComponent sender) {
     *        setSender(sender);
     *        WOComponent result = ....
     *        return result;
     *     }
     * }
     * </code></pre> The nice thing about this is that this allows you to keep your
     * logic confined to just a handful of classes, without the need to
     * constantly create new components that just handle one action.
     * 
     */
    public ERDBranchDelegateInterface pageController() {
        if (_pageController == null) {
            _pageController = (ERDBranchDelegateInterface) d2wContext().valueForKey("pageController");
        }
        return _pageController;
    }

    public void setPageController(ERDBranchDelegateInterface aPageController) {
        _pageController = aPageController;
    }

    @Override
    public boolean showCancel() {
        return _nextPageDelegate != null || _nextPage != null;
    }

    public NSDictionary settings() {
        String pc = d2wContext().dynamicPage();
        if(pc != null) {
            return new NSDictionary(pc, "parentPageConfiguration");
        }
        return null;
    }

	/**
	 * Gets the name of the page wrapper component. Overrides the superclass' implementation which caches the 
	 * name too aggressively.  This method allows you to drive the page wrapper component via the rules.
	 * Defaults to <code>PageWrapper</code> if a value is not found from the D2W rules.
	 *
	 * @return the name of the page wrapper
	 */
    @Override
	public String pageWrapperName() {
		String name = (String)d2wContext().valueForKey(D2WModel._PageWrapperNameKey);
		
		if (null == name) {
			name = "PageWrapper";
		}
		
		return name;
	}

    /**
     * This variant of pageWithName provides a Java5 genericized version of the
     * original pageWithName. You would call it with:
     * 
     * MyNextPage nextPage = pageWithName(MyNextPage.class);
     * 
     * @param <T>
     *            the type of component to create
     * @param componentClass
     *            the Class of the component to load
     * @return an instance of the requested component class
     */
    @SuppressWarnings("unchecked")
    public <T extends WOComponent> T pageWithName(Class<T> componentClass) {
        return (T) super.pageWithName(componentClass.getName());
    }

    public boolean isTopLevelPage() {
        return this == topLevelPage();
    }

    /**
     * Gets the top level D2WPage.
     * @return the page
     */
    private ERD2WPage topLevelPage() {
        ERD2WPage page = this;
        boolean hasParentPage = true;
        while (hasParentPage) {
            WOComponent component = page.parent();
            // Try to get the next ERD2WPage up the chain.
            while (component != null && !(component instanceof ERD2WPage)) {
                component = component.parent();
            }

            if (null == component) {
                hasParentPage = false;
            } else {
                page = (ERD2WPage)component;
            }
        }
        return page;
    }

    /**
     * Determines whether the component should display the detailed "inline" page metrics.
     * @return true if should show the detailed metrics
     */
    public boolean shouldDisplayDetailedPageMetrics() {
        return ERDirectToWeb.pageMetricsEnabled() && ERDirectToWeb.detailedPageMetricsEnabled();
    }

    /**
     * Determines whether the component should display the page metrics summary.  On display the summary for the
     * top-level page by default, and for all pages when showing detailed metrics.
     * @return true if should show metrics summary
     */
    public boolean shouldDisplayPageMetricsSummary() {
        boolean metricsEnabled = ERDirectToWeb.pageMetricsEnabled();
        return isTopLevelPage() ? metricsEnabled : (metricsEnabled && shouldDisplayDetailedPageMetrics());
    }

    /**
     * Creates a stats key to identify stats to the current property key.
     * @return the stats key
     */
    public String statsKeyForCurrentPropertyKey() {
        return makeStatsKey(propertyKey());
    }

    /**
     * Makes the stats key, prepending a prefix to identify the stats to the originating page.
     * @param key to format
     * @return the formatted key
     */
    protected String makeStatsKey(String key) {
        return statsKeyPrefix() + key;
    }

    /**
     * A stats key prefix that guarantees the stats will be identifiable to this instance.
     * @return the key prefix
     */
    public String statsKeyPrefix() {
        if (null == _statsKeyPrefix) {
            _statsKeyPrefix = d2wContext().dynamicPage() + "_0x" + hashCode() + ".";
        }
        return _statsKeyPrefix;
    }

    /**
     * Gets the latest metrics event for the current property key.
     * @return the event
     */
    public ERXStats.LogEntry latestEntryForCurrentPropertyKey() {
        return ERXStats.logEntryForKey(ERXStats.Group.Component, statsKeyForCurrentPropertyKey());
    }

    /**
     * Gets the aggregate duration of events sharing the current property key.
     * @return the duration
     */
    public long aggregateEventDurationForCurrentPropertyKey() {
        return ERXStats.logEntryForKey(ERXStats.Group.Component, statsKeyForCurrentPropertyKey()).sum();
    }

    /**
     * Gets the stats for the current page.
     * @return the stats
     */
    public NSDictionary statsForPage() {
        NSMutableDictionary result = new NSMutableDictionary();
        NSDictionary statsDict = ERXStats.statistics();
        for (Enumeration keysEnum = statsDict.keyEnumerator(); keysEnum.hasMoreElements();) {
            String key = (String)keysEnum.nextElement();
            if (key.contains(statsKeyPrefix())) {
                String statsGroup = ERXStringUtilities.firstPropertyKeyInKeyPath(key);
                NSMutableArray events = (NSMutableArray)result.objectForKey(statsGroup);
                if (null == events) {
                    events = new NSMutableArray();
                    result.setObjectForKey(events, statsGroup);
                }
                events.addObject(statsDict.objectForKey(key));
            }
        }
        return result;
    }

    /**
     * Gets the CSS class(es) for the container element, based on the current entity and task.
     * @return the css classes
     */
    public String cssClassForPageContainerElement() {
        NSMutableArray classes = new NSMutableArray();
        D2WContext d2wContext = d2wContext();
		String task = d2wContext.task();
		String subTask = (String)d2wContext.valueForKey("subTask");
		String elementClassPrefix = ERXStringUtilities.capitalize(task) + "Table";
		classes.addObject(elementClassPrefix);
		if (subTask != null) {
			classes.addObject(ERXStringUtilities.capitalize(task) + ERXStringUtilities.capitalize(subTask) + "Table");
		}
		if (d2wContext.dynamicPage() != null && d2wContext.dynamicPage().indexOf("Embedded") > -1) {
			classes.addObject(ERXStringUtilities.capitalize(task) + "Embedded");
			classes.addObject("embedded");
		}
        if (entityName() != null) {
            classes.addObject(elementClassPrefix + entityName());
        }
        classes.addObject(elementClassPrefix + d2wContext.dynamicPage());
		return classes.componentsJoinedByString(" ");
    }

    /**
     * Gets the CSS class(es) that should be applied to the current property name container element.
     * @return the css classes
     */
    public String cssClassForPropertyName() {
        return _cssClassForTemplateForCurrentPropertyKey("cssClassForPropertyName");
    }

    /**
     * Gets the CSS class(es) that should be applied to the current property key container element.
     * @return the css classes
     */
    public String cssClassForPropertyKey() {
        return _cssClassForTemplateForCurrentPropertyKey("cssClass");
    }

    /**
     * Gets the CSS class(es) that should be applied to the current container element.
     * @param cssKey from the d2wContext that defines the CSS for this element
     * @return the css classes
     */
    private String _cssClassForTemplateForCurrentPropertyKey(String cssKey) {
        NSMutableArray classes = new NSMutableArray();
        D2WContext d2wContext = d2wContext();
        String propertyKey = d2wContext.propertyKey();
        if (propertyKey != null) {
            classes.addObject(propertyKey.replaceAll("\\.", "_"));

            // Required?
            if (ERXValueUtilities.booleanValue(d2wContext.valueForKey("displayRequiredMarker")) && !"query".equals(task())) {
                classes.addObject("required");
            }

            // Has error?
            if (hasValidationExceptionForPropertyKey()) {
                classes.addObject("error");
            }

            // Explicitly defined class(es).
            NSArray explicitClasses = ERXValueUtilities.arrayValueWithDefault(d2wContext.valueForKey(cssKey), NSArray.EmptyArray);
            if (explicitClasses.count() > 0) {
                classes.addObjectsFromArray(explicitClasses);
            }
        }
        return classes.componentsJoinedByString(" ");
    }

    /**
     * Gets any inline style declarations for the current property name container element.
     * @return the inline style declarations
     */
    public String inlineStyleDeclarationForPropertyName() {
        return (String)d2wContext().valueForKey("inlineStyleForPropertyName");
    }

    /**
     * Gets any inline style declarations for the current property key container element.
     * @return the inline style declarations
     */
    public String inlineStyleDeclarationForPropertyKey() {
        return (String)d2wContext().valueForKey("inlineStyle");
    }

	private void writeObject(ObjectOutputStream out) throws IOException {
		out.defaultWriteObject();
		out.writeObject(d2wContext().valueForKey(Keys.tabKey));
		out.writeObject(d2wContext().valueForKey(Keys.tabCount));
		out.writeObject(d2wContext().valueForKey(Keys.tabIndex));
	}
	
	private void readObject(ObjectInputStream in) throws IOException, ClassNotFoundException {
		in.defaultReadObject();
        d2wContext().takeValueForKey(in.readObject(), Keys.tabKey);
        d2wContext().takeValueForKey(in.readObject(), Keys.tabCount);
        d2wContext().takeValueForKey(in.readObject(), Keys.tabIndex);
	}
}
