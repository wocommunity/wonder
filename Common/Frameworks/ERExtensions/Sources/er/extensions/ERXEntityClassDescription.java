/*
 * Copyright (C) NetStruxr, Inc. All rights reserved.
 *
 * This software is published under the terms of the NetStruxr
 * Public Software License version 0.5, a copy of which has been
 * included with this distribution in the LICENSE.NPL file.  */
package er.extensions;

import java.io.File;
import java.lang.reflect.Constructor;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.Enumeration;

import org.apache.log4j.Logger;

import com.webobjects.eoaccess.EOAttribute;
import com.webobjects.eoaccess.EOEntity;
import com.webobjects.eoaccess.EOEntityClassDescription;
import com.webobjects.eoaccess.EOModel;
import com.webobjects.eoaccess.EOModelGroup;
import com.webobjects.eoaccess.EORelationship;
import com.webobjects.eocontrol.EOClassDescription;
import com.webobjects.eocontrol.EOEditingContext;
import com.webobjects.eocontrol.EOEnterpriseObject;
import com.webobjects.eocontrol.EOGenericRecord;
import com.webobjects.eocontrol.EOGlobalID;
import com.webobjects.eocontrol.EOKeyGlobalID;
import com.webobjects.eocontrol.EOQualifier;
import com.webobjects.eocontrol.EOQualifierEvaluation;
import com.webobjects.foundation.NSArray;
import com.webobjects.foundation.NSDictionary;
import com.webobjects.foundation.NSForwardException;
import com.webobjects.foundation.NSKeyValueCoding;
import com.webobjects.foundation.NSKeyValueCodingAdditions;
import com.webobjects.foundation.NSMutableArray;
import com.webobjects.foundation.NSMutableDictionary;
import com.webobjects.foundation.NSNotification;
import com.webobjects.foundation.NSNotificationCenter;
import com.webobjects.foundation.NSPropertyListSerialization;
import com.webobjects.foundation.NSSelector;
import com.webobjects.foundation.NSTimestamp;
import com.webobjects.foundation.NSValidation;

import er.extensions.partials.ERXPartial;

/**
 * The main purpose of the ERXClassDescription class is
 * to throw {@link ERXValidationException}s instead of the
 * usual {@link com.webobjects.foundation.NSValidation.ValidationException 
 * NSValidation.ValidationException} objects. See the
 * ERXValidationException and ERXValidationFactory class
 * for more information about localized and templatized
 * validation exceptions. This class is configured to
 * register itself as the class description by calling
 * the method <code>registerDescription</code>. This method
 * is called when the principal class of this framework is
 * loaded. This happens really early so you shouldn't have
 * to worry about this at all.<br/>
 * <br />
 * Additionally, this class allows for model driven validations in a "poor-mans-Validity-way":
 * add a <code>ERXValidation</code> user info entry on your entity.
 * This is an example:<code><pre>
 * "ERXValidation" = {
 *     // these keys are evaluated on validateForSave, they don't correspond to properties
 *     additionalValidationKeys = ("validateEmailPassword");
 *
 *     // This dictionary holds the keys to use for validating properties
 *     validateForKey =
 *     {
 *
 *         // these keys are evaluated on validateForSave, they don't correspond to properties
 *         email =
 *         (
 *          {
 *              // this is the message code into ValidationStrings.plist
 *              //    User.email.wrongLength = "The mail does not have the right size (5 to 50)";
 *              message = "wrongLength";
 *              
 *              // skip this rule if the value is null
 *              ignoreIfNull = true;
 *              
 *              // if there is a qualifier key, then a dictionary containing "object" and "value" is evaluated 
 *              // and an exception is thrown if the evaluation returns false
 *              qualifier = "(value.length >= 5) AND (value.length < 50)";
 *          },
 *          {
 *              // again, this is the message code into ValidationStrings.plist
 *              message = "sampleTest";
 *
 *              // Given this key, an object of the corresponding EOQualifierEvaluation subclass is created 
 *              // and given this dictionary on creation. This object needs to be re-entrant.
 *              className = "SampleTest";
 *              // an example is:
 *              // public class SampleTest implements EOQualifierEvaluation {
 *              //	int minLength, maxLength;
 *              //	public SampleTest(Object param) {
 *              //		NSDictionary dict = (NSDictionary)param;
 *              //		minLength = ERXValueUtilities.intValue(dict.objectForKey("minLength"));
 *              //		maxLength = ERXValueUtilities.intValue(dict.objectForKey("maxLength"));
 *              //	}
 *              //	public boolean evaluateObject(Object o) {
 *              //		ERXEntityClassDescription.ValidationObjectValue val
 *              //		  = (ERXEntityClassDescription.ValidationObjectValue)o;
 *              //		EOEnterpriseObject eo = val.object();
 *              //		String value = (String)val.value();
 *              //		return value.length() >= minLength && value.length() <= maxLength;
 *              //	}
 *              // }
 *
 *              minLength = "5";
 *              maxLength = "10";
 *          }
 *          );
 *
 *         // This key does not correspond to any property, it get's evaluated in D2WApps where you have a 
 *         // multi-step page and need to do validation before validateForSave
 *         "validateEmailPassword" =
 *             (
 *              {
 *                  message = "stupidTestWithEmailAndPassword";
 *
 *                  // means to get D2W to highlight the fields involved instead of only displaying the message
 *                  // For this to work, your corresponding localized String should be
 *                  //   User.email,password.stupidTestWithEmailAndPassword = "Stupid test failed";
 *                  keyPaths = "email,password";
 *                  
 *                  qualifier = "(object.email.length >= object.password.length)";
 *              }
 *              );
 *     };
 *
 *     // These get checked when the object gets saved, additionally to "additionalValidations"
 *     // The structure of "validateForInsert", "validateForUpdate" and "validateForDelete" is the same.
 *     validateForSave =
 *         (
 *          {
 *              message = "cantBeBoth";
 *
 *              keyPaths = "isEditor,isAdmin";
 *
 *              qualifier = "(object.isEditor = 'Y' and object.isAdmin = 'Y')";
 *          }
 *          );
 * }</pre></code>
 * This code is mainly a quick-and-dirty rewrite from PRValidation by Proteon.
 * <br/>
 * Additionally, this class adds a concept of "Default" values that get pushed into the object at creation time.
 * Simply add a "ERXDefaultValues" key into the entity's userInfo dictionary that contains key-value-pairs for every default you want to set. Alternately, you can set a "default" key on each of the relationship or attrbute's userInfo<br />
 * Example:<pre><code>
 * "ERXDefaultValues" = {
 *
 *     // Example for simple values.
 *     isAdmin = N;
 *     isEditor = Y;
 *
 *     // Example for a related object (->Languages(pk,displayName)). You need to enter the primary key value.
 *     language = "de";
 *
 *     // Example for an NSArray of related objects
 *     recommendingUser = "@threadStorage.actor";
 *
 *     // Example for an NSArray
 *     articlesToRevisit = "@threadStorage.actor.articles";
 *
 *     // Example for a NSTimestamp. All static methods from ERXTimestampUtilities are supported.
 *     created = "@now";
 *     updatePassword = "@tomorrow";
 *
 * }</pre></code>
 * <br/>
 * If you wish to provide your own class description subclass
 * see the documentation associated with the Factory inner class.
 */

