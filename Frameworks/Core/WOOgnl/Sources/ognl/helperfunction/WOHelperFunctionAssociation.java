package ognl.helperfunction;

import com.webobjects.appserver.WOAssociation;
import com.webobjects.appserver._private.WOReadOnlyKeyValueAssociation;

/**
 * Factory methods for creating WOAssociations (ripped from WOAssociation)
 * 
 * @author mschrag
 */
public class WOHelperFunctionAssociation {
	private static boolean _keyPathIsReadOnly(String keyPath) {
		return keyPath.startsWith("@") || keyPath.indexOf(".@") > 0;
	}

	/**
	 * Returns a WOAssociation with a value.
	 * 
	 * @param obj the value
	 * @return a corresponding WOAssociation
	 */
	public static WOAssociation associationWithValue(Object obj) {
		return new WOHelperFunctionConstantValueAssociation(obj);
	}

	/**
	 * Creates a WOAssociation with a keyPath.
	 * 
	 * @param keyPath the keypath
	 * @return a corresponding WOAssociation
	 */
	public static WOAssociation associationWithKeyPath(String keyPath) {
		WOAssociation association;
		if (keyPath.charAt(0) == '^') {
			association = new WOHelperFunctionBindingNameAssociation(keyPath);
		}
		else if (_keyPathIsReadOnly(keyPath)) {
			association = new WOReadOnlyKeyValueAssociation(keyPath);
		}
		else {
			association = new WOHelperFunctionKeyValueAssociation(keyPath);
		}
		return association;
	}
}
