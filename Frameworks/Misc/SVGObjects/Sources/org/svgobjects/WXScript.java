/*
 * Copyright (c) 2001 ravi@svgobjects.com. All rights reserved.
 *
 * The code in this file are subject to the Artistic License:
 * http://www.svgobjects.com/documentation/reference/Copyright.html
 */
package org.svgobjects;

import com.webobjects.foundation.*;
import com.webobjects.appserver.*;

/*
 * XML <script..></script>
 */
public class WXScript extends WOComponent {

    /*
    * constructor
    */
    public WXScript(WOContext context) {
	super(context);
    }

    /*
     * non synching component
     */
    public boolean synchronizesVariablesWithBindings() {
        return false;
    }

    /*
     * stateless component
     */
    public boolean isStateless() {
        return true;
    }

    /*
     * accessors
     */
    public boolean hasScriptString() {
        return hasBinding("scriptString");
    }

    public String href() {
        String href = (String) valueForBinding("scriptSource");

        if (href == null && hasBinding("scriptFile")) {
            WOResourceManager resourceManager = application().resourceManager();
            WORequest request = context().request();
            NSArray languages = request.browserLanguages();
            String filename = (String) valueForBinding("scriptFile");
            
            // url
            href = resourceManager.urlForResourceNamed(filename, null, languages, request);
        } return href;
    }

    public String scriptString() {
        String scriptString = (String) valueForBinding("scriptString");
        return ("<![CDATA[\n" + scriptString + "\n]]>");
    }
}