//
// ERXOncePerRequestConditional.java: Class file for WO Component 'ERXOncePerRequestConditional'
// Project simple
//
// Created by ak on Tue Mar 19 2002
//
package er.extensions;

import com.webobjects.foundation.*;
import com.webobjects.appserver.*;
import com.webobjects.eocontrol.*;
import com.webobjects.eoaccess.*;
import er.extensions.*;
import org.apache.log4j.Category;

/** ERXOncePerRequestConditional is a component that will render it's embedded content only once during the RR loop.
 *  Useful for JavaScript code that should be included only once.
 */
public class ERXOncePerRequestConditional extends ERXStatelessComponent {
    static final Category cat = Category.getInstance(ERXOncePerRequestConditional.class.getName());
    
    String keyName = null;
    NSMutableDictionary displayCountDict;
    int stage = 0;
    String lastID;
	
    public ERXOncePerRequestConditional(WOContext context) {
        super(context);
	if(application().isConcurrentRequestHandlingEnabled())
	    cat.warn("This Component is not multi-threading safe!");
    }

    public int displayCountForKey(String key) {
	return ((Integer)displayCountDict.objectForKey(key)).intValue();
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

    public void resetDict(int currentStage, String contextID) {
	int count = 0;
	String key = keyName();

        if(cat.isDebugEnabled())
            cat.debug("stage:" + stage + ", currentStage:" + currentStage + ", context:" + contextID + ", lastID:" + lastID);

	
	if(stage != currentStage || !lastID.equals(contextID)) {
            if(cat.isDebugEnabled())
                cat.debug("did reset");
	    if(displayCountDict != null)
		displayCountDict.removeAllObjects();
	    stage = currentStage;
	    lastID = contextID;
	}
	
	if(displayCountDict == null) {
	    displayCountDict = new NSMutableDictionary(new Integer(0), key);
	} else if(displayCountDict.objectForKey(key) == null) {
	    displayCountDict.setObjectForKey(new Integer(0),key);
	} else {
	    count = ((Integer)displayCountDict.objectForKey(key)).intValue() + 1;
	    displayCountDict.setObjectForKey(new Integer(count), key);
	}
    }
    
    public void takeValuesFromRequest(WORequest aRequest, WOContext aContext) {
	resetDict(0, context().session().sessionID() + context().contextID());
	super.takeValuesFromRequest(aRequest,aContext);
    }

    public WOActionResults invokeAction(WORequest aRequest, WOContext aContext) {
	resetDict(1, context().session().sessionID() + context().contextID());
	return super.invokeAction(aRequest,aContext);
    }

    public void appendToResponse(WOResponse aResponse, WOContext aContext) {
	resetDict(2, context().session().sessionID() + context().contextID());
	super.appendToResponse(aResponse,aContext);
    }

    public boolean displayContent() {
	int showCount = displayCountForKey(keyName());
        if(cat.isDebugEnabled())
            cat.debug("displayContent - showCount: " + showCount + " stage:" + stage);
	return showCount == 0;
    }
}
