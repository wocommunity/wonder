/*
 * JSComponent.java
 * [JavaWOExtensions Project]
 *
 * © Copyright 2001 Apple Computer, Inc. All rights reserved.
 *
 * IMPORTANT:  This Apple software is supplied to you by Apple Computer,
 * Inc. (“Apple”) in consideration of your agreement to the following 
 * terms, and your use, installation, modification or redistribution of 
 * this Apple software constitutes acceptance of these terms.  If you do
 * not agree with these terms, please do not use, install, modify or
 * redistribute this Apple software.
 *
 * In consideration of your agreement to abide by the following terms, 
 * and subject to these terms, Apple grants you a personal, non-
 * exclusive license, under Apple’s copyrights in this original Apple 
 * software (the “Apple Software”), to use, reproduce, modify and 
 * redistribute the Apple Software, with or without modifications, in 
 * source and/or binary forms; provided that if you redistribute the  
 * Apple Software in its entirety and without modifications, you must 
 * retain this notice and the following text and disclaimers in all such
 * redistributions of the Apple Software.  Neither the name, trademarks,
 * service marks or logos of Apple Computer, Inc. may be used to endorse
 * or promote products derived from the Apple Software without specific
 * prior written permission from Apple.  Except as expressly stated in
 * this notice, no other rights or licenses, express or implied, are
 * granted by Apple herein, including but not limited to any patent
 * rights that may be infringed by your derivative works or by other
 * works in which the Apple Software may be incorporated.
 *
 * The Apple Software is provided by Apple on an "AS IS" basis.  APPLE 
 * MAKES NO WARRANTIES, EXPRESS OR IMPLIED, INCLUDING WITHOUT LIMITATION
 * THE IMPLIED WARRANTIES OF NON-INFRINGEMENT, MERCHANTABILITY AND
 * FITNESS FOR A PARTICULAR PURPOSE, REGARDING THE APPLE SOFTWARE OR ITS
 * USE AND OPERATION ALONE OR IN COMBINATION WITH YOUR PRODUCTS. 
 *
 * IN NO EVENT SHALL APPLE BE LIABLE FOR ANY SPECIAL, INDIRECT,
 * INCIDENTAL OR CONSEQUENTIAL DAMAGES (INCLUDING, BUT NOT LIMITED TO,
 * PROCUREMENT OF SUBSTITUTE GOODS OR SERVICES; LOSS OF USE, DATA, OR
 * PROFITS; OR BUSINESS INTERRUPTION) ARISING IN ANY WAY OUT OF THE USE,
 * REPRODUCTION, MODIFICATION AND/OR DISTRIBUTION OF THE APPLE SOFTWARE,
 * HOWEVER CAUSED AND WHETHER UNDER THEORY OF CONTRACT, TORT (INCLUDING
 * NEGLIGENCE), STRICT LIABILITY OR OTHERWISE, EVEN IF APPLE HAS BEEN
 * ADVISED OF THE POSSIBILITY OF SUCH DAMAGE.
 */

package com.webobjects.woextensions;

import com.webobjects.appserver.*;
import com.webobjects.foundation.*;


public abstract class JSComponent extends WOComponent {
    public JSComponent(WOContext aContext)  {
        super(aContext);
    }

    public boolean synchronizesVariablesWithBindings() {
           return false;
    }


    public String framework() {
        String aFramework = (String)_WOJExtensionsUtil.valueForBindingOrNull("framework",this);
        if ((aFramework != null) && aFramework.equalsIgnoreCase("app"))
            aFramework=null;
        return aFramework;
    }

    public String imageLocation() {

           // Return the image source (SRC) location ...
        String aFilename = (String)_WOJExtensionsUtil.valueForBindingOrNull("filename",this);

        return application(). resourceManager(). urlForResourceNamed( aFilename, framework(), session().languages(), context().request());

    }

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
