package er.sproutcore.views;

import com.webobjects.appserver.WOAssociation;
import com.webobjects.appserver._private.WOConstantValueAssociation;

import er.ajax.AjaxOption;
import er.ajax.AjaxValue;

public class SCProperty {
	private String _name;
	private WOAssociation _association;
	private AjaxOption.Type _type;
	private Object _defaultValue;
	private boolean _skipIfNull;

	public SCProperty(String name) {
		this(name, null, null, AjaxOption.DEFAULT, true);
	}
	
	public SCProperty(String name, Object defaultValue) {
		this(name, null, defaultValue, AjaxOption.DEFAULT, true);
	}
	
	public SCProperty(String name, WOAssociation association, Object defaultValue, AjaxOption.Type type, boolean skipIfNull) {
		_name = name;
		_association = association;
		_defaultValue = defaultValue;
		_type = type;
		_skipIfNull = skipIfNull;
	}
	
	@Override
	public boolean equals(Object obj) {
		return obj instanceof SCProperty && ((SCProperty)obj)._name.equals(_name);
	}
	
	@Override
	public int hashCode() {
		return _name.hashCode();
	}
	
	public String name() {
		return _name;
	}
	
	public boolean isBound() {
		return _association != null;
	}
	
	public WOAssociation association() {
		return (_association == null) ? new WOConstantValueAssociation(_defaultValue) : _association;
	}
	
	public AjaxOption.Type type() {
		return _type;
	}
	
	public boolean skipIfNull() {
		return _skipIfNull;
	}
	
	public String javascriptValue(Object value) {
        return new AjaxValue(_type, value).javascriptValue();
	}
}
