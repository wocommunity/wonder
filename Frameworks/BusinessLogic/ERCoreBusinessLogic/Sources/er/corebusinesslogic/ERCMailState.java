// ERCMailState.java
// (c) by Anjo Krank (ak@kcmedia.ag)
package er.corebusinesslogic;

import er.extensions.eof.ERXConstant;

/**
 * Mail state.
 */
public class ERCMailState extends ERXConstant.StringConstant {
    public ERCMailState(String key, String name) {
        super(key, name);
    }
    
    public static ERCMailState mailState(String key) {
    	return (ERCMailState) constantForClassNamed(key, ERCMailState.class.getName());
    }

    public static ERCMailState EXCEPTION_STATE = new ERCMailState ("xcpt", "Exception");
    public static ERCMailState READY_TO_BE_SENT_STATE = new ERCMailState("rtbs", "Ready to be sent");
    public static ERCMailState SENT_STATE = new ERCMailState("sent", "Sent");
    public static ERCMailState RECEIVED_STATE = new ERCMailState("rcvd", "Received");
    public static ERCMailState WAIT_STATE = new ERCMailState("wait", "Wait");
    public static ERCMailState PROCESSING_STATE = new ERCMailState("proc", "Processing");
}