public class ERXEntityClassDescription extends EOEntityClassDescription {

    /** logging support */
    public static final Logger log = Logger.getLogger(ERXEntityClassDescription.class);

    /** validation logging support */
    public static final Logger validationLog = Logger.getLogger("er.validation.ERXEntityClassDescription");

    /** default logging support */
    public static final Logger defaultLog = Logger.getLogger("er.default.ERXEntityClassDescription");

    /** Holds validation info from the entities user info dictionary */
    protected NSDictionary _validationInfo;

    /** Holds validation qualifiers */
    protected NSMutableDictionary  _validationQualiferCache;

    /** Holds default values */
    protected NSMutableDictionary _initialDefaultValues;

    /** Holds the default factory instance */
    private static Factory _factory;
    
    /** holds validity Methods */
    private static Method[] validityMethods = null;

    /** index of validity save method */ 
    private static int VALIDITY_SAVE = 0;

    /** index of validity delete method */ 
    private static int VALIDITY_DELETE = 1;

    /** index of validity insert method */
    private static int VALIDITY_INSERT = 2;

    /** index of validity update method */
    private static int VALIDITY_UPDATE = 3;

    /** the shared validity engine instance as Object to eliminate compile errors
        * if validity is not linked and should not be used
        */ 
    private static Object sharedGSVEngineInstance;

    /** Boolean that gets initialized on first use to indicate if validity should
        * be used or not, remember that the call System.getProperty acts synchronized
        * so this saves some time in multithreaded apps.
        */
    private static Boolean useValidity;
    

    /**
     * This factory inner class is registered as the observer
     * for three notifications: modelWasAdded, classDescriptionNeededForEntity
     * and classDescriptionNeededForClass. If you wish to provide your own
     * subclass of ERXEntityClassDescription then you need to create a
     * subclass of Factory and set that class name in the system properties
     * under the key: <code>er.extensions.ERXClassDescription.factoryClass</code>
     * In your Factory subclass override the method: newClassDescriptionForEntity
     * to provide your own ERXEntityClassDescription subclass.
     */
    public static class Factory {
    	
    	/** Public constructor */
    	public Factory() {
    		// Need to be able to preempt the model registering descriptions.
    		NSNotificationCenter.defaultCenter().addObserver(this, new NSSelector("modelWasAdded", ERXConstant.NotificationClassArray), EOModelGroup.ModelAddedNotification, null);
    		NSNotificationCenter.defaultCenter().addObserver(this, new NSSelector("modelGroupWasAdded", ERXConstant.NotificationClassArray), ERXModelGroup.ModelGroupAddedNotification, null);
    		NSNotificationCenter.defaultCenter().addObserver(this, new NSSelector("classDescriptionNeededForEntityName", ERXConstant.NotificationClassArray), EOClassDescription.ClassDescriptionNeededForEntityNameNotification, null);
    		NSNotificationCenter.defaultCenter().addObserver(this, new NSSelector("classDescriptionNeededForClass", ERXConstant.NotificationClassArray), EOClassDescription.ClassDescriptionNeededForClassNotification, null);
    	}

        public void reset() {
            _registeredModelNames = new NSMutableArray();
            _entitiesForClass = new NSMutableDictionary();
            _classDescriptionForEntity = new NSMutableDictionary();
        }

        protected boolean isRapidTurnaroundEnabled() {
            return ERXProperties.booleanForKey("er.extensions.ERXEntityClassDescription.isRapidTurnaroundEnabled");
        }

        protected boolean isFixingRelationshipsEnabled() {
            return ERXProperties.booleanForKey("er.extensions.ERXEntityClassDescription.isFixingRelationshipsEnabled");
        }

        /**
         * Method called when a model group did load.
         */
        public final void modelGroupWasAdded(NSNotification n) {
            log.debug("modelGroupWasAdded: " + n);
            EOModelGroup group = (EOModelGroup) n.object();
            processModelGroup(group);
        }

