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
 * Root class of all SVG graphics elements.
 * Elements which have a style attribute
 */
public class SODynamicElement extends WODynamicElement {
    private NSMutableDictionary _associations;
    private WOAssociation _styleAssociation;
    private WOAssociation _hrefAssociation;
    private WOElement _children;
    protected String elementName;

    /*
     * public binding keys
     */
    public static final String StyleKey = "style";
    public static final String HrefKey = "href";

    /*
     * constructors
     */
    public SODynamicElement(String name, NSDictionary associations, WOElement element) {
        super(name, associations, element);

        // set the content/children
        _children = element;

        // set the associations
        _associations = new NSMutableDictionary(associations);

        // remove the public bindings/associations
        _styleAssociation = (WOAssociation) associations.objectForKey(StyleKey);
        _associations.removeObjectForKey(StyleKey);
        _hrefAssociation = (WOAssociation) associations.objectForKey(HrefKey);
        _associations.removeObjectForKey(HrefKey);
    }

    /*
     * request/response
     */
    public void appendToResponse(WOResponse response, WOContext context) {
        WOComponent component = context.component();
        String style = new String();

        // open tag
        response.appendContentString("<" + elementName);

        // the style string
        if (_styleAssociation != null)
            style = (String) _styleAssociation.valueInComponent(component);

        // append bindings
        if (_associations.count() > 0) {
            Enumeration keyEnumerator = _associations.keyEnumerator();

            while (keyEnumerator.hasMoreElements()) {
                String key = (String) keyEnumerator.nextElement();
                WOAssociation association = (WOAssociation) _associations.objectForKey(key);
                Object value = association.valueInComponent(component);

                // append...
                if (value != null) {
                    // ...as $style attributes
                    if (key.startsWith("$"))
                            style = (style + key.substring(1) + ":" + value + ";");

                    // ...as <tag...> attribute
                    else response.appendContentString(" " + key + "=" + "\"" + value + "\"");
                }
            }
        }

        // append style string
        if (!style.equals(""))
            response.appendContentString(" style=" + "\"" + style + "\"");

        // the href
        if (_hrefAssociation != null) {
            String href = (String) _hrefAssociation.valueInComponent(component);
            response.appendContentString(" " + "xlink:href" + "=" + "\"" + href + "\"");
        }

        // close opening tag
        response.appendContentString(">");

        // children/component content
        if (_children != null) _children.appendToResponse(response, context);

        // close tag
        response.appendContentString("</" + elementName + ">");
    }
}