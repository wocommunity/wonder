package er.indexing.eof;

import com.webobjects.eocontrol.*;

public class ERIFileContent extends _ERIFileContent {

    @SuppressWarnings("unused")
    private static final org.apache.log4j.Logger log = org.apache.log4j.Logger.getLogger(ERIFileContent.class);

    public static final ERIFileContentClazz clazz = new ERIFileContentClazz();
    public static class ERIFileContentClazz extends _ERIFileContent._ERIFileContentClazz {
        /* more clazz methods here */
    }

    public interface Key extends _ERIFileContent.Key {}

    /**
     * Initializes the EO. This is called when an EO is created, not when it is 
     * inserted into an EC.
     */
    public void init(EOEditingContext ec) {
        super.init(ec);
    }
    
    // more EO methods here
}
