package er.corebusinesslogic.audittrail;

import com.webobjects.eocontrol.*;

public class ERCAuditBlob extends _ERCAuditBlob {

    @SuppressWarnings("unused")
    private static final org.apache.log4j.Logger log = org.apache.log4j.Logger.getLogger(ERCAuditBlob.class);

    public static final ERCAuditBlobClazz clazz = new ERCAuditBlobClazz();
    public static class ERCAuditBlobClazz extends _ERCAuditBlob._ERCAuditBlobClazz {
        /* more clazz methods here */
    }

    public interface Key extends _ERCAuditBlob.Key {}

    /**
     * Initializes the EO. This is called when an EO is created, not when it is 
     * inserted into an EC.
     */
    public void init(EOEditingContext ec) {
        super.init(ec);
    }
    
    // more EO methods here
}
