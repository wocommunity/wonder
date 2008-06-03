/*
 * Copyright (C) NetStruxr, Inc. All rights reserved.
 *
 * This software is published under the terms of the NetStruxr
 * Public Software License version 0.5, a copy of which has been
 * included with this distribution in the LICENSE.NPL file.  */
package er.extensions.components;

import com.webobjects.appserver.WOContext;

/**
 * 
 * Synopsis:<br/>
  *
 * @binding showForm
 * @binding action
 * @binding name
 * @binding enctype
 * @binding directActionName
 * @binding actionClass
 */
public class ERXOptionalForm extends ERXStatelessComponent {

    /**
     * Public constructor
     * @param aContext current context
     */
    public ERXOptionalForm(WOContext aContext) {
        super(aContext);
    }

    /**
     * Determines if a form tag should be shown.
     * Two conditions determine if a form tag should
     * be shown. The first is if the boolean binding
     * 'showForm', if this is false then a form is not
     * displayed. The second conditiion to not display
     * a form is if the WOContext thinks that we are currently
     * within a form, which would cause a nested form.
     * This defaults to true.
     * @return if a form should be displayed.
     */
    public boolean showForm() {
        // Defaults to true
        boolean showForm = booleanValueForBinding("showForm", true);
        return showForm ? !context().isInForm() : false;
    }
}
