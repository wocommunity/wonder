/*
 * Copyright (C) NetStruxr, Inc. All rights reserved.
 *
 * This software is published under the terms of the NetStruxr
 * Public Software License version 0.5, a copy of which has been
 * included with this distribution in the LICENSE.NPL file.  */

/* ERXValidationFactory.java created by max on Sun 22-Apr-2001 */
package er.extensions;

import com.webobjects.directtoweb.*;
import com.webobjects.foundation.*;
import com.webobjects.eocontrol.*;
import com.webobjects.eoaccess.*;
import com.webobjects.appserver.*;
import com.webobjects.directtoweb.*;
import org.apache.log4j.Category;
import java.util.*;
import java.lang.reflect.*;

public class ERXValidationFactory {

    ////////////////////////////////////////////  log4j category  //////////////////////////////////////////////
    public final static Category cat = Category.getInstance("er.validation.ERXValidationFactory");

    private static final String DEFAULT_VALIDATION_FILE_NAME = "ValidationTemplate.strings";

    private static ERXValidationFactory _defaultFactory;
    public static void setDefaultFactory(ERXValidationFactory aFactory) { _defaultFactory = aFactory; }
    public static ERXValidationFactory defaultFactory() {
        if (_defaultFactory == null)
            setDefaultFactory(new ERXValidationFactory());
        return _defaultFactory;
    }

    private static Object _defaultValidationDelegate = null;
    public static Object defaultDelegate() { return _defaultValidationDelegate; }
    public static void setDefaultDelegate(Object obj) { _defaultValidationDelegate = obj; }

    public interface FactoryInterface {
        public Class validationExceptionClass();
        public void setValidationExceptionClass(Class class1);
        public ERXValidationException createException(EOEnterpriseObject eo, String property, Object value, String type);
        public ERXValidationException createCustomException(EOEnterpriseObject eo, String method);
    }

    public interface ExceptionDelegateInterface {
        public String messageForException(ERXValidationException erv);
        public String templateForException(ERXValidationException erv);
        public NSKeyValueCoding contextForException(ERXValidationException erv);
    }

    private static final String EDI_MFE_METHOD_NAME = "messageForException";
    private static final String EDI_TFE_METHOD_NAME = "templateForException";
    private static final Class[] EDI_FE_ARGS = new Class[] {ERXValidationException.class};

    private static NSDictionary _mappings;
    static {
        Object keys[] = {				// MESSAGE LIST:
            "to be null", 				// "The 'xxxxx' property is not allowed to be NULL"
            "Invalid Number", 				// "Invalid Number"
            "must have at least one",			// "The exercises property of ERPCompanyRole must have at least one ERPExercise"
            "relationship, there is a related object",	// "Removal of ERPAccount object denied because its children relationship is not empty"
            "relationship, there are related objects",	// "Removal of ERPAccount object denied because its children relationship is not empty"
        };

        Object objects[] = {
            ERXValidationException.NullPropertyException,
            ERXValidationException.InvalidNumberException,
            ERXValidationException.MandatoryRelationshipException,
            ERXValidationException.ObjectRemovalException,
            ERXValidationException.ObjectsRemovalException,
        };
        _mappings = new NSDictionary( objects, keys );
    }

    // Useful in developement for re-loading templates when they change.
    private static FileObserver _defaultFileObserver;
    public static FileObserver defaultFileObserver() {
        if (_defaultFileObserver == null)
            _defaultFileObserver = new FileObserver();
        return _defaultFileObserver;
    }

    public static class FileObserver {
        public void fileDidChange(NSNotification n) {
            ERXValidationFactory.defaultFactory().loadTemplates();
        }
    }
    
    private Class _validationExceptionClass;
    public void setValidationExceptionClass(Class class1) { _validationExceptionClass = class1; }
    public Class validationExceptionClass() {
        if (_validationExceptionClass == null) {
            _validationExceptionClass = ERXValidationException.class;
        }
        return _validationExceptionClass;
    }

