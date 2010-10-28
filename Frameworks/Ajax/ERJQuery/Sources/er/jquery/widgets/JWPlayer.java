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
 * @see <a href="http://www.longtailvideo.com/support/jw-player/jw-player-for-flash-v5">JW Player</a>
 * @see <a href="http://www.longtailvideo.com/support/jw-player/jw-player-for-flash-v5/14625/html5-support">JW Player HTML5 support</a>
 * 
 * You will need to include the script jwplayer.js in your page if using Unobtrusively
 * 
 * @binding poster		ERAttachment for the video poster
 * @binding sources		An array of ERAttachments comprising the video sources
 * @binding skin		(optional) A zip of the skin for the video player that you bundle in your WO app.
 * 						If you include your own JW video player skin be sure to copy the player.swf into your WO app WebServerResources.
 * 						See <a href="http://www.longtailvideo.com/support/jw-player/jw-player-for-flash-v5/12538/supported-player-embed-methods#skins">Skins</a>
 * @binding framework	Set to "app" if you're including your own skin or licensed copy of the JW video player (.swf)
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
	
	/*
	 * Bindings
	 */
	public static interface Bindings {
		public static final String skin = "skin";
		public static final String id = "id";
		public static final String framework = "framework";
	}

	// non-synching component
	@Override
	public boolean synchronizesVariablesWithBindings() {
		return false;
	}
	
	// accessors
	public String id() {
		String id = (String) valueForBinding(Bindings.id);
		return (!"".equals(id)) ? id : ERXStringUtilities.safeIdentifierName(context().elementID());
	}
	
	public boolean hasSkin() {
		return hasBinding(Bindings.skin);
	}
	
	public String framework() {
		return hasBinding(Bindings.framework) ? (String) valueForBinding(Bindings.framework) : hasSkin() ? "app" : "ERJQuery";
	}
	
	public String skin() {
		return hasSkin() ? (String) valueForBinding(Bindings.skin) : null;
	}
	
	// R&R
    @Override
	public void appendToResponse(WOResponse response, WOContext context) {
    	super.appendToResponse(response, context);
    	
    	if (!useUnobtrusively) {
    		//ERXResponseRewriter.addScriptResourceInHead(response, context, "ERJQuery", "jquery-1.4.2.min.js");
    		ERXResponseRewriter.addScriptResourceInHead(response, context, "ERJQuery", "jwplayer.js");
    	}
    }
}
