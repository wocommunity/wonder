package er.ajax;

import com.webobjects.appserver.WOAssociation;
import com.webobjects.appserver.WOComponent;
import com.webobjects.foundation.NSDictionary;

public class AjaxConstantOption extends AjaxOption {
	private Object _constantValue;
	
	public AjaxConstantOption(String name, Object constantValue) {
		super(name);
		_constantValue = constantValue;
	}

	public AjaxConstantOption(String name, Object constantValue, Type type) {
		super(name, type);
		_constantValue = constantValue;
	}

	public AjaxConstantOption(String name, String bindingName, Object constantValue, Type type) {
		super(name, bindingName, constantValue, type);
		_constantValue = constantValue;
	}

	@Override
	protected Object valueInComponent(WOComponent component) {
		return _constantValue;
	}
	
	@Override
	protected Object valueInComponent(WOComponent component, NSDictionary<String, ? extends WOAssociation> associations) {
		return _constantValue;
	}
}
