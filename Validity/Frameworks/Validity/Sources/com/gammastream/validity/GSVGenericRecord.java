package com.gammastream.validity;

import com.webobjects.foundation.*;
import com.webobjects.appserver.*;
import com.webobjects.eocontrol.*;
import com.webobjects.eoaccess.*;

/**
 *	In order for your EOGenericRecords to take advantage of Validity validation,
 *	they must extend GSVGenericRecord. Similarly, your EOCustomObjects must extend
 *	GSVCustomObject.
 *
 *	@author GammaStream Technologies, Inc.
 */
public class GSVGenericRecord extends EOGenericRecord {

    public void validateForDelete(){
        try{
            GSVEngine.sharedValidationEngine().validateEOObjectOnDelete(this);
        } catch(NSValidation.ValidationException e){
            throw e;
        }
        super.validateForDelete();
    }

    public void validateForInsert(){
    	try{
            GSVEngine.sharedValidationEngine().validateEOObjectOnInsert(this); 
        } catch(NSValidation.ValidationException e){
            throw e;
        }
	super.validateForInsert();
    }

    public void validateForSave(){
    	try{
            GSVEngine.sharedValidationEngine().validateEOObjectOnSave(this); 
        } catch(NSValidation.ValidationException e){
            throw e;
        }
        super.validateForSave();
    }

    public void validateForUpdate(){
    	try{
            GSVEngine.sharedValidationEngine().validateEOObjectOnUpdate(this); 
        } catch(NSValidation.ValidationException e){
            throw e;
        }
        super.validateForUpdate();
    }

    public Object validateValueForKey(Object value, String key){
        //NSLog.debug.appendln("*** Validity: GSVGenericRecord.validateValueForKey(Object value, String key)");
        if( this.shouldUseDefaultValidition() ){
            return super.validateValueForKey(value, key);
        } else {
            return value;
        }
    }

    /**
     *	Override this method in your EOGenericRecord if you do not want to take advantage
     *	of the built in EOF validation.
     */
    public boolean shouldUseDefaultValidition(){
        return true;
    }

}
