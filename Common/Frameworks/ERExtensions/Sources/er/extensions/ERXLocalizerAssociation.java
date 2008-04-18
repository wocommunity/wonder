package er.extensions;

import com.webobjects.appserver.WOAssociation;
import com.webobjects.appserver.WOComponent;

/**
 * Localizes the value it is bound to. Can handle both <code>loc:value="SomeKey"</code> and
 * <code>loc:value=someReturnValue</code> syntax.
 * Install with <br><br><code> WOOgnl.setAssociationClassForPrefix(ERXLocalizerAssociation.class, "loc");</code>
 * @author ak
 * 
 */
public class ERXLocalizerAssociation extends WOAssociation {

	private boolean _isConstant;
	private String _value;
	
	public ERXLocalizerAssociation(Object value, boolean isConstant) {
		super();
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
			Object value =  wocomponent.valueForKeyPath(keyPath());
			if(value != null) {
				key = value.toString();
			}
		}
		return ERXLocalizer.defaultLocalizer().localizedValueForKeyWithDefault(key);
	}

	@Override
	public String bindingInComponent(WOComponent wocomponent) {
        return _value;
	}

	@Override
	public String keyPath() {
		return _isConstant ? "<none>" : _value.toString();
	}
}
