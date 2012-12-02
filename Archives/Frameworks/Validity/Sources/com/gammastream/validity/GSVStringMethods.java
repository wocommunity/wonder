package com.gammastream.validity;

import java.math.BigDecimal;

import com.gammastream.gammacore.gammatext.GSTextUtilities;
import com.webobjects.foundation.NSDictionary;
import com.webobjects.foundation.NSKeyValueCoding;
import com.webobjects.foundation.NSLog;
import com.webobjects.foundation.NSMutableDictionary;

/**
 *	This class provides a set of predefined rules for performing
 *	validation on <code>Strings</code>. These rules are part of
 *	the default set of 'QuickRules'.
 * 
 *	@author GammaStream Technologies, Inc.
 */
public class GSVStringMethods {

    /**
     *	For programatic purposes, we include this constant which is used for the <code>compareTo</code> method.
     */
    public final static String EQUAL = "==";
    
    /**
     *	For programatic purposes, we include this constant which is used for the <code>compareTo</code> method.
     */
    public final static String NOT_EQUAL = "!=";
    
    /**
     *	For programatic purposes, we include this constant which is used for the <code>compareTo</code> method.
     */
    public final static String GREATER_THAN = ">";
    
    /**
     *	For programatic purposes, we include this constant which is used for the <code>compareTo</code> method.
     */
    public final static String GREATER_EQUAL = ">=";
    
    /**
     *	For programatic purposes, we include this constant which is used for the <code>compareTo</code> method.
     */
    public final static String LESS_THAN = "<";
    
    /**
     *	For programatic purposes, we include this constant which is used for the <code>compareTo</code> method.
     */
    public final static String LESS_EQUAL = "<=";

    /**
     *	Determines whether the specified attribute is empty.
     *	<br>An empty String is defined as a String that contains at least one non-white-space character.
     *	<br>(i.e. This method will return <code>true</code> if the attribute only contains spaces, CRs, NLs, etc.)
     *
     *	@param	object		The object whose attribute is being validated.
     *	@param	attribute 	The attribute being validated.
     *	@param	key 		The key used to access the attribute.
     *	@param	params 		The param dictionary which must contain the above mentioned key-value pairs.
     *
     *	@return	<code>true</code> if the string is empty; otherwise, <code>false</code>
     */
    public final static boolean isStringEmpty(Object object, Object attribute, String key, NSDictionary params){
        if(attribute instanceof String){
            return GSTextUtilities.isStringEmpty( (String)attribute );
        }
        return true;
    }
    
    /**
     *	One of the many 'mutators' which never fail, unless of course, an exception is thrown.
     *	<br>A mutator simply modifies (or mutates) the attribute in some way.
     *	<br>In this case, it converts the <code>String</code> to all upper case characters.
     *
     *	@param	object		The object whose attribute is being validated.
     *	@param	attribute 	The attribute being validated.
     *	@param	key 		The key used to access the attribute.
     *	@param	params 		The param dictionary which must contain the above mentioned key-value pairs.
     *
     *	@return	always <code>true</code>
     */
    public final static boolean toUpperCase(Object object, Object attribute, String key, NSDictionary params){
        if( (attribute instanceof String) && (attribute != null) ){
            NSKeyValueCoding.Utility.takeValueForKey(object, ((String)attribute).toUpperCase(), key);
        }
        return true;
    }
    
    /**
     *	One of the many 'mutators' which never fail, unless of course, an exception is thrown.
     *	<br>A mutator simply modifies (or mutates) the attribute is some way.
     *	<br>In this case, it converts the <code>String</code> to all lower case characters.
     *
     *	@param	object		The object whose attribute is being validated.
     *	@param	attribute 	The attribute being validated.
     *	@param	key 		The key used to access the attribute.
     *	@param	params 		The param dictionary which must contain the above mentioned key-value pairs.
     *
     *	@return	always <code>true</code>
     */
    public final static boolean toLowerCase(Object object, Object attribute, String key, NSDictionary params){
        if( (attribute instanceof String) && (attribute != null) ){
            NSKeyValueCoding.Utility.takeValueForKey(object, ((String)attribute).toLowerCase(), key);
        }
        return true;
    }

