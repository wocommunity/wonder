package ognl.helperfunction;

import com.webobjects.appserver.WOComponent;
import com.webobjects.appserver._private.WOBindingNameAssociation;

/**
 * WOHelperFunction version of WOBindingNameAssociation that adds support for dynamic debugSupport.
 * 
 * @author mschrag
 */
public class WOHelperFunctionBindingNameAssociation extends WOBindingNameAssociation {
	public WOHelperFunctionBindingNameAssociation(String s) {
		super(s);
	}

	protected String _debugDescription() {
		return _parentBindingName;
	}

	@Override
	public void setValue(Object obj, WOComponent wocomponent) {
		if (WOHelperFunctionParser._debugSupport) {
			WOHelperFunctionDebugUtilities.setDebugEnabled(this, wocomponent);
		}
		super.setValue(obj, wocomponent);
	}
}
