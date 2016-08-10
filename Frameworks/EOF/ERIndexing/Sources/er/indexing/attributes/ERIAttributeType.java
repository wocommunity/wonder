package er.indexing.attributes;

import java.text.Format;

import com.webobjects.eocontrol.EOEditingContext;

public class ERIAttributeType extends _ERIAttributeType {
	/**
	 * Do I need to update serialVersionUID?
	 * See section 5.6 <cite>Type Changes Affecting Serialization</cite> on page 51 of the 
	 * <a href="http://java.sun.com/j2se/1.4/pdf/serial-spec.pdf">Java Object Serialization Spec</a>
	 */
	private static final long serialVersionUID = 1L;

    public static final ERIAttributeTypeClazz clazz = new ERIAttributeTypeClazz();
    public static class ERIAttributeTypeClazz extends _ERIAttributeType._ERIAttributeTypeClazz {
        /* more clazz methods here */
    }

    public interface Key extends _ERIAttributeType.Key {}

    @Override
    public void init(EOEditingContext ec) {
        super.init(ec);
    }

    public Format formatter() {
        return valueType().formatterForFormat(format());
    }
}
