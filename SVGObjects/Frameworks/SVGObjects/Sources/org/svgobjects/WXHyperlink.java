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
 * XML <a xlink:href=...></a>
 */
public class WXHyperlink extends WODynamicElement {
    private NSMutableDictionary _associations;
    private WOAssociation _pageNameAssociation;
    private WOAssociation _directActionNameAssociation;
    private WOAssociation _queryDictionaryAssociation;
    private WOAssociation _actionAssociation;
    private WOAssociation _hrefAssociation;
    private WOElement _children;

    /*
     * public binding keys
     */
    public static final String PageNameNameKey = "pageName";
    public static final String DirectActionNameKey = "directActionName";
    public static final String QueryDictionaryKey = "queryDictionary";
    public static final String HrefKey = "href";
    public static final String ActionKey = "action";

    /*
     * constructors
     */
    public WXHyperlink(String name, NSDictionary associations, WOElement element) {
        super(name, associations, element);

        // set the content
        _children = element;

        // set the associations
        _associations = new NSMutableDictionary(associations);

        // remove the public bindings/associations
        _pageNameAssociation = (WOAssociation) associations.objectForKey(PageNameNameKey);
        _associations.removeObjectForKey(PageNameNameKey);
        _directActionNameAssociation = (WOAssociation) associations.objectForKey(DirectActionNameKey);
        _associations.removeObjectForKey(DirectActionNameKey);
        _queryDictionaryAssociation = (WOAssociation) associations.objectForKey(QueryDictionaryKey);
        _associations.removeObjectForKey(QueryDictionaryKey);
        _actionAssociation = (WOAssociation) associations.objectForKey(ActionKey);
        _associations.removeObjectForKey(ActionKey);
        _hrefAssociation = (WOAssociation) associations.objectForKey(HrefKey);
        _associations.removeObjectForKey(HrefKey);
    }

    /*
     * request/response
     */
    public void appendToResponse(WOResponse response, WOContext context) {
        WOComponent component = context.component();
        String href = null;

        // open tag
        response.appendContentString("<a");

        // href
        if (_hrefAssociation != null)
            href = (String) _hrefAssociation.valueInComponent(component);

        // wo component action
        if (_pageNameAssociation != null ||
            _actionAssociation != null)
            href = context.componentActionURL();

        // wa direct action
        else if (_directActionNameAssociation != null) {
            String directActionName = (String) _directActionNameAssociation.valueInComponent(component);
            NSDictionary queryDictionary = null;

            // queryDictionary
            if (_queryDictionaryAssociation != null) queryDictionary = (NSDictionary) _queryDictionaryAssociation.valueInComponent(component);

            href = context.directActionURLForActionNamed(directActionName, queryDictionary);
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
                if (value != null)
                    response.appendContentString(" " + key + "=" + "\"" + value + "\"");
            }
        }

        // close opening tag
        response.appendContentString(">");

        // children/component content
        if (_children != null) _children.appendToResponse(response, context);

        // close tag
        response.appendContentString("</a>");
    }

    /*
     * action
     */
    public WOActionResults invokeAction(WORequest request, WOContext context) {

        // check to see if the component action was initiated from this element
        if (context.senderID().equals(context.elementID())) {
            WOComponent component = context.component();

            if (_pageNameAssociation != null) {
                String pageName = (String) _pageNameAssociation.valueInComponent(component);
                WOApplication application = WOApplication.application();

                return application.pageWithName(pageName, context);
            } else if (_actionAssociation != null)
                return (WOActionResults) _actionAssociation.valueInComponent(component);
        } return null;
    }
}