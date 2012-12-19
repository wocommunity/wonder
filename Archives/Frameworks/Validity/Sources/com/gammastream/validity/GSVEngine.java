package com.gammastream.validity;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;

import com.webobjects.appserver.xml.WOXMLDecoder;
import com.webobjects.eoaccess.EOEntity;
import com.webobjects.eoaccess.EOModel;
import com.webobjects.eoaccess.EOModelGroup;
import com.webobjects.eocontrol.EOEnterpriseObject;
import com.webobjects.foundation.NSArray;
import com.webobjects.foundation.NSDictionary;
import com.webobjects.foundation.NSKeyValueCoding;
import com.webobjects.foundation.NSMutableArray;
import com.webobjects.foundation.NSMutableDictionary;
import com.webobjects.foundation.NSPathUtilities;
import com.webobjects.foundation.NSValidation;

/**
 *	This is Validity's validation engine. This class loads and
 *	applies the rules you have modeled in your Validity model file.
 *	This class is accessed via a singleton accessor.
 *
 *	@author GammaStream Technologies, Inc.
 */
public final class GSVEngine {

    /**********************************  STATIC  **********************************/
    
    /**
     *	Key used to access the Validity exception dictionary from
     *	NSValidation.Exception's userinfo dictionary.
     */
    public static String ERROR_DICTIONARY_KEY = "GSVExceptions";
    
    //internal constants used to determine when a rule should be applied.
    private static int ON_SAVE = 1;
    private static int ON_INSERT = 2;
    private static int ON_UPDATE = 3;
    private static int ON_DELETE = 4;
    private static int ON_DEMAND = 5;
    
    //GSVEngine is a singlet class, this iVar stores the single instance.
    private static GSVEngine _sharedValidationEngine = null;
    
    /**
     *	Creates a new GSVEngine if one is not already created, and returns the shared instance.
     *
     *	@return The shared validation engine.
     */
    public static GSVEngine sharedValidationEngine(){
        if( _sharedValidationEngine == null ){
            //NSLog.debug.appendln("*** Validity: GSVEngine.sharedValidationEngine(): Creating Shared Validation Engine");
            _sharedValidationEngine = new GSVEngine();
        }
        return _sharedValidationEngine;
    }

    public static NSValidation.ValidationException exceptionByAppendingErrorToException(String errorMessage, String key, NSValidation.ValidationException ex){
        NSMutableDictionary userInfo = null;
        NSMutableDictionary errorDict = null;
        NSMutableArray errorList = null;
        NSValidation.ValidationException returnException = ex;
        
        //get or create the userInfo dictionary
        if( returnException == null || returnException.userInfo() == null ){
            userInfo = new NSMutableDictionary(new NSMutableDictionary(), GSVEngine.ERROR_DICTIONARY_KEY);
        } else {
            userInfo = new NSMutableDictionary(returnException.userInfo());
        }
        
        //get or create the GSV error dictionary
        errorDict = (NSMutableDictionary)userInfo.objectForKey(GSVEngine.ERROR_DICTIONARY_KEY);
        if( errorDict == null ){
            errorDict = new NSMutableDictionary();
            userInfo.setObjectForKey(errorDict, GSVEngine.ERROR_DICTIONARY_KEY);
        }
        
        //get or create the gsv error list
        errorList = (NSMutableArray)errorDict.objectForKey(key);
        if( errorList == null ){
            errorList = new NSMutableArray();
            errorDict.setObjectForKey(errorList, key);
        }
        
        //add the error
        errorList.addObject(errorMessage);
        
        //return the new aggregated exception
        return new NSValidation.ValidationException("Validity Exception", userInfo);
    }
    
    /**********************************  INSTANCE  **********************************/
    
    //caches
    private NSMutableDictionary _modelCache = null;
    private NSMutableDictionary _classCache = null;
    private NSMutableDictionary _methodCache = null;
    
    //stores the reusable method signature for all gsv rules
    private Class[] _gsvRuleSignature = new Class[4];
    
    //The default model group used of accessing the EOModel associated with the Validity model
    private EOModelGroup _defaultModelGroup = null;
    
    //store whether the beta/trial period has passed
    private boolean _expired = false;
    
    /**
     *	Private constructor called by the singlet accessor.
     */
    private GSVEngine(){
        super();
        
        this.checkExpirationDate();
        this.generateGSVRuleSignature();
        
        _defaultModelGroup = EOModelGroup.defaultGroup();
        
        //initialize caches
        _modelCache = new NSMutableDictionary();
        _classCache = new NSMutableDictionary();
        _methodCache = new NSMutableDictionary();
    }
    
