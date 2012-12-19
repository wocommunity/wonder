/*
 * JSValidatedField.java
 * (c) Copyright 2001 Apple Computer, Inc. All rights reserved.
 * This a modified version.
 * Original license: http://www.opensource.apple.com/apsl/
 */

package com.webobjects.woextensions;

import java.util.Random;

import com.webobjects.appserver.WOComponent;
import com.webobjects.appserver.WOContext;

public class JSValidatedField extends WOComponent {
	/**
	 * Do I need to update serialVersionUID?
	 * See section 5.6 <cite>Type Changes Affecting Serialization</cite> on page 51 of the 
	 * <a href="http://java.sun.com/j2se/1.4/pdf/serial-spec.pdf">Java Object Serialization Spec</a>
	 */
	private static final long serialVersionUID = 1L;

	private static Random _random = new Random();

	public String uniqueID;

    public JSValidatedField(WOContext aContext)  {
        super(aContext);
    }

    @Override
    public void awake() {
        // We need to give each image a unique name, with considerations that there might be
        // more than ImageFlyover per page.
        if (uniqueID == null) {
        	uniqueID = "Image"+_random.nextInt();
        }
    }


    @Override
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

