//
// ERXOncePerRequestConditional.java: Class file for WO Component 'ERXOncePerRequestConditional'
// Project simple
//
// Created by ak on Tue Mar 19 2002
//
package er.extensions;

import com.webobjects.appserver.*;
import com.webobjects.foundation.*;

/** 
 * ERXOncePerRequestConditional is a component that will 
 * render it's embedded content only once during the RR loop.
 *  Useful for JavaScript code that should be included only once.
 */
public class ERXOncePerRequestConditional extends ERXStatelessComponent {
    /** logging support */
    public static final ERXLogger log = ERXLogger.getERXLogger(ERXOncePerRequestConditional.class.getName());
    
    String keyName = null;
    int currentStage = -1;

    /**
     * Public constructor
     * @param context context of request
     */
    public ERXOncePerRequestConditional(WOContext context) {
        super(context);
    }

    public NSMutableDictionary displayCountDict() {
        if(!(context() instanceof ERXMutableUserInfoHolderInterface)) {
            throw new IllegalStateException("Context must implement ERXMutableUserInfoHolderInterface");
        }

        NSMutableDictionary displayCountDict = (NSMutableDictionary)((ERXMutableUserInfoHolderInterface)context()).mutableUserInfo().objectForKey("ERXOncePerRequestDisplayCountDict");
        if(displayCountDict == null) {
             displayCountDict = new NSMutableDictionary();
            ((ERXMutableUserInfoHolderInterface)context()).mutableUserInfo().setObjectForKey(displayCountDict, "ERXOncePerRequestDisplayCountDict");
        }
        return displayCountDict;
    }

    public int displayCountForKey(String key) {
	return ((Integer)displayCountDict().objectForKey(key)).intValue();
    }
    
    String keyName() {
	if(!hasBinding("keyName")) {
	    throw new IllegalStateException("'keyName' is a required binding");
	}
	if(keyName == null) {
	    keyName = (String)valueForBinding("keyName");
	}
	return keyName;
    }

    public void reset() {
	super.reset();
	keyName = null;
    }

    public void awake() {
	super.awake();
    }

    public void resetDict() {
        String key = keyName() + "--" + currentStage;

        if(displayCountDict().objectForKey(key) == null) {
            displayCountDict().setObjectForKey(ERXConstant.integerForInt(-1), key);
        }
        int count = ((Integer)displayCountDict().objectForKey(key)).intValue() + 1;
        displayCountDict().setObjectForKey(ERXConstant.integerForInt(count), key);
    }

    public void takeValuesFromRequest(WORequest aRequest, WOContext aContext) {
        currentStage = 0;
	resetDict();
	super.takeValuesFromRequest(aRequest,aContext);
    }

    public WOActionResults invokeAction(WORequest aRequest, WOContext aContext) {
        currentStage = 1;
	resetDict();
	return super.invokeAction(aRequest,aContext);
    }

    public void appendToResponse(WOResponse aResponse, WOContext aContext) {
        currentStage = 2;
	resetDict();
	super.appendToResponse(aResponse,aContext);
    }

    public boolean displayContent() {
	int showCount = displayCountForKey(keyName() + "--" + currentStage);
        if(log.isDebugEnabled())
            log.debug("displayContent - showCount: " + showCount + " stage:" + currentStage);
	return showCount == 0;
    }
}
