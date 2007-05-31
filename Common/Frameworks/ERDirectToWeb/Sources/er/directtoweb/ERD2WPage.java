/*
 * Copyright (C) NetStruxr, Inc. All rights reserved.
 *
 * This software is published under the terms of the NetStruxr
 * Public Software License version 0.5, a copy of which has been
 * included with this distribution in the LICENSE.NPL file.  */
package er.directtoweb;

import java.util.Enumeration;
import java.util.NoSuchElementException;

import org.apache.log4j.Logger;
import org.apache.log4j.NDC;

import com.webobjects.appserver.WOActionResults;
import com.webobjects.appserver.WOApplication;
import com.webobjects.appserver.WOComponent;
import com.webobjects.appserver.WOContext;
import com.webobjects.appserver.WORequest;
import com.webobjects.appserver.WOResponse;
import com.webobjects.directtoweb.D2WContext;
import com.webobjects.directtoweb.D2WPage;
import com.webobjects.directtoweb.InspectPageInterface;
import com.webobjects.directtoweb.NextPageDelegate;
import com.webobjects.eocontrol.EODataSource;
import com.webobjects.eocontrol.EOEditingContext;
import com.webobjects.eocontrol.EOEnterpriseObject;
import com.webobjects.foundation.NSArray;
import com.webobjects.foundation.NSDictionary;
import com.webobjects.foundation.NSKeyValueCoding;
import com.webobjects.foundation.NSMutableArray;
import com.webobjects.foundation.NSMutableDictionary;
import com.webobjects.foundation.NSMutableSet;
import com.webobjects.foundation.NSTimestamp;

import er.extensions.ERXComponentActionRedirector;
import er.extensions.ERXExceptionHolder;
import er.extensions.ERXExtensions;
import er.extensions.ERXGuardedObjectInterface;
import er.extensions.ERXLocalizer;
import er.extensions.ERXValidation;
import er.extensions.ERXValidationException;
import er.extensions.ERXValueUtilities;

/**
 * Common superclass for all ERD2W templates (except ERD2WEditRelationshipPage).
 * Has tons of extra functionality:<br />
 * <li>Debugging support.<br />
 * Special handlers add extra info in the request-response loop
 * <li>Workflow extensions.<br />
 * If your NextPageDelegate is a {@link ERDBranchDelegate}, then all of the
 * code for actions can be handled in your delegate.
 * <li>Display key extensions. We support tab and sectioned pages via the
 * d2wContext array.<br />
 * 
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
 * (section3, key4, key..) ), ... ) OR with the alternate syntax, which ist most
 * useful with the WebAssistant ( "[tab1]", "(section1)", key1, key2, ...
 * "[tab2]", "(section3)", key4, key..... )
 * 
 */

public abstract class ERD2WPage extends D2WPage implements ERXExceptionHolder, ERDUserInfoInterface, ERXComponentActionRedirector.Restorable, ERDBranchInterface {

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

        public static final String displayNameForTabKey = "displayNameForTabKey";

        public static final String displayPropertyKeys = "displayPropertyKeys";

        public static final String tabSectionsContents = "tabSectionsContents";

