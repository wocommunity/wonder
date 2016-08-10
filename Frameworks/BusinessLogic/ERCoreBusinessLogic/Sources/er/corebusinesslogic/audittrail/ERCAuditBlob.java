package er.corebusinesslogic.audittrail;

import com.webobjects.eocontrol.EOEditingContext;

public class ERCAuditBlob extends _ERCAuditBlob {
	/**
	 * Do I need to update serialVersionUID?
	 * See section 5.6 <cite>Type Changes Affecting Serialization</cite> on page 51 of the 
	 * <a href="http://java.sun.com/j2se/1.4/pdf/serial-spec.pdf">Java Object Serialization Spec</a>
	 */
	private static final long serialVersionUID = 1L;

    public static final ERCAuditBlobClazz clazz = new ERCAuditBlobClazz();
    public static class ERCAuditBlobClazz extends _ERCAuditBlob._ERCAuditBlobClazz {
        /* more clazz methods here */
    }

    public interface Key extends _ERCAuditBlob.Key {}

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
