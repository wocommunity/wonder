//
//  ERXEC.java
//  ERExtensions
//
//  Created by Max Muller on Sun Feb 23 2003.
//
package er.extensions;

import com.webobjects.foundation.*;
import com.webobjects.eocontrol.*;

/**
 * Factory for creating editing contexts.
 */
public class ERXEC {

    /** logging support */
    public static final ERXLogger log = ERXLogger.getERXLogger(ERXEC.class);

    /** name of the notification that is posted after editing context is created */
    public static final String EditingContextDidCreateNotification = "EOEditingContextDidCreate";
    
    public static interface Factory {
        public Object defaultEditingContextDelegate();
        public void setDefaultEditingContextDelegate(Object delegate);
        
        public Object defaultNoValidationDelegate();
        public void setDefaultNoValidationDelegate(Object delegate);

        public void setDefaultDelegateOnEditingContext(EOEditingContext ec);
        public void setDefaultDelegateOnEditingContext(EOEditingContext ec, boolean validation);

        public EOEditingContext _newEditingContext();
        public EOEditingContext _newEditingContext(boolean validationEnabled);
        public EOEditingContext _newEditingContext(EOObjectStore objectStore);
        public EOEditingContext _newEditingContext(EOObjectStore objectStore, boolean validationEnabled);
    }

    public static class DefaultFactory implements Factory {

        /** logging support */
        public static final ERXLogger log = ERXLogger.getERXLogger(DefaultFactory.class);
        
        /** holds a reference to the default ec delegate */
        protected Object defaultEditingContextDelegate;

        /** holds a reference to the default no validation delegate */
        protected Object defaultNoValidationDelegate;

        public DefaultFactory() {
            // Initing defaultEditingContext delegates
            defaultEditingContextDelegate = new ERXDefaultEditingContextDelegate();
            defaultNoValidationDelegate = new ERXECNoValidationDelegate();
        }
        
        /**
         * Returns the default editing context delegate.
         * This delegate is used by default for all editing
         * contexts that are created.
         * @return the default editing context delegate
         */
        public Object defaultEditingContextDelegate() { return defaultEditingContextDelegate; }        

        /**
         * Sets the default editing context delegate to be
         * used for editing context creation.
         * @param delegate to be set on every created editing
         *		context by default.
         */
        public void setDefaultEditingContextDelegate(Object delegate) {
            defaultEditingContextDelegate = delegate;
        }        
        
        /**
         * Default delegate that does not perform validation.
         * Not performing validation can be a good thing when
         * using nested editing contexts as sometimes you only
         * want to validation one object, not all the objects.
         * @returns default delegate that doesn't perform validation
         */
        public Object defaultNoValidationDelegate() { return defaultNoValidationDelegate; }

        /**
         * Sets the default editing context delegate to be
         * used for editing context creation that does not
         * allow validation.
         * @param delegate to be set on every created editing
         *		context that doesn't allow validation.
         */
        public void setDefaultNoValidationDelegate(Object delegate) {
            defaultNoValidationDelegate = delegate;
        }        

        /**
         * Sets either the default editing context delegate
         * that does or does not allow validation based on
         * the validation flag passed in on the given editing context.
         * @param ec editing context to have it's delegate set.
         * @param validation flag that determines if the editing context
         * 		should perform validation on objects being saved.
         */
        public void setDefaultDelegateOnEditingContext(EOEditingContext ec, boolean validation) {
            if (log.isDebugEnabled()) {
                log.debug("Setting default delegate on editing context: " + ec
                          + " allows validation: " + validation);
            }
            if (ec != null) {
                if (validation) {
                    ec.setDelegate(defaultEditingContextDelegate());
                } else {
                    ec.setDelegate(defaultNoValidationDelegate());
                }
            } else {
                log.warn("Attempting to set a default delegate on a null ec!");
            }
        }

