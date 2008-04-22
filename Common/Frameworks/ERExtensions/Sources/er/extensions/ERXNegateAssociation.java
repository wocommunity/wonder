package er.extensions;

import com.webobjects.appserver.WOAssociation;
import com.webobjects.appserver.WOComponent;

/**
 * Negates the value it is bound to. Can handle both <code>not:value=true</code> and
 * <code>not:value=someReturnValue</code> syntax.
 * Install with <br><br><code> WOOgnl.setAssociationClassForPrefix(ERXLocalizerAssociation.class, "not");</code>
 * @author ak
 * 
 */
public class ERXNegateAssociation extends WOAssociation {

	private boolean _isConstant;
	private String _value;
	
	public ERXNegateAssociation(Object value, boolean isConstant) {
		_value = value != null ? value.toString() : null;
		_isConstant = isConstant;
	}
	
	@Override
	public boolean isValueConstant() {
		return _isConstant;
	}
	
	public Object valueInComponent(WOComponent wocomponent) {
		String key = null;
		if(_isConstant) {
			key = _value;
		} else {
			Object value = wocomponent.valueForKeyPath(keyPath());
			if(value != null) {
				key = value.toString();
			}
		}
		return Boolean.valueOf(key) ? Boolean.FALSE : Boolean.TRUE;
	}

	@Override
	public String bindingInComponent(WOComponent wocomponent) {
        return _value;
	}

	@Override
	public String keyPath() {
		return _isConstant ? "<none>" : _value.toString();
	}

	@Override
	public Object clone() {
		return new ERXNegateAssociation(_value, _isConstant);
	}

	@Override
	public String toString() {
		return "<" + getClass().getName() + ": value=" + _value + ", isConstant=" + _isConstant + ">";
	}
}
