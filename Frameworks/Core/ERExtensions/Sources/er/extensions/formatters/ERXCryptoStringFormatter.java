package er.extensions.formatters;

import java.text.FieldPosition;
import java.text.Format;
import java.text.ParsePosition;

import er.extensions.crypting.ERXCryptoString;

public class ERXCryptoStringFormatter extends Format {
	/**
	 * Do I need to update serialVersionUID?
	 * See section 5.6 <cite>Type Changes Affecting Serialization</cite> on page 51 of the 
	 * <a href="http://java.sun.com/j2se/1.4/pdf/serial-spec.pdf">Java Object Serialization Spec</a>
	 */
	private static final long serialVersionUID = 1L;

	@Override
	public StringBuffer format(Object obj, StringBuffer toAppendTo, FieldPosition pos) {
		if(obj instanceof ERXCryptoString) {
			ERXCryptoString crypto = (ERXCryptoString)obj;
			toAppendTo.append(crypto.toString());
		} else if(obj != null) {
			throw new IllegalArgumentException("The object argument must be an instance of " + ERXCryptoString.class.getName() + ". Object is an instance of the class " + obj.getClass().getName());
		}
		return toAppendTo;
	}

	@Override
	public Object parseObject(String source, ParsePosition pos) {
		ERXCryptoString crypto = new ERXCryptoString(source);
		pos.setIndex(source.length());
		return crypto;
	}

}
