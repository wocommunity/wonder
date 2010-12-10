/*
 * Copyright (C) NetStruxr, Inc. All rights reserved.
 *
 * This software is published under the terms of the NetStruxr
 * Public Software License version 0.5, a copy of which has been
 * included with this distribution in the LICENSE.NPL file.  */
package er.directtoweb.pages.templates;

import com.webobjects.appserver.WOContext;

import er.directtoweb.pages.ERD2WMessagePage;

/**
 * More robust confirm messaging.  Still a work in progress.<br />
 * 
 * @d2wKey pageWrapperName
 * @d2wKey confirmMessageManditory
 * @d2wKey confirmMessageKey
 * @d2wKey confirmRows
 * @d2wKey confirmMessageTextfieldSize
 * @d2wKey confirmMessageManditoryErrorMessage
 * @d2wKey confirmMessageExplanation
 * @d2wKey confirmMessageIsTextfield
 * @d2wKey confirmMessageTextfieldMaxlength
 * @d2wKey okButtonLabel
 * @d2wKey cancelButtonLabel
 * @d2wKey explanationComponentName
 * @d2wKey explanationConfigurationName
 * @d2wKey shouldProvideConfirmMessage
 */
public class ERD2WConfirmPageTemplate extends ERD2WMessagePage {

    public ERD2WConfirmPageTemplate(WOContext context) { super(context); }
}
