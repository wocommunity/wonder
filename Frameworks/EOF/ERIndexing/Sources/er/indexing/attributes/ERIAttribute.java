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

    private class IdentityFormat extends Format {

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
    
    public interface Key extends _ERIAttribute.Key {}

    /**
     * Initializes the EO. This is called when an EO is created, not when it is 
     * inserted into an EC.
     */
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
        // AK: only strings for now
        return new IdentityFormat();
    }
}