    private static Class[] _exceptionConstructor = new Class[] { String.class, NSDictionary.class };
    private static Class[] _regularConstructor = new Class[] { EOEnterpriseObject.class, String.class, Object.class, String.class };
    private static Class[] _customConstructor = new Class[] { EOEnterpriseObject.class, String.class };

    public ERXValidationException createException(EOEnterpriseObject eo, String property, Object value, String type) {
        ERXValidationException erve = null;
        try {
            //erve = (ERXValidationException)validationExceptionClass().getConstructor(_regularConstructor).newInstance(new Object[] {eo, property,
            //    value, type});
            cat.debug("Creating exception for type: " + type + " validationExceptionClass: " + validationExceptionClass().getName());
            erve = (ERXValidationException)validationExceptionClass().getConstructor(_exceptionConstructor).newInstance(new Object[] {type,
                new NSMutableDictionary()});
            erve.exceptionForObject(eo, property, value, type);
        } catch (InvocationTargetException ite) {
            cat.error("Caught InvocationTargetException creating regular validation exception: " + ite.getTargetException());            
        } catch (Exception e) {
            cat.error("Caught exception creating regular validation exception: " + e);
        }
        return erve;
    }

    public ERXValidationException createCustomException(EOEnterpriseObject eo, String method) {
        ERXValidationException erve = null;
        try {
            erve = (ERXValidationException)validationExceptionClass().getConstructor(_customConstructor).newInstance(new Object[] {eo,
                method});
        } catch (Exception e) {
            cat.error("Caught exception creating custom validation exception: " + e.getMessage());
        }
        return erve;
    }

    // Not very eligant, but until we have a way of throwing our own class of validation exception this is the only way.
    // Note that this is only used to convert model thrown exceptions in EREntityClassDescription.
    public ERXValidationException convertException(NSValidation.ValidationException eov) { return convertException(eov, null); }
    public ERXValidationException convertException(NSValidation.ValidationException eov, Object value) {
        ERXValidationException erve = null;
        if (cat.isDebugEnabled())
            cat.debug("Converting exception: " + eov + " value: " + (value != null ? value : "<NULL>"));
        if (!(eov instanceof ERXValidationException)) {
            String message = eov.getMessage();
            NSDictionary userInfo = eov.userInfo() != null ? (NSDictionary)eov.userInfo() : ERXConstant.EmptyDictionary;
            for (Enumeration e = _mappings.allKeys().objectEnumerator(); e.hasMoreElements();) {
                EOEnterpriseObject eo = (EOEnterpriseObject)userInfo.objectForKey(NSValidation.ValidationException.ValidatedObjectUserInfoKey);
                String key = (String)e.nextElement();
                String type = (String)_mappings.objectForKey(key);
                if (message.lastIndexOf(key) >= 0) {
                    // String property = (String)userInfo.objectForKey(NSValidation.ValidationException.ValidatedKeyUserInfoKey);
                    cat.debug("UserInfo:" + userInfo);
                    String property = eov.key();
                    if(property == null && message.indexOf("Removal") == 0) {
                        //FIXME: (ak) pattern matching?
                        property = (String)(NSArray.componentsSeparatedByString(message, "'").objectAtIndex(3));
                    }
                    erve = createException(eo, property, value, type);
                    break;
                }
            }
            NSArray additionalExceptions = (NSArray)userInfo.objectForKey(NSValidation.ValidationException.AdditionalExceptionsKey);
            if (erve == null) {
                cat.error("Unable to convert validation exception: " + eov);
            } else if (additionalExceptions != null && additionalExceptions.count() > 0) {
                NSMutableArray erveAddtionalExceptions = new NSMutableArray();
                for (Enumeration e = additionalExceptions.objectEnumerator(); e.hasMoreElements();) {
                    ERXValidationException erven = convertException((NSValidation.ValidationException)e.nextElement());
                    if (erven != null)
                        erveAddtionalExceptions.addObject(erven);
                }
                if (erveAddtionalExceptions.count() > 0)
                    erve.setObjectForKey(erveAddtionalExceptions, NSValidation.ValidationException.AdditionalExceptionsKey);
            }
        } else {
            cat.warn("Attempting to convert validation exception: " + eov + " that is already of type ERXValidationException");
            erve = (ERXValidationException)eov;
        }
        return erve;
    }

