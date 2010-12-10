package er.extensions.components;

import com.webobjects.appserver.WOActionResults;
import com.webobjects.appserver.WOAssociation;
import com.webobjects.appserver.WOContext;
import com.webobjects.appserver.WOElement;
import com.webobjects.appserver.WORequest;
import com.webobjects.appserver.WOResponse;
import com.webobjects.appserver._private.WODynamicGroup;
import com.webobjects.foundation.NSDictionary;
import com.webobjects.foundation.NSMutableArray;

/**
 * ERXWOFormBarrier allows you to wrap a section of your page that you want to trick into believing that
 * it is not actually inside of a WOForm. As an example, you could wrap an AjaxModalDialog that has a
 * form inside of it so that the form inside the dialog would process correctly because the barrier
 * would trick WO into believing the inner form is structurally distinct an outer form that might
 * already be wrapping the dialog. 
 * 
 * @author mschrag
 */
public class ERXWOFormBarrier extends WODynamicGroup {
	public ERXWOFormBarrier(String aName, NSDictionary someAssociations, WOElement template) {
		super(aName, someAssociations, template);
	}

	public ERXWOFormBarrier(String aName, NSDictionary someAssociations, NSMutableArray children) {
		super(aName, someAssociations, children);
	}

	@Override
	public void appendToResponse(WOResponse aResponse, WOContext aContext) {
		boolean wasInForm = aContext.isInForm();
		boolean wasFormSubmitted = aContext._wasFormSubmitted();
		aContext.setInForm(false);
		aContext._setFormSubmitted(false);
		try {
			super.appendToResponse(aResponse, aContext);
		}
		finally {
			aContext.setInForm(wasInForm);
			aContext._setFormSubmitted(wasFormSubmitted);
		}
	}

	@Override
	public void takeValuesFromRequest(WORequest aRequest, WOContext aContext) {
		boolean wasInForm = aContext.isInForm();
		boolean wasFormSubmitted = aContext._wasFormSubmitted();
		aContext.setInForm(false);
		aContext._setFormSubmitted(false);
		try {
			super.takeValuesFromRequest(aRequest, aContext);
		}
		finally {
			aContext.setInForm(wasInForm);
			aContext._setFormSubmitted(wasFormSubmitted);
		}
	}

	@Override
	public WOActionResults invokeAction(WORequest aRequest, WOContext aContext) {
		boolean wasInForm = aContext.isInForm();
		boolean wasFormSubmitted = aContext._wasFormSubmitted();
		aContext.setInForm(false);
		aContext._setFormSubmitted(false);
		try {
			WOActionResults results = super.invokeAction(aRequest, aContext);
			return results;
		}
		finally {
			aContext.setInForm(wasInForm);
			aContext._setFormSubmitted(wasFormSubmitted);
		}
	}
}
