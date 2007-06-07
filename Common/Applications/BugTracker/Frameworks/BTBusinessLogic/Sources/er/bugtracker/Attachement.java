package er.bugtracker;
import com.webobjects.eocontrol.*;

public class Attachement extends _Attachement {

    private static final org.apache.log4j.Logger log = org.apache.log4j.Logger.getLogger(Attachement.class);

    public static final AttachementClazz clazz = new AttachementClazz();
    public static class AttachementClazz extends _Attachement._AttachementClazz {/* more clazz methods here */}

    public final static String ENTITY = "Attachement";

    public interface Key extends _Attachement.Key {}

    /**
     * Intitializes the EO. This is called when an EO is created, not when it is 
     * inserted into an EC.
     */
    public void init(EOEditingContext ec) {
        super.init(ec);
    }
    
    // more EO methods here
}
