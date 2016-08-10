package com.gammastream.validity;

import java.math.BigDecimal;

import com.webobjects.foundation.NSDictionary;
import com.webobjects.foundation.NSLog;

/**
 *	This class provides a set of predefined rules for performing
 *	validation on <code>Numbers</code>. These rules are part of
 *	the default set of 'QuickRules'.
 * 
 *	@author GammaStream Technologies, Inc.
 */
public class GSVNumberMethods {

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
     *	Compares the specified attribute to a number provided in the params dictionary.
     *	<br>
     *	<br>The required key-value pairs include:
     *	<br>"Operator" = The specified operator string. (i.e. "==", "!=", ">", ">=", "<", or "<=" )
     *	<br>"RightOperand" = The number to compare the attribute to. (i.e. 0, 20, etc.)
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
        if(attribute instanceof Number){
            String sign = (String)params.objectForKey("Operator");
            String number = (String)params.objectForKey("RightOperand");
            BigDecimal left = new BigDecimal(((Number)attribute).doubleValue());
            BigDecimal right = new BigDecimal(number);
            int comparisonValue=left.compareTo(right);

            if(sign.equals(GSVNumberMethods.EQUAL)){
                return(comparisonValue==0);
            } else if(sign.equals(GSVNumberMethods.NOT_EQUAL)){
                return(comparisonValue!=0);
            } else if(sign.equals(GSVNumberMethods.GREATER_THAN)){
                return(comparisonValue==1);
            } else if(sign.equals(GSVNumberMethods.GREATER_EQUAL)){
                return(comparisonValue==1 || comparisonValue==0);
            } else if(sign.equals(GSVNumberMethods.LESS_THAN)){
                return(comparisonValue==-1);
            } else if(sign.equals(GSVNumberMethods.LESS_EQUAL)){
                return(comparisonValue==-1 || comparisonValue==0);
            }
        }
        return false;
    }
	
    /**
     *	Checks to make sure the attribute falls within the range specified in the params dictionary.
     *	<br>The attribute is allowed to equal the 'Low' or 'High' value.
     *	<br>
     *	<br>The required key-value pairs include:
     *	<br>"Low" = The lowest possible value for this attribute.
     *	<br>"High" = The highest possible value for this attribute.
     *	<br>
     *
     *	@param	object		The object whose attribute is being validated.
     *	@param	attribute 	The attribute being validated.
     *	@param	key 		The key used to access the attribute.
     *	@param	params 		The param dictionary which must contain the above mentioned key-value pairs.
     *
     *	@return	<code>true</code> if the provided number false within the specified range; otherwise, <code>false</code>
     */
    public final static boolean isInRange(Object object, Object attribute, String key, NSDictionary params){
        if(attribute instanceof Number){
            try {
                BigDecimal low = new BigDecimal((String)params.objectForKey("Low"));
                BigDecimal high = new BigDecimal((String)params.objectForKey("High"));
                BigDecimal number = new BigDecimal(((Number)attribute).doubleValue());
                int comparisonValueLow=low.compareTo(number);
                int comparisonValueHigh=high.compareTo(number);
                if(comparisonValueLow==-1 || comparisonValueLow==0){
                    if(comparisonValueHigh==1 || comparisonValueHigh==0){
                        return true;
                    }
                }
            } catch(Exception e){
                NSLog.err.appendln(e.getMessage());
            }
        }
        return false;
    }


    /**
     *	Verifies that attribute is a positive number.
     *
     *	@param	object		The object whose attribute is being validated.
     *	@param	attribute 	The attribute being validated.
     *	@param	key 		The key used to access the attribute.
     *	@param	params 		The param dictionary which must contain the above mentioned key-value pairs.
     *
     *	@return	<code>true</code> if the provided number is positive value; otherwise, <code>false</code>
     */
    public final static boolean isPositiveNumber(Object object,Object attribute,String key, NSDictionary params){
        if(attribute instanceof Number){
            return(((Number)attribute).intValue()>=0);
        }
        return false;
    }
    
    /**
     *	Verifies that attribute is a negative number.
     *
     *	@param	object		The object whose attribute is being validated.
     *	@param	attribute 	The attribute being validated.
     *	@param	key 		The key used to access the attribute.
     *	@param	params 		The param dictionary which must contain the above mentioned key-value pairs.
     *
     *	@return	<code>true</code> if the provided number is a negative value; otherwise, <code>false</code>
     */
    public final static boolean isNegativeNumber(Object object,Object attribute,String key, NSDictionary params){
        if(attribute instanceof Number){
            return(((Number)attribute).intValue()<0);
        }
        return false;
    }

}
