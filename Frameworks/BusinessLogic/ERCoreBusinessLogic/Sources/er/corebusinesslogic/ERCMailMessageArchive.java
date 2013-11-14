package er.corebusinesslogic;
import com.webobjects.eocontrol.EOEditingContext;

public class ERCMailMessageArchive extends _ERCMailMessageArchive {
	/**
	 * Do I need to update serialVersionUID?
	 * See section 5.6 <cite>Type Changes Affecting Serialization</cite> on page 51 of the 
	 * <a href="http://java.sun.com/j2se/1.4/pdf/serial-spec.pdf">Java Object Serialization Spec</a>
	 */
	private static final long serialVersionUID = 1L;

    @SuppressWarnings("unused")
    private static final org.apache.log4j.Logger log = org.apache.log4j.Logger.getLogger(ERCMailMessageArchive.class);

    public static final ERCMailMessageArchiveClazz clazz = new ERCMailMessageArchiveClazz();
    public static class ERCMailMessageArchiveClazz extends _ERCMailMessageArchive._ERCMailMessageArchiveClazz {/* more clazz methods here */}

    public final static String ENTITY = "ERCMailMessageArchive";

    public interface Key extends _ERCMailMessageArchive.Key {}

    /**
     * Intitializes the EO. This is called when an EO is created, not when it is 
     * inserted into an EC.
     */
    @Override
    public void init(EOEditingContext ec) {
        super.init(ec);
    }
    
    // more EO methods here
}
