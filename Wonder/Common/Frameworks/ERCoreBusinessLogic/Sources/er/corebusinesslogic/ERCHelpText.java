package er.corebusinesslogic;
import com.webobjects.eocontrol.EOEditingContext;

public class ERCHelpText extends _ERCHelpText {

    private static final org.apache.log4j.Logger log = org.apache.log4j.Logger.getLogger(ERCHelpText.class);

    public static final ERCHelpTextClazz clazz = new ERCHelpTextClazz();
    public static class ERCHelpTextClazz extends _ERCHelpTextClazz {/* more clazz methods here */}

    public final static String ENTITY = "ERCHelpText";

    public interface Key extends _ERCHelpText.Key {}

    /**
     * Intitializes the EO. This is called when an EO is created, not when it is 
     * inserted into an EC.
     */
    public void init(EOEditingContext ec) {
        super.init(ec);
    }
    
    // more EO methods here
}
