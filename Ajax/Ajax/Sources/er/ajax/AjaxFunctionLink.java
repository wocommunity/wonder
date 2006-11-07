package er.ajax;

import com.webobjects.appserver.WOElement;
import com.webobjects.appserver._private.WOConstantValueAssociation;
import com.webobjects.foundation.NSDictionary;
import com.webobjects.foundation.NSMutableDictionary;

import er.extensions.ERXHyperlink;

/**
 * AjaxFunctionLink is just like a WOHyperlink except that you 
 * only set onclick, and its href automatically becomes "javascript:void(0)".
 *
 *  @binding onclick the javascript to execute when the link is clicked.
 *  
 * @author mschrag
 */
public class AjaxFunctionLink extends ERXHyperlink {
	public AjaxFunctionLink(String aName, NSDictionary associations, WOElement template) {
		super(aName, AjaxFunctionLink.processAssociations(associations), template);
	}

	protected static NSDictionary processAssociations(NSDictionary associations) {
		NSMutableDictionary mutableAssociations = (NSMutableDictionary) associations;
		mutableAssociations.setObjectForKey(new WOConstantValueAssociation("javascript:void(0)"), "href");
		return mutableAssociations;
	}
}
