package er.indexing.attributes;

import com.webobjects.eocontrol.*;

public class ERIAttribute extends _ERIAttribute {

    @SuppressWarnings("unused")
    private static final org.apache.log4j.Logger log = org.apache.log4j.Logger.getLogger(ERIAttribute.class);

    public static final ERIAttributeClazz clazz = new ERIAttributeClazz();
    public static class ERIAttributeClazz extends _ERIAttribute._ERIAttributeClazz {
        /* more clazz methods here */
    }

    public interface Key extends _ERIAttribute.Key {}

    /**
     * Initializes the EO. This is called when an EO is created, not when it is 
     * inserted into an EC.
     */
    public void init(EOEditingContext ec) {
        super.init(ec);
    }
    
    // more EO methods here
}
