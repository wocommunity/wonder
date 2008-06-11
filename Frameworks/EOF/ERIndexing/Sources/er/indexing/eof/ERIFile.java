package er.indexing.eof;

import com.webobjects.eocontrol.*;

public class ERIFile extends _ERIFile {

    @SuppressWarnings("unused")
    private static final org.apache.log4j.Logger log = org.apache.log4j.Logger.getLogger(ERIFile.class);

    public static final ERIFileClazz clazz = new ERIFileClazz();
    public static class ERIFileClazz extends _ERIFile._ERIFileClazz {
        /* more clazz methods here */
    }

    public interface Key extends _ERIFile.Key {}

    /**
     * Initializes the EO. This is called when an EO is created, not when it is 
     * inserted into an EC.
     */
    public void init(EOEditingContext ec) {
        super.init(ec);
    }
    
    // more EO methods here
}
