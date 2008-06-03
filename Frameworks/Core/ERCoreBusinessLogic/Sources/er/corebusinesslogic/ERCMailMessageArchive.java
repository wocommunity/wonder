package er.corebusinesslogic;
import com.webobjects.eocontrol.*;

public class ERCMailMessageArchive extends _ERCMailMessageArchive {

    private static final org.apache.log4j.Logger log = org.apache.log4j.Logger.getLogger(ERCMailMessageArchive.class);

    public static final ERCMailMessageArchiveClazz clazz = new ERCMailMessageArchiveClazz();
    public static class ERCMailMessageArchiveClazz extends _ERCMailMessageArchive._ERCMailMessageArchiveClazz {/* more clazz methods here */}

    public final static String ENTITY = "ERCMailMessageArchive";

    public interface Key extends _ERCMailMessageArchive.Key {}

    /**
     * Intitializes the EO. This is called when an EO is created, not when it is 
     * inserted into an EC.
     */
    public void init(EOEditingContext ec) {
        super.init(ec);
    }
    
    // more EO methods here
}
