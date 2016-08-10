package er.indexing.storage;

import com.webobjects.eocontrol.EOEditingContext;

public class ERIFileContent extends _ERIFileContent {
	/**
	 * Do I need to update serialVersionUID?
	 * See section 5.6 <cite>Type Changes Affecting Serialization</cite> on page 51 of the 
	 * <a href="http://java.sun.com/j2se/1.4/pdf/serial-spec.pdf">Java Object Serialization Spec</a>
	 */
	private static final long serialVersionUID = 1L;

    public static final ERIFileContentClazz clazz = new ERIFileContentClazz();
    public static class ERIFileContentClazz extends _ERIFileContent._ERIFileContentClazz {
        /* more clazz methods here */
    }

    public interface Key extends _ERIFileContent.Key {}

    /**
     * Initializes the EO. This is called when an EO is created, not when it is 
     * inserted into an EC.
     */
    @Override
    public void init(EOEditingContext ec) {
        super.init(ec);
    }
    
    // more EO methods here
}
