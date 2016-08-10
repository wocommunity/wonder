package er.prototaculous.support;

import com.webobjects.appserver.WOComponent;
import com.webobjects.appserver.WOContext;
import com.webobjects.appserver.WORequest;

import er.extensions.appserver.ajax.ERXAjaxApplication;

/**
 * Any component vended as an action via Ajax.Updater (or Ajax.Request) needs to be a subclass of WXPageFragment
 * only really necessary IF it has form controls as content (and it requires form values to be passed between requests)!
 * 
 * @author mendis
 *
 */
public abstract class WXPageFragment extends WOComponent {
	private String _forceFormSubmittedElementID;		// var used to remember the form it is contained in
    
    /*
     * The Ajax.Updater and Ajax.Request buttons exploit the _forceForSubmitted form value to get the ajax component to perform the taking form values from the request.
     * This is necessary when your container being updated is INSIDE a form component. The ajax pageFragment would not perform the takeValues... part
     * of the WO request/response cycle without this logic.
     * 
     * (non-Javadoc)
     * @see com.webobjects.appserver.WOComponent#takeValuesFromRequest(com.webobjects.appserver.WORequest, com.webobjects.appserver.WOContext)
     */
	@Override
	public void takeValuesFromRequest(WORequest request, WOContext context) {
		if (_forceFormSubmittedElementID == null) _forceFormSubmittedElementID = (String) request.formValueForKey("_forceFormSubmitted");
		String forceFormSubmittedElementID = (String) request.formValueForKey("_forceFormSubmitted");
		boolean forceFormSubmitted = forceFormSubmittedElementID != null && forceFormSubmittedElementID.equals(_forceFormSubmittedElementID);
		boolean _wasFormSubmitted = context.wasFormSubmitted();

		if (isAjax() && forceFormSubmitted) context.setFormSubmitted(true);
		super.takeValuesFromRequest(request, context);
		if (isAjax() && forceFormSubmitted) context.setFormSubmitted(_wasFormSubmitted);
	}
	
	private boolean isAjax() {
		return ERXAjaxApplication.isAjaxRequest(context().request());
	}
	
	// default constructor
    public WXPageFragment(WOContext context) {
        super(context);
    }
}