    /**
     * Compares the length of the attribute.
     * <br>NSDictionary key/value pairs:
     * <br>Operator=""
     * <br>RightOperand=""
     *
     * @return	true or false
     * @param  	object		object being validated.
     * @param  	attribute 	attribute being validated.
     * @param	key 		key for attribute.
     * @param	params 		extra parameters.
     */
     
    /**
     *	Determines whether the specified string is of a specified length. (i.e. 'x' characters long)
     *	<br>
     *	<br>The required key-value pairs include:
     *	<br>"Operator" = One of the defined operator constants.
     *	<br>"RightOperand" = A number representing the number of characters one is interested.
     *	<br>
     *
     *	@param	object		The object whose attribute is being validated.
     *	@param	attribute 	The attribute being validated.
     *	@param	key 		The key used to access the attribute.
     *	@param	params 		The param dictionary which must contain the above mentioned key-value pairs.
     *
     *	@return	<code>true</code> if the comparison succeeds; otherwise, <code>false</code>
     */
    public final static boolean length(Object object, Object attribute, String key, NSDictionary params){
        if(attribute instanceof String){
            String sign = (String)params.objectForKey("Operator");
            String number = (String)params.objectForKey("RightOperand");
            BigDecimal left = new BigDecimal( ((String)attribute).length() );
            BigDecimal right = new BigDecimal(number);
            int comparisonValue = left.compareTo( right );

            if( sign.equals(GSVStringMethods.EQUAL) ){
                return ( comparisonValue == 0 );
            } else if( sign.equals(GSVStringMethods.NOT_EQUAL) ){
                return ( comparisonValue != 0 );
            } else if( sign.equals(GSVStringMethods.GREATER_THAN) ){
                return ( comparisonValue == 1 );
            } else if( sign.equals(GSVStringMethods.GREATER_EQUAL) ){
                return ( (comparisonValue == 1) || (comparisonValue == 0) );
            } else if( sign.equals(GSVStringMethods.LESS_THAN) ){
                return ( comparisonValue == -1 );
            } else if( sign.equals(GSVStringMethods.LESS_EQUAL) ){
                return ( (comparisonValue == -1) || (comparisonValue == 0) );
            }
        }
        return false;
    }

    /**
        *	Determines whether the specified string is of a specified length. (i.e. 'x' characters long)
     *	<br>
     *	<br>The required key-value pairs include:
     *	<br>"minLength" = A number representing the number of characters one is interested.
     *	<br>
     *
     *	@param	object		The object whose attribute is being validated.
     *	@param	attribute 	The attribute being validated.
     *	@param	key 		The key used to access the attribute.
     *	@param	params 		The param dictionary which must contain the above mentioned key-value pairs.
     *
     *	@return	<code>true</code> if the comparison succeeds; otherwise, <code>false</code>
     */
    public final static boolean minLength(Object object, Object attribute, String key, NSDictionary params) {
        NSMutableDictionary mparams = params.mutableClone();
        mparams.setObjectForKey(">", "Operator");
        mparams.setObjectForKey(params.objectForKey("minLength"), "RightOperand");
        return length(object, attribute, key, mparams);
    }

    /**
        *	Determines whether the specified string is of a specified length. (i.e. 'x' characters long)
     *	<br>
     *	<br>The required key-value pairs include:
     *	<br>"maxLength" = A number representing the number of characters one is interested.
     *	<br>
     *
     *	@param	object		The object whose attribute is being validated.
     *	@param	attribute 	The attribute being validated.
     *	@param	key 		The key used to access the attribute.
     *	@param	params 		The param dictionary which must contain the above mentioned key-value pairs.
     *
     *	@return	<code>true</code> if the comparison succeeds; otherwise, <code>false</code>
     */
    public final static boolean maxLength(Object object, Object attribute, String key, NSDictionary params) {
        NSMutableDictionary mparams = params.mutableClone();
        mparams.setObjectForKey("<", "Operator");
        mparams.setObjectForKey(params.objectForKey("maxLength"), "RightOperand");
        return length(object, attribute, key, mparams);
    }