    /**
     *	Called from an object being validated (i.e. validateForSave());
     */
    public boolean validateEOObjectOnSave(EOEnterpriseObject eoObject){
        //NSLog.debug.appendln("*** Validity: GSVEngine.validateEOObjectOnSave(EOEnterpriseObject eoObject)");
        return ( this.validateEOObject(eoObject, ON_SAVE) );
    }
    
    /**
     *	Called from an object being validated (i.e. validateForInsert());
     */
    public boolean validateEOObjectOnInsert(EOEnterpriseObject eoObject){
        //NSLog.debug.appendln("*** Validity: GSVEngine.validateEOObjectOnInsert(EOEnterpriseObject eoObject)");
        return ( this.validateEOObject(eoObject, ON_INSERT) );
    }
    
    /**
     *	Called from an object being validated (i.e. validateForUpdate());
     */
    public boolean validateEOObjectOnUpdate(EOEnterpriseObject eoObject){
        //NSLog.debug.appendln("*** Validity: GSVEngine.validateEOObjectOnUpdate(EOEnterpriseObject eoObject)");
        return ( this.validateEOObject(eoObject, ON_UPDATE) );
    }
    
    /**
     *	Called from an object being validated (i.e. validateForDelete());
     */
    public boolean validateEOObjectOnDelete(EOEnterpriseObject eoObject){
        //NSLog.debug.appendln("*** Validity: GSVEngine.validateEOObjectOnDelete(EOEnterpriseObject eoObject)");
        return ( this.validateEOObject(eoObject, ON_DELETE) );
    }

    public NSDictionary validateKeyAndValueInEntity(String key, String value, String entity) {
        GSVEntity gsvEntity = null;
        GSVAttribute currentAttribute = null;
        GSVRule currentRule = null;
        NSMutableDictionary errorDict = new NSMutableDictionary();
        boolean rulePassed = false;
	gsvEntity = this.entityWithName(entity);
	if( gsvEntity != null ){
	    //check each of the object's attributes
	    currentAttribute = gsvEntity.attributeNamed(key);
	    if (currentAttribute != null) {
		NSArray rules = currentAttribute.rules();
		if (rules != null) {
		    for(int r=0; r<currentAttribute.rules().count(); r++){
			currentRule = (GSVRule)currentAttribute.rules().objectAtIndex(r);
			rulePassed = this.checkRule(currentRule, key, errorDict, value, ON_SAVE);
			if(currentRule.stopIfFails() && !rulePassed){
			    break;
			}
		    }
		}
	    }
	}
	//if the error dict contains values, rules failed, so throw an exception
	if(errorDict.allKeys().count()>0) {
	    return errorDict;
	} else {
	    return null;
	}
    }

    /**
     *	May be called arbitrarily to validate and eo object
     *	Returns <code>true</code> if all validation succeeds.
     *	Throws an NSValidation.ValidationException if one or more of the rules fails.
     */
    public boolean validateEOObject(EOEnterpriseObject eoObject, int when){
        GSVEntity gsvEntity = null;
        GSVAttribute currentAttribute = null;
        GSVRule currentRule = null;
        NSMutableDictionary errorDict = new NSMutableDictionary();
        boolean rulePassed = false;
        if( !_expired ){
            gsvEntity = this.entityForObject(eoObject);
            if( gsvEntity != null ){
                //check each of the object's attributes
                for(int i=0;i<gsvEntity.attributes().count();i++){
                    currentAttribute = (GSVAttribute)gsvEntity.attributes().objectAtIndex(i);
		    //check each or the attribute's rules
		    for(int r=0;r<currentAttribute.rules().count();r++){
			currentRule = (GSVRule)currentAttribute.rules().objectAtIndex(r);
			rulePassed = this.checkRule(currentRule, currentAttribute.name(), errorDict, eoObject, when);
			if(currentRule.stopIfFails() && !rulePassed){
			    break;
			}
		    }

		}
	    }    
            //if the error dict contains values, rules failed, so throw an exception
            if(errorDict.allKeys().count()>0){
                NSDictionary userInfo = new NSDictionary(errorDict, GSVEngine.ERROR_DICTIONARY_KEY);
                System.out.println("eo="+eoObject);
                throw new NSValidation.ValidationException("Validity Exception", userInfo);
            }
            
        } else {
            //System.out.println("Validity 1.0  has expired.  Please visit http://www.gammastream.com");
            //System.out.println("for the latest release.");
            return false;
        }
        return true;
    }
    
    
    /**
     *	May be called arbitrarily to validate an object
     *	Returns <code>true</code> if all validation succeeds.
     *	Throws an NSValidation.ValidationException if one or more of the rules fails.
     */
    public boolean validateAttribute(Object object, String attributeName, GSVRule rule){
        NSMutableDictionary errorDict = new NSMutableDictionary();
        if( !_expired ){
            if( object != null ){
                this.checkRule(rule, attributeName, errorDict, object, GSVEngine.ON_DEMAND);
            }
            
            //if the error dict contains values, rules failed, so throw an exception
            if(errorDict.allKeys().count()>0){
                NSDictionary userInfo = new NSDictionary(errorDict, GSVEngine.ERROR_DICTIONARY_KEY);
                throw new NSValidation.ValidationException("Validity Exception", userInfo);
            }
            
        } else {
            //System.out.println("Validity 1.0 b1 has expired.  Please visit http://www.gammastream.com");
            //System.out.println("for the latest release.");
            return false;
        }
        return true;
    }

