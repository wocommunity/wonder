/*
 * Copyright (C) NetStruxr, Inc. All rights reserved.
 *
 * This software is published under the terms of the NetStruxr
 * Public Software License version 0.5, a copy of which has been
 * included with this distribution in the LICENSE.NPL file.  */
package er.extensions;

import com.webobjects.foundation.*;
import com.webobjects.eocontrol.*;
import com.webobjects.eoaccess.*;
import java.lang.*;
import java.util.*;
import org.apache.log4j.Category;

/**
 * The main purpose of the ERXClassDescription class is
 * to throw {@link ERXValidationException}s instead of the
 * usual NSValidation.ValidationException objects. See the
 * ERXValidationException and ERXValidationFactory class
 * for more information about localized and templatized
 * validation exceptions. This class is configured to
 * register itself as the class description by calling
 * the method <code>registerDescription</code>. This method
 * is called when the principal class of this framework is
 * loaded. This happens really early so you shouldn't have
 * to worry about this at all.<br/>
 * <br/>
 * If you wish to provide your own class description subclass
 * see the documentation associated with the Factory inner class.
 */
public class ERXEntityClassDescription extends EOEntityClassDescription {

    /** logging support */
    public static final Category cat = Category.getInstance(ERXEntityClassDescription.class);

    /**
     * This factory inner class is registered as the observer
     * for three notifications: modelWasAdded, classDescriptionNeededForEntity
     * and classDescriptionNeededForClass. If you wish to provide your own
     * subclass of ERXEntityClassDescription then you need to create a
     * subclass of Factory and set that class name in the system properties
     * under the key: er.extensions.ERXClassDescription.factoryClass
     * In your Factory subclass override the method: newClassDescriptionForEntity
     * to provide your own ERXEntityClassDescription subclass.
     */
    public static class Factory {
        /** Public constructor */
        public Factory() {}

        public void reset() {
            _registeredModelNames = new NSMutableArray();
            _entitiesForClass = new NSMutableDictionary();
        }

        /**
         * Method called by the {@link NSNotificationCenter} when
         * an ERXCompilerProxy did reset.
         */
        public void compilerProxyDidCompileClasses(NSNotification n) {
            cat.debug("compilerProxyDidCompileClasses: " + n);
            reset();
       }

        /**
         * Method called by the {@link NSNotificationCenter} when
         * an EOModel is loaded. This method just calls the method
         * <code>registerDescriptionForEntitiesInModel</code>
         * @param n notification that has the EOModel that was loaded.
         */
        public void modelWasAddedNotification(NSNotification n) {
            // Don't want this guy getting in our way.
            cat.debug("modelWasAddedNotification: " + ((EOModel)n.object()).name());
            // FIXME: This is done twice
            NSNotificationCenter.defaultCenter().removeObserver((EOModel)n.object());
            registerDescriptionForEntitiesInModel((EOModel)n.object());
        }

        /**
         * Method called by the {@link NSNotificationCenter} when
         * a class description is needed for a given entity. Usually
         * this method isn't needed seeing as we preempt the on demand
         * loading of class descriptions by loading all of them when
         * the EOModel is loaded. This method just calls the method
         * <code>registerDescriptionForEntity</code>
         * @param n notification that has the name of the entity
         * 	that needs the class description.
         */
        public void classDescriptionNeededForEntityName(NSNotification n) {
            cat.debug("classDescriptionNeededForEntityName: " + (String)n.object());
            String name = (String)n.object();
            EOEntity e = EOModelGroup.defaultGroup().entityNamed(name); //FIXME: This isn't the best way to get the entity
            if(e == null) cat.error("Entity " + name + " not found in the default model group!");
            registerDescriptionForEntity(e);
        }

        /**
         * Method called by the {@link NSNotificationCenter} when
         * a class description is needed for a given Class. Usually
         * this method isn't needed seeing as we preempt the on demand
         * loading of class descriptions by loading all of them when
         * the EOModel is loaded. This method just calls the method
         * <code>registerDescriptionForClass</code>
         * @param n notification that has the Class object
         * 	that needs a class description.
         */
        public void classDescriptionNeededForClass(NSNotification n) {
            Class c = (Class)n.object();
            cat.debug("classDescriptionNeededForClass: " + c.getName());
            registerDescriptionForClass(c);
        }

        /**
         * Factory method that is used to create a new class
         * description for a given entity. Sub classes that
         * wish to provide a sub class of ERXEntityClassDescription
         * should override this method to create that custom
         * description. By default this method returns a new
         * ERXEntityClassDescription.
         * @param entity to create the class description for
         * @return new class description for the given entity
         */
        public ERXEntityClassDescription newClassDescriptionForEntity(EOEntity entity) {
            return new ERXEntityClassDescription(entity);
        }

        /** holds a reference to all of the registered model names */
        private NSMutableArray _registeredModelNames = new NSMutableArray();
        /** holds a mapping of class to entities */
        private NSMutableDictionary _entitiesForClass = new NSMutableDictionary();

