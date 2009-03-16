package er.indexing.attributes;

import java.text.FieldPosition;
import java.text.Format;
import java.text.ParseException;
import java.text.ParsePosition;

import com.webobjects.eocontrol.*;
import com.webobjects.foundation.NSForwardException;

public class ERIAttribute extends _ERIAttribute {

    @SuppressWarnings("unused")
    private static final org.apache.log4j.Logger log = org.apache.log4j.Logger.getLogger(ERIAttribute.class);

    public static final ERIAttributeClazz clazz = new ERIAttributeClazz();
    public static class ERIAttributeClazz extends _ERIAttribute._ERIAttributeClazz {
        /* more clazz methods here */
    }
    
    public interface Key extends _ERIAttribute.Key {}

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