    private boolean checkRule(GSVRule rule, String attributeName, NSMutableDictionary errorDict, String value, Object eoObject, int when) {
        //determine whether this rule applies to the provided 'when'
        if( (when == GSVEngine.ON_SAVE && rule.onSave()) ||
            (when == GSVEngine.ON_INSERT && rule.onInsert()) ||
            (when == GSVEngine.ON_UPDATE && rule.onUpdate()) ||
            (when == GSVEngine.ON_DELETE && rule.onDelete()) ||
            (when == GSVEngine.ON_DEMAND)){

            try{
		Object attributeValue = (eoObject == null) ? value : NSKeyValueCoding.Utility.valueForKey(eoObject,attributeName);
                //if the attributeValue is null, we can stop
                if( attributeValue == null && rule.continueIfNULL()==false){
                    if( rule.failIfNULL() ){
                        NSMutableArray errorArray = (NSMutableArray)errorDict.objectForKey( attributeName );
                        //if this is the first error for this attribute, we need to create an error array
                        if(errorArray == null){
                            errorArray = new NSMutableArray();
                            //place the new error array into the error dictionary
                            errorDict.setObjectForKey(errorArray, attributeName);
                        }
                        //now append the error message to error array
                        if(rule.errorMessage()!=null){
                            errorArray.addObject(rule.errorMessage());
                        }else{
                            errorArray.addObject(attributeName+": Error - No message provided.");
                        }
			return false;
                    }
                    return true;	//rule passed
                }

                //continue
                String classKey = rule.cName();
                String ruleKey = rule.cName() + "." + rule.mName();
                Class clss = (Class)_classCache.objectForKey(classKey);
                Method method = (Method)_methodCache.objectForKey(ruleKey);
                Class methodReturnType = null;
                boolean rulePassed = false;

                if(method == null){
                    //first lets get the class, if we don't have it already
                    if( clss == null ){
                        clss = Class.forName(classKey);
                        //the class was found, cache it.
                        if( clss != null ){
                            _classCache.setObjectForKey(clss, classKey);
                        }
                    }
                    //now get the method from the class, if the class was found
                    if( clss != null ){
                        method = clss.getMethod(rule.mName(), _gsvRuleSignature);
                        //the method was found, cache it.
                        if(method != null){
                            _methodCache.setObjectForKey(method, ruleKey);
                        }
                    }
                }

                //we have the method, lets continue;

                if( method != null ){

                    methodReturnType = method.getReturnType();
                    //this return type must be a <code>boolean</code>
                    if(methodReturnType.toString().equals("boolean")){
                        Object[] params = {eoObject, attributeValue , attributeName, rule.parameters()};
                        rulePassed = ((Boolean)method.invoke(null, params)).booleanValue();
                        //did the rule fail, if so, populate the error dictionary
                        if( (!rule.negate() && !rulePassed) || (rule.negate() && rulePassed) ){
                            NSMutableArray errorArray = (NSMutableArray)errorDict.objectForKey( attributeName );
                            //if this is the first error for this attribute, we need to create an error array
                            if(errorArray == null){
                                errorArray = new NSMutableArray();
                                //place the new error array into the error dictionary
                                errorDict.setObjectForKey(errorArray, attributeName);
                            }
                            //now append the error message to error array
                            if(rule.errorMessage()!=null){
                                errorArray.addObject(rule.errorMessage());
                            }else{
                                errorArray.addObject(attributeName+": Error - No message provided.");
                            }
                        }
                    }
                }
            }catch(InvocationTargetException e){
                System.out.println(e);
                return false;
            }catch(IllegalAccessException e){
                System.out.println(e);
                return false;
            }catch(ClassNotFoundException e){
                System.out.println(e);
                return false;
            }catch(NoSuchMethodException e){
                System.out.println(e);
                return false;
            }
        }
        return true;
    }