    /**
     *	Verifies that the attribute contains the specified string.
     *	<br>
     *	<br>The required key-value pairs include:
     *	<br>"Contains" = The string of interest.
     *	<br>
     *
     *	@param	object		The object whose attribute is being validated.
     *	@param	attribute 	The attribute being validated.
     *	@param	key 		The key used to access the attribute.
     *	@param	params 		The param dictionary which must contain the above mentioned key-value pairs.
     *
     *	@return	<code>true</code> if the attribute contains the specified string; otherwise, <code>false</code>
     */   
    public final static boolean contains(Object object, Object attribute, String key, NSDictionary params){
        if(attribute instanceof String && params != null){
            String what = (String)params.objectForKey("Contains");
            String attributeAtString = (String)attribute;
            return ( attributeAtString.indexOf(what) != -1 );
        }
        return false;
    }

    /**
     *	Verifies that the attribute ends with the specified string.
     *	<br>
     *	<br>The required key-value pairs include:
     *	<br>"EndsWith" = The string of interest.
     *	<br>
     *
     *	@param	object		The object whose attribute is being validated.
     *	@param	attribute 	The attribute being validated.
     *	@param	key 		The key used to access the attribute.
     *	@param	params 		The param dictionary which must contain the above mentioned key-value pairs.
     *
     *	@return	<code>true</code> if the attribute ends with the specified string; otherwise, <code>false</code>
     */
    public final static boolean endsWith(Object object, Object attribute, String key, NSDictionary params){
        if(attribute instanceof String){
            return(((String)attribute).endsWith((String)params.objectForKey("EndsWith")));
        }
        return false;
    }
    
    /**
     *	Verifies that the attribute starts with the specified string.
     *	<br>
     *	<br>The required key-value pairs include:
     *	<br>"StartsWith" = The string of interest.
     *	<br>
     *
     *	@param	object		The object whose attribute is being validated.
     *	@param	attribute 	The attribute being validated.
     *	@param	key 		The key used to access the attribute.
     *	@param	params 		The param dictionary which must contain the above mentioned key-value pairs.
     *
     *	@return	<code>true</code> if the attribute starts with the specified string; otherwise, <code>false</code>
     */
    public final static boolean startsWith(Object object, Object attribute, String key, NSDictionary params){
        if(attribute instanceof String)
            return(((String)attribute).startsWith((String)params.objectForKey("StartsWith")));
        return false;
    }
    
    /**
     *	Verifies that the attribute contains only alphabetic characters. (i.e. 'a'-'z' || 'A'-'Z')
     *
     *	@param	object		The object whose attribute is being validated.
     *	@param	attribute 	The attribute being validated.
     *	@param	key 		The key used to access the attribute.
     *	@param	params 		The param dictionary which must contain the above mentioned key-value pairs.
     *
     *	@return	<code>true</code> if the string only contains alphabetic characters; otherwise, <code>false</code>
     */
    public final static boolean isAlphabetic(Object object, Object attribute, String key, NSDictionary params){
        if(attribute instanceof String){
            return(GSTextUtilities.isAlphabetic((String)attribute));
        }
        return false;
    }

    /**
     *	Verifies that the attribute contains only letters or numbers. (i.e. 'a'-'z' || 'A'-'Z' || '0'-'9')
     *
     *	@param	object		The object whose attribute is being validated.
     *	@param	attribute 	The attribute being validated.
     *	@param	key 		The key used to access the attribute.
     *	@param	params 		The param dictionary which must contain the above mentioned key-value pairs.
     *
     *	@return	<code>true</code> if the string only contains letters or numbers; otherwise, <code>false</code>
     */
    public final static boolean isAlphaNumeric(Object object, Object attribute, String key, NSDictionary params){
        if(attribute instanceof String){
            return(GSTextUtilities.isAlphaNumeric((String)attribute));
        }
        return false;
    }

