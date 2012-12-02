//
// GSVDateMethods.java
// Project Validity
//
// Created by admin on Sun Nov 25 2001
//
package com.gammastream.validity;

import com.webobjects.foundation.NSDictionary;
import com.webobjects.foundation.NSKeyValueCoding;
import com.webobjects.foundation.NSTimestamp;

/**
 *	This class provides a set of predefined rules for performing
 *	validation on <code>NSTimestamps</code>. These rules are part of
 *	the default set of 'QuickRules'.
 * 
 *	@author GammaStream Technologies, Inc.
 */
public class GSVDateMethods {

    /**
     *	One of the many 'mutators' which never fail, unless of course, an exception is thrown.
     *	<br>A mutator simply modifies (or mutates) the attribute is some way.
     *	<br>In this case, it updates the attribute to the current time.
     *
     *	@param	object		The object whose attribute is being validated.
     *	@param	attribute 	The attribute being validated.
     *	@param	key 		The key used to access the attribute.
     *	@param	params 		The param dictionary which must contain the above mentioned key-value pairs.
     *
     *	@return	always <code>true</code>
     */
    public final static boolean updateTimestamp(Object object, Object attribute, String key, NSDictionary params){
        NSKeyValueCoding.Utility.takeValueForKey(object, new NSTimestamp(), key);
        return true;
    }
    

}
