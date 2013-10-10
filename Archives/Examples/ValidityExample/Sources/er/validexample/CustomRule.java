package er.validexample;
//
// CustomRule.java
// Project ValidityExample
//
// Created by msacket on Mon Jun 11 2001
//

import com.webobjects.foundation.NSDictionary;
import com.webobjects.foundation.NSKeyValueCoding;

public class CustomRule {

    //Example of a custome rule.  It is used by the password attribute.
    public final static boolean toLowerCase(Object object, Object attribute, String key, NSDictionary params){
        if(attribute instanceof String){
            NSKeyValueCoding.Utility.takeValueForKey( object, ((String)attribute).toLowerCase(), key);
            return true;
        }
        return false;
    }

}