        /**
         * This method allows for entities to be altered
         * before they have a custom class description
         * registered. Sub classes can override this method
         * to provide any extra alterings before the description
         * is registered. However be sure to call super as this
         * method does convert the class name from EOGenericRecord
         * to ERXGenericRecord, which unfortunately is required
         * for custom validation to work at the moment.
         * @param eoentity to be prepared for registration
         */
        public void prepareEntityForRegistration(EOEntity eoentity) {
            String className = eoentity.className();
            if(className.equals("EOGenericRecord")) {
                className = ERXGenericRecord.class.getName();
                eoentity.setClassName(className);
                cat.debug(eoentity.name() + ": setting class from EOGenericRecord to " + className);
            }
            //(ak) this should probably move to the plugin, but it won't get loaded until the model is opened
        }

        /**
         * This method registers custom class descriptions for all
         * of the entities in a given model. This method is called
         * when a model is loaded. The reason for this method is
         * to preempt the usual class description loading mechanism
         * which has a race condition involved for the order in
         * which the notifications are recieved.
         * @param model that contains all of the entities to be registerd
         */
        public void registerDescriptionForEntitiesInModel(EOModel model) {
            if (!_registeredModelNames.containsObject(model.name())) {
                for (Enumeration e = model.entities().objectEnumerator(); e.hasMoreElements();) {
                    EOEntity eoentity = (EOEntity)e.nextElement();
                    String className = eoentity.className();

                    prepareEntityForRegistration(eoentity);

                    NSMutableArray array = (NSMutableArray)_entitiesForClass.objectForKey(className);
                    if(array == null) {
                        array = new NSMutableArray();
                    }
                    if(cat.isDebugEnabled())
                        cat.debug("Adding entity " +eoentity.name()+ " with class " + eoentity.className());
                    array.addObject(eoentity);
                    _entitiesForClass.setObjectForKey(array, eoentity.className());

                    //HACK ALERT: (ak) We work around classDescriptionForNewInstances() of EOEntity being broken here...
                    registerDescriptionForEntity(eoentity);
                }
                _registeredModelNames.addObject(model.name());
            }
            // Don't want this guy getting in our way later on ;
            NSNotificationCenter.defaultCenter().removeObserver(model);
        }

        /**
         * This is a hack to work around RadarBug:2867501. EOEntity
         * is hardwired to return an EOEntityClassdescription for the
         * method classDescriptionForNewInstances, this causes a serious
         * problem when using custom class descriptions with D2W which
         * makes use of this method. What this hack does is use the magic
         * of key-value coding to push our custom class description onto
         * a given entity. In order to do this we needed to add the
         * custom {@link KVCProtectedAccessor} to the package
         * com.webobjects.eoaccess.
         * @param entity to have the custom class description set on
         * @param cd class description to set on the entity
         */
        private void _setClassDescriptionOnEntity(EOEntity entity, ERXEntityClassDescription cd)  {
            try {
                //HACK ALERT: (ak) We push the cd rather rudely into the entity to have it ready when classDescriptionForNewInstances() is called on it. We will have to add a com.webobjects.eoaccess.KVCProtectedAccessor to make this work
                NSKeyValueCoding.Utility.takeValueForKey(entity, cd, "classDescription");
            } catch(RuntimeException ex) {
                cat.warn("_setClassDescriptionOnEntity: " + ex);
            }
        }

        /**
         * Registers a custom class description for the given
         * entity using the method <code>newClassDescriptionForEntity</code>
         * which can be overridden by subclasses to provide a
         * different class description subclass.
         * @param entity to register the class description for
         */
        public void registerDescriptionForEntity(EOEntity entity) {
            try {
                String className = entity.className();
                if (cat.isDebugEnabled())
                    cat.debug("Registering description for entity: " + entity.name() + " with class: " + className);
                Class entityClass = className.equals("EOGenericRecord") ? EOGenericRecord.class : Class.forName(className);
                ERXEntityClassDescription cd = newClassDescriptionForEntity(entity);
                EOClassDescription.registerClassDescription(cd, entityClass);
                _setClassDescriptionOnEntity(entity, cd);
            } catch (java.lang.ClassNotFoundException ex) {
                cat.error("Invalid class name for entity: " + entity.name() + " exception: " + ex);
            }
        }

        /**
         * This method is called when a class description is
         * needed for a particular class. Here we use the
         * previous cache that we constructed of class to
         * entity map when the models were loaded. In this
         * way we can register all of the custom class
         * descriptions for a given class if need be.
         * @param class1 class object to have a custom class
         *		description registered for.
         */
        public void registerDescriptionForClass(Class class1) {
            NSArray entities = (NSArray)_entitiesForClass.objectForKey(class1.getName());
            if (entities != null) {
                if (cat.isDebugEnabled())
                    cat.debug("Registering descriptions for class: " + class1.getName() + " found entities: " + entities.valueForKey("name"));
                for (Enumeration e = entities.objectEnumerator(); e.hasMoreElements();) {
                    EOEntity entity = (EOEntity)e.nextElement();
                    ERXEntityClassDescription cd = newClassDescriptionForEntity(entity);
                    EOClassDescription.registerClassDescription(cd, class1);
                    _setClassDescriptionOnEntity(entity, cd);
                }
            } else {
                cat.error("Unable to register descriptions for class: " + class1.getName());
            }
        }
        
    }