    // FIXME: Right now the delegate methods are implemented as a formal interface.  Not ideal.  Should be implemented as
    //	an informal interface.  Can still return null to not have an effect.
    public String messageForException(ERXValidationException erv) {
        String message = null;
        if (erv.delegate() != null && erv.delegate() instanceof ExceptionDelegateInterface) {
            message = ((ExceptionDelegateInterface)erv.delegate()).messageForException(erv);
        }
        if (message == null) {
            message = ERXSimpleTemplateParser.sharedInstance().parseTemplateWithObject(templateForException(erv), templateDelimiter(), erv);
        }
        return message;
    }

    //ak: This should belong to ERXMultiKey if we are prepared to put NULL values in it, if not, we should check every throw if ERXValidationException if it has values for every argument...
    private Object _kvcNullValue(Object o) {return (o == null? NSKeyValueCoding.NullValue : o);}
    
    // Override this method in subclasses to provide a different mechanism for resolving getting/generating a template.
    private final static String UNDEFINED_VALIDATION_TEMPLATE = "Undefined Validation Template";
    public String templateForException(ERXValidationException erv) {
        String template = null;
        if (erv.delegate() != null && erv.delegate() instanceof ExceptionDelegateInterface) {
            template = ((ExceptionDelegateInterface)erv.delegate()).templateForException(erv);
        }
        if (template == null) {
            String entityName = erv.eoObject().entityName();
            String property = erv.isCustomMethodException() ? erv.method() : erv.propertyKey();
            String type = erv.type();
            String targetLanguage = erv.targetLanguage() != null ? erv.targetLanguage() : defaultTargetLanguage();

            cat.debug("templateForException with entityName: " + entityName + "; property: " + property + "; type: " + type + "; targetLanguage: " + targetLanguage);
            ERXMultiKey k = new ERXMultiKey(new NSArray(new Object[] {_kvcNullValue(entityName), _kvcNullValue(property), _kvcNullValue(type), _kvcNullValue(targetLanguage)}));
            template = (String)_cache.get(k);
            // Not in the cache.  Simple resolving.
            if (template == null) {
                template = templateForEntityPropertyType(entityName, property, type, targetLanguage);
                _cache.put(k, template);
            }
        }
        return template;
    }

    // Override this method in subclasses to provide a different context for the template parse, a d2wcontext comes to mind ;)
    public NSKeyValueCoding contextForException(ERXValidationException erv) {
        NSKeyValueCoding context = null;
        if (erv.delegate() != null && erv.delegate() instanceof ExceptionDelegateInterface) {
            context = ((ExceptionDelegateInterface)erv.delegate()).contextForException(erv);
        }
        if (context == null)
            context = erv;
        return context;
    }

    private String _delimiter = "@";
    public String templateDelimiter() { return _delimiter; }
    public void setDelimiter(String delimiter) { _delimiter = delimiter; }

    private boolean _cachingEnabled = true;
    public void setCachingEnabled(boolean cachingEnabled) { _cachingEnabled = cachingEnabled; }
    public boolean cachingEnabled() { return _cachingEnabled; }
    
    public void configureFactory() {
        // If caching is not enabled then we want to enable dynamic reloading of validation templates.
        setCachingEnabled(WOApplication.application().isCachingEnabled());
        loadTemplates();
        ERXValidation.setPushChangesDefault(ERXUtilities.booleanValueWithDefault(System.getProperties().getProperty("er.extensions.ERXValidationShouldPushChangesToObject"), ERXValidation.DO_NOT_PUSH_INCORRECT_VALUE_ON_EO));
    }
    
