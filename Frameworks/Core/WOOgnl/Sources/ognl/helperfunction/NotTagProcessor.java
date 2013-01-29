package ognl.helperfunction;

import com.webobjects.appserver._private.WOConstantValueAssociation;
import com.webobjects.appserver._private.WODeclaration;
import com.webobjects.foundation.NSMutableDictionary;

/**
 * "not" tag processor. This is a shortcut for a WOConditional with negate = 'true'. All you set is "condition".
 * 
 * @author mschrag
 */
public class NotTagProcessor extends WOTagProcessor {
	@Override
	public WODeclaration createDeclaration(String elementName, String elementType, NSMutableDictionary associations) {
		String newElementType = "ERXWOConditional";
		if (associations.objectForKey("negate") != null) {
			throw new IllegalArgumentException("You already specified a binding for 'negate' of " + associations.objectForKey("negate") + " on a wo:not.");
		}
		associations.setObjectForKey(new WOConstantValueAssociation(Boolean.TRUE), "negate");
		return super.createDeclaration(elementName, newElementType, associations);
	}
}
