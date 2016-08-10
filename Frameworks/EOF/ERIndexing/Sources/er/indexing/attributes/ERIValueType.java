package er.indexing.attributes;

import java.text.DecimalFormat;
import java.text.FieldPosition;
import java.text.Format;
import java.text.ParsePosition;

import com.webobjects.foundation.NSTimestampFormatter;

import er.extensions.eof.ERXConstant;

public class ERIValueType extends ERXConstant.NumberConstant {
	/**
	 * Do I need to update serialVersionUID?
	 * See section 5.6 <cite>Type Changes Affecting Serialization</cite> on page 51 of the 
	 * <a href="http://java.sun.com/j2se/1.4/pdf/serial-spec.pdf">Java Object Serialization Spec</a>
	 */
	private static final long serialVersionUID = 1L;

    private static class IdentityFormat extends Format {
    	/**
    	 * Do I need to update serialVersionUID?
    	 * See section 5.6 <cite>Type Changes Affecting Serialization</cite> on page 51 of the 
    	 * <a href="http://java.sun.com/j2se/1.4/pdf/serial-spec.pdf">Java Object Serialization Spec</a>
    	 */
    	private static final long serialVersionUID = 1L;

        @Override
        public StringBuffer format(Object obj, StringBuffer toAppendTo, FieldPosition pos) {
            return toAppendTo.append(obj);
        }

        @Override
        public Object parseObject(String source, ParsePosition pos) {
            if(source != null) {
                return source.toString();
            }
            return null;
        }
    }
    
    private static class BooleanFormat extends Format {
    	/**
    	 * Do I need to update serialVersionUID?
    	 * See section 5.6 <cite>Type Changes Affecting Serialization</cite> on page 51 of the 
    	 * <a href="http://java.sun.com/j2se/1.4/pdf/serial-spec.pdf">Java Object Serialization Spec</a>
    	 */
    	private static final long serialVersionUID = 1L;

        @Override
        public StringBuffer format(Object obj, StringBuffer toAppendTo, FieldPosition pos) {
            return toAppendTo.append(obj);
        }

        @Override
        public Object parseObject(String source, ParsePosition pos) {
            if(source != null) {
                return Boolean.valueOf(source);
            }
            return null;
        }
    }
    
    public static ERIValueType STRING = new ERIValueType(1, "ERIValueTypeString", new IdentityFormat());
    public static ERIValueType INTEGER = new ERIValueType(2, "ERIValueTypeInteger", new DecimalFormat("0"));
    public static ERIValueType DECIMAL = new ERIValueType(3, "ERIValueTypeDecimal", new DecimalFormat("0.00"));
    public static ERIValueType DATE = new ERIValueType(4, "ERIValueTypeDate", new NSTimestampFormatter());
    public static ERIValueType BOOLEAN = new ERIValueType(5, "ERIValueTypeBoolean", new BooleanFormat());

    private Format _format;
    
    protected ERIValueType(int value, String name, Format format) {
        super(value, name);
        _format = format;
    }
    
    public static ERIValueType valueType(int key) {
        return (ERIValueType) constantForClassNamed(key, ERIValueType.class.getName());
    }

    public Format formatterForFormat(String format) {
        return _format;
    }
}
