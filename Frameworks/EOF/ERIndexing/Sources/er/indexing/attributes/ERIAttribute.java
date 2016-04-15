package er.indexing.attributes;

import java.text.Format;
import java.text.ParseException;

import com.webobjects.eocontrol.EOEditingContext;
import com.webobjects.foundation.NSForwardException;

public class ERIAttribute extends _ERIAttribute {
	/**
	 * Do I need to update serialVersionUID?
	 * See section 5.6 <cite>Type Changes Affecting Serialization</cite> on page 51 of the 
	 * <a href="http://java.sun.com/j2se/1.4/pdf/serial-spec.pdf">Java Object Serialization Spec</a>
	 */
	private static final long serialVersionUID = 1L;

    public static final ERIAttributeClazz clazz = new ERIAttributeClazz();
    public static class ERIAttributeClazz extends _ERIAttribute._ERIAttributeClazz {
        /* more clazz methods here */
    }
    
    public interface Key extends _ERIAttribute.Key {}

    @Override
    public void init(EOEditingContext ec) {
        super.init(ec);
    }
    
    public String formatValue(Object value) {
        return formatter().format(value);
    }
    
    public Object parseValue(String value) {
        try {
            return formatter().parseObject(value);
        } catch (ParseException e) {
            throw NSForwardException._runtimeExceptionForThrowable(e);
        }
    }
    
    public Format formatter() {
        return attributeType().formatter();
    }
}
