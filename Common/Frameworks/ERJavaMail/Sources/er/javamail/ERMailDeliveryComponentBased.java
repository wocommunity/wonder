/*
 $Id$

 ERMailDeliveryComponentBased.java - Camille Troillard - tuscland@mac.com
 */

package er.javamail;

import com.webobjects.foundation.*;
import com.webobjects.appserver.*;
import com.webobjects.eocontrol.*;

/** This abstract class is the basis for all WOComponetn based deliverers.
    @author Camille Troillard <tuscland@mac.com> */
public abstract class ERMailDeliveryComponentBased extends ERMailDelivery
{
    /** WOComponent used to render the HTML message. */
    protected WOComponent _component;

    /** Sets the WOComponent used to render the HTML message.
	@deprecated use setComponent instead.*/
    public void setWOComponentContent (WOComponent component) {
		this.setComponent (component);
    }

    /** Sets the WOComponent used to render the HTML message. */
    public void setComponent (WOComponent component) {
        _component = component;
    }

    public WOComponent component () {
	return _component;
    }
}
