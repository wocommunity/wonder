package er.extensions.localization;

import com.webobjects.appserver.WOAssociation;
import com.webobjects.appserver.WOComponent;
import com.webobjects.foundation.NSKeyValueCodingAdditions;

import er.extensions.foundation.ERXStringUtilities;

/**
 * <div class="en">
 * Localizes the value it is bound to. Can handle both <code>loc:value="SomeKey"</code> and
 * <code>loc:value=someReturnValue</code> syntax.
 * Install with<pre><code>WOOgnl.setAssociationClassForPrefix(ERXLocalizerAssociation.class, "loc");</code></pre>
 * </div>
 * 
 * <div class="ja">
 * バインディングされている値をローカライズします。
 * 次の記述に対応しています：
 * <code>loc:value="SomeKey"</code>　と <code>loc:value=someReturnValue</code>
 * <p>
 * インストール<pre><code>WOOgnl.setAssociationClassForPrefix(ERXLocalizerAssociation.class, "loc");</code></pre>
 * </div>
 * 
 * @author ak
 */
public class ERXLocalizerAssociation extends WOAssociation {

	private boolean _isConstant;
	private String _value;
	private String _parentBinding;
	
	public ERXLocalizerAssociation(Object value, boolean isConstant) {
		_value = value != null ? value.toString() : null;
		_isConstant = isConstant;
		if(!_isConstant && _value != null && _value.startsWith("^")) {
			_parentBinding = _value;
			if(_parentBinding.indexOf('.') > 0) {
				_parentBinding = ERXStringUtilities.keyPathWithoutLastProperty(_value);
				_value = ERXStringUtilities.keyPathWithoutFirstProperty(_value);
			} else {
				_value = null;
			}
			_parentBinding = _parentBinding.substring(1);
		}
	}
	
	@Override
	public boolean isValueConstant() {
		return _isConstant;
	}
	
	@Override
	public Object valueInComponent(WOComponent wocomponent) {
		String key = null;
		if(_isConstant) {
			key = _value;
		} else {
			Object value = null;
			if(_parentBinding  != null) {
				value = wocomponent.valueForBinding(_parentBinding);
				if(_value != null && value != null) {
					value = NSKeyValueCodingAdditions.Utility.valueForKeyPath(value, _value);
				}
			} else {
				value = wocomponent.valueForKeyPath(keyPath());
			}
			if(value != null) {
				key = value.toString();
			}
		}
		return ERXLocalizer.currentLocalizer().localizedValueForKeyWithDefault(key);
	}

	@Override
	public String bindingInComponent(WOComponent wocomponent) {
        return _value;
	}

	@Override
	public String keyPath() {
		return _isConstant ? "<none>" : (_parentBinding != null ? _parentBinding : _value);
	}

	@Override
	public Object clone() {
		String path = _value;
		if(_parentBinding != null) {
			path = "^" + _parentBinding + (_value != null ? "." + _value : "");
		}
		return new ERXLocalizerAssociation(path, _isConstant);
	}

	@Override
	public String toString() {
		return "<" + getClass().getName() + ": value=" + _value + ", isConstant=" + _isConstant + ">";
	}
}
