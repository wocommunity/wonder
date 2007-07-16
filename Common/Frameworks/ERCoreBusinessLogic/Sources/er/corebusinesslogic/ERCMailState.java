// ERCMailState.java
// (c) by Anjo Krank (ak@kcmedia.ag)
package er.corebusinesslogic;

import com.webobjects.eocontrol.*;
import er.extensions.*;

/**
 * Mail state.
 * You must populate your DB via the populate.sql script (you might need to adapt it)
 */
public class ERCMailState extends _ERCMailState {

    /** logging support */
    public static final ERXLogger log = ERXLogger.getERXLogger(ERCMailState.class);

    public static ERCMailState EXCEPTION_STATE;
    public static ERCMailState READY_TO_BE_SENT_STATE;
    public static ERCMailState SENT_STATE;
    public static ERCMailState RECEIVED_STATE;
    public static ERCMailState WAIT_STATE;
    public static ERCMailState PROCESSING_STATE;

    public ERCMailState() {
        super();
    }

    public void awakeFromInsertion(EOEditingContext ec) {
        super.awakeFromInsertion(ec);
    }
        
    // Class methods go here
    
    public static class ERCMailStateClazz extends _ERCMailStateClazz {
        public ERCMailState sharedMailStateForKey(String key) {
            return (ERCMailState)ERXEOControlUtilities.sharedObjectWithPrimaryKey(entityName(), key);
        }
        
        public void initializeSharedData() {
            // this default allows you not to have to create the table if you don't use the mail facility
            // this defaults to false.
            if (ERCMailDelivery.usesMail()) {
                ERCMailState.EXCEPTION_STATE = sharedMailStateForKey("xcpt");
                ERCMailState.READY_TO_BE_SENT_STATE = sharedMailStateForKey("rtbs");
                ERCMailState.SENT_STATE = sharedMailStateForKey("sent");
                ERCMailState.RECEIVED_STATE = sharedMailStateForKey("rcvd");
                ERCMailState.WAIT_STATE = sharedMailStateForKey("wait");
                ERCMailState.PROCESSING_STATE = sharedMailStateForKey("proc");
            } else {
                // make ERCMailState non-shared so it does not get loaded
                ERXEOAccessUtilities.makeEditableSharedEntityNamed("ERCMailState");
            }
        }
    }

    public static ERCMailStateClazz mailStateClazz() { return (ERCMailStateClazz)EOEnterpriseObjectClazz.clazzForEntityNamed("ERCMailState"); }
}
