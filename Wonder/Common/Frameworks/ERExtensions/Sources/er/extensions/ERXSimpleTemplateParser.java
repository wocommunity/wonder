/*
 * Copyright (C) NetStruxr, Inc. All rights reserved.
 *
 * This software is published under the terms of the NetStruxr
 * Public Software License version 0.5, a copy of which has been
 * included with this distribution in the LICENSE.NPL file.  */

/* ERSimpleTemplateParser.java created by max on Sun 22-Apr-2001 */
package er.extensions;

import com.webobjects.directtoweb.*;
import com.webobjects.foundation.*;
import com.webobjects.eocontrol.*;
import com.webobjects.eoaccess.*;
import com.webobjects.appserver.*;
import org.apache.log4j.Category;
import java.util.Enumeration;

// Very simple template parser.  For example if you have the delimiter: @, then a possible template might look like:
// "Hello, @name@.  How are you feeling today?",  In this case the object will get asked for the value name.
// This works with key-paths as well.
public class ERXSimpleTemplateParser {

    //////////////////////////////////////////////  log4j category  ////////////////////////////////////////
    public final static Category cat = Category.getInstance(ERXSimpleTemplateParser.class.getName());

    private static ERXSimpleTemplateParser _sharedInstance;
    public static ERXSimpleTemplateParser sharedInstance() {
        if (_sharedInstance == null)
            _sharedInstance = new ERXSimpleTemplateParser();
        return _sharedInstance;
    }

    public String parseTemplateWithObject(String template, String delimiter, Object object) {
        return parseTemplateWithObject(template,
                                       delimiter,
                                       object,
                                       null);
    }

    /* This method replaces the keys enclosed between the delimeter with the values found in object and otherObject. It first looks for a value in object, and then in otherObject if the key is not found in object. Therefore, otherObject is a good place to store default values while other object is a good place to override default values. */
    public String parseTemplateWithObject(String template, String delimiter, Object object, Object otherObject) {
        if (template == null)
            throw new RuntimeException("Attempting to parse null template!");
        if (object == null)
            throw new RuntimeException("Attempting to parse template with null object!");
        if (cat.isDebugEnabled())
            cat.debug("Parsing template: " + template + " with delimiter: " + delimiter + " object: " + object);
        cat.debug("Template: " + template);
        cat.debug("Delim: " + delimiter);
        cat.debug("otherObject: " + otherObject);
        StringBuffer buffer = new StringBuffer();
        boolean deriveElement = false; // if the template starts with delim, the first component will be a zero-length string
        cat.debug("Components: " + NSArray.componentsSeparatedByString(template, delimiter));
        for (Enumeration e = NSArray.componentsSeparatedByString(template, delimiter).objectEnumerator(); e.hasMoreElements();) {
            String element = (String)e.nextElement();
            cat.debug("Processing Element: " + element);
            if (deriveElement) {
                cat.debug("Deriving value ...");
                if(element.length() == 0)
                    throw new RuntimeException("\"\" is not a valid keypath");
                Object obj;
                try {
                    obj = ERXKeyValueCoding.valueForKeyPath(object, element);
                } catch (Throwable t) {
                    try {
                        if (otherObject != null) {
                            obj = ERXKeyValueCoding.valueForKeyPath(otherObject, element);
                        } else {
                            throw new RuntimeException("Could not find a value for \"" + element + "\" of a template in either the object or extra data");
                        }
                    } catch (Throwable t1) {
                        throw new RuntimeException("An exception occured while parsing element, " + element + ", of template, " + template + ": " + t1.getMessage());
                    }
                }
                buffer.append(obj.toString());                
                deriveElement = false;
            } else {
                if (element.length() > 0)
                    buffer.append(element);
                deriveElement = true;
            }
            cat.debug("Buffer: " + buffer);
        }
        return buffer.toString();
    }
}
