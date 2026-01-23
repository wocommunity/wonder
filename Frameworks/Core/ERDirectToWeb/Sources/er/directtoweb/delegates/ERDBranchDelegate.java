/*
 * Copyright (C) NetStruxr, Inc. All rights reserved.
 *
 * This software is published under the terms of the NetStruxr
 * Public Software License version 0.5, a copy of which has been
 * included with this distribution in the LICENSE.NPL file.  */
package er.directtoweb.delegates;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.lang.reflect.Modifier;
import java.util.Enumeration;
import java.util.Iterator;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.webobjects.appserver.WOComponent;
import com.webobjects.directtoweb.D2WContext;
import com.webobjects.directtoweb.NextPageDelegate;
import com.webobjects.eocontrol.EOEnterpriseObject;
import com.webobjects.foundation.NSArray;
import com.webobjects.foundation.NSDictionary;
import com.webobjects.foundation.NSForwardException;
import com.webobjects.foundation.NSMutableArray;
import com.webobjects.foundation.NSMutableDictionary;

import er.directtoweb.ERDirectToWeb;
import er.directtoweb.interfaces.ERDMessagePageInterface;
import er.directtoweb.pages.ERD2WPage;
import er.extensions.foundation.ERXArrayUtilities;
import er.extensions.foundation.ERXDictionaryUtilities;
import er.extensions.localization.ERXLocalizer;

/**
 * The branch delegate is used in conjunction with the
 * {@link ERDMessagePageInterface ERDMessagePageInterface} to allow
 * flexible branching for message pages. Branch delegates can
 * only be used with templates that implement the 
 * {@link ERDBranchInterface ERDBranchInterface}.
 */
public abstract class ERDBranchDelegate implements ERDBranchDelegateInterface {
	/**
	 * Do I need to update serialVersionUID?
	 * See section 5.6 <cite>Type Changes Affecting Serialization</cite> on page 51 of the 
	 * <a href="http://java.sun.com/j2se/1.4/pdf/serial-spec.pdf">Java Object Serialization Spec</a>
	 */
	private static final long serialVersionUID = 1L;

	/**
	 * Runtime flags for the delegate, so you can have one delegate for all tasks.
	 */
	@Retention(RetentionPolicy.RUNTIME)
	@Target(ElementType.METHOD)
	public @interface D2WDelegate {
		/**
		 * Returns the names of the scope where you can have this method. One of "selection,object"
		 */
		public String scope() default "";

		/**
		 * Returns the names of the tasks where you can have this method. Example "query,select"
		 */
		public String availableTasks() default "";
		
		/**
		 * Returns the names of the pages where you can have this method. Example "ListWebMaster,QueryWebMaster"
		 */
		public String availablePages() default "";
	}
	
    /** logging support */
    public final static Logger log = LoggerFactory.getLogger(ERDBranchDelegate.class);

    /** holds the WOComponent class array used to lookup branch delegate methods */
    // MOVEME: Should belong in a WO constants class
    public final static Class[] WOComponentClassArray = new Class[] { WOComponent.class };
    
    public static final String BRANCH_CHOICES = "branchChoices";
    public static final String BRANCH_BUTTON_ID = "branchButtonID";
    public static final String BRANCH_NAME = "branchName";
    public static final String BRANCH_LABEL = "branchButtonLabel";
    public static final String BRANCH_PREFIX = "Button";
    
