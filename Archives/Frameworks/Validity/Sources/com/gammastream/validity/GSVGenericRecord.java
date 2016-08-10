package com.gammastream.validity;

import com.webobjects.eocontrol.EOGenericRecord;

/**
 *	In order for your EOGenericRecords to take advantage of Validity validation,
 *	they must extend GSVGenericRecord. Similarly, your EOCustomObjects must extend
 *	GSVCustomObject.
 *
 *	@author GammaStream Technologies, Inc.
 */
public class GSVGenericRecord extends EOGenericRecord {

    public void validateForDelete(){
        GSVEngine.sharedValidationEngine().validateEOObjectOnDelete(this);

        super.validateForDelete();
    }

    public void validateForInsert(){
        GSVEngine.sharedValidationEngine().validateEOObjectOnInsert(this);

        super.validateForInsert();
    }

    public void validateForSave(){
        GSVEngine.sharedValidationEngine().validateEOObjectOnSave(this); 

        super.validateForSave();
    }

    public void validateForUpdate(){
        GSVEngine.sharedValidationEngine().validateEOObjectOnUpdate(this); 

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