    public boolean checkRule(GSVRule rule, String attributeName, NSMutableDictionary errorDict, String value, int when){
	return checkRule(rule, attributeName, errorDict, value, null, when);
    }
    
    /**
     *	May be called arbitrarily to validate an EO Object, though
     *	it is explicitly called by <code>validateObject</code>
     *	Returns <code>true</code> if all validation succeeds.
     *	Returns <code>false</code> if the rule fails, and populates the error
     *	dictionary with the error.
     */
    public boolean checkRule(GSVRule rule, String attributeName, NSMutableDictionary errorDict, Object eoObject, int when){
	return  checkRule(rule, attributeName, errorDict, null, eoObject, when);
    }

    private GSVEntity entityWithName(String name) {
        EOEntity eoentity = _defaultModelGroup.entityNamed(name);
	return entityForEntity(eoentity);
    }

    private GSVEntity entityForEntity(EOEntity eoentity) {
        //if the eoentity, indeed exists, find out the name of it's model
        if( eoentity != null ){
            String modelName = eoentity.model().name();

            //check the cache to see if this Valididty model has been loaded
            GSVModel model = (GSVModel)_modelCache.objectForKey(modelName);

            //if the model hasn't been cache, load then cache it
            if( model == null ){
                EOModel eomodel = eoentity.model();
                String eoModelPath = NSPathUtilities.stringByDeletingLastPathComponent(eomodel.path());
                eoModelPath = NSPathUtilities.stringByAppendingPathComponent(eoModelPath, eomodel.name());
                eoModelPath = NSPathUtilities.stringByAppendingPathExtension(eoModelPath, "eomodeld");
                String gsvModelPath = NSPathUtilities.stringByAppendingPathComponent(eoModelPath, GSVModel.MODEL_NAME);
                gsvModelPath = NSPathUtilities.stringByAppendingPathExtension(gsvModelPath, GSVModel.MODEL_EXTENSION);
                WOXMLDecoder decoder = WOXMLDecoder.decoder();
                decoder.setEncoding("UTF-8");
                model = (GSVModel)decoder.decodeRootObject(gsvModelPath);
                model.setEomodelPath(eoModelPath);	//not sure why we need to do this?
		model.init(eomodel);

                model.saveModel();					//not sure why we need to do this?
				       //now cache
                _modelCache.setObjectForKey(model, modelName);
            }
            return model.entityNamed(eoentity.name());
        }
        //the eo entity wasn't found
        return null;
    }
    /**
     *	Loads and returns the GSVEntity associated with the enterprise object.
     */
    private GSVEntity entityForObject(EOEnterpriseObject eoObject){
        EOEntity eoentity = _defaultModelGroup.entityNamed(eoObject.entityName());
        return entityForEntity(eoentity);
    }
    /**
     *	Checks the expiration date for the trial/beta versions of Validity.
     */
    private void checkExpirationDate(){
        /*
        NSTimestamp now = new NSTimestamp();
        if( now.yearOfCommonEra() >= 2002 && now.monthOfYear() >= 2 && now.dayOfMonth() >= 1){
            System.out.println("Validity 1.0 b1 has expired.  Please visit http://www.gammastream.com");
            System.out.println("for the latest release.");
            _expired = true;
        }
        */
        _expired = false;
    }
    
    /**
     *	Simply generates the method signature for a Validity Rule.  A Validity Rule
     *	must use this signature.
     */
    private void generateGSVRuleSignature(){
        try{
            Class[] temp = {
                Class.forName("java.lang.Object"),
                Class.forName("java.lang.Object"),
                Class.forName("java.lang.String"),
                Class.forName("com.webobjects.foundation.NSDictionary")
            };
            _gsvRuleSignature = temp;
        } catch(Exception e){
            System.out.println(e);
        } 
    }
    
}
