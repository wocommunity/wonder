/*
 * JSValidatedField.java
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
import java.util.Random;

public class JSValidatedField extends WOComponent {
    public String uniqueID;

    public JSValidatedField(WOContext aContext)  {
        super(aContext);
    }

    public void awake() {
        // We need to give each image a unique name, with considerations that there might be
        // more than ImageFlyover per page.
        if (uniqueID == null) {
            uniqueID = "Image"+(new Random()).nextInt();
        }
    }


    public boolean synchronizesVariablesWithBindings() {
        return false;
    }

    public String validateFunction() {
            // Return the name of the javascript function for the validation
            return "validate_"+uniqueID+"()";
    }


    public String validationString() {

        String alertMessage;
        String functionScript;
        String addOr = null;

        // Start the function with it's name
        functionScript = "function "+validateFunction()+" {\n \tif (";

        // Get the validation rules from the bindings; first check 'inputIsRequired' and add to the
        // javascript function if it exists

        if (valueForBinding("inputIsRequired") != null) {	

            functionScript = functionScript + "document.forms."+valueForBinding("formName")+"."+uniqueID+".value == \"\"";

                // Note that we already have one condition
                addOr = "YES";

        }


        // Check for text that the user requires

        String requiredText = (String)_WOJExtensionsUtil.valueForBindingOrNull("requiredText",this);

        if (requiredText!=null) {
            // Add the OR if we need it
            if (addOr == "YES")
                functionScript = functionScript + " || ";

            functionScript = functionScript + "document.forms."+valueForBinding("formName")+"."+uniqueID+".value.indexOf(\""+requiredText+"\") == -1";

        }

        if (addOr =="YES") // none of the previous tests have fired
            functionScript = functionScript +"0";
        
        // Check the user input for an alert message, and add it to the rules

        alertMessage = (String)_WOJExtensionsUtil.valueForBindingOrNull("errorMessage",this);
        if (alertMessage==null)
            alertMessage = "You have entered incorrect values -- please try again";

        functionScript = functionScript +")\n { alert(\""+alertMessage+"\"); return false; } }";

        // Return the function script
        return functionScript;
    }
}

