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

// This class is used to throw ERXValidationExceptions.  See the description of ERXValidationException for
// details on the improvements made to validation handling.
public class ERXEntityClassDescription extends EOEntityClassDescription {

    public static final Category cat = Category.getInstance(ERXEntityClassDescription.class);

    public static class Observer {
        public void modelWasAddedNotification(NSNotification n) {
            // Don't want this guy getting in our way.
            cat.debug("modelWasAddedNotification: " + ((EOModel)n.object()).name());
            NSNotificationCenter.defaultCenter().removeObserver((EOModel)n.object());
            ERXEntityClassDescription.registerDescriptionForEntitiesInModel((EOModel)n.object());
        }
        public void classDescriptionNeededForEntityName(NSNotification n) {
            cat.debug("classDescriptionNeededForEntityName: " + (String)n.object());
            String name = (String)n.object();
            EOEntity e = EOModelGroup.defaultGroup().entityNamed(name); //FIXME: This isn't the best way to get
            ERXEntityClassDescription.registerDescriptionForEntity(e);
        }
        public void classDescriptionNeededForClass(NSNotification n) {
            Class c = (Class)n.object();
            cat.debug("classDescriptionNeededForClass: " + c.getName());
            ERXEntityClassDescription.registerDescriptionForClass(c);
        }
    }
    
    private static boolean _registered = false;
    public static void registerDescription() {
        if (!_registered) {
            Observer observer=new Observer();
            ERXRetainer.retain(observer);
            // Need to be able to preempt the model registering descriptions.
            NSNotificationCenter.defaultCenter().addObserver(observer,
                                                             new NSSelector("modelWasAddedNotification", ERXConstant.NotificationClassArray),
                                                             EOModelGroup.ModelAddedNotification,
                                                             null);
            NSNotificationCenter.defaultCenter().addObserver(observer,
                                                             new NSSelector("classDescriptionNeededForEntityName",
                                                                            ERXConstant.NotificationClassArray),
                                                             EOClassDescription.ClassDescriptionNeededForEntityNameNotification,
                                                             null);
            NSNotificationCenter.defaultCenter().addObserver(observer,
                                                             new NSSelector("classDescriptionNeededForClass", ERXConstant.NotificationClassArray),
                                                             EOClassDescription.ClassDescriptionNeededForClassNotification,
                                                             null);
            _registered = true;
        }
    }

    private static NSMutableArray _registeredModelNames = new NSMutableArray();
    private static NSMutableDictionary _entitiesForClass = new NSMutableDictionary();

    public static void registerDescriptionForEntitiesInModel(EOModel model) {
        if (!_registeredModelNames.containsObject(model.name())) {
            for (Enumeration e = model.entities().objectEnumerator(); e.hasMoreElements();) {
                EOEntity eoentity = (EOEntity)e.nextElement();
                String className = eoentity.className();
                if(className.equals("EOGenericRecord")) {
                    className = ERXGenericRecord.class.getName();
                    eoentity.setClassName(className);
                    cat.debug(eoentity.name() + ": setting class from EOGenericRecord to " + className);
                }
                if(cat.isDebugEnabled())
                    cat.debug("Adding entity " +eoentity.name()+ " with class " + eoentity.className());
                
                NSMutableArray array = (NSMutableArray)_entitiesForClass.objectForKey(className);
                if(array == null) {
                    array = new NSMutableArray();
                }
                array.addObject(eoentity);
                _entitiesForClass.setObjectForKey(array, eoentity.className());
                //HACK ALERT: (ak) We work around classDescriptionForNewInstances() of EOEntity being broken here...
                registerDescriptionForEntity(eoentity);
            }
            _registeredModelNames.addObject(model.name());
        }
        // Don't want this guy getting in our way later on ;)
        NSNotificationCenter.defaultCenter().removeObserver(model);
    }

    public static void registerDescriptionForEntity(EOEntity entity) {
        try {
            Class entityClass = entity.className().equals("EOGenericRecord") ? EOGenericRecord.class : Class.forName(entity.className());
            if (cat.isDebugEnabled())
                cat.debug("Registering description for entity: " + entity.name() + " with class: " + entity.className());
            ERXEntityClassDescription cd = new ERXEntityClassDescription(entity);
            EOClassDescription.registerClassDescription(cd, entityClass);
            _setClassDescriptionOnEntity(entity, cd);
        } catch (java.lang.ClassNotFoundException ex) {
            cat.error("Invalid class name for entity: " + entity.name() + " exception: " + ex);
        }
    }
    static void _setClassDescriptionOnEntity(EOEntity entity, ERXEntityClassDescription cd)  {
        try {
            //HACK ALERT: (ak) We push the cd rather rudely into the entity to have it ready when classDescriptionForNewInstances() is called on it. We will have to add a com.webobjects.eoaccess.KVCProtectedAccessor to make this work
            NSKeyValueCoding.Utility.takeValueForKey(entity, cd, "classDescription");
        } catch(Exception ex) {
            cat.warn("_setClassDescriptionOnEntity: " + ex);
        }
    }
    // What we do here is go ahead and register all of the entities mapped onto this class, except for EOGenericRecord.
    public static void registerDescriptionForClass(Class class1) {
        NSArray entities = (NSArray)_entitiesForClass.objectForKey(class1.getName());
        if (entities != null) {
            if (cat.isDebugEnabled())
                cat.debug("Registering descriptions for class: " + class1.getName() + " found entities: " + entities.valueForKey("name"));
            for (Enumeration e = entities.objectEnumerator(); e.hasMoreElements();) {
                EOEntity entity = (EOEntity)e.nextElement();
                ERXEntityClassDescription cd = new ERXEntityClassDescription(entity);
                EOClassDescription.registerClassDescription(cd, class1);
                _setClassDescriptionOnEntity(entity, cd);
            }
        } else {
            cat.error("Unable to register descriptions for class: " + class1.getName());
        }
    }
    
    public ERXEntityClassDescription(EOEntity entity) { super(entity); }

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
}
