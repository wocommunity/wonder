package er.indexing.eof;

import com.webobjects.eocontrol.*;

public class ERIEntity extends _ERIEntity {

    @SuppressWarnings("unused")
    private static final org.apache.log4j.Logger log = org.apache.log4j.Logger.getLogger(ERIEntity.class);

    public static final ERIEntityClazz clazz = new ERIEntityClazz();
    public static class ERIEntityClazz extends _ERIEntity._ERIEntityClazz {
        /* more clazz methods here */
    }

    public interface Key extends _ERIEntity.Key {}

    /**
     * Initializes the EO. This is called when an EO is created, not when it is 
     * inserted into an EC.
     */
    public void init(EOEditingContext ec) {
        super.init(ec);
    }
    
    // more EO methods here
}
