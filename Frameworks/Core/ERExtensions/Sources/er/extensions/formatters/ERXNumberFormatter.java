/*
 * Copyright (C) NetStruxr, Inc. All rights reserved.
 *
 * This software is published under the terms of the NetStruxr 
 * Public Software License version 0.5, a copy of which has been
 * included with this distribution in the LICENSE.NPL file.  */
package er.extensions.formatters;

import java.math.BigDecimal;
import java.math.BigInteger;
import java.text.FieldPosition;
import java.text.Format;
import java.util.Hashtable;

import com.webobjects.foundation.NSNumberFormatter;

import er.extensions.foundation.ERXProperties;
import er.extensions.localization.ERXLocalizer;

/**
 * An extension to the number formatter. It
 * will strip out the characters '%$,' when parsing
 * a string and can scale values by setting a pattern like 
 * <code>(/1024=)0.00 KB</code> which will divide the actual value by 1024 or
 * <code>(*60;4=)0.00</code> which will multiply the actual value by 60. 
 * When used for parsing, the resulting value will be scaled to a scale of 4. 
 * So when the real value is 0.0165, the display value will be 0.99 and 
 * when this is re-entered, the resulting value will again be 0.0165.
 */
public class ERXNumberFormatter extends NSNumberFormatter {
	/**
	 * Do I need to update serialVersionUID?
	 * See section 5.6 <cite>Type Changes Affecting Serialization</cite> on page 51 of the 
	 * <a href="http://java.sun.com/j2se/1.4/pdf/serial-spec.pdf">Java Object Serialization Spec</a>
	 */
	private static final long serialVersionUID = 1L;

	/** holds a reference to the repository */
	private static Hashtable _repository = new Hashtable();
	protected static final String DefaultKey = "ERXNumberFormatter.DefaultKey";
	
	static {
		_repository.put(DefaultKey, new ERXNumberFormatter());
	}
	
	private String _ignoredChars = ERXProperties.stringForKeyWithDefault("er.extensions.ERXNumberFormatter.ignoredChars", "%$");
    private Integer _scale;
    private BigDecimal _factor;
	private String _operator;
    private String _stringForNegativeInfinity = "-Inf";
    private String _stringForPositiveInfinity = "+Inf";
	 
    /**
     * Returns the default shared instance
     * @return shared instance
     */
    public static NSNumberFormatter sharedInstance() {
         return numberFormatterForPattern(DefaultKey);
    }

	/**
	 * @param object
	 */
	public static Format defaultNumberFormatterForObject(Object object) {
		Format result = null;
		if(object != null && !(object instanceof String)) {
			if((object instanceof Double) || (object instanceof BigDecimal) || (object instanceof Float))
				result = numberFormatterForPattern("#,##0.00;-(#,##0.00)");
			else if(object instanceof Number)
				result = numberFormatterForPattern("0");
		}
		return result;
	}

	/**
     * Returns a shared instance for the specified pattern.
     * @return shared instance of formatter
     */
    public static NSNumberFormatter numberFormatterForPattern(String pattern) {
    	NSNumberFormatter formatter;
    	if(ERXLocalizer.useLocalizedFormatters()) {
    		ERXLocalizer localizer = ERXLocalizer.currentLocalizer();
    		formatter = (NSNumberFormatter)localizer.localizedNumberFormatForKey(pattern);
    	} else {
    		formatter = (NSNumberFormatter)_repository.get(pattern);
    		if(formatter == null) {
    			formatter = new ERXNumberFormatter(pattern);
    			_repository.put(pattern, formatter);
    		}
    	}
    	return formatter;
    }
    
    /**
     * Sets a shared instance for the specified pattern.
     */
    public static void setNumberFormatterForPattern(NSNumberFormatter formatter, String pattern) {
    	if(ERXLocalizer.useLocalizedFormatters()) {
    		ERXLocalizer localizer = ERXLocalizer.currentLocalizer();
    		localizer.setLocalizedNumberFormatForKey(formatter, pattern);
    	} else {
    		if(formatter == null) {
    			_repository.remove(pattern);
    		} else {
    			_repository.put(pattern, formatter);
    		}
    	}
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
	
	protected void setScale(Integer value) {
		_scale = value;
	}
	
	/**
	 * Overridden to search the pattern for operators and factors. The pattern should be
	 * <code>'(' . operatorChar . factor . [';' scale ] . '=)' normalFormatterString</code>
	 * @see com.webobjects.foundation.NSNumberFormatter#setPattern(java.lang.String)
	 */
	@Override
	public void setPattern(String pattern) {
		int offset = pattern == null ? -1 : pattern.indexOf("=)");
		if(offset != -1) {
			try {
			    String factorString = pattern.substring(2, offset);
			    int scaleOffset = factorString.indexOf(";");
			    if(scaleOffset >= 0) {
			        String scaleString = factorString.substring(scaleOffset+1);
			        Integer scale = Integer.valueOf(scaleString);
			        setScale(scale);
			        factorString = factorString.substring(0,scaleOffset);
			    }
				setFactor(new BigDecimal(factorString));
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
	 */
	protected BigDecimal performFormat(BigDecimal value) {
		if("*".equals(_operator)) {
			value = value.multiply(_factor);
		} else if("/".equals(_operator)) {
		    int scale = _scale == null ? value.scale() : _scale.intValue();
			value = value.divide(_factor, scale, BigDecimal.ROUND_HALF_EVEN);
		}
		return value;
	}
	
	/**
	 * Override this in your subclass to provide for other operations when parsing a value.
	 * @param value
	 */
	protected BigDecimal performParse(BigDecimal value) {
		if("*".equals(_operator)) {
		    int scale = _scale == null ? value.scale() : _scale.intValue();
			value = value.divide(_factor, scale, BigDecimal.ROUND_HALF_EVEN);
		} else if("/".equals(_operator)) {
			value = value.multiply(_factor);
		}
		return value;
	}
	
	/**
     * Strips out the ignored characters and optionally performs an operation on the value
     * from the string to be parsed.
     * @param aString to be parsed
     * @return the parsed object
     */
    @Override
    public Object parseObject(String aString) throws java.text.ParseException {
        char[] chars = aString.toCharArray();
        char[] filteredChars = new char[chars.length];
        int count = 0;
        for (int i = 0; i < chars.length; i++) {
            if (_ignoredChars.indexOf(chars[i]) < 0) {
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
        	
        	if(result instanceof BigInteger && !(result instanceof BigDecimal)) {
        		result = new BigInteger("" + newValue.intValue());
        	} else {
        		result = newValue;
        	}
        }
        return result;
    }
    
    /**
     * Overridden to perform optional conversions on the value given.
     * @see java.text.Format#format(java.lang.Object, java.lang.StringBuffer, java.text.FieldPosition)
     */
    @Override
    public StringBuffer format(Object value, StringBuffer buffer, FieldPosition position) {
    	if (value instanceof Number && _operator != null) {
    		BigDecimal newValue = null;
    		if(value instanceof BigDecimal) {
    			newValue = (BigDecimal)value;
    		} else {
    			newValue = new BigDecimal(((Number)value).doubleValue());
    		}
    		
    		// HACK ak: if we get an integer, we add a few digits to the right, 
    		// because the BigDecimal constructor will have a scale of zero
    		// FIXME: we should actually find out how many digits we need to display
    		if(newValue.scale() == 0) {
    			newValue = newValue.setScale(4);
    		}
    		
     		newValue = performFormat(newValue);
     		value = newValue;
    	}
    	// handling for NaN and Infinity
    	if (value instanceof Double) {
    		Double doubleValue = (Double) value;
    		if (doubleValue.isNaN()) {
    			return buffer.append(stringForNotANumber());
    		} else if (doubleValue == Double.NEGATIVE_INFINITY) {
    			return buffer.append(stringForNegativeInfinity());
    		} else if (doubleValue == Double.POSITIVE_INFINITY) {
    			return buffer.append(stringForPositiveInfinity());
    		}
    	} else if (value instanceof Float) {
    		Float floatValue = (Float) value;
    		if (floatValue.isNaN()) {
    			return buffer.append(stringForNotANumber());
    		} else if (floatValue == Float.NEGATIVE_INFINITY) {
    			return buffer.append(stringForNegativeInfinity());
    		} else if (floatValue == Float.POSITIVE_INFINITY) {
    			return buffer.append(stringForPositiveInfinity());
    		}
    	}
    	return super.format(value, buffer, position);
    }

    public String stringForNegativeInfinity() {
    	return _stringForNegativeInfinity;
    }

	public void setStringForNegativeInfinity(String newString) {
		if (newString == null) {
			throw new IllegalArgumentException("The string for Negative Infinity must not be null");
		}
		this._stringForNegativeInfinity = newString;
	}

    public String stringForPositiveInfinity() {
    	return _stringForPositiveInfinity;
    }

	public void setStringForPositiveInfinity(String newString) {
		if (newString == null) {
			throw new IllegalArgumentException("The string for Positive Infinity must not be null");
		}
		this._stringForPositiveInfinity = newString;
	}
}