    /**
     * Implementation of the {@link NextPageDelegate NextPageDelegate}
     * interface. This method provides the dynamic dispatch based on
     * the selected branch provided by the sender. Will call the 
     * method &lt;branchName&gt;(WOComponent) on itself returning the 
     * result. 
     * @param sender template invoking the branch delegate
     * @return result of dynamic method lookup and execution on itself.
     */
    public final WOComponent nextPage(WOComponent sender) {
        WOComponent nextPage = null;
        if (sender instanceof ERDBranchInterface) {
            String branchName = ((ERDBranchInterface)sender).branchName();
            if( branchName != null ) {
                if (log.isDebugEnabled())
                    log.debug("Branching to branch: " + branchName);
                try {
                    Method m = getClass().getMethod(branchName, WOComponentClassArray);
                    nextPage = (WOComponent)m.invoke(this, new Object[] { sender });
                } catch (InvocationTargetException ite) {
                    log.error("Invocation exception occurred in ERBranchDelegate: " + ite.getTargetException() + " for branch name: " + branchName, ite.getTargetException());
                    throw new NSForwardException(ite.getTargetException());
                } catch (Exception e) {
                    log.error("Exception occurred in ERBranchDelegate: " + e.toString() + " for branch name: " + branchName);
                    throw new NSForwardException(e);
                }
            }
        } else {
            log.warn("Branch delegate being used with a component that does not implement the ERBranchInterface");
        }
        return nextPage;
    }
    
    /**
     * Utility to build branch choice dictionaries in code.
     * @param method name of the method in question
     * @param label label for the button, a beautified method name will be used if set to null.
     * @return NSDictionary suitable as a branch choice.
     */
    protected NSDictionary branchChoiceDictionary(String method, String label) {
    	if(label == null) {
    		label = ERXLocalizer.currentLocalizer().localizedDisplayNameForKey(BRANCH_PREFIX, method);
    	}
    	return ERXDictionaryUtilities.dictionaryWithObjectsAndKeys(new Object [] { method, BRANCH_NAME, label, BRANCH_LABEL, method +  "Action", BRANCH_BUTTON_ID});
    }
    
    /**
     * Calculates which branches to show in the display first
     * asking the context for the key <b>branchChoices</b>. If
     * this returns null then 
     * @param context current D2W context
     * @return array of branch names.
     */
    public NSArray branchChoicesForContext(D2WContext context) {
        NSArray choices = (NSArray)context.valueForKey(BRANCH_CHOICES);
        if (choices == null) {
            choices = defaultBranchChoices(context);
        } else {
        	NSMutableArray translatedChoices = new NSMutableArray();
        	for (Iterator iter = choices.iterator(); iter.hasNext();) {
        		Object o = iter.next();
				String method = null;
				String label = null;
	       		NSMutableDictionary entry = new NSMutableDictionary();
        		if (o instanceof NSDictionary) {
        			entry.addEntriesFromDictionary((NSDictionary) o);
        			method = (String) entry.objectForKey(BRANCH_NAME);
        			label = (String) entry.objectForKey(BRANCH_LABEL);
        		} else if (o instanceof String) {
                    method = (String) o;
                    entry.setObjectForKey(method, BRANCH_NAME);
                }
                if (label == null) {
        			label = ERXLocalizer.currentLocalizer().localizedDisplayNameForKey(BRANCH_PREFIX, method);
           		} else if(label.startsWith(BRANCH_PREFIX + ".")){
           			String localizerKey = label;
        			String localized = ERXLocalizer.currentLocalizer().localizedStringForKey(label);
        			if(localized == null) {
        				label = ERXLocalizer.currentLocalizer().localizedDisplayNameForKey(BRANCH_PREFIX, method);
            			ERXLocalizer.currentLocalizer().takeValueForKey(label, localizerKey);
            		} else {
            			label = localized;
        			}
           		} else {
           			// assume it's a user-provided value. If we have an entry in the localizer, use it
           			// otherwise just return it.
        			label = ERXLocalizer.currentLocalizer().localizedStringForKeyWithDefault(label);
        		}
        		entry.setObjectForKey(label, BRANCH_LABEL);
        		entry.setObjectForKey(method +  "Action", BRANCH_BUTTON_ID);
        		translatedChoices.addObject(entry);
        	}
        	choices = translatedChoices;
        }
        return choices;
    }