        /**
         * Called when a model group finished loading. Checks foreign keys by default. Override to to more...
         * @param group
         */
		protected void processModelGroup(EOModelGroup group) {
			for (Enumeration ge = group.models().objectEnumerator(); ge.hasMoreElements();) {
                EOModel model = (EOModel)ge.nextElement();
                String frameworkName = null;
                String modelPath = null;
                log.debug("ApplicationDidFinishLaunching: " + model.name());
                
                if(isRapidTurnaroundEnabled()) {
                    for(Enumeration e = NSArray.componentsSeparatedByString(model.pathURL().getFile(), File.separator).reverseObjectEnumerator(); e.hasMoreElements(); ) {
                        String a = (String)e.nextElement();
                        if(a.indexOf(".framework") > 0) {
                            frameworkName = a.substring(0, a.indexOf(".framework"));
                            break;
                        }
                    }
                    if(frameworkName == null) {
                        frameworkName = "app";
                    }
                    modelPath = ERXFileUtilities.pathForResourceNamed(model.name() + ".eomodeld", frameworkName, null);
                    defaultLog.debug("Path for model <" + model.name() + "> in framework <"+frameworkName+">: " + modelPath);
                }
                                
                for (Enumeration ee = model.entities().objectEnumerator(); ee.hasMoreElements();) {
                    EOEntity entity = (EOEntity)ee.nextElement();
                    checkForeignKeys(entity);
                    EOClassDescription cd = EOClassDescription.classDescriptionForEntityName(entity.name());
                    defaultLog.debug("Reading defaults for: " + entity.name());
                    if(cd instanceof ERXEntityClassDescription) {
                        ((ERXEntityClassDescription)cd).readDefaultValues();

                        if(isRapidTurnaroundEnabled() && modelPath != null) {
                            String path = modelPath + File.separator + entity.name() + ".plist";
                            ERXFileNotificationCenter.defaultCenter().addObserver(cd, new NSSelector("modelFileDidChange", ERXConstant.NotificationClassArray), path);
                        }
                    } else {
                        defaultLog.warn("Entity classDescription is not ERXEntityClassDescription: " + entity.name());
                    }
                }
            }
		}
        
        /**
         * Method called by the {@link com.webobjects.foundation.NSNotificationCenter NSNotificationCenter}   
         * when an EOModel is loaded. 
         * This method just calls the method
         * <code>registerDescriptionForEntitiesInModel</code>
         * 
         * @param n notification that has the EOModel that was loaded.
         */
        public final void modelWasAdded(NSNotification n) {
        	EOModel model = ((EOModel)n.object());
            log.debug("ModelWasAddedNotification: " + model.name());
            // Don't want this guy getting in our way.
            NSNotificationCenter.defaultCenter().removeObserver(model);
            try {
                registerDescriptionForEntitiesInModel(model);
            } catch (RuntimeException e) {
                log.error("Error registering model: " + model.name(), e);
                throw e;
            }
        }

        /**
         * Method called by the {@link com.webobjects.foundation.NSNotificationCenter NSNotificationCenter} 
         * when a class description is needed 
         * for a given entity. Usually this method isn't needed seeing 
         * as we preempt the on demand loading of class descriptions 
         * by loading all of them when the EOModel is loaded.  
         * This method just calls the method
         * <code>registerDescriptionForEntity</code>
         *
         * @param n notification that has the name of the entity
         * 	that needs the class description.
         */
        public void classDescriptionNeededForEntityName(NSNotification n) {
            log.debug("classDescriptionNeededForEntityName: " + (String)n.object());
            String name = (String)n.object();
            EOEntity e = ERXEOAccessUtilities.entityNamed(null,name);
            if(e == null) log.error("Entity " + name + " not found in the default model group!");
            registerDescriptionForEntity(e);
        }