        /**
         * Sets the default editing context delegate on
         * the given editing context.
         * @param ec editing context to have it's delegate set.
         */
        public void setDefaultDelegateOnEditingContext(EOEditingContext ec) {
            setDefaultDelegateOnEditingContext(ec, true);
        }        
        
        /**
         * See static method for documentation.
         */
        public EOEditingContext _newEditingContext() {
            return _newEditingContext(EOEditingContext.defaultParentObjectStore(), true);
        }        

        /**
         * See static method for documentation.
         */
        public EOEditingContext _newEditingContext(boolean validationEnabled) {
            return _newEditingContext(EOEditingContext.defaultParentObjectStore(), validationEnabled);
        }
        
        /**
         * See static method for documentation.
         */        
        public EOEditingContext _newEditingContext(EOObjectStore objectStore) {
            return _newEditingContext(objectStore, true);
        }        

        /**
         * See static method for documentation.
         */        
        public EOEditingContext _newEditingContext(EOObjectStore objectStore, boolean validationEnabled) {
            EOEditingContext ec = new EOEditingContext(objectStore);
            setDefaultDelegateOnEditingContext(ec, validationEnabled);
            NSNotificationCenter.defaultCenter().postNotification(EditingContextDidCreateNotification, ec);
            return ec;
        }

    }

    /** holds a reference to the factory used to create editing contexts */
    protected static Factory factory;

    /**
     * Gets the factory used to create editing contexts
     * @return editing context factory
     */
    public static Factory factory() {
        if (factory == null) {
            factory = new DefaultFactory();
        }
        return factory;
    }

    /**
     * Sets the default editing context factory
     * @param factory factory used to create editing contexts
     */
    public static void setFactory(Factory aFactory) {
        factory = aFactory;
    }

    /**
     * Factory method to create a new editing context. Sets
     * the current default delegate on the newly created
     * editing context.
     * @return a newly created editing context with the
     *		default delegate set.
     */
    public static EOEditingContext newEditingContext() {
        return factory()._newEditingContext();
    }    

    /**
     * Creates a new editing context with the specified object
     * store as the parent object store and with validation turned
     * on or off depending on the flag passed in. This method is useful
     * when creating nested editing contexts. After creating
     * the editing context the default delegate is set on the
     * editing context if validation is enabled or the default no
     * validation delegate is set if validation is disabled.<br/>
     * <br/>
     * Note: an {@link EOEditingContext} is a subclass of EOObjectStore
     * so passing in another editing context to this method is
     * completely kosher.
     * @param parent object store for the newly created
     *		editing context.
     * @param validationEnabled determines if the editing context should perform
     *		validation
     * @return new editing context with the given parent object store
     *		and the delegate corresponding to the validation flag
     */    
    public static EOEditingContext newEditingContext(EOObjectStore parent, boolean validationEnabled) {
        return factory()._newEditingContext(parent, validationEnabled);
    }

    /**
        * Factory method to create a new editing context with
     * validation disabled. Sets the default no validation
     * delegate on the editing context. Becareful an
     * editing context that does not perform validation
     * means that none of the usual validation methods are
     * called on the enterprise objects before they are saved
     * to the database.
     * @param validation flag that determines if validation
     *		should or should not be enabled.
     * @return a newly created editing context with a delegate
     *		set that has disabled validation.
     */
    public static EOEditingContext newEditingContext(boolean validation) {
        return factory()._newEditingContext(validation);
    }

    /**
     * Creates a new editing context with the specified object
     * store as the parent object store. This method is useful
     * when creating nested editing contexts. After creating
     * the editing context the default delegate is set on the
     * editing context.<br/>
     * <br/>
     * Note: an {@link EOEditingContext} is a subclass of EOObjectStore
     * so passing in another editing context to this method is
     * completely kosher.
     * @param objectStore parent object store for the newly created
     *		editing context.
     * @return new editing context with the given parent object store
     */
    public static EOEditingContext newEditingContext(EOObjectStore objectStore) {
        return factory()._newEditingContext(objectStore);
    }
}
