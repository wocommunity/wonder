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
 * Component that generates a mailto href of the form: "<a href=mailto:foo@bar.com>foo@bar.com</a>".
 * <br/>
 * Synopsis:<br/>
 * email=<i>anEmail</i>;
 * <br/>
 * @binding email email address to generate href
 * <br/>
 */
public class ERXMailTo extends WOComponent {

    /** Default constructor */
    public ERXMailTo(WOContext aContext) {
        super(aContext);
    }

    /** component is stateless */
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
