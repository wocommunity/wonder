package er.ajax;

import com.webobjects.appserver.WOElement;
import com.webobjects.appserver._private.WOConstantValueAssociation;
import com.webobjects.foundation.NSDictionary;
import com.webobjects.foundation.NSMutableDictionary;

import er.extensions.components.conditionals.ERXWOTemplate;

public class AjaxInPlaceViewTemplate extends ERXWOTemplate{

	public AjaxInPlaceViewTemplate(String aName, NSDictionary associations, WOElement template) {
		super(aName, AjaxInPlaceViewTemplate.processAssociations(associations), template);
	}

	protected static NSDictionary processAssociations(NSDictionary associations) {
		NSMutableDictionary mutableAssociations = (NSMutableDictionary) associations;
		mutableAssociations.setObjectForKey(new WOConstantValueAssociation("view"), "templateName");
		return mutableAssociations;
	}


}