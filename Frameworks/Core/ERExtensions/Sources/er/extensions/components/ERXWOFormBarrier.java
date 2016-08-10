package er.extensions.components;

import com.webobjects.appserver.WOActionResults;
import com.webobjects.appserver.WOAssociation;
import com.webobjects.appserver.WOContext;
import com.webobjects.appserver.WOElement;
import com.webobjects.appserver.WORequest;
import com.webobjects.appserver.WOResponse;
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
public class ERXWOFormBarrier extends ERXDynamicElement {
	public ERXWOFormBarrier(String name, NSDictionary<String, WOAssociation> associations, WOElement template) {
		super(name, associations, template);
	}

	public ERXWOFormBarrier(String name, NSDictionary<String, WOAssociation> associations, NSMutableArray<WOElement> children) {
		super(name, associations, children);
	}

	@Override
	public void appendToResponse(WOResponse response, WOContext context) {
		boolean wasInForm = context.isInForm();
		boolean wasFormSubmitted = context.wasFormSubmitted();
		context.setInForm(false);
		context.setFormSubmitted(false);
		try {
			super.appendToResponse(response, context);
		}
		finally {
			context.setInForm(wasInForm);
			context.setFormSubmitted(wasFormSubmitted);
		}
	}

	@Override
	public void takeValuesFromRequest(WORequest request, WOContext context) {
		boolean wasInForm = context.isInForm();
		boolean wasFormSubmitted = context.wasFormSubmitted();
		context.setInForm(false);
		context.setFormSubmitted(false);
		try {
			super.takeValuesFromRequest(request, context);
		}
		finally {
			context.setInForm(wasInForm);
			context.setFormSubmitted(wasFormSubmitted);
		}
	}

	@Override
	public WOActionResults invokeAction(WORequest request, WOContext context) {
		boolean wasInForm = context.isInForm();
		boolean wasFormSubmitted = context.wasFormSubmitted();
		context.setInForm(false);
		context.setFormSubmitted(false);
		try {
			WOActionResults results = super.invokeAction(request, context);
			return results;
		}
		finally {
			context.setInForm(wasInForm);
			context.setFormSubmitted(wasFormSubmitted);
		}
	}
}
