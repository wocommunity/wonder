/*
 * Copyright (C) NetStruxr, Inc. All rights reserved.
 *
 * This software is published under the terms of the NetStruxr
 * Public Software License version 0.5, a copy of which has been
 * included with this distribution in the LICENSE.NPL file.  */
package er.extensions.components;

import com.webobjects.appserver.WOComponent;
import com.webobjects.appserver.WOContext;

/**
 * Component that generates a mailto href of the form: {@literal "<a href=mailto:foo@bar.com>foo@bar.com</a>"}.
 * <h3> Synopsis:</h3>
 * email=<i>anEmail</i>;
 * 
 * @binding email email address to generate href
 */
public class ERXMailTo extends WOComponent {
	/**
	 * Do I need to update serialVersionUID?
	 * See section 5.6 <cite>Type Changes Affecting Serialization</cite> on page 51 of the 
	 * <a href="http://java.sun.com/j2se/1.4/pdf/serial-spec.pdf">Java Object Serialization Spec</a>
	 */
	private static final long serialVersionUID = 1L;

    /** Default constructor */
    public ERXMailTo(WOContext aContext) {
        super(aContext);
    }

    /** component is stateless */
    @Override
    public boolean isStateless() { return true; }

    /**
     * Generates the href link from email binding of the form:
     * "<a href=mailto:foo@bar.com>".
     * @return generated href
     */
    public String href() {
        String result=null;
        String email=(String)valueForBinding("email");
        if (email!=null) result="mailto:"+email;
        return result;
    }
}
