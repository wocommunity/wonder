package com.gammastream.validity;

import com.webobjects.eoaccess.EOUtilities;
import com.webobjects.eocontrol.EOEditingContext;
import com.webobjects.eocontrol.EOEnterpriseObject;
import com.webobjects.foundation.NSArray;
import com.webobjects.foundation.NSDictionary;
import com.webobjects.foundation.NSKeyValueCoding;

/**
 *	This class provides a set of predefined rules for performing
 *	validation on miscellaneous data types. These rules are part of
 *	the default set of 'QuickRules'.
 * 
 *	@author GammaStream Technologies, Inc.
 */
public class GSVOtherMethods {

    //Editing Context for occasional fetching
    private static EOEditingContext _editingContext = null;

    /**
     *	Determines whether a value has been provided for the specified attribute.
     *
     *	@param	object		The object whose attribute is being validated.
     *	@param	attribute 	The attribute being validated.
     *	@param	key 		The key used to access the attribute.
     *	@param	params 		The param dictionary which must contain the above mentioned key-value pairs.
     *
     *	@return	<code>true</code> if the value is <code>null</code>; otherwise, <code>false</code>
     */
    public final static boolean isNull(Object object,Object attribute,String key, NSDictionary params){
        return ( attribute == null );     
    }
     
    /**
     *	Determines whether the specified to-many relationship contains any objects.
     *	<br>
     *	<br>The required key-value pairs include:
     *	<br>"RelationshipKey" = The relationship key. (i.e. "toUsers" )
     *	<br>
     *
     *	@param	object		The object whose attribute is being validated.
     *	@param	attribute 	The attribute being validated.
     *	@param	key 		The key used to access the attribute.
     *	@param	params 		The param dictionary which must contain the above mentioned key-value pairs.
     *
     *	@return	<code>true</code> if the to-many array is empty; otherwise, <code>false</code>
     */
    public final static boolean isArrayEmpty(Object object,Object attribute,String key, NSDictionary params){
        Object value = NSKeyValueCoding.Utility.valueForKey(object,(String)params.objectForKey("RelationshipKey"));
        if( value instanceof NSArray ){
            return ( ((NSArray)value).count() == 0 );
        }
        return true;
    }
   
    /**
     *	Verifies that the specified attribute is unique.
     *
     *	@param	object		The object whose attribute is being validated.
     *	@param	attribute 	The attribute being validated.
     *	@param	key 		The key used to access the attribute.
     *	@param	params 		The param dictionary which must contain the above mentioned key-value pairs.
     *
     *	@return	<code>true</code> if the attribute is empty; otherwise, <code>false</code>
     */
    public final static boolean isUnique(Object object, Object attribute, String key, NSDictionary params){
        if(GSVOtherMethods._editingContext == null){
            GSVOtherMethods._editingContext = new EOEditingContext();
        }
        if( object instanceof EOEnterpriseObject ){
            if( attribute != null ){
                String entityName = ((EOEnterpriseObject)object).entityName();
                NSArray array = EOUtilities.objectsMatchingKeyAndValue(GSVOtherMethods._editingContext, entityName, key, attribute);
                if( array.count() == 0 ){
                    return true;
                } else if( array.count() == 1 ) {
                    //it could be that the object found is itself
                    EOEnterpriseObject localObject = EOUtilities.localInstanceOfObject(GSVOtherMethods._editingContext, (EOEnterpriseObject)object);
                    return ( (EOEnterpriseObject)array.objectAtIndex(0) == localObject );
                }
            }
        }
        return false;
    }
    

    
    

}