    /**
     *	One of the many 'mutators' which never fail, unless of course, an exception is thrown.
     *	<br>A mutator simply modifies (or mutates) the attribute is some way.
     *	<br>In this case, it strips any HTML out of the <code>String</code>.
     *	<br>HTML is considered anything (and including) '<' and '>'.
     *
     *	@param	object		The object whose attribute is being validated.
     *	@param	attribute 	The attribute being validated.
     *	@param	key 		The key used to access the attribute.
     *	@param	params 		The param dictionary which must contain the above mentioned key-value pairs.
     *
     *	@return	always <code>true</code>
     */
    public final static boolean stripHTML(Object object, Object attribute, String key, NSDictionary params){
        if(attribute instanceof String && attribute !=null){
            NSKeyValueCoding.Utility.takeValueForKey(object,GSTextUtilities.stringStrippedOfHTML((String)attribute), key);
        }
        return true;
    }
    
    /**
     *	Performs a string comparision using the specified params dictionary.
     *	<br>
     *	<br>The required key-value pairs include:
     *	<br>"Operator" = The specified operator string. (i.e. "==", "!=", ">", ">=", "<", or "<=" )
     *	<br>"RightOperand" = The <code>String</code> to compare the attribute to.
     *	<br>
     *
     *	@param	object		The object whose attribute is being validated.
     *	@param	attribute 	The attribute being validated.
     *	@param	key 		The key used to access the attribute.
     *	@param	params 		The param dictionary which must contain the above mentioned key-value pairs.
     *
     *	@return	<code>true</code> if the comparision succeeds; otherwise, <code>false</code>
     */
    public final static boolean compareTo(Object object, Object attribute, String key, NSDictionary params){
        if(attribute instanceof String){
            String sign = (String)params.objectForKey("Operator");
            String right = (String)params.objectForKey("RightOperand");
            int comparisonValue = ((String)attribute).compareTo(right);

            if(sign.equals(GSVStringMethods.EQUAL)){
                return(comparisonValue==0);
            }else if(sign.equals(GSVStringMethods.NOT_EQUAL)){
                return(comparisonValue!=0);
            }else if(sign.equals(GSVStringMethods.GREATER_THAN)){
                return(comparisonValue>0);
            }else if(sign.equals(GSVStringMethods.GREATER_EQUAL)){
                return(comparisonValue>=0);
            }else if(sign.equals(GSVStringMethods.LESS_THAN)){
                return(comparisonValue<0);
            }else if(sign.equals(GSVStringMethods.LESS_EQUAL)){
                return(comparisonValue<=0);
            }
        }
        return false;
    }

    /**
	*	Verifies the attribute is a valid url. (Proper Syntax)
     *	<br>The validator will only verify 'http' urls.
     *	<br>
     *
     *	@param	object		The object whose attribute is being validated.
     *	@param	attribute 	The attribute being validated.
     *	@param	key 		The key used to access the attribute.
     *	@param	params 		The param dictionary which must contain the above mentioned key-value pairs.
     *
     *	@return	<code>true</code> if the url is valid; otherwise, <code>false</code>
     */
    public final static boolean isValidURL(Object object, Object attribute, String key, NSDictionary params){
        if(attribute instanceof String) {
            try {
                java.net.URL url = new java.net.URL((String)attribute);
                // accept only http-urls
                return url.getProtocol().equals("http");

            } catch(java.net.MalformedURLException mue) {
                //ignore
                NSLog.err.appendln(mue.getMessage());
            }
        }

        return false;
    }

    /**
	*	Verifies the attribute is a valid email address. (Proper Syntax)
     *
     *	@param	object		The object whose attribute is being validated.
     *	@param	attribute 	The attribute being validated.
     *	@param	key 		The key used to access the attribute.
     *	@param	params 		The param dictionary which must contain the above mentioned key-value pairs.
     *
     *	@return	<code>true</code> if the address is valid; otherwise, <code>false</code>
     */
    public final static boolean isValidEmailAddress(Object object, Object attribute, String key, NSDictionary params){
        if(attribute instanceof String) {
            try {
                javax.mail.internet.InternetAddress add = new javax.mail.internet.InternetAddress((String)attribute);
                add.validate();
                // get sure it is only the address and no personal
                return (add.getPersonal() == null);

            } catch(javax.mail.internet.AddressException ae) {
                //ignore
                NSLog.err.appendln(ae.getMessage());
            }
        }

        return false;
    }
}
