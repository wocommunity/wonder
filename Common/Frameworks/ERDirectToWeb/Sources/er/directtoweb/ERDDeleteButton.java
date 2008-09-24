package er.directtoweb;

import org.apache.log4j.Logger;

import com.webobjects.appserver.WOComponent;
import com.webobjects.appserver.WOContext;
import com.webobjects.directtoweb.ConfirmPageInterface;
import com.webobjects.directtoweb.D2W;
import com.webobjects.directtoweb.D2WPage;
import com.webobjects.eocontrol.EODataSource;
import com.webobjects.eocontrol.EOEnterpriseObject;

import er.extensions.ERXGuardedObjectInterface;
import er.extensions.ERXLocalizer;

import java.lang.reflect.Constructor;
import java.lang.reflect.InvocationTargetException;

/**
 * Delete button for repetitions. 
 *
 * @binding object
 * @binding dataSource
 * @binding d2wContext
 * @binding trashcanExplanation
 * @binding noTrashcanExplanation
 *
 * @created ak on Mon Sep 01 2003
 * @project ERDirectToWeb
 */

public class ERDDeleteButton extends ERDActionButton {

    /** logging support */
    private static final Logger log = Logger.getLogger(ERDDeleteButton.class);
    
    public static interface Keys {
        public static final String DeletionDelegateClass = "deletionDelegateClass";
    }
    
    /**
     * Public constructor
     * @param context the context
     */
    public ERDDeleteButton(WOContext context) {
        super(context);
    }

    public boolean canDelete() {
        return object() != null && object() instanceof ERXGuardedObjectInterface ? ((ERXGuardedObjectInterface)object()).canDelete() : true;
    }

    /**
     * Gets the deletion delegate.  Defaults to using a new {@link ERDDeletionDelegate}, but you can provide an instance of
     * your own subclass using the <code>deletionDelegateClass</code> D2W key to provide its fully-qualified class name.
     * @return the deletion delegate
     */
    public ERDDeletionDelegate deletionDelegate() {
        ERDDeletionDelegate delegate = deletionDelegateInstance();
        return delegate != null ? delegate : new ERDDeletionDelegate(object(), dataSource(), context().page());
    }

    public WOComponent deleteObjectAction() {
        ConfirmPageInterface nextPage = (ConfirmPageInterface)D2W.factory().pageForConfigurationNamed((String)valueForBinding("confirmDeleteConfigurationName"), session());
        nextPage.setConfirmDelegate(deletionDelegate());
        nextPage.setCancelDelegate(new ERDPageDelegate(context().page()));
        D2WPage d2wPage = ((D2WPage)nextPage);
        
        String message = ERXLocalizer.currentLocalizer().localizedTemplateStringForKeyWithObject("ERDTrashcan.confirmDeletionMessage", d2wContext()); 
        nextPage.setMessage(message);
        d2wPage.setObject(object());
        return (WOComponent) nextPage;
    }

    public String onMouseOverTrashcan() {
        return hasBinding("trashcanExplanation") ? "self.status='" + valueForBinding("trashcanExplanation") + "'; return true" : "";
    }

    public String onMouseOverNoTrashcan() {
        return hasBinding("noTrashcanExplanation") ? "self.status='" + valueForBinding("noTrashcanExplanation") + "'; return true" : "";
    }
    
    /**
     * Attempts to instantiate the custom deletion delegate subclass, if one has been specified.
     */
    private ERDDeletionDelegate deletionDelegateInstance() {
        ERDDeletionDelegate delegate = null;
        String delegateClassName = (String)d2wContext().valueForKey(Keys.DeletionDelegateClass);
        if (delegateClassName != null) { 
            try {
                Class delegateClass = Class.forName(delegateClassName);
                Constructor delegateClassConstructor = delegateClass.getConstructor(EOEnterpriseObject.class, EODataSource.class, WOComponent.class);
                delegate = (ERDDeletionDelegate)delegateClassConstructor.newInstance(object(), dataSource(), context().page());
            } catch (LinkageError le) {
                if (le instanceof ExceptionInInitializerError) {
                    log.warn("Could not initialize deletion delegate class: " + delegateClassName);
                } else {
                    log.warn("Could not load deletion delegate class: " + delegateClassName + " due to: " + le.getMessage());
                }
            } catch (ClassNotFoundException cnfe) {
                log.warn("Could not find class for deletion delegate: " + delegateClassName);
            } catch (NoSuchMethodException nsme) {
                log.warn("Could not find constructor for deletion delegate class: " + delegateClassName);
            } catch (SecurityException se) {
                log.warn("Insufficient privileges to access deletion delegate constructor: " + delegateClassName);
            } catch (IllegalAccessException iae) {
                log.warn("Insufficient access to create deletion delegate instance: " + iae.getMessage());
            } catch (IllegalArgumentException iae) {
                log.warn("Used an illegal argument when creating deletion delegate instance: " + iae.getMessage());
            } catch (InstantiationException ie) {
                log.warn("Could not instantiate deletion delegate instance: " + ie.getMessage());
            } catch (InvocationTargetException ite) {
                log.warn("Exception while invoking deletion delegate constructor: " + ite.getMessage());
            }
        }

        return delegate;
    }
}
