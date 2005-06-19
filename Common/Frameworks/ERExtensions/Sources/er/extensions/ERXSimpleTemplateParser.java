/*
 * Copyright (C) NetStruxr, Inc. All rights reserved.
 *
 * This software is published under the terms of the NetStruxr
 * Public Software License version 0.5, a copy of which has been
 * included with this distribution in the LICENSE.NPL file.  */
package er.extensions;

import com.webobjects.foundation.*;
import com.webobjects.eocontrol.*;
import com.webobjects.eoaccess.*;
import java.util.Enumeration;

/**
 * Very simple template parser.  For example if you have the delimiter:
 * @@, then a possible template might look like: "Hello, @@name@@.  How are
 * you feeling today?",  In this case the object will get asked for the
 * value name. This works with key-paths as well.
 */
public class ERXSimpleTemplateParser {

    /** The default label for keys not found while parsing */
    public static final String DEFAULT_UNDEFINED_KEY_LABEL = "?";

    /** logging support */
    public static final ERXLogger log = ERXLogger.getERXLogger(ERXSimpleTemplateParser.class.getName());

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
    protected boolean isLoggingDisabled = false;

    /** The label that will be appeared where an undefined key is found */ 
    private final String _undefinedKeyLabel;

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
        _undefinedKeyLabel = undefinedKeyLabel;
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
        if (delimiter == null)
            delimiter = "@@";
        boolean deriveElement = false; // if the template starts with delim, the first component will be a zero-length string
        NSArray components = NSArray.componentsSeparatedByString(template, delimiter);
        if (! isLoggingDisabled  &&  log.isDebugEnabled()) 
            log.debug("Components: " + components);
        for (Enumeration e = components.objectEnumerator(); e.hasMoreElements();) {
            String element = (String)e.nextElement();
            if (deriveElement) {
                if (element.length() == 0)
                    throw new RuntimeException("\"\" is not a valid keypath");
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
     */
    public String parseTemplateWithObject(String template, String delimiter, Object object) {
        return parseTemplateWithObject(template,
                                       delimiter,
                                       object,
                                       null);
    }
    
    /**
     * This method replaces the keys enclosed between the
     * delimeter with the values found in object and otherObject.
     * It first looks for a value in object, and then in otherObject
     * if the key is not found in object. Therefore, otherObject is
     * a good place to store default values while object is a
     * good place to override default values. 
     * <p>
     * When the value is not found in both object and otherObject, 
     * it will replace the key with the undefined key label which 
     * defaults to "?". You can set the label via the constructor 
     * {@link #ERXSimpleTemplateParser(String)}. 
     * 
     * @param template to use to parse
     * @param delimiter to use to check for keys
     * @param object to resolve keys off of
     * @param otherObject object used to resolve default keys
     * @return parsed template with keys replaced
     */
    public String parseTemplateWithObject(String template, String delimiter, Object object, Object otherObject) {
        if (template == null)
            throw new RuntimeException("Attempting to parse null template!");
        if (object == null)
            throw new RuntimeException("Attempting to parse template with null object!");
        if (delimiter == null) delimiter = "@@";
        if (! isLoggingDisabled  &&  log.isDebugEnabled()) {
            log.debug("Parsing template: " + template + " with delimiter: " + delimiter + " object: " + object);
            log.debug("Template: " + template);
            log.debug("Delim: " + delimiter);
            log.debug("otherObject: " + otherObject);
        }
        if (delimiter.equals("@@") && template.indexOf(delimiter) < 0 && template.indexOf("@") >= 0) {
            if (!isLoggingDisabled) {
                log.warn("It seems that the template string '" + template + "' is using the old delimiter '@' instead of '@@'. I will use '@' for now but you should fix this by updating the template.");
            }
            delimiter = "@";
        }
        StringBuffer buffer = new StringBuffer();
        boolean deriveElement = false; // if the template starts with delim, the first component will be a zero-length string
        NSArray components = NSArray.componentsSeparatedByString(template, delimiter);
        if (! isLoggingDisabled  &&  log.isDebugEnabled())
            log.debug("Components: " + components);
        for (Enumeration e = components.objectEnumerator(); e.hasMoreElements();) {
            String element = (String)e.nextElement();
            if (! isLoggingDisabled)  log.debug("Processing Element: " + element);
            if (deriveElement) {
                if (! isLoggingDisabled)  log.debug("Deriving value ...");
                if (element.length() == 0)
                    throw new RuntimeException("\"\" is not a valid keypath in template: " + template);
                Object obj;
                try {
                    if (!isLoggingDisabled && log.isDebugEnabled()) {
                        log.debug("calling valueForKeyPath("+object+", "+element+")");
                    }
                    obj = NSKeyValueCodingAdditions.Utility.valueForKeyPath(object, element); 
                    // For just in case the above doesn't throw an exception when the 
                    // key is not defined. (NSDictionary doesn't seem to throw the exception.)
                    if (obj == null  &&  otherObject != null) {
                        throw new NSKeyValueCoding.UnknownKeyException("The key is not defined in the object.", null, element);
                    }
                } catch (NSKeyValueCoding.UnknownKeyException t) {
                    if (otherObject != null) {
                    	try {
                    		obj = NSKeyValueCodingAdditions.Utility.valueForKeyPath(otherObject, element);
                    	} catch (NSKeyValueCoding.UnknownKeyException t1) {
                    		if (!isLoggingDisabled && log.isDebugEnabled()) {
                    			log.debug("Could not find a value for \"" + element + "\" of template, \"" + template + "\" in either the object or extra data: " + t1.getMessage());
                    		}
                    		obj = null;
                    	} catch (Throwable t1) {
                    		throw new NSForwardException(t, "An exception occured while parsing element, " + element + ", of template, \"" + template + "\": " + t1.getMessage());
                    	}
                    } else {
                    	obj = null;
                    }
                } catch (Throwable t) {
                    throw new NSForwardException(t, "An exception occured while parsing element, " + element + ", of template, \"" + template + "\": " + t.getMessage());
                }
                buffer.append(obj == null ? _undefinedKeyLabel : obj.toString());                
                deriveElement = false;
            } else {
                if (element.length() > 0)
                    buffer.append(element);
                deriveElement = true;
            }
            if (! isLoggingDisabled  && log.isDebugEnabled())
                log.debug("Buffer: " + buffer);
        }
        return buffer.toString();
    }
}
