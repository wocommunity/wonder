package er.ajax;

import com.webobjects.appserver.WOElement;
import com.webobjects.appserver._private.WOConstantValueAssociation;
import com.webobjects.foundation.NSDictionary;
import com.webobjects.foundation.NSMutableDictionary;

import er.extensions.ERXWOTemplate;

public class AjaxInPlaceEditTemplate extends ERXWOTemplate{

	public AjaxInPlaceEditTemplate(String aName, NSDictionary associations, WOElement template) {
		super(aName, AjaxInPlaceEditTemplate.processAssociations(associations), template);
		// TODO Auto-generated constructor stub
	}

	protected static NSDictionary processAssociations(NSDictionary associations) {
		NSMutableDictionary mutableAssociations = (NSMutableDictionary) associations;
		mutableAssociations.setObjectForKey(new WOConstantValueAssociation("edit"), "templateName");
		return mutableAssociations;
	}


}