        /**
         * Method called by the {@link com.webobjects.foundation.NSNotificationCenter NSNotificationCenter} 
         * when a class description is needed 
         * for a given Class. Usually this method isn't needed seeing 
         * as we preempt the on demand loading of class descriptions 
         * by loading all of them when the EOModel is loaded.  
         * This method just calls the method
         * <code>registerDescriptionForClass</code>
         * @param n notification that has the Class object
         * 	that needs a class description.
         */
        public void classDescriptionNeededForClass(NSNotification n) {
            Class c = (Class)n.object();
            log.debug("classDescriptionNeededForClass: " + c.getName());
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
        protected ERXEntityClassDescription newClassDescriptionForEntity(EOEntity entity) {
        	String key = entity.name();
        	EOModel model = entity.model();
        	if (model != null) {
        		key = model.name() + " " + key;
        	}
        	ERXEntityClassDescription classDescription = (ERXEntityClassDescription)_classDescriptionForEntity.objectForKey(key);
        	if (classDescription == null) {
        		classDescription = new ERXEntityClassDescription(entity);
        		_classDescriptionForEntity.setObjectForKey(classDescription, key);
        	}
        	return classDescription;
        }

        /** holds a reference to all of the registered model names */
        private NSMutableArray _registeredModelNames = new NSMutableArray();
        /** holds a mapping of class to entities */
        private NSMutableDictionary _entitiesForClass = new NSMutableDictionary();
        /** holds a mapping of entity to class descrpitions */
        private NSMutableDictionary _classDescriptionForEntity = new NSMutableDictionary();

        /**
         * Allows for entities to be altered
         * before they have a custom class description
         * registered. Sub classes can override this method
         * to provide any extra alterings before the description
         * is registered. However be sure to call super as this
         * method does convert the class name from EOGenericRecord
         * to ERXGenericRecord, which unfortunately is required
         * for custom validation to work at the moment.
         * @param eoentity to be prepared for registration
         */
        protected void prepareEntityForRegistration(EOEntity eoentity) {
            String className = eoentity.className();
            String defaultClassName = ERXProperties.stringForKeyWithDefault("er.extensions.ERXEntityClassDescription.defaultClassName", ERXGenericRecord.class.getName());
            String alternateClassName = ERXProperties.stringForKey("er.extensions.ERXEntityClassDescription." + eoentity.name() + ".ClassName");
            if (alternateClassName != null) {
                log.debug(eoentity.name() + ": setting class from: " + className + " to: " + alternateClassName);
                eoentity.setClassName(alternateClassName);
            } else if (className.equals("EOGenericRecord")) {
                eoentity.setClassName(defaultClassName);
            }
        }

        /**
         * Handles errors when an optional relationship has a source attribute
         * that is set to allow null values. Subclasses can override this to do more specific handling.
         */
        protected void handleOptionalRelationshipError(EOEntity eoentity, EORelationship relationship, EOAttribute attribute) {
            if(isFixingRelationshipsEnabled()) {
                relationship.setIsMandatory(true);
                log.info(eoentity.name() + ": relationship '"
                         + relationship.name() + "' was switched to mandatory, because the foreign key '"
                         + attribute.name() + "' does NOT allow NULL values");
            } else {
                log.warn(eoentity.name() + ": relationship '"
                          + relationship.name() + "' is marked to-one and optional, but the foreign key '"
                          + attribute.name() + "' does NOT allow NULL values");
            }
        }

        /**
         * Handles errors when a mandatory relationship has a source attribute
         * that is set to not allow null values. Subclasses can override this to do more specific handling.
         */
        protected void handleMandatoryRelationshipError(EOEntity eoentity, EORelationship relationship, EOAttribute attribute) {
            if(isFixingRelationshipsEnabled()) {
                relationship.setIsMandatory(false);
                log.info(eoentity.name() + ": relationship '"
                          + relationship.name() + "' was switched to optional, because the foreign key '"
                          + attribute.name() + "' allows NULL values");
            } else {
                log.warn(eoentity.name() + ": relationship '"
                          + relationship.name() + "' is marked to-one and mandatory, but the foreign key '"
                          + attribute.name() + "' allows NULL values");
            }
        }

        /**
         * Checks for foreign keys that are <code>NOT NULL</code>,
         * but whose relationship is marked as non-mandatory and vice-versa. This
         * error is not checked by EOModeler, so we do it here.
         * @param eoentity to be check
         */
        public void checkForeignKeys(EOEntity eoentity) {
            NSArray primaryKeys = eoentity.primaryKeyAttributes();
            for(Enumeration relationships = eoentity.relationships().objectEnumerator(); relationships.hasMoreElements(); ) {
                EORelationship relationship = (EORelationship)relationships.nextElement();
                if(!relationship.isToMany()) {
                    if(relationship.isMandatory()) {
                        for(Enumeration attributes = relationship.sourceAttributes().objectEnumerator(); attributes.hasMoreElements(); ) {
                            EOAttribute attribute = (EOAttribute)attributes.nextElement();
                            if(attribute.allowsNull()) {
                                handleMandatoryRelationshipError(eoentity, relationship, attribute);
                            }
                        }
                    } else {
                        for(Enumeration attributes = relationship.sourceAttributes().objectEnumerator(); attributes.hasMoreElements(); ) {
                            EOAttribute attribute = (EOAttribute)attributes.nextElement();
                            if(!attribute.allowsNull() && !primaryKeys.containsObject(attribute)) {
                                handleOptionalRelationshipError(eoentity, relationship, attribute);
                            }
                        }
                    }
                }
            }
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
        protected void registerDescriptionForEntitiesInModel(EOModel model) {
            if (!_registeredModelNames.containsObject(model.name())) {
                for (Enumeration e = model.entities().objectEnumerator(); e.hasMoreElements();) {
                    EOEntity eoentity = (EOEntity)e.nextElement();
                    String className = eoentity.className();

                    prepareEntityForRegistration(eoentity);

                    NSMutableArray array = (NSMutableArray)_entitiesForClass.objectForKey(className);
                    if(array == null) {
                        array = new NSMutableArray();
                    }
                    if (log.isDebugEnabled())
                        log.debug("Adding entity " +eoentity.name()+ " with class " + eoentity.className());
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
                log.warn("_setClassDescriptionOnEntity: " + ex);
            }
        }

        /**
         * Registers a custom class description for the given
         * entity using the method <code>newClassDescriptionForEntity</code>
         * which can be overridden by subclasses to provide a
         * different class description subclass.
         * @param entity to register the class description for
         */
        protected void registerDescriptionForEntity(EOEntity entity) {
            Class entityClass = EOGenericRecord.class;
            try {
                String className = entity.className();
                if (log.isDebugEnabled())
                    log.debug("Registering description for entity: " + entity.name() + " with class: " + className);
                entityClass = className.equals("EOGenericRecord") ? EOGenericRecord.class : Class.forName(className);
            } catch (java.lang.ClassNotFoundException ex) {
                log.warn("Invalid class name for entity: " + entity.name() + " exception: " + ex + " using " + entityClass.getName() + " instead");
                entity.setClassName("EOGenericRecord");
            }
            ERXEntityClassDescription cd = newClassDescriptionForEntity(entity);
            EOClassDescription.registerClassDescription(cd, entityClass);
            _setClassDescriptionOnEntity(entity, cd);
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
        protected void registerDescriptionForClass(Class class1) {
            NSArray entities = (NSArray)_entitiesForClass.objectForKey(class1.getName());
            if (entities != null) {
                if (log.isDebugEnabled())
                    log.debug("Registering descriptions for class: " + class1.getName() + " found entities: " + entities.valueForKey("name"));
                for (Enumeration e = entities.objectEnumerator(); e.hasMoreElements();) {
                    EOEntity entity = (EOEntity)e.nextElement();
                    ERXEntityClassDescription cd = newClassDescriptionForEntity(entity);
                    EOClassDescription.registerClassDescription(cd, class1);
                    _setClassDescriptionOnEntity(entity, cd);
                }
            } else {
            	if(class1.getName().indexOf('$') < 0) {
            		log.error("Unable to register descriptions for class: " + class1.getName(), new RuntimeException("Dummy"));
            	}
            }
        }
        
    }

    /** getter for the factory */
    public static Factory factory() {
        return _factory;
    }

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
    	if (_factory == null) {
    		_factory = null;
    		try {
    			String className = System.getProperty("er.extensions.ERXClassDescription.factoryClass");
    			if (className != null) {
    				_factory = (Factory)Class.forName(className).newInstance();
    			}
    		} catch(Exception ex) {
    			log.warn("Exception while registering factory, using default: " + ex );
    		}

    		if(_factory == null)
    			_factory=new Factory();
    	}
    }
    
    /**
     * Public constructor
     * @param entity that this class description corresponds to
     */
    public ERXEntityClassDescription(EOEntity entity) {
        super(entity);
        _validationInfo = ERXValueUtilities.dictionaryValue(entity.userInfo().objectForKey("ERXValidation"));
        _validationQualiferCache = ERXMutableDictionary.synchronizedDictionary();
    }

    public void modelFileDidChange(NSNotification n) {
        File file = (File)n.object();
        try {
            defaultLog.debug("Reading .plist for entity <"+entity()+">");

            NSDictionary userInfo = (NSDictionary)NSPropertyListSerialization.propertyListFromString(ERXFileUtilities.stringFromFile(file));
            entity().setUserInfo((NSDictionary)userInfo.objectForKey("userInfo"));
            
            _validationInfo = ERXValueUtilities.dictionaryValue(entity().userInfo().objectForKey("ERXValidation"));
            _validationQualiferCache = ERXMutableDictionary.synchronizedDictionary();
            _initialDefaultValues = null;
            readDefaultValues();
        } catch(Exception ex) {
            defaultLog.error("Can't read file <"+file.getAbsolutePath()+">", ex);
        }
    }
    
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
            if (useValidity()) {
                invokeValidityMethodWithType(VALIDITY_DELETE, obj);
            }
            super.validateObjectForDelete(obj);
            validateObjectWithUserInfo(obj, null, "validateForDelete", "validateForDelete");
        } catch (ERXValidationException eov) {
            throw eov;
        } catch (NSValidation.ValidationException eov) {
            if (log.isDebugEnabled())
                log.debug("Caught validation exception: " + eov);
            ERXValidationException erv = ERXValidationFactory.defaultFactory().convertException(eov, obj);
            throw (erv != null ? erv : eov);
        }
    }