    // This loads a configures the templates.  Override to provide custom template loading.
    private NSMutableDictionary _templates = new NSMutableDictionary();
    public void loadTemplates() {
        resetTemplateCache();
        resetTemplateHolder();
        int loadedTempaltes = 0;
        if (cat.isDebugEnabled()) cat.debug("Starting loading of templates");
        // Load all of the files from all of the frameworks.
        for (Enumeration e = ERXUtilities.allFrameworkNames().objectEnumerator(); e.hasMoreElements();) {
            String frameworkName = (String)e.nextElement();
            for (Enumeration ee = templateFileNames().objectEnumerator(); ee.hasMoreElements();) {
                String fileName = (String)ee.nextElement();
                for (Enumeration eee = targetDisplayLanguages().objectEnumerator(); eee.hasMoreElements();) {
                    String targetLanguage = (String)eee.nextElement();
                    NSDictionary template = (NSDictionary)ERXExtensions.readPropertyListFromFileInFramework(fileName, frameworkName,
                                                                                                           new NSArray(targetLanguage));
                    if (template != null) {
                        addTemplatesFromDictionary(template, targetLanguage);
                        loadedTempaltes++;
                        if (!cachingEnabled()) {
                            String filePath = WOApplication.application().resourceManager().pathForResourceNamed(fileName,
                                                                                                                 frameworkName,
                                                                                                                 new NSArray(targetLanguage));
                            ERXFileNotificationCenter.defaultCenter().addObserver(defaultFileObserver(),
                                                                                  new NSSelector("fileDidChange", ERXConstant.NotificationClassArray),
                                                                                  filePath);
                        }
                        if (cat.isDebugEnabled())
                            cat.debug("Found validation template in file: " + fileName + " in framework: " + frameworkName + " for target language: " +
                                      targetLanguage);
                    }
                }
            }
        }
        // Load all the files from the application.
        for (Enumeration e = templateFileNames().objectEnumerator(); e.hasMoreElements();) {
            String fileName = (String)e.nextElement();
            for (Enumeration ee = targetDisplayLanguages().objectEnumerator(); ee.hasMoreElements();) {
                String targetLanguage = (String)ee.nextElement();
                NSDictionary template = (NSDictionary)ERXExtensions.readPropertyListFromFileInFramework(fileName, null, new NSArray(targetLanguage));
                if (template != null) {
                    addTemplatesFromDictionary(template, targetLanguage);
                    loadedTempaltes++;
                    if (!cachingEnabled()) {
                        String filePath = WOApplication.application().resourceManager().pathForResourceNamed(fileName,
                                                                                                             null,
                                                                                                             new NSArray(targetLanguage));
                        ERXFileNotificationCenter.defaultCenter().addObserver(defaultFileObserver(),
                                                                              new NSSelector("fileDidChange", ERXConstant.NotificationClassArray),
                                                                              filePath);
                    }
                    if (cat.isDebugEnabled())
                        cat.debug("Found validation template in file: " + fileName + " for language: " + targetLanguage + " in application");
                }                
            }
        }
        if (cat.isDebugEnabled()) cat.debug("Finished loading: " + loadedTempaltes + " templates files.  Structure: " + _templates);
    }

    // Resets the cache.
    private Hashtable _cache=new Hashtable(1000);
    protected void resetTemplateCache() {
        _cache = new Hashtable(1000);
        if (cat.isDebugEnabled()) cat.debug("Resetting template cache");
    }
    protected void resetTemplateHolder() {
        _templates = new NSMutableDictionary();
        if (cat.isDebugEnabled()) cat.debug("Resetting template holder");
    }

