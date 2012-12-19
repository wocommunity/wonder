/*
 * JSComponent.java
 * (c) Copyright 2001 Apple Computer, Inc. All rights reserved.
 * This a modified version.
 * Original license: http://www.opensource.apple.com/apsl/
 */

package com.webobjects.woextensions;

import com.webobjects.appserver.WOComponent;
import com.webobjects.appserver.WOContext;
import com.webobjects.foundation.NSArray;


/**
 * <span class="ja">
 *  このパッケージ内の Javascript のスーパークラス
 * </span>
 */
public abstract class JSComponent extends WOComponent {
	/**
	 * Do I need to update serialVersionUID?
	 * See section 5.6 <cite>Type Changes Affecting Serialization</cite> on page 51 of the 
	 * <a href="http://java.sun.com/j2se/1.4/pdf/serial-spec.pdf">Java Object Serialization Spec</a>
	 */
	private static final long serialVersionUID = 1L;

    public JSComponent(WOContext aContext)  {
        super(aContext);
    }

    @Override
    public boolean synchronizesVariablesWithBindings() {
           return false;
    }

	/** <span class="ja">フレームワーク</span> */
    public String framework() {
        String aFramework = (String)_WOJExtensionsUtil.valueForBindingOrNull("framework",this);
        if ((aFramework != null) && aFramework.equalsIgnoreCase("app"))
            aFramework=null;
        return aFramework;
    }

	/** <span class="ja">イメージ・ロケーション</span> */
    public String imageLocation() {

           // Return the image source (SRC) location ...
        String aFilename = (String)_WOJExtensionsUtil.valueForBindingOrNull("filename",this);

        return application(). resourceManager(). urlForResourceNamed( aFilename, framework(), session().languages(), context().request());

    }

	/** <span class="ja">アクションURL</span> */
    public String contextComponentActionURL() {

        // If the user specified an action or pageName, return the source URL
        if (hasBinding("action") || (valueForBinding("pageName")!=null)) {

            // Return the URL to the action or page placed in the context by invokeAction
            return context().componentActionURL();

        }

        // If the user specified some javascript, put that into the HREF and return it
        String theFunction = (String)_WOJExtensionsUtil.valueForBindingOrNull("javascriptFunction",this);
        if (theFunction!=null) {

            // Make sure there are no extra quotations marks - replace them with apostrophes
            theFunction = NSArray.componentsSeparatedByString(theFunction, "\""). componentsJoinedByString("'");

            // Return the javascript HREF of what the user passed
            return "javascript:"+theFunction;
        }
        return null;


    }

	/** 
     * <span class="ja">アクション実行</span>
     */
    public WOComponent invokeAction() {

            // Set the result of the link, either an action from the parent or a page
            WOComponent anActionResult = (WOComponent)_WOJExtensionsUtil.valueForBindingOrNull("action",this);

            if ((anActionResult==null) && hasBinding("pageName")) {
                    String aPageName = (String)_WOJExtensionsUtil.valueForBindingOrNull("pageName",this);
                    anActionResult = pageWithName(aPageName);
            }

            return anActionResult;
    }
}
