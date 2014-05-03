/*
 * Copyright (C) NetStruxr, Inc. All rights reserved.
 *
 * This software is published under the terms of the NetStruxr
 * Public Software License version 0.5, a copy of which has been
 * included with this distribution in the LICENSE.NPL file.  */
package er.extensions.foundation;

import java.util.Enumeration;

import org.apache.log4j.Logger;

import com.webobjects.appserver.WOApplication;
import com.webobjects.foundation.NSArray;
import com.webobjects.foundation.NSForwardException;
import com.webobjects.foundation.NSKeyValueCoding;
import com.webobjects.foundation.NSKeyValueCodingAdditions;
import com.webobjects.foundation.NSMutableSet;

import er.extensions.logging.ERXPatternLayout;

/**
 * Very simple template parser.  For example if you have the delimiter:
 * {@literal @}{@literal @}, then a possible template might look like: "Hello, {@literal @}{@literal @}name{@literal @}{@literal @}.  How are
 * you feeling today?",  In this case the object will get asked for the
 * value name. This works with key-paths as well.
 * 
 * @property er.extensions.ERXSimpleTemplateParser.useOldDelimiter if false, only {@literal @}{@literal @} delimeters are supported (defaults to true)
 */
public class ERXSimpleTemplateParser {

    /** The default label for keys not found while parsing */
    public static final String DEFAULT_UNDEFINED_KEY_LABEL = "?";

    /** The default delimiter */
    public static final String DEFAULT_DELIMITER = "@@";

    /** The deprecated delimiter */
    @Deprecated
    private static final String DEPRECATED_DELIMITER = "@";

    /** logging support */
    public static final Logger log = Logger.getLogger(ERXSimpleTemplateParser.class.getName());

    /** holds a reference to the shared instance of the parser */
    private static ERXSimpleTemplateParser _sharedInstance;

    /**
     * Convience method to return the shared instance
     * of the template parser.
     * 
     * @return shared instance of the parser
     * @see #setSharedInstance
     */
    public static ERXSimpleTemplateParser sharedInstance() {
        if (_sharedInstance == null) 
            setSharedInstance(new ERXSimpleTemplateParser());
        return _sharedInstance;
    }
    
    /**
     * Sets the shared instance of the template parser.
     * 
     * @param newSharedInstance  the parser object that will be shared
     * @see #sharedInstance
     */
    public static synchronized void setSharedInstance(ERXSimpleTemplateParser newSharedInstance) {
        _sharedInstance = newSharedInstance;
    } 
    
    /** 
     * Flag to disable logging. {@link ERXPatternLayout} will set 
     * this to true for its internal parser object in order to 
     * prevent an infinite debug logging loop. 
     */ 
    public boolean isLoggingDisabled = false;

    /** The label that will be appeared where an undefined key is found */ 
    private final String _undefinedKeyLabel;

    /** Defines if @ can be used as alternative delimiter */
    private Boolean _useOldDelimiter;

    
    /** 
     * Returns a parser object with the default undefined label
     * 
     * @see #DEFAULT_UNDEFINED_KEY_LABEL
     */
    public ERXSimpleTemplateParser() {
        this(DEFAULT_UNDEFINED_KEY_LABEL);
    }

    /** 
     * Returns a parser object with the given string as the undefined key label 
     * 
     * @param undefinedKeyLabel  string as the undefined key label, 
     *                            for example, "?", "N/A"
     */
    public ERXSimpleTemplateParser(String undefinedKeyLabel) {
        super();
        _undefinedKeyLabel = (undefinedKeyLabel == null ? DEFAULT_UNDEFINED_KEY_LABEL : undefinedKeyLabel);
    }

    /** 
     * Returns a parser object with the given string as the undefined key label. 
     * Depending on useOldDelimiter value @ can be used as delimiter if @@ is not present 
     * in the template.
     * 
     * @param undefinedKeyLabel  string as the undefined key label, 
     *                            for example, "?", "N/A"
     * @param useOldDelimiter   boolean defining if @ is used as delimiter if @@ is not available in the template
     */
    public ERXSimpleTemplateParser(String undefinedKeyLabel, boolean useOldDelimiter) {
        this(undefinedKeyLabel);
        _useOldDelimiter = Boolean.valueOf(useOldDelimiter);
    }
    
    protected boolean useOldDelimiter() {
        if (_useOldDelimiter == null) {
            _useOldDelimiter = Boolean.valueOf(ERXProperties.booleanForKeyWithDefault("er.extensions.ERXSimpleTemplateParser.useOldDelimiter", true));
        }
        return _useOldDelimiter.booleanValue();
    }
    
    /**
     * Calculates the set of keys used in a given template
     * for a given delimiter.
     * 
     * @param template to check for keys
     * @param delimiter for finding keys
     * @return array of keys
     */
    public NSArray keysInTemplate(String template, String delimiter) {
        NSMutableSet keys = new NSMutableSet();
        if (delimiter == null) {
            delimiter = DEFAULT_DELIMITER;
        }
        NSArray components = NSArray.componentsSeparatedByString(template, delimiter);
        if (! isLoggingDisabled  &&  log.isDebugEnabled()) {
            log.debug("Components: " + components);
        }
        boolean deriveElement = false; // if the template starts with delim, the first component will be a zero-length string
        for (Enumeration e = components.objectEnumerator(); e.hasMoreElements();) {
            String element = (String)e.nextElement();
            if (deriveElement) {
                if (element.length() == 0) {
                    throw new IllegalArgumentException("\"\" is not a valid keypath");
                }
                keys.addObject(element);
                deriveElement = false;
            } else {
                deriveElement = true;
            }
        }
        return keys.allObjects();
    }    

    /**
     * Cover method for calling the four argument method
     * passing in <code>null</code> for the <code>otherObject</code>
     * parameter. See that method for documentation.
     * 
     * @param template to use to parse
     * @param delimiter to use to find keys
     * @param object to resolve keys
     * @return parsed template with keys replaced
     */
    public String parseTemplateWithObject(String template, String delimiter, Object object) {
        return parseTemplateWithObject(template,
                                       delimiter,
                                       object,
                                       null);
    }
    
    /**
     * This method replaces the keys enclosed between the
     * delimiter with the values found in object and otherObject.
     * It first looks for a value in object, and then in otherObject
     * if the key is not found in object. Therefore, otherObject is
     * a good place to store default values while object is a
     * good place to override default values. 
     * <p>
     * When the value is not found in both object and otherObject, 
     * it will replace the key with the undefined key label which 
     * defaults to "?". You can set the label via the constructor 
     * {@link #ERXSimpleTemplateParser(String)}. Note that a <code>null</code> 
     * result will also output the label, so you might want to have the empty
     * string as the undefined key label.
     * 
     * @param template to use to parse
     * @param delimiter to use to check for keys
     * @param object to resolve keys off of
     * @param otherObject object used to resolve default keys
     * @return parsed template with keys replaced
     */
    public String parseTemplateWithObject(String template, String delimiter, Object object, Object otherObject) {
        if (template == null)
            throw new IllegalArgumentException("Attempting to parse null template!");
        if (object == null) {
            throw new IllegalArgumentException("Attempting to parse template with null object!");
        }
        if (delimiter == null) {
            delimiter = DEFAULT_DELIMITER;
        }
        if (! isLoggingDisabled  &&  log.isDebugEnabled()) {
            log.debug("Parsing template: " + template + " with delimiter: " + delimiter + " object: " + object);
            log.debug("Template: " + template);
            log.debug("Delim: " + delimiter);
            log.debug("otherObject: " + otherObject);
        }
        if (useOldDelimiter() && delimiter.equals(DEFAULT_DELIMITER) && template.indexOf(delimiter) < 0 && template.indexOf(DEPRECATED_DELIMITER) >= 0) {
            if (!isLoggingDisabled) {
                log.warn("It seems that the template string '" + template + "' is using the old delimiter '@' instead of '@@'. I will use '@' for now but you should fix this by updating the template.");
            }
            delimiter = DEPRECATED_DELIMITER;
        }
        NSArray components = NSArray.componentsSeparatedByString(template, delimiter);
        if (! isLoggingDisabled  &&  log.isDebugEnabled()) {
            log.debug("Components: " + components);
        }
        boolean deriveElement = false; // if the template starts with delim, the first component will be a zero-length string
        StringBuilder sb = new StringBuilder();
        Object objects[];
        if (otherObject != null) {
            objects = new Object[] {object, otherObject};
        } else {
            objects = new Object[] {object};
        }
        for (Enumeration e = components.objectEnumerator(); e.hasMoreElements();) {
            String element = (String)e.nextElement();
            if(!isLoggingDisabled) {
                log.debug("Processing Element: " + element);
            }
            if(deriveElement) {
                if(!isLoggingDisabled) {
                    log.debug("Deriving value ...");
                }
                if(element.length() == 0) {
                    throw new IllegalArgumentException("\"\" is not a valid keypath in template: " + template);
                }
                Object result = _undefinedKeyLabel;
                for (int i = 0; i < objects.length; i++) {
                    Object o = objects[i];
                    if(o != null && result == _undefinedKeyLabel) {
                        try {
                            if(!isLoggingDisabled && log.isDebugEnabled()) {
                                log.debug("calling valueForKeyPath("+o+", "+element+")");
                            }
                            result = doGetValue(element, o);
                            // For just in case the above doesn't throw an exception when the 
                            // key is not defined. (NSDictionary doesn't seem to throw the exception.)
                            if(result == null) {
                                result = _undefinedKeyLabel;
                            }
                        } catch (NSKeyValueCoding.UnknownKeyException t) {
                            result = _undefinedKeyLabel;
                        } catch (Throwable t) {
                            throw new NSForwardException(t, "An exception occured while parsing element, "
                                            + element + ", of template, \""
                                            + template + "\": "
                                            + t.getMessage());
                        }
                    }
                }
                if(result == _undefinedKeyLabel) {
                    if (!isLoggingDisabled && log.isDebugEnabled()) {
                        log.debug("Could not find a value for \"" + element
                                + "\" of template, \"" + template
                                + "\" in either the object or extra data.");
                    }
                }
                sb.append(result.toString());
                deriveElement = false;
            } else {
                if(element.length() > 0) {
                    sb.append(element);
                }
                deriveElement = true;
            }
            if(!isLoggingDisabled && log.isDebugEnabled()) {
                log.debug("Buffer: " + sb);
            }
        }
        return sb.toString();
    }
    
	/**
	 * To allow flexibility of the variable provider object type we use similar
	 * logic to NSDictionary valueForKeyPath. Consequently
	 * <code>java.util.Properties</code> objects that use keyPath separator (.)
	 * in the property names (which is common) can be reliably used as object
	 * providers.
	 * 
	 * @param aKeyPath
	 * @param anObject
	 * @return the value corresponding to either a key with value
	 *         <code>aKeypath</code>, or when no key, a keyPath with value
	 *         <code>aKeyPath</code>
	 */
	protected Object doGetValue(String aKeyPath, Object anObject) {
		// Mimic NSDictionary valueForKeypath behavior which first checks for a
		// "flattened" key before calling real valueForKeypath logic
		Object result = null;
		try {
			result = NSKeyValueCoding.Utility.valueForKey(anObject, aKeyPath);
		}
		catch (NSKeyValueCoding.UnknownKeyException t) {
		}

		if (result == null) {
			return NSKeyValueCodingAdditions.Utility.valueForKeyPath(anObject, aKeyPath);
		}
		return result;
	}
    
    /**
     * Parses the given templateString with an ERXSimpleTemplateParser.
     * 
     * @param templateString the template string to parse
     * @param templateObject the object to bind to
     * @return the parsed template string
     */
    public static String parseTemplatedStringWithObject(String templateString, Object templateObject) {
        String convertedValue = templateString;
        if (templateString == null || templateString.indexOf("@@") == -1) {
            return templateString;
        }

        String lastConvertedValue = null;
        while (convertedValue != lastConvertedValue && convertedValue.indexOf("@@") > -1) {
            lastConvertedValue = convertedValue;
            convertedValue = new ERXSimpleTemplateParser("ERXSystem:KEY_NOT_FOUND").parseTemplateWithObject(convertedValue, "@@", templateObject, WOApplication.application());
        }

        // MS: Should we warn here? This is awfully quiet ...
        if (convertedValue.indexOf("ERXSystem:KEY_NOT_FOUND") > -1) {
            return templateString; // not all keys are present
        }

        return convertedValue;
    }
}