    /** 
     * flag to know if the <code>registerDescription</code>
     * method has been called
     */
    private static boolean _registered = false;

    /**
     * This method is called by the principal class
     * of the framework when the framework's NSBundle is
     * loaded. This method registers an observer, either
     * a Factory object, ehich is an inner class of this class
     * or a custom Factory subclass specified in the property:
     * <b>er.extensions.ERXClassDescription.factoryClass</b>.
     * This observer listens for notifications when a model
     * is loaded or a class description is needed and responds
     * by creating and registering custom class descriptions.
     */
    public static void registerDescription() {
        if (!_registered) {
            Factory observer = null;
            try {
                String className = System.getProperty("er.extensions.ERXClassDescription.factoryClass");
                if (className != null) {
                    observer = (Factory)Class.forName(className).newInstance();
                }
            } catch(Exception ex) {
                cat.warn("Exception while registering factory, using default: " + ex );
            }
            
            if(observer == null)
                observer=new Factory();
            ERXRetainer.retain(observer);
            // Need to be able to preempt the model registering descriptions.
            NSNotificationCenter.defaultCenter().addObserver(observer,                                                              new NSSelector("modelWasAddedNotification", ERXConstant.NotificationClassArray),                                                              EOModelGroup.ModelAddedNotification,                                                              null);
            NSNotificationCenter.defaultCenter().addObserver(observer,                                                              new NSSelector("classDescriptionNeededForEntityName",                                                                              ERXConstant.NotificationClassArray), EOClassDescription.ClassDescriptionNeededForEntityNameNotification, null);
            NSNotificationCenter.defaultCenter().addObserver(observer, new NSSelector("classDescriptionNeededForClass", ERXConstant.NotificationClassArray), EOClassDescription.ClassDescriptionNeededForClassNotification, null);
            NSNotificationCenter.defaultCenter().addObserver(observer, new NSSelector("compilerProxyDidCompileClasses", new Class[] { NSNotification.class } ), ERXCompilerProxy.CompilerProxyDidCompileClassesNotification, null);
            _registered = true;
        }
    }

    /**
     * Public constructor
     * @param entity that this class description corresponds to
     */
    public ERXEntityClassDescription(EOEntity entity) { super(entity); }

    /**
     * This method is called when an object is
     * about to be deleted. If any validation
     * exceptions occur they are converted to an
     * {@link ERXValidationException} and that is
     * thrown.
     * @param obj enterprise object to be deleted
     * @throws validation exception
     */
    public void validateObjectForDelete(EOEnterpriseObject obj) throws NSValidation.ValidationException {
        try {
            super.validateObjectForDelete(obj);
        } catch (NSValidation.ValidationException eov) {
            if (cat.isDebugEnabled())
                cat.debug("Caught validation exception: " + eov);
            ERXValidationException erv = ERXValidationFactory.defaultFactory().convertException(eov, obj);
            throw (erv != null ? erv : eov);
        }
    }
    
    /**
     * This method is called to validate a value
     * for a particular key. Typcial validation
     * exceptions that might occur are non-null
     * constraints or string is greater in length
     * than is allowed. If a validation
     * exception does occur they are converted to an
     * {@link ERXValidationException} and that is
     * thrown.
     * @param obj value to be validated
     * @param s property key to validate the value
     *		against.
     * @throws validation exception
     */
    public Object validateValueForKey(Object obj, String s) throws NSValidation.ValidationException {
        Object validated = null;
        if (cat.isDebugEnabled())
            cat.debug("Validate value: " + obj + " for key: " + s);
        try {
            validated = super.validateValueForKey(obj, s);
        } catch (NSValidation.ValidationException eov) {
            if (cat.isDebugEnabled())
                cat.debug("Caught validation exception: " + eov);
            ERXValidationException erv = ERXValidationFactory.defaultFactory().convertException(eov, obj);
            throw (erv != null ? erv : eov);
        }
        return validated;
    }

    /**
     * Calculates a display name for a key using
     * an improved method.
     * @param key to be converted
     * @return pretty display name
     */
    public String displayNameForKey(String key) {
        return ERXStringUtilities.displayNameForKey(key);
    }
    // CHECKME: Why is this disabled?
    /*
    public void validateObjectForSave(EOEnterpriseObject obj) throws NSValidation.ValidationException {
        try {
            super.validateObjectForSave(obj);
        } catch (NSValidation.ValidationException eov) {
            if (cat.isDebugEnabled())
                cat.debug("Caught validation exception: " + eov);
            ERXValidationException erv = ERXValidationFactory.defaultFactory().convertException(eov, obj);
            throw (erv != null ? erv : eov);
        }
    }
    */
}
