package er.indexing.eof;

import com.webobjects.eocontrol.*;

public class ERIAttributeType extends _ERIAttributeType {

    @SuppressWarnings("unused")
    private static final org.apache.log4j.Logger log = org.apache.log4j.Logger.getLogger(ERIAttributeType.class);

    public static final ERIAttributeTypeClazz clazz = new ERIAttributeTypeClazz();
    public static class ERIAttributeTypeClazz extends _ERIAttributeType._ERIAttributeTypeClazz {
        /* more clazz methods here */
    }

    public interface Key extends _ERIAttributeType.Key {}

    /**
     * Initializes the EO. This is called when an EO is created, not when it is 
     * inserted into an EC.
     */
    public void init(EOEditingContext ec) {
        super.init(ec);
    }
    
    // more EO methods here
}