        public static final String alternateKeyInfo = "alternateKeyInfo";
    }

    /** logging support */
    public final static Logger log = Logger.getLogger(ERD2WPage.class);

    public static final Logger validationLog = Logger.getLogger("er.directtoweb.validation.ERD2WPage");

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
    public void awake() {
        super.awake();
        if (_context != null) {
            _context.lock();
        }
    }

    /**
     * Utility method to get a value from the user prefs.
     * 
     * @param key
     * @return
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
     * @return
     */
    protected Object userPreferencesValueForPageConfigurationKey(String key) {
        key = ERXExtensions.userPreferencesKeyFromContext(key, d2wContext());
        return userPreferencesValueForKey(key);
    }

    /**
     * Overridden to unlock the page's editingContext, if there is any present.
     */
    public void sleep() {
        if (_context != null) {
            _context.unlock();
        }
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
     * Implementation of the {@link ERXComponentActionRedirector$Restorable}
     * interface. This implementation creates an URL with the name of the
     * current pageConfiguration as a direct action, which assumes a
     * {@link ERD2WDirectAction} as the default direct action. Subclasses need
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
    public void setObject(EOEnterpriseObject eo) {
        setEditingContext((eo != null) ? eo.editingContext() : null);
        // for SmartAssignment
        d2wContext().takeValueForKey(eo, Keys.object);
        super.setObject(eo);
    }
    
    /**
     * Return the object from the d2wContext.
     */
    public EOEnterpriseObject object() {
        return (EOEnterpriseObject) d2wContext().valueForKey(Keys.object);
    }

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
    public void setLocalContext(D2WContext newValue) {
        if (ERXExtensions.safeDifferent(newValue, localContext())) {
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

    protected NSMutableArray keyPathsWithValidationExceptions = new NSMutableArray();

    protected String errorMessage = "";

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
        errorMessages.removeAllObjects();
        errorKeyOrder.removeAllObjects();
        keyPathsWithValidationExceptions.removeAllObjects();
    }

    /**
     * Should incorrect values still be set into the EO. If not set, then the
     * user must re-enter them.
     */
    public boolean shouldSetFailedValidationValue() {
        return ERXValueUtilities.booleanValue(d2wContext().valueForKey(Keys.shouldSetFailedValidationValue));
    }

    /** Used to hold a cleaned-up validation key and message. */
    private NSMutableDictionary _temp = new NSMutableDictionary();

    /** Handles validation errors. */
    public void validationFailedWithException(Throwable e, Object value, String keyPath) {
        if (validationLog.isDebugEnabled()) {
            validationLog.debug("Validation failed with exception: " + e + " value: " + value + " keyPath: " + keyPath);
        }
        if (shouldCollectValidationExceptions()) {
            if (e instanceof ERXValidationException) {
                ERXValidationException erv = (ERXValidationException) e;

                // DT: if we are using the ERXValidation dictionary in the
                // EOModel to define validation rules AND
                // if we are using keyPaths like person.firstname instead of
                // firstname because we have something like:
                // user <<-> person and are editing an user instance then
                // without this fix here the ERD2WPropertyKey
                // would not recognize that 'his' value failed.
                if (keyPath.equals("value")) {
                    keyPath = "" + d2wContext().valueForKey("propertyKey");
                }
                erv.setContext(d2wContext());
                if (d2wContext().propertyKey() != null) {
                    errorKeyOrder.addObject(d2wContext().displayNameForProperty());
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
                if("saveChangesExceptionKey".equals(keyPath) && erv.propertyKey() != null) { 
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
    public boolean isEntityReadOnly() {
        return !isEntityEditable();
    }

    /**
     * If the key <code>isEntityEditable</code> is set, then this value is
     * used, otherwise the value from the super implementation, which checks if
     * the entity is not in the list of <code>readOnlyEntityNames</code>.
     * 
     * @return
     */
    public boolean isEntityEditable() {
        return ERXValueUtilities.booleanValueWithDefault(d2wContext().valueForKey("isEntityEditable"), !super.isEntityReadOnly());
    }

    /**
     * Checks if there is a validation exception in the D2WContext for the
     * current property key.
     */
    public boolean hasValidationExceptionForPropertyKey() {
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

    protected static NSMutableSet _allConfigurations = new NSMutableSet();

    /**
     * Collects the names of all page configurations as you walk through your
     * application.
     * 
     * @return
     */
    public static NSArray allConfigurationNames() {
        synchronized (_allConfigurations) {
            return _allConfigurations.allObjects();
        }
    }

    /**
     * Overridden from the parent for better logging. Reports exceptions in the
     * console for easier debugging.
     */
    public void appendToResponse(WOResponse r, WOContext c) {
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
        super.appendToResponse(r, c);
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
     * Sets the user choosen branch.
     * 
     * @param branch
     *            choosen by user.
     */
    public void setBranch(NSDictionary branch) {
        _branch = branch;
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
            return (String) branch().valueForKey("branchName");
        }
        return null;
    }

    /**
     * Calculates the branch choices for the current page. This method is just a
     * cover for calling the method <code>branchChoicesForContext</code> on
     * the current {@link ERDBranchDelegate ERDBranchDelegate}.
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
     * @return array of {@link ERD2WContainer} holding the keys for the current
     *         section
     */
    public NSArray currentSectionKeys() {
        if (log.isDebugEnabled())
            log.debug("currentSectionKeys()");
        NSArray keys = (NSArray) d2wContext().valueForKey(Keys.alternateKeyInfo);
        if (log.isDebugEnabled())
            log.debug("currentSectionKeys (from alternateKeyInfo):" + keys);
        keys = keys == null ? (NSArray) this.currentSection().keys : keys;
        if (log.isDebugEnabled())
            log.debug("Setting sectionKey and keys: " + _currentSection.name + keys);
        return keys;
    }

    /** Holds the section info */
    private NSMutableArray _sectionsContents;

    /**
     * The array of sections. You bind to this method.
     * 
     * @return array of arrays of {@link ERD2WContainer} holding the keys.
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

    /** Holds the array of {@link ERD2WContainer} defining the tabs. */
    private NSArray _tabSectionsContents;

    /**
     * Returns the array of {@link ERD2WContainer} defining the tabs. A tab is a
     * key and an array of sections
     */
    public NSArray tabSectionsContents() {
        if (_tabSectionsContents == null) {
            NSArray tabSectionContentsFromRule = (NSArray) d2wContext().valueForKey("tabSectionsContents");
            if (tabSectionContentsFromRule == null)
                tabSectionContentsFromRule = (NSArray) d2wContext().valueForKey(Keys.displayPropertyKeys);

            if (tabSectionContentsFromRule == null)
                throw new RuntimeException("Could not find tabSectionsContents in " + d2wContext());
            _tabSectionsContents = tabSectionsContentsFromRuleResult(tabSectionContentsFromRule);
            d2wContext().takeValueForKey(tabSectionContentsFromRule, "tabSectionsContents");
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

    /** Dummy denoting to sections. */
    private final static NSArray _NO_SECTIONS = new NSArray("");

    /** Returns the sections on the current tab. */
    public NSArray sectionsForCurrentTab() {
        return currentTab() != null ? currentTab().keys : _NO_SECTIONS;
    }

    /** Holds the current tab. */
    private ERD2WContainer _currentTab;

    /** Returns the {@link ERD2WContainer} defining the current tab. */
    public ERD2WContainer currentTab() {
        return _currentTab;
    }

    /** Sets the current tab. */
    public void setCurrentTab(ERD2WContainer value) {
        _currentTab = value;
        if (value != null && value.name != null && !value.name.equals("")) {
            d2wContext().takeValueForKey(value.name, Keys.tabKey);
            if (log.isDebugEnabled())
                log.debug("Setting tabKey: " + value.name);
        }
    }

    /** Helper method to calulate the tab key array */
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
    // moved them here to avoid to much subclassing
    public boolean isEmbedded() {
        return ERXValueUtilities.booleanValueForBindingOnComponentWithDefault("isEmbedded", this, false);
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

    public WOComponent nextPage() {
        return _nextPage;
    }

    public void setNextPage(WOComponent wocomponent) {
        _nextPage = wocomponent;
    }

    /**
     * Checks if the delegate is present and can be invoked, then returns the
     * page from it.
     * 
     * @return
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

    public NextPageDelegate nextPageDelegate() {
        if (_nextPageDelegate == null) {
            _nextPageDelegate = (NextPageDelegate) d2wContext().valueForKey("nextPageDelegate");
        }
        return _nextPageDelegate;
    }

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
     * d2wContext. The most conventient way to set and use a pageController is
     * via the rule system:<code><pre>
     *  100: (entity.name='WebSite') and (task = 'list') =&gt; pageController = &quot;ListWebSiteController&quot; [er.directtoweb.ERDDelayedObjectCreationAssignment]
     *  100: (entity.name='WebSite') =&gt; actions = {left = (editAction, controllerAction);}
     *  100: (propertyKey = 'controllerAction') =&gt; componentName = &quot;ERDControllerButton&quot;
     * </pre></code> Then ListWebSiteController would be:<code><pre>
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
     * </pre></code> The nice thing about this is that this allows you to keep your
     * logic confined to just a handful of classes, without the need to
     * constantly create new components that just handle one action.
     * 
     * @return
     */
    public ERDBranchDelegateInterface pageController() {
        if (_pageController == null) {
            _pageController = (ERDBranchDelegateInterface) d2wContext().valueForKey("pageController");
            ;
        }
        return _pageController;
    }

    public void setPageController(ERDBranchDelegateInterface aPageController) {
        _pageController = aPageController;
    }

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
}
