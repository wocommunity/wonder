package ognl.helperfunction;

import com.webobjects.appserver.WOComponent;
import com.webobjects.appserver._private.WOKeyValueAssociation;

/**
 * WOHelperFunction version of WOKeyValueAssociation that adds support for dynamic debugSupport.
 * 
 * @author mschrag
 */
public class WOHelperFunctionKeyValueAssociation extends WOKeyValueAssociation {
	public WOHelperFunctionKeyValueAssociation(String s) {
		super(s);
	}

	@Override
	public void setValue(Object obj, WOComponent wocomponent) {
		if (WOHelperFunctionParser._debugSupport) {
			WOHelperFunctionDebugUtilities.setDebugEnabled(this, wocomponent);
		}
		super.setValue(obj, wocomponent);
	}

	@Override
	protected String _debugDescription() {
		return keyPath();
	}
}
