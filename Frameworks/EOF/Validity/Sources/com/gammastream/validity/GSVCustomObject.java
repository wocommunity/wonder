package com.gammastream.validity;

import com.webobjects.eocontrol.EOCustomObject;

/**
 *	In order for your EOCustomObjects to take advantage of Validity validation,
 *	they must extend GSVCustomObject. Similarly, your EOGenericRecords must extend
 *	GSVGenericRecord.
 *
 *	@author GammaStream Technologies, Inc.
 */
public class GSVCustomObject extends EOCustomObject {

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
        if( this.shouldUseDefaultValidition() ){
            return super.validateValueForKey(value, key);
        } else {
            return value;
        }
    }
    
    /**
     *	Override this method in your EOCustomObject if you do not want to take advantage
     *	of the built in EOF validation.
     */
    public boolean shouldUseDefaultValidition(){
        return true;
    }

}
