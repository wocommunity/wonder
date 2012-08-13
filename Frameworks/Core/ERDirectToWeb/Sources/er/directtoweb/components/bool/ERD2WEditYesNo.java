/*
 * Copyright (C) NetStruxr, Inc. All rights reserved.
 *
 * This software is published under the terms of the NetStruxr
 * Public Software License version 0.5, a copy of which has been
 * included with this distribution in the LICENSE.NPL file.  */
package er.directtoweb.components.bool;

import org.apache.log4j.Logger;

import com.webobjects.appserver.WOContext;

/**
 * Edits a boolean with radio buttons and Yes/No<br />
 * You should use ERD2WCustomEditBoolean with the choicesNames d2w key instead.
 */

@Deprecated
public class ERD2WEditYesNo extends ERD2WCustomEditBoolean {
	/**
	 * Do I need to update serialVersionUID?
	 * See section 5.6 <cite>Type Changes Affecting Serialization</cite> on page 51 of the 
	 * <a href="http://java.sun.com/j2se/1.4/pdf/serial-spec.pdf">Java Object Serialization Spec</a>
	 */
	private static final long serialVersionUID = 1L;
    
    public static Logger log = Logger.getLogger(ERD2WEditYesNo.class);

    public ERD2WEditYesNo(WOContext context) {
        super(context);
    }
}