    /**
     * Overridden to perform a check if the entity is still in a model group.
     * This can happen if you remove the entity, clone it to change things and re-add it afterwards.
     */
    public EOEntity entity() {
        checkEntity();
        return super.entity();
    }

    protected void checkEntity() {
        if(_entity.model() == null) {
            try {

                EOEntity registeredEntity = ERXEOAccessUtilities.entityNamed(null,_entity.name());

                if(registeredEntity != null) {
                    _entity = registeredEntity;
                } else {
                    EOModel model = _entity.model();
                    if(model == null) {
                        model = (EOModel)ERXEOAccessUtilities.modelGroup(null).models().lastObject();
                    }
                    model.addEntity(_entity);
                    log.warn("Added <" + _entity.name() + "> to default model group.");
                }
            } catch (Exception ex) {
                throw new RuntimeException("Model or modelgroup for <" + _entity.name() + "> is null: " + entity().model(), ex);
            }
        }
    }
    
    public EOEnterpriseObject createInstanceWithEditingContext(EOEditingContext ec, EOGlobalID gid) {
        checkEntity();
        return super.createInstanceWithEditingContext(ec, gid);
    }
    
    /**
     * This method is called when an object is
     * about to be updated. If any validation
     * exceptions occur they are converted to an
     * {@link ERXValidationException} and that is
     * thrown.
     * @param obj enterprise object to be deleted
     * @throws validation exception
     */
    public void validateObjectForUpdate(EOEnterpriseObject obj) throws NSValidation.ValidationException {
        try {
            if (useValidity()) {
                invokeValidityMethodWithType(VALIDITY_UPDATE, obj);
            }
            validateObjectWithUserInfo(obj, null, "validateForUpdate", "validateForUpdate");
        } catch (ERXValidationException eov) {
            throw eov;
        } catch (NSValidation.ValidationException eov) {
            if (log.isDebugEnabled())
                log.debug("Caught validation exception: " + eov);
            ERXValidationException erv = ERXValidationFactory.defaultFactory().convertException(eov, obj);
            throw (erv != null ? erv : eov);
        }
    }
    
