/*
 * Copyright (c) 2001 ravi@svgobjects.com. All rights reserved.
 *
 * The code in this file are subject to the Artistic License:
 * http://www.svgobjects.com/documentation/reference/Copyright.html
 */
package org.svgobjects;

import com.webobjects.foundation.*;
import com.webobjects.appserver.*;
import java.util.*;

/*
 * SVG <image...></image>
 * additional bindings prefixed with "$" get appended onto the style attribute.
 */
public class SOImage extends WODynamicElement {
    private NSMutableDictionary _associations;
    private WOAssociation _hrefAssociation;
    private WOAssociation _filenameAssociation;
    private WOAssociation _frameworkAssociation;
    private WOAssociation _styleAssociation;
    private WOElement _children;

    /*
     * public binding keys
     */
    public static final String StyleKey = "style";
    public static final String FilenameKey = "filename";
    public static final String FrameworkKey = "framework";
    public static final String HrefKey = "href";

    /*
     * constructors
     */
    public SOImage(String name, NSDictionary associations, WOElement element) {
        super(name, associations, element);

        // set the content/children
        _children = element;

        // set the associations
        _associations = new NSMutableDictionary(associations);

        // remove the public bindings/associations
        _filenameAssociation = (WOAssociation) associations.objectForKey(FilenameKey);
        _associations.removeObjectForKey(FilenameKey);
        _frameworkAssociation = (WOAssociation) associations.objectForKey(FrameworkKey);
        _associations.removeObjectForKey(FrameworkKey);
        _hrefAssociation = (WOAssociation) associations.objectForKey(HrefKey);
        _associations.removeObjectForKey(HrefKey);
        _styleAssociation = (WOAssociation) associations.objectForKey(StyleKey);
        _associations.removeObjectForKey(StyleKey);
    }

    /*
     * request/response
     */
    public void appendToResponse(WOResponse response, WOContext context) {
        WOComponent component = context.component();
        String style = new String();
        String href = null;

        // open tag
        response.appendContentString("<image");

        // the style string
        if (_styleAssociation != null)
            style = (String) _styleAssociation.valueInComponent(component);

        // href
        if (_hrefAssociation != null)
            href = (String) _hrefAssociation.valueInComponent(component);

        // filename
        else if (_filenameAssociation != null) {
            WOResourceManager resourceManager = WOApplication.application().resourceManager();
            String filename = (String) _filenameAssociation.valueInComponent(component);
            String framework = null;

            // set the url
            if (_frameworkAssociation != null) {
                framework = (String) _frameworkAssociation.valueInComponent(component);
                if ( framework.equals("app") ) framework = null;
            }
            href = resourceManager.urlForResourceNamed(filename, framework, null, context.request());
        }

        response.appendContentString(" xlink:href=" + "\"" + href + "\"");

        // append other bindings
        if (_associations.count() > 0) {
            Enumeration keyEnumerator = _associations.keyEnumerator();

            while (keyEnumerator.hasMoreElements()) {
                String key = (String) keyEnumerator.nextElement();
                WOAssociation association = (WOAssociation) _associations.objectForKey(key);
                Object value = association.valueInComponent(component);

                // append attribute
                if (value != null) {
                    // ...as $style attributes
                    if (key.startsWith("$"))
                            style = (style + key.substring(1) + ":" + value + ";");

                    // ...as <text> attribute
                    else response.appendContentString(" " + key + "=" + "\"" + value + "\"");
                }
            }
        }

        // append style string
        if (!style.equals(""))
            response.appendContentString(" style=" + "\"" + style + "\"");
        
        // close opening tag
        response.appendContentString(">");

        // children/component content
        if (_children != null) _children.appendToResponse(response, context);

        // close tag
        response.appendContentString("</image>");
    }
}