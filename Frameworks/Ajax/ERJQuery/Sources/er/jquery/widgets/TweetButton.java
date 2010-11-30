package er.jquery.widgets;

import com.webobjects.appserver.WOApplication;
import com.webobjects.appserver.WOComponent;
import com.webobjects.appserver.WOContext;
import com.webobjects.foundation.NSDictionary;

import er.extensions.appserver.ERXApplication;
import er.extensions.appserver.ERXWOContext;

/**
 * TweetButton as WO component
 * 
 * @see <a href="http://dev.twitter.com/pages/tweet_button">Tweet Button</a>
 * 
 * @binding		directAction		Direct action URL to link to in tweet
 * @binding		queryDictionary		Query parameters for direct action
 * @binding		dataText			@see <a href="http://dev.twitter.com/pages/tweet_button#properties-which-can-be-used-by-all-types-of-tweet-button">data-text</a>
 * @binding		dataVia				@see <a href="http://dev.twitter.com/pages/tweet_button#properties-which-can-be-used-by-all-types-of-tweet-button">data-via</a>
 * @binding		dataCount			@see <a href="http://dev.twitter.com/pages/tweet_button#properties-which-can-be-used-by-all-types-of-tweet-button">data-count</a>
 * 
 * @author ravim
 * 
 * TODO			Add javascript via code/allow for unobtrusive-ness
 *
 */
public class TweetButton extends WOComponent {
    public TweetButton(WOContext context) {
        super(context);
    }
    
    // non-synching
    @Override
    public boolean synchronizesVariablesWithBindings() {
    	return false;
    }
    
    /*
     * Bindings
     */
    public static interface Bindings {
    	public static final String directAction = "directAction";
    	public static final String queryDictionary = "queryDictionary";
    	public static final String dataText = "dataText";
    	public static final String dataVia = "dataVia";
    	public static final String dataCount = "dataCount";
    }
    
    // accessors
    public String dataCount() {
    	return hasBinding(Bindings.dataCount) ? (String) valueForBinding(Bindings.dataCount) : "none";
    }
    
    public String dataURL() {
		String host = WOApplication.application().host();
    	Integer port = (!WOApplication.application().isDirectConnectEnabled() || ERXApplication.isDevelopmentModeSafe()) ? null : 80; 
		return ERXWOContext.directActionUrl(context(), host, port, null, directAction(), queryDictionary(), false, false);
    }

	private NSDictionary<String, Object> queryDictionary() {
		return hasBinding(Bindings.queryDictionary) ? (NSDictionary) valueForBinding(Bindings.queryDictionary) : null;
	}

	private String directAction() {
		return (String) valueForBinding(Bindings.directAction);
	}
}