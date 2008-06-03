package ognl.helperfunction;

import com.webobjects.appserver._private.WODeclaration;
import com.webobjects.foundation.NSMutableDictionary;

/**
 * WOTagProcessor allows you to munge the associations for a tag declaration. For instance, you could map elementType
 * "not" to a tag processor that returns a WOConditional with the "negate = 'true'" association added to its
 * associations dictionary.
 * 
 * @author mschrag
 */
public abstract class WOTagProcessor {
	public WOTagProcessor() {
	}

	public WODeclaration createDeclaration(String elementName, String elementType, NSMutableDictionary associations) {
		return new WODeclaration(elementName, elementType, associations);
	}
}
