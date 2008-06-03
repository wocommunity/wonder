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
 * HTML <embed type="image/svg"...> 
 * Note: WOSVG will pass additional bindings to the component SVG
 */
public class WOSVG extends WODynamicElement {
    private NSMutableDictionary _associations;
    private WOAssociation _pageNameAssociation;
    private WOAssociation _directActionNameAssociation;
    private WOAssociation _queryDictionaryAssociation;
    private WOAssociation _heightAssociation;
    private WOAssociation _widthAssociation;
    private WOAssociation _filenameAssociation;
    private WOAssociation _frameworkAssociation;

    /*
     * public binding keys
     */
    public static final String PageNameNameKey = "pageName";
    public static final String DirectActionNameKey = "directActionName";
    public static final String QueryDictionaryKey = "queryDictionary";
    public static final String HeightKey = "height";
    public static final String WidthKey = "width";
    public static final String FilenameKey = "filename";
    public static final String FrameworkKey = "framework";
    
    /*
     * constructors
     */
    public WOSVG(String name, NSDictionary associations, WOElement element) {
        super(name, associations, element);

        // set the associations
        _associations = new NSMutableDictionary(associations);

        // remove the public bindings/associations
        _pageNameAssociation = (WOAssociation) associations.objectForKey(PageNameNameKey);
        _associations.removeObjectForKey(PageNameNameKey);
        _directActionNameAssociation = (WOAssociation) associations.objectForKey(DirectActionNameKey);
        _associations.removeObjectForKey(DirectActionNameKey);
        _queryDictionaryAssociation = (WOAssociation) associations.objectForKey(QueryDictionaryKey);
        _associations.removeObjectForKey(QueryDictionaryKey);
        _heightAssociation = (WOAssociation) associations.objectForKey(HeightKey);
        _associations.removeObjectForKey(HeightKey);
        _widthAssociation = (WOAssociation) associations.objectForKey(WidthKey);
        _associations.removeObjectForKey(WidthKey);
        _filenameAssociation = (WOAssociation) associations.objectForKey(FilenameKey);
        _associations.removeObjectForKey(FilenameKey);
        _frameworkAssociation = (WOAssociation) associations.objectForKey(FrameworkKey);
        _associations.removeObjectForKey(FrameworkKey);
    }

    /*
     * request/response
     */
    public void appendToResponse(WOResponse response, WOContext context) {
        WOComponent component = context.component();
        String src = null;
        
        // open tag
        response.appendContentString("<EMBED");

        // plug-ins page
        response.appendContentString(" pluginspage=" + "\"" + "http://www.adobe.com/svg/viewer/install/" + "\"");

        // wo component action
        if (_pageNameAssociation != null)
            src = context.componentActionURL();

        // wa direct action
        else if (_directActionNameAssociation != null) {
            String directActionName = (String) _directActionNameAssociation.valueInComponent(component);
            NSDictionary queryDictionary = null;

            // queryDictionary
            if (_queryDictionaryAssociation != null) queryDictionary = (NSDictionary) _queryDictionaryAssociation.valueInComponent(component);

            src = context.directActionURLForActionNamed(directActionName, queryDictionary);
        }

        // wr resource action
        else if (_filenameAssociation != null) {
            WOResourceManager resourceManager = WOApplication.application().resourceManager();
            String filename = (String) _filenameAssociation.valueInComponent(component);
            String framework = null;

            // set the url
            if (_frameworkAssociation != null) {
                framework = (String) _frameworkAssociation.valueInComponent(component);
                if (framework.equals("app")) framework = null;
            }
            src = resourceManager.urlForResourceNamed(filename, framework, null, context.request());
        }
        
        response.appendContentString(" src=" + "\"" + src + "\"");

        // height
        if (_heightAssociation != null) {
            Object height = _heightAssociation.valueInComponent(component);

            if (height != null)
                response.appendContentString(" height=" + "\"" + height + "\"");
        }

        // width
        if (_widthAssociation != null) {
            Object width = _widthAssociation.valueInComponent(component);

            if (width != null)
                response.appendContentString(" width=" + "\"" + width + "\"");
        }

        // close tag
        response.appendContentString(" type=\"image/svg+xml\">");
    }

    /*
     * action
     */
    public WOActionResults invokeAction(WORequest request, WOContext context) {

        // check to see if the component action was initiated from this element
        if (_pageNameAssociation != null &&
            context.senderID().equals(context.elementID())) {
            WOComponent component = context.component();
            String pageName = (String) _pageNameAssociation.valueInComponent(component);
            WOApplication application = WOApplication.application();
            WOComponent svg = application.pageWithName(pageName, context);

            // takeValues from additional bindings
            if (_associations.count() > 0) {
                Enumeration keyEnumerator = _associations.keyEnumerator();

                while (keyEnumerator.hasMoreElements()) {
                    String key = (String) keyEnumerator.nextElement();
                    WOAssociation association = (WOAssociation) _associations.objectForKey(key);
                    Object value = association.valueInComponent(component);

                    // takeValue
                    if (value != null) svg.takeValueForKey(value, key);
                }
            } return svg;
        } else return null;
    }
}