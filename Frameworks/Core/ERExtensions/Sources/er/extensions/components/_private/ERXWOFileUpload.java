package er.extensions.components._private;

import org.apache.log4j.Logger;

import com.webobjects.appserver.WOContext;
import com.webobjects.appserver.WOElement;
import com.webobjects.appserver.WORequest;
import com.webobjects.appserver.WOResponse;
import com.webobjects.appserver._private.WODynamicElementCreationException;
import com.webobjects.appserver._private.WOFileUpload;
import com.webobjects.foundation.NSDictionary;

import er.extensions.appserver.ERXBrowserFactory;
import er.extensions.appserver.ERXSession;
import er.extensions.appserver.ERXWOContext;

/**
 * Enhanced WOFileUpload.
 * <ul>
 *  <li>throws an IllegalArgumentException when it is embedded in a WOForm that does not have enctype=multipart/form-data
 *  <li>catches "ran out of data" IllegalStateException in superclass when the user backtracked.
 *</ul>
 *
 * @author ak on Wed Oct 09 2002
 * @deprecated use {@link WOFileUpload} as parent class
 */
// CHECKME is that class obsolete as the cause of its presence was a bug fixed in WO 5.2 or should some logic be maintained?
@Deprecated
public class ERXWOFileUpload extends com.webobjects.appserver._private.WOFileUpload {

    /** logging support */
    private static final Logger log = Logger.getLogger(ERXWOFileUpload.class);
	
    /**
     * Public constructor
     */
    public ERXWOFileUpload(String string, NSDictionary nsdictionary,
                        WOElement woelement) {
        super(string, nsdictionary, woelement);
        if (nsdictionary.objectForKey("data") != null && nsdictionary.objectForKey("filePath") == null) {
            throw new WODynamicElementCreationException("<" + getClass().getName() + "> 'filePath' must be bound if 'data' is bound.");
        }
    }

    public void checkEnctype(WOContext context) {
    	if(!("multipart/form-data".equals(ERXWOContext.contextDictionary().objectForKey("enctype")))) {
    		throw new IllegalArgumentException("This form is missing a 'enctype=multipart/form-data' attribute. It is required for WOFileUpload to work.");
    	}
    }
    
    @Override
	public void takeValuesFromRequest(WORequest worequest, WOContext wocontext) {
    	try {
    		super.takeValuesFromRequest(worequest, wocontext);
    	} catch(IllegalStateException ex) {
    		// AK: Safari has the habit of posting only a part of the request when you backtrack and
    		// this in turn triggers an IllegalStateException in our superclass
    		// so we only rethrow when we didn't backtrack
    		boolean doThrow = !wocontext.hasSession() || !(wocontext.session() instanceof ERXSession) || !((ERXSession)wocontext.session()).didBacktrack();
    		if(doThrow) {
    			throw ex;
    		}
    		log.info("Ignoring a problem when reading the form values as the user backtracked: " + ex);
    	}
    }
    
    @Override
	public void appendToResponse(WOResponse response, WOContext context) {
        checkEnctype(context);
        /* Fix for lame Safari upload bug.  - CH
         * For more details, see
         * http://lists.macosforge.org/pipermail/webkit-unassigned/2007-January/026203.html
         * http://lists.apple.com/archives/macnetworkprog/2006/Dec/msg00021.html
		 * https://bugs.webkit.org/show_bug.cgi?id=5760
		 * http://www.webmasterworld.com/macintosh_webmaster/3300569.htm
		 * http://blog.airbladesoftware.com/2007/8/17/note-to-self-prevent-uploads-hanging-in-safari
         */
        if (ERXBrowserFactory.factory().browserMatchingRequest(context.request()).isSafari()) {
        	response.setHeader("close", "Connection");
        }
        super.appendToResponse(response, context);
    }
}
