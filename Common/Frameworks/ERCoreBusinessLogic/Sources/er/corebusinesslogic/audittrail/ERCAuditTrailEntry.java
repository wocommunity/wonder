package er.corebusinesslogic.audittrail;

import com.webobjects.eocontrol.*;

public class ERCAuditTrailEntry extends _ERCAuditTrailEntry {

    @SuppressWarnings("unused")
    private static final org.apache.log4j.Logger log = org.apache.log4j.Logger.getLogger(ERCAuditTrailEntry.class);

    public static final ERCAuditTrailEntryClazz clazz = new ERCAuditTrailEntryClazz();
    public static class ERCAuditTrailEntryClazz extends _ERCAuditTrailEntry._ERCAuditTrailEntryClazz {
        /* more clazz methods here */
    }

    public interface Key extends _ERCAuditTrailEntry.Key {}

    /**
     * Initializes the EO. This is called when an EO is created, not when it is 
     * inserted into an EC.
     */
    public void init(EOEditingContext ec) {
        super.init(ec);
    }
    
    // more EO methods here
}
