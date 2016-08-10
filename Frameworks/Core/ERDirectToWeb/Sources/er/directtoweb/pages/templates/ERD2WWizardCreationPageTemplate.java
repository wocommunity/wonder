/*
 * Copyright (C) NetStruxr, Inc. All rights reserved.
 *
 * This software is published under the terms of the NetStruxr
 * Public Software License version 0.5, a copy of which has been
 * included with this distribution in the LICENSE.NPL file.  */
package er.directtoweb.pages.templates;

import com.webobjects.appserver.WOContext;

import er.directtoweb.pages.ERD2WWizardCreationPage;

/**
 * A wizard inspect/edit template.
 * 
 * @d2wKey cancelButtonLabel
 * @d2wKey bannerFileName
 * @d2wKey showBanner
 * @d2wKey headerComponentName
 * @d2wKey formEncoding
 * @d2wKey repetitionComponentName
 * @d2wKey previousButtonLabel
 * @d2wKey pageWrapperName
 * @d2wKey nextButtonLabel
 * @d2wKey saveButtonLabel
 */
public class ERD2WWizardCreationPageTemplate extends ERD2WWizardCreationPage {
	/**
	 * Do I need to update serialVersionUID?
	 * See section 5.6 <cite>Type Changes Affecting Serialization</cite> on page 51 of the 
	 * <a href="http://java.sun.com/j2se/1.4/pdf/serial-spec.pdf">Java Object Serialization Spec</a>
	 */
	private static final long serialVersionUID = 1L;

    public ERD2WWizardCreationPageTemplate(WOContext context) {
        super(context);
    }
}
