/*
 * Copyright (C) NetStruxr, Inc. All rights reserved.
 *
 * This software is published under the terms of the NetStruxr 
 * Public Software License version 0.5, a copy of which has been
 * included with this distribution in the LICENSE.NPL file.  */
package er.extensions;

import com.webobjects.foundation.*;

import java.math.BigDecimal;
import java.math.BigInteger;
import java.text.*;

/**
 * An extension to the number formatter. It
 * will strip out the characters '%$,' when parsing
 * a string and can scale values by setting a pattern like 
 * <code>(/1024=)0.00 KB</code> which will divide the actual value by 1024 or
 * <code>(*60=)0 seconds</code> which will multiply the actual value by 60.
 */
public class ERXNumberFormatter extends NSNumberFormatter {

    /** holds a reference to the shared instance */
    protected static ERXNumberFormatter _sharedInstance;
    private String _ignoredChars = "%$,";
    private BigDecimal _factor;
	private String _operator;
    
    /**
     * Returns the shared instance
     * @return shared instance
     */
    public static ERXNumberFormatter sharedInstance() {
        if (_sharedInstance == null)
            _sharedInstance = new ERXNumberFormatter();
        return _sharedInstance;
    }

    /**
     * Public constructor
     */
    public ERXNumberFormatter(String pattern) {
    	super(pattern);
    }

    /**
	 * 
	 */
	public ERXNumberFormatter() {
	}
	
	public void setIgnoredChars(String value) {
		_ignoredChars = value;
	}
	
	protected void setFactor(BigDecimal value) {
		_factor = value;
	}
	
	protected void setOperator(String value) {
		_operator = value;
	}
	
	/**
	 * Overridden to search the pattern for operators and factors. The pattern should be
	 * <code>'(' . operatorChar . factor . '=)' normalFormatterString</code>
	 * @see com.webobjects.foundation.NSNumberFormatter#setPattern(java.lang.String)
	 */
	public void setPattern(String pattern) {
		int offset = pattern.indexOf("=)");
		if(offset != -1) {
			try {
				setFactor(new BigDecimal(pattern.substring(2, offset)));
				setOperator(pattern.substring(1, 2));
				pattern = pattern.substring(offset+2);
			} catch(NumberFormatException e1) {
				throw new IllegalArgumentException("ERXNumberFormatter must have a pattern like '(*1024=)#,##0.00', where 1024 is the factor.");
			} catch(IndexOutOfBoundsException e) {
				throw new IllegalArgumentException("ERXNumberFormatter must have a pattern like '(*1024=)#,##0.00', where 1024 is the factor.");
			}
		} else {
			setFactor(null);
			setOperator(null);
		}
		super.setPattern(pattern);
	}
	
	
	/**
	 * Override this in your subclass to provide for other operations when formatting a value.
	 * @param value
	 * @return
	 */
	protected BigDecimal performFormat(BigDecimal value) {
		if("*".equals(_operator)) {
			value = value.divide(_factor, value.scale(), BigDecimal.ROUND_HALF_EVEN);
		} else if("/".equals(_operator)) {
			value = value.multiply(_factor);
		}
		return value;
	}
	
	/**
	 * Override this in your subclass to provide for other operations when parsing a value.
	 * @param value
	 * @return
	 */
	protected BigDecimal performParse(BigDecimal value) {
		if("*".equals(_operator)) {
			value = value.multiply(_factor);
		} else if("/".equals(_operator)) {
			value = value.divide(_factor, value.scale(), BigDecimal.ROUND_HALF_EVEN);
		}
		return value;
	}
	
	/**
     * Strips out the ignored characters and optionally performs an operation on the value
     * from the string to be parsed.
     * @param aString to be parsed
     * @return the parsed object
     */
    public Object parseObject(String aString) throws java.text.ParseException {
        char[] chars = aString.toCharArray();
        char[] filteredChars = new char[chars.length];
        int count = 0;
        for (int i = 0; i < chars.length; i++) {
            if (_ignoredChars.indexOf((int)chars[i]) < 0) {
                filteredChars[count++] = chars[i];
            }
        }
        String filteredString = new String(filteredChars, 0, count);
        Object result = super.parseObject(filteredString);
        if(result instanceof Number && _operator != null) {
        	BigDecimal newValue = null;
        	if(result instanceof BigDecimal) {
        		newValue = (BigDecimal)result;
        	} else {
        		newValue = new BigDecimal(((Number)result).doubleValue());
        	}
        	
        	newValue = performParse(newValue);
        	
        	if(result instanceof BigInteger) {
        		result = new BigInteger("" + newValue.intValue());
        	} else {
        		result = newValue;       		
        	}
        }
        return result;
    }
    
    /**
     *  Overridden to perform optional conversions on the value given.
     * @see java.text.Format#format(java.lang.Object, java.lang.StringBuffer, java.text.FieldPosition)
     */
    public StringBuffer format(Object value, StringBuffer buffer, FieldPosition position) {
    	if (value instanceof Number && _operator != null) {
    		BigDecimal newValue = null;
    		if(value instanceof BigDecimal) {
    			newValue = (BigDecimal)value;
    		} else {
    			newValue = new BigDecimal(((Number)value).doubleValue());
    		}
    		
    		newValue = performFormat(newValue);

    		if(value instanceof BigInteger) {
    			value = new BigInteger("" + newValue.intValue());
    		} else {
    			value = newValue;       		
    		}
    	}
    	return super.format(value, buffer, position);
    }
}