    /**
     * Uses reflection to find all of the public methods that don't start with 
     * an underscore and take a single WOComponent as a parameter are returned.
     * The methods are sorted by this key.
     * @param context current D2W context
     */
    protected NSArray defaultBranchChoices(D2WContext context) {
        NSArray choices = NSArray.EmptyArray;
        try {
        	String task = context.task();
        	String pageName = context.dynamicPage();
            NSMutableArray methodChoices = new NSMutableArray();
            Method methods[] = getClass().getMethods();
            for (Enumeration e = new NSArray(methods).objectEnumerator(); e.hasMoreElements();) {
                Method method = (Method)e.nextElement();
                if (method.getParameterTypes().length == 1 
                        &&  method.getParameterTypes()[0] == WOComponent.class 
                        && !method.getName().equals("nextPage")
                        && method.getName().charAt(0) != '_'
                        && ((method.getModifiers() & Modifier.PUBLIC) == Modifier.PUBLIC) 
                ) {
                    boolean isAllowed = true;
                    if(method.isAnnotationPresent(D2WDelegate.class)) {
                    	D2WDelegate info = method.getAnnotation(D2WDelegate.class);
                    	String scope = info.scope();
                    	String availableTasks = info.availableTasks();
                    	String availablePages = info.availablePages();
						if(scope.length() > 0) {
							if("object".equals(scope) && context.valueForKey("object") == null) {
								isAllowed = false;
							}
							if(!"object".equals(scope) && context.valueForKey("object") != null) {
								isAllowed = false;
							}
						}
						if(availableTasks.length() > 0 && !availableTasks.contains(task)) {
							isAllowed = false;
						}
						if(availablePages.length() > 0 && !availablePages.contains(pageName)) {
							isAllowed = false;
						}
                    }
                    if(isAllowed) {
                    	NSDictionary branch = branchChoiceDictionary(method.getName(), null);
                    	methodChoices.addObject(branch);      
                    }
                }
            }
            choices = ERXArrayUtilities.sortedArraySortedWithKey(methodChoices, BRANCH_LABEL);
        } catch (SecurityException e) {
            log.error("Caught security exception while calculating the branch choices for delegate: " 
                    + this + " exception: " + e.getMessage());
        }
        return choices;
    }
 
    /**
     * Gets the D2W context from the innermost enclosing D2W component of the sender.
     * @param sender
     */
    protected D2WContext d2wContext(WOComponent sender) {
    	if(ERDirectToWeb.D2WCONTEXT_SELECTOR.implementedByObject(sender)) {
            return (D2WContext) sender.valueForKey(ERDirectToWeb.D2WCONTEXT_SELECTOR.name());
    	}
        throw new IllegalStateException("Can't figure out d2wContext from: " + sender);
    }
    
    /**
     * return the innermost object which might be of interest
     * @param sender
     */
    protected EOEnterpriseObject object(WOComponent sender) {
        return object(d2wContext(sender));
    }
    
    /**
     * Returns the current object form the d2w context
     * @param context
     */
    protected EOEnterpriseObject object(D2WContext context) {
        return (EOEnterpriseObject) context.valueForKey(ERD2WPage.Keys.object);
    }

    /**
     * Utility to remove entries based on an array of keys
     * @param keys
     * @param choices
     */
    protected NSArray choiceByRemovingKeys(NSArray keys, NSArray choices) {
        NSMutableArray result = new NSMutableArray(choices.count());
        for (Enumeration e = choices.objectEnumerator(); e.hasMoreElements();) {
            NSDictionary choice = (NSDictionary) e.nextElement();
            if(!keys.containsObject(choice.objectForKey(BRANCH_NAME))) {
                result.addObject(choice);
            }
        }
        return result;
    }

    /**
     * Utility to leave entries based on an array of keys
     * @param keys
     * @param choices
     */
    protected NSArray choiceByLeavingKeys(NSArray keys, NSArray choices) {
        NSMutableArray result = new NSMutableArray(choices.count());
        for (Enumeration e = choices.objectEnumerator(); e.hasMoreElements();) {
            NSDictionary choice = (NSDictionary) e.nextElement();
            if(keys.containsObject(choice.objectForKey(BRANCH_NAME))) {
                result.addObject(choice);
            }
        }
        return result;
    }

}
