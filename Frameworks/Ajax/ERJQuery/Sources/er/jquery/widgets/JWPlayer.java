package er.jquery.widgets;

import com.webobjects.appserver.WOComponent;
import com.webobjects.appserver.WOContext;
import com.webobjects.appserver.WOResponse;
import com.webobjects.foundation.NSKeyValueCoding;

import er.extensions.appserver.ERXResponseRewriter;
import er.extensions.foundation.ERXProperties;
import er.extensions.foundation.ERXStringUtilities;

/**
 * Component encapsulating HTML5 jwplayer
 * 
 * @see http://www.longtailvideo.com/support/jw-player/jw-player-for-html5
 * 
 * You will need to include the script jquery.jwplayer.js in your page if using Unobtrusively
 * 
 * @binding poster		ERAttachment for the video poster
 * @binding sources		An array of ERAttachments comprising the video sources
 *
 * @author ravim
 *
 */
public class JWPlayer extends WOComponent {
	private static boolean useUnobtrusively = ERXProperties.booleanForKeyWithDefault("er.jquery.useUnobtrusively", true);

	/** ERAttachment */
	public NSKeyValueCoding source;

	public JWPlayer(WOContext aContext) {
		super(aContext);
	}

	// non-synching component
	@Override
	public boolean synchronizesVariablesWithBindings() {
		return false;
	}
	
	// accessors
	public String id() {
		String id = (String) valueForBinding("id");
		return (!"".equals(id)) ? id : ERXStringUtilities.safeIdentifierName(context().elementID());
	}
	
	// R&R
    @Override
	public void appendToResponse(WOResponse response, WOContext context) {
    	super.appendToResponse(response, context);
    	
    	if (!useUnobtrusively) {
    		ERXResponseRewriter.addScriptResourceInHead(response, context, "ERJQuery", "jquery-1.4.2.min.js");
    		ERXResponseRewriter.addScriptResourceInHead(response, context, "ERJQuery", "jquery.jwplayer.js");
    	}
    }
}
