/*
 * Copyright (C) NetStruxr, Inc. All rights reserved.
 *
 * This software is published under the terms of the NetStruxr
 * Public Software License version 0.5, a copy of which has been
 * included with this distribution in the LICENSE.NPL file.  */
package er.directtoweb.pages.templates;
import com.webobjects.appserver.WOContext;

import er.directtoweb.pages.ERD2WPickListPage;

/**
 * A basic list but adding the ability to choose an arbitrary number of eos.  
 * Useful as a user-friendly replacement of the edit relationship page.<br />
 * 
 */

public class ERD2WPickListPageTemplate extends ERD2WPickListPage {

     public ERD2WPickListPageTemplate(WOContext context) { super(context); }    
}