    /**
     * This method is called when an object is
     * about to be inserted. If any validation
     * exceptions occur they are converted to an
     * {@link ERXValidationException} and that is
     * thrown.
     * @param obj enterprise object to be deleted
     * @throws validation exception
     */
    public void validateObjectForInsert(EOEnterpriseObject obj) throws NSValidation.ValidationException {
        try {
            if (useValidity()) {
                invokeValidityMethodWithType(VALIDITY_INSERT, obj);
            }
            validateObjectWithUserInfo(obj, null, "validateForInsert", "validateForInsert");
        } catch (ERXValidationException eov) {
            throw eov;
        } catch (NSValidation.ValidationException eov) {
            if (log.isDebugEnabled())
                log.debug("Caught validation exception: " + eov);
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
        if (log.isDebugEnabled())
            log.debug("Validate value: " + obj + " for key: " + s);
        try {
            if(obj instanceof ERXConstant) {
                validated = obj;
            } else {
                validated = super.validateValueForKey(obj, s);
            }
        } catch (ERXValidationException eov) {
            throw eov;
        } catch (NSValidation.ValidationException eov) {
            if (log.isDebugEnabled())
                log.debug("Caught validation exception: " + eov);
            ERXValidationException erv = ERXValidationFactory.defaultFactory().convertException(eov, obj);
            throw (erv != null ? erv : eov);
        }
        return validated;
    }

    /**
     * This method is called when an object is
     * about to be saved. Adds support for extra validation keys to
     * be set in an array in the entity's userInfo under the keypath
     * <code>ERXValidation.additionalValidationKeys</code>. If any validation
     * exceptions occur they are converted to an
     * {@link ERXValidationException} and that is
     * thrown. 
     * @param obj enterprise object to be saved
     * @throws validation exception
     */

    public void validateObjectForSave(EOEnterpriseObject obj) throws NSValidation.ValidationException {
        try {
            if (useValidity()) {
                invokeValidityMethodWithType(VALIDITY_SAVE, obj);
            }
            
            if(_validationInfo != null) {
                NSArray additionalValidationKeys = (NSArray)_validationInfo.objectForKey("additionalValidationKeys");
                if(additionalValidationKeys != null) {
                    for(Enumeration e = additionalValidationKeys.objectEnumerator(); e.hasMoreElements();) {
                        String key = (String)e.nextElement();
                        NSSelector selector = new NSSelector(key);
                        if(selector.implementedByObject(obj)) {
                            try {
                                selector.invoke(obj);
                            } catch (Exception ex) {
                                if(ex instanceof NSValidation.ValidationException)
                                    throw (NSValidation.ValidationException)ex;
                                log.error(ex);
                            }
                        } else {
                            validateObjectWithUserInfo(obj, null, "validateForKey." + key, key);
                        }
                    }
                }
            }
            validateObjectWithUserInfo(obj, null, "validateForSave", "validateForSave");
        } catch (ERXValidationException eov) {
            throw eov;
        } catch (NSValidation.ValidationException eov) {
            if (log.isDebugEnabled())
                log.debug("Caught validation exception: " + eov);
            ERXValidationException erv = ERXValidationFactory.defaultFactory().convertException(eov, obj);
            throw (erv != null ? erv : eov);
        }
    }

    public static class ValidationObjectValue {
        protected EOEnterpriseObject object;
        protected Object value;
        public ValidationObjectValue(EOEnterpriseObject object, Object value) {
            this.object = object;
            this.value = value;
        }
        public Object value() { return value;}
        public EOEnterpriseObject object() { return object;}
    }
    public static class QualiferValidation implements EOQualifierEvaluation {
        protected EOQualifier qualifier;
        public QualiferValidation(Object info) {
            NSDictionary dict =(NSDictionary)info;
            qualifier = EOQualifier.qualifierWithQualifierFormat((String)dict.objectForKey("qualifier"), null);
        }
        public boolean evaluateWithObject(Object o) {
            return qualifier.evaluateWithObject(o);
        }
    }
    
    protected boolean validateObjectValueDictWithInfo(ValidationObjectValue values, NSDictionary info, String cacheKey) {
        EOQualifierEvaluation q = (EOQualifierEvaluation)_validationQualiferCache.objectForKey(cacheKey);
        if(q == null) {
            try {
                String className = (String)info.objectForKey("className");
                if(className == null) {
                    className = QualiferValidation.class.getName();
                }
                Class cl = ERXPatcher.classForName(className);
                Constructor co = cl.getConstructor(new Class [] {Object.class});
                Object o = co.newInstance(new Object[] {info});
                q = (EOQualifierEvaluation)o;
            } catch(Exception ex) {
                throw new NSForwardException(ex);
            }
            _validationQualiferCache.setObjectForKey(q, cacheKey);
        }
        if(values.value() == null && "true".equals(info.objectForKey("ignoreIfNull")))
           return true;
        if(q != null)
            return q.evaluateWithObject(values);
        return true;
    }

    public void validateObjectWithUserInfo(EOEnterpriseObject object, Object value, String validationTypeString, String property) {
        if(_validationInfo != null) {
            NSArray qualifiers = (NSArray)_validationInfo.valueForKeyPath(validationTypeString);
            if(qualifiers != null) {
                ValidationObjectValue values = new ValidationObjectValue(object, value);
                int i = 0;
                for(Enumeration e = qualifiers.objectEnumerator(); e.hasMoreElements();) {
                    NSDictionary info = (NSDictionary)e.nextElement();
                    if(validationLog.isDebugEnabled())
                        validationLog.debug("Validate " + validationTypeString +"."+ property +" with <"+ value + "> on " + object + "\nRule: " + info);
                    if(!validateObjectValueDictWithInfo(values, info, validationTypeString+property+i)) {
                        String message = (String)info.objectForKey("message");
                        String keyPaths = (String)info.objectForKey("keyPaths");
                        property = keyPaths == null ? property : keyPaths;
                        if(validationLog.isDebugEnabled())
                            validationLog.info("Validation failed " + validationTypeString +"."+ property +" with <"+ value + "> on " + object);
                        throw ERXValidationFactory.defaultFactory().createException(object, property, value,message);
                    }
                    i = i+1;
                }
            }
        }
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


    /**** default handling */

    // Default handling from here on
    protected String defaultKey = "default";

    public static interface Default {
        public static final int AdaptorNumberType = 0;
        public static final int AdaptorCharactersType = 1;
        public static final int AdaptorBytesType = 2;
        public static final int AdaptorDateType = 3;

        public void setValueInObject(EOEnterpriseObject eo);
    }

    public static class AttributeDefault implements Default {
        String key;
        String stringValue;
        int adaptorType;
        EOAttribute attribute;
        
        public AttributeDefault(EOAttribute attribute, String stringValue) {
            this(attribute.name(), stringValue, attribute.adaptorValueType());
            this.attribute = attribute;
        }
        
        public AttributeDefault(String key, String stringValue, int adaptorType) {
            this.key = key;
            this.stringValue = stringValue;
            this.adaptorType = adaptorType;
        }

        public AttributeDefault(String key, String stringValue) {
            this(key, stringValue, AdaptorCharactersType);
        }

        public void setValueInObject(EOEnterpriseObject eo) {
            Object defaultValue = this.stringValue;
            if(stringValue.startsWith("@threadStorage.")) {
                String keyPath = stringValue.substring("@threadStorage.".length());
                defaultValue = ERXThreadStorage.valueForKeyPath(keyPath);
            } else {
                if(attribute != null && attribute.valueFactoryMethodName() != null && attribute.factoryMethodArgumentType() == EOAttribute.FactoryMethodArgumentIsString) {
                    defaultValue = attribute.newValueForString(stringValue);
                }
            }
            if(defaultValue != null) {
                String s = defaultValue.toString();
                s = s.substring(s.indexOf("@")+1);
                if(adaptorType == AdaptorDateType) {
                    defaultValue = ERXTimestampUtilities.timestampForString(s);
                } else if (adaptorType == AdaptorNumberType) {
                    NSTimestamp temp = ERXTimestampUtilities.timestampForString(s);
                    if(temp != null) {
                        defaultValue = ERXTimestampUtilities.unixTimestamp(temp);
                    } else {
                        //the value will be coerced by the eo...
                        defaultValue = ERXValueUtilities.bigDecimalValue(s);
                    }
                }
            }
            eo.takeValueForKey(defaultValue, key);
        }
    }

    public static class RelationshipDefault implements Default {
        String key;
        String stringValue;
        int adaptorType;
        String relationshipEntityName;

        public RelationshipDefault(String key, String stringValue, int adaptorType, String relationshipEntityName) {
            this.key = key;
            this.stringValue = stringValue;
            this.adaptorType = adaptorType;
            this.relationshipEntityName = relationshipEntityName;
        }

        public void setValueInObject(EOEnterpriseObject eo) {
            Object defaultValue = this.stringValue;
            EOEditingContext ec = eo.editingContext();

            if(stringValue.charAt(0) == '@') { // computed key
                if(stringValue.equals("@new")) {
                    EOClassDescription cd = EOClassDescription.classDescriptionForEntityName(relationshipEntityName);
                    EOEnterpriseObject newObject = cd.createInstanceWithEditingContext(eo.editingContext(), null);
                    ec.insertObject(newObject);
                    eo.addObjectToBothSidesOfRelationshipWithKey(newObject,key);
                } else if(stringValue.startsWith("@threadStorage.")) {
                    String keyPath = stringValue.substring("@threadStorage.".length());
                    Object o = ERXThreadStorage.valueForKey(keyPath);
                    if(keyPath.indexOf(".") > 0) {
                        keyPath = stringValue.substring(keyPath.indexOf(".")+1);
                        o = NSKeyValueCodingAdditions.Utility.valueForKeyPath(o, keyPath);
                    }
                    if(o != null) {
                        if(o instanceof EOEnterpriseObject) {
                            ERXEOControlUtilities.addObjectToObjectOnBothSidesOfRelationshipWithKey((EOEnterpriseObject)o, eo, key);
                        } else if(o instanceof NSArray) {
                            NSArray newObjects = (NSArray)o;
                            for(Enumeration e = newObjects.objectEnumerator(); e.hasMoreElements();) {
                                ERXEOControlUtilities.addObjectToObjectOnBothSidesOfRelationshipWithKey((EOEnterpriseObject)e.nextElement(), eo, key);
                            }
                        } else {
                            defaultLog.warn("setValueInObject: Object is neither an EO nor an array");
                        }
                    }
                }
            } else {
                if (adaptorType == AdaptorNumberType) {
                    defaultValue = new Integer(Integer.parseInt(stringValue));
                }
                EOGlobalID gid = EOKeyGlobalID.globalIDWithEntityName(relationshipEntityName, new Object[] {defaultValue});
                EOEnterpriseObject fault = ec.faultForGlobalID(gid,ec);
                eo.addObjectToBothSidesOfRelationshipWithKey(fault,key);
            }
        }
    }

    public void readDefaultValues() {
        if(_initialDefaultValues == null) {
            _initialDefaultValues = new NSMutableDictionary();

            EOEntity entity = entity();
            NSMutableDictionary dict = new NSMutableDictionary();
            NSDictionary entityInfo = (NSDictionary)entity.userInfo().objectForKey("ERXDefaultValues");

            for( Enumeration e = entity.attributes().objectEnumerator(); e.hasMoreElements();) {
                EOAttribute attr = (EOAttribute)e.nextElement();
                String defaultValue = null;

                if(attr.userInfo() != null)
                    defaultValue = (String)attr.userInfo().objectForKey(defaultKey);

                if(defaultValue == null && entityInfo != null) {
                    defaultValue = (String)entityInfo.objectForKey(attr.name());
                }
                if(defaultValue != null)
                    setDefaultAttributeValue(attr, defaultValue);
            }

            for( Enumeration e = entity.relationships().objectEnumerator(); e.hasMoreElements();) {
                EORelationship rel = (EORelationship)e.nextElement();
                String defaultValue = null;

                if(rel.userInfo() != null)
                    defaultValue = (String)rel.userInfo().objectForKey(defaultKey);

                if(defaultValue == null && entityInfo != null) {
                    defaultValue = (String)entityInfo.objectForKey(rel.name());
                }
                if(defaultValue != null)
                    setDefaultRelationshipValue(rel, defaultValue);
            }
        }
    }

    public void setDefaultAttributeValue(EOAttribute attr, String defaultValue) {
        String name = attr.name();
        defaultLog.debug("Adding: " + name + "-" + defaultValue);
        AttributeDefault d = new AttributeDefault(attr, defaultValue);
        _initialDefaultValues.setObjectForKey(d, name);
    }

    public void setDefaultRelationshipValue(EORelationship rel, String defaultValue) {
        String name = rel.name();
        defaultLog.debug("Adding: " + name + "-" + defaultValue);
        NSArray attrs = rel.destinationAttributes();
        if(!rel.isFlattened() && attrs !=  null && attrs.count() == 1) {
            EOAttribute relAttr = (EOAttribute)attrs.objectAtIndex(0);
            if(defaultValue != null) {
                RelationshipDefault d = new RelationshipDefault(name, defaultValue, relAttr.adaptorValueType(), rel.destinationEntity().name());
                _initialDefaultValues.setObjectForKey(d, name);
            }
        }
    }

    public void setDefaultValuesInObject(EOEnterpriseObject eo,  EOEditingContext ec) {
        defaultLog.debug("About to set values in EO");
        if(_initialDefaultValues == null) {
            readDefaultValues();
        }
        for( Enumeration e = _initialDefaultValues.keyEnumerator(); e.hasMoreElements();) {
            String key = (String)e.nextElement();
            defaultLog.debug("About to set <"+key+"> in EO");
            ((Default)_initialDefaultValues.objectForKey(key)).setValueInObject(eo);
        }
    }

    public void awakeObjectFromInsertion(EOEnterpriseObject eo, EOEditingContext ec) {
        super.awakeObjectFromInsertion(eo, ec);
        setDefaultValuesInObject(eo, ec);
    }

    public String localizedKey(String key) {
    	key = key + "_" + ERXLocalizer.currentLocalizer().languageCode();
    	if(!allPropertyKeys().containsObject(key)) {
    		key = null;
    	}
    	return key;
    }

    public String inverseForRelationshipKey(String relationshipKey) {
        String result = null;
        EORelationship relationship = entity().relationshipNamed(relationshipKey);
        if(relationship != null && relationship.userInfo() != null) {
            result = (String) relationship.userInfo().objectForKey("ERXInverseRelationshipName");
        }
        if(result == null) {
            result = super.inverseForRelationshipKey(relationshipKey);
        }
        return result;
    }

    private static boolean useValidity() {
        if (useValidity == null) {
            useValidity = "true".equals(System.getProperty("er.extensions.ERXGenericRecord.useValidity")) ? Boolean.TRUE : Boolean.FALSE;
        }
        return useValidity.booleanValue();
    }


    private void invokeValidityMethodWithType(int type, EOEnterpriseObject eo) throws NSValidation.ValidationException{
        try {
            Object dummy = null;
            Method m = validityMethods()[type];
            m.invoke(sharedGSVEngineInstance(), new Object[]{eo});
        } catch (IllegalAccessException e1) {
            log.error("an exception occured in validityValidateEOObjectOnSave", e1);
        } catch (IllegalArgumentException e2) {
            log.error("an exception occured in validityValidateEOObjectOnSave", e2);
        } catch (NullPointerException e3) {
            log.error("an exception occured in validityValidateEOObjectOnSave", e3);
        } catch (InvocationTargetException e4) {
            Throwable targetException = e4.getTargetException();
            if (targetException instanceof NSValidation.ValidationException) {
                throw (NSValidation.ValidationException)targetException;
            } else {
                log.error("an exception occured in validityValidateEOObjectOnSave", e4);
            }
        }
    }

    private Method[] validityMethods() {
        if (validityMethods == null) {
            validityMethods = new Method[4];
            Method m = methodInSharedGSVEngineInstanceWithName("validateEOObjectOnSave");
            validityMethods[0] = m;
            
            m = methodInSharedGSVEngineInstanceWithName("validateEOObjectOnDelete");
            validityMethods[1] = m;
            
            m = methodInSharedGSVEngineInstanceWithName("validateEOObjectOnInsert");
            validityMethods[2] = m;
            
            m = methodInSharedGSVEngineInstanceWithName("validateEOObjectOnUpdate");
            validityMethods[3] = m;
        }
        return validityMethods;
    }
    
    private static Method methodInSharedGSVEngineInstanceWithName(String name) {
        try {
            return sharedGSVEngineInstance().getClass().getMethod(name, new Class[]{EOEnterpriseObject.class});
        } catch (IllegalArgumentException e2) {
            throw new NSForwardException(e2);
        } catch (NullPointerException e3) {
            throw new NSForwardException(e3);
        } catch (NoSuchMethodException e4) {
            throw new NSForwardException(e4);
        }
    }
    
    private static Object sharedGSVEngineInstance() {
        if (sharedGSVEngineInstance == null) {
            try {
                Class gsvEngineClass = Class.forName("com.gammastream.validity.GSVEngine");
                Method m = gsvEngineClass.getMethod("sharedValidationEngine", new Class[]{});
                Object dummy = null;
                sharedGSVEngineInstance = m.invoke(dummy, new Object[]{});
            } catch (ClassNotFoundException e1) {
                throw new NSForwardException(e1);
            } catch (NoSuchMethodException e2) {
                throw new NSForwardException(e2);
            } catch (IllegalAccessException e3) {
                throw new NSForwardException(e3);
            } catch (InvocationTargetException e4) {
                throw new NSForwardException(e4);
            }
        }
        return sharedGSVEngineInstance;
    }

	public Class _enforcedKVCNumberClassForKey(String key) {
		EOAttribute attribute = entity().attributeNamed(key);
		if(attribute != null && attribute.userInfo() != null) {
			String className = (String) attribute.userInfo().objectForKey("ERXConstantClassName");
			if(className != null) {
                Class c = ERXPatcher.classForName(className);
				return c;
			}
		}
		return super._enforcedKVCNumberClassForKey(key);
	}
    
    private NSMutableArray<Class<ERXPartial>> _partialClasses = new NSMutableArray<Class<ERXPartial>>();
    
    /**
     * Associates a partial entity class with this entity.
     * 
     * @see er.extensions.partials
     * @param partialClass the partial class to associate
     */
	public void _addPartialClass(Class<ERXPartial> partialClass) {
		_partialClasses.addObject(partialClass);
	}
	
	/**
	 * Returns the list of partial entity classes for this entity.
	 * 
     * @see er.extensions.partials
	 * @return the list of partial entity classes for this entity
	 */
	public NSArray<Class<ERXPartial>> partialClasses() {
    	return _partialClasses;
    }
}