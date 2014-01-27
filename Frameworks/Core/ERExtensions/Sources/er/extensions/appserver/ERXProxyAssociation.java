package er.extensions.appserver;

import com.webobjects.appserver.WOAssociation;
import com.webobjects.appserver.WOComponent;
import com.webobjects.appserver._private.WOConstantValueAssociation;

/**
 * ERXProxyAssociation allows you to create a wrapper around an existing
 * association for the purposes of injecting a prefix or a suffix to the
 * association's value.  This is useful in, for instance, dynamic elements
 * where you want to guarantee that a particular CSS class is prepended
 * to an existing "class" binding where you don't necessarily have
 * easy access to the original "class" WOAssociation (like if you extend
 * WOHyperlink, for example).
 * 
 * @author mschrag
 */
public class ERXProxyAssociation extends WOAssociation implements Cloneable {
	private WOAssociation _proxiedAssociation;
	private String _prefix;
	private String _suffix;
	private boolean _treatNullAsEmptyString;

	public ERXProxyAssociation(WOAssociation proxiedAssociation, String prefix, String suffix, boolean treatNullAsEmptyString) {
		_proxiedAssociation = proxiedAssociation;
		if (_proxiedAssociation == null) {
			_proxiedAssociation = new WOConstantValueAssociation(null);
		}
		_prefix = prefix;
		_suffix = suffix;
		_treatNullAsEmptyString = treatNullAsEmptyString;
	}

	@Override
	public Object clone() {
		return new ERXProxyAssociation(_proxiedAssociation, _prefix, _suffix, _treatNullAsEmptyString);
	}

	@Override
	public String toString() {
		return "<" + getClass().getName() + ": proxiedAssociation=" + _proxiedAssociation + ", prefix=" + _prefix + ", suffix=" + _suffix + ">";
	}

	@Override
	public Object valueInComponent(WOComponent wocomponent) {
		Object value = _proxiedAssociation.valueInComponent(wocomponent);
		if (value instanceof String || (value == null && _treatNullAsEmptyString)) {
			StringBuilder newValue = new StringBuilder();
			if (_prefix != null) {
				newValue.append(_prefix);
			}
			if (value != null) {
				newValue.append(value);
			}
			if (_suffix != null) {
				newValue.append(_suffix);
			}
			value = newValue.toString();
		}
		return value;
	}

	protected Object processValue(Object obj) {
		Object newValue = obj;
		if (newValue instanceof String) {
			String newStr = (String) newValue;
			if (newStr.startsWith(_prefix)) {
				newStr = newStr.substring(_prefix.length());
			}
			if (newStr.endsWith(_suffix)) {
				newStr = newStr.substring(0, newStr.length() - _suffix.length());
			}
			newValue = newStr;
		}
		return newValue;
	}

	@Override
	public void setValue(Object obj, WOComponent wocomponent) {
		_proxiedAssociation.setValue(processValue(obj), wocomponent);
	}

	@Override
	public void _setValueNoValidation(Object obj, WOComponent wocomponent) {
		_proxiedAssociation._setValueNoValidation(processValue(obj), wocomponent);
	}

	@Override
	public boolean isValueSettable() {
		return _proxiedAssociation.isValueSettable();
	}

	@Override
	public boolean isValueConstant() {
		return _proxiedAssociation.isValueConstant();
	}

	@Override
	public String keyPath() {
		return _proxiedAssociation.keyPath();
	}

	@Override
	public String bindingInComponent(WOComponent wocomponent) {
		return _proxiedAssociation.bindingInComponent(wocomponent);
	}
}