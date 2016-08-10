//
// ERXOncePerRequestConditional.java: Class file for WO Component 'ERXOncePerRequestConditional'
// Project simple
//
// Created by ak on Tue Mar 19 2002
//
package er.extensions.components.conditionals;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.webobjects.appserver.WOActionResults;
import com.webobjects.appserver.WOContext;
import com.webobjects.appserver.WORequest;
import com.webobjects.appserver.WOResponse;
import com.webobjects.foundation.NSMutableDictionary;

import er.extensions.appserver.ERXWOContext;
import er.extensions.components.ERXStatelessComponent;
import er.extensions.eof.ERXConstant;

/** 
 * Will render its embedded content only once during the RR loop.
 * Useful for JavaScript code that should be included only once.
 * @binding keyName
 * @binding ERXOncePerRequestDisplayCountDict
 */
public class ERXOncePerRequestConditional extends ERXStatelessComponent {
	/**
	 * Do I need to update serialVersionUID?
	 * See section 5.6 <cite>Type Changes Affecting Serialization</cite> on page 51 of the 
	 * <a href="http://java.sun.com/j2se/1.4/pdf/serial-spec.pdf">Java Object Serialization Spec</a>
	 */
	private static final long serialVersionUID = 1L;

    private static final Logger log = LoggerFactory.getLogger(ERXOncePerRequestConditional.class);
    
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
    	NSMutableDictionary displayCountDict = (NSMutableDictionary)ERXWOContext.contextDictionary().objectForKey("ERXOncePerRequestDisplayCountDict");
    	if(displayCountDict == null) {
    		displayCountDict = new NSMutableDictionary();
    		ERXWOContext.contextDictionary().setObjectForKey(displayCountDict, "ERXOncePerRequestDisplayCountDict");
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

    @Override
    public void reset() {
	super.reset();
	keyName = null;
    }

    @Override
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

    @Override
    public void takeValuesFromRequest(WORequest aRequest, WOContext aContext) {
        currentStage = 0;
	resetDict();
	super.takeValuesFromRequest(aRequest,aContext);
    }

    @Override
    public WOActionResults invokeAction(WORequest aRequest, WOContext aContext) {
        currentStage = 1;
	resetDict();
	return super.invokeAction(aRequest,aContext);
    }

    @Override
    public void appendToResponse(WOResponse aResponse, WOContext aContext) {
        currentStage = 2;
	resetDict();
	super.appendToResponse(aResponse,aContext);
    }

    public boolean displayContent() {
	int showCount = displayCountForKey(keyName() + "--" + currentStage);
    log.debug("displayContent - showCount: {} stage: {}", showCount, currentStage);
	return showCount == 0;
    }
}