    protected String templateForEntityPropertyType(String entityName, String property, String type, String targetLanguage) {
        // 1st try the whole string.
        String template = templateForKeyPath(entityName + "." + property + "." + type, targetLanguage);
        // 2nd try everything minus the type.
        if (template == null)
            template = templateForKeyPath(entityName + "." + property, targetLanguage);
        // 3rd try property plus type
        if (template == null)
            template = templateForKeyPath(property + "." + type, targetLanguage);
        // 4th try just property
        if (template == null)
            template = templateForKeyPath(property, targetLanguage);
        // 5th try just type
        if (template == null)
            template = templateForKeyPath(type, targetLanguage);
        if (template == null) {
            cat.error("Unable to find template for entity: " + entityName + " property: " + property + " type: "
                      + type + " targetLanguage: " + targetLanguage);
            template = UNDEFINED_VALIDATION_TEMPLATE;
        }
        return template;
    }

    public NSDictionary templatesForLanguage(String language) { return (NSDictionary)(language != null ? _templates.objectForKey(language) :
                                                                                      _templates.objectForKey(defaultTargetLanguage())); }

    protected NSMutableDictionary editableTemplateForLanguage(String language) {
        language = language != null ? language : defaultTargetLanguage();
        NSMutableDictionary template = (NSMutableDictionary)_templates.objectForKey(language);
        if (template == null) {
            template = new NSMutableDictionary();
            _templates.setObjectForKey(template, language);
            if (cat.isDebugEnabled())
                cat.debug("Creating template entry for language: " + language);
        }
        return template;
    }

    // Note that this only resolves directly
    public String templateForKeyPath(String key, String language) {
        NSDictionary templates = templatesForLanguage(language);
        if (templates == null)
            cat.warn("Templates for language: " + language + " are not currently loaded.");
        return templates != null ? (String)templates.objectForKey(key) : null;
    }
    public void addTemplateForKeyPath(String template, String key, String language) {
        NSMutableDictionary templates = editableTemplateForLanguage(language);
        if (templates.objectForKey(key) != null) {
            cat.info("Replacing previous validation template: " + templates.objectForKey(key) + " for key: " + key + " with: " + template);
            templates.removeObjectForKey(key);
        }
        templates.setObjectForKey(template, key);
    }

    public void addTemplatesFromDictionary(NSDictionary templates, String language) {
        for (Enumeration e = templates.keyEnumerator(); e.hasMoreElements();) {
            String key = (String)e.nextElement();
            addTemplateForKeyPath((String)templates.objectForKey(key), key, language);
        }
    }

    private NSMutableArray _templateFileNames = new NSMutableArray(DEFAULT_VALIDATION_FILE_NAME);
    public NSArray templateFileNames() { return _templateFileNames; }
    public void setTemplateFileNames(NSArray fileNames) { _templateFileNames = new NSMutableArray(fileNames); }
    public void addTemplateFileName(String name) {
        if (!_templateFileNames.containsObject(name))
            _templateFileNames.addObject(name);
    }

    // These are used for knowing how to build the cache, ie which localized resources to load.
    private static final String DEFAULT_TARGET_DISPLAY_LANGUAGE = "English";
    private NSMutableArray _targetDisplayLanguages = new NSMutableArray(DEFAULT_TARGET_DISPLAY_LANGUAGE);
    public NSArray targetDisplayLanguages() { return _targetDisplayLanguages; }
    public void setTargetDisplayLanguages(NSArray targets) { _targetDisplayLanguages = new NSMutableArray(targets); }
    public void addTargetDisplayLanguage(String target) {
        if (!_targetDisplayLanguages.containsObject(target))
            _targetDisplayLanguages.addObject(target);
    }

    private String _defaultTargetLanguage = DEFAULT_TARGET_DISPLAY_LANGUAGE;
    public String defaultTargetLanguage() { return _defaultTargetLanguage; }
    public void setDefaultTargetLanguage(String target) { _defaultTargetLanguage = target; }
}
