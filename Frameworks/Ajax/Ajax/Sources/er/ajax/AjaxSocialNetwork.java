package er.ajax;

import java.util.NoSuchElementException;

import com.webobjects.foundation.NSDictionary;
import com.webobjects.foundation.NSMutableDictionary;

import er.extensions.foundation.ERXMutableURL;
import er.extensions.foundation.ERXStringUtilities;

/**
 * Provides an interface to retrieving information and URLs 
 * about various social network sites.
 * 
 * @author mschrag
 */
public abstract class AjaxSocialNetwork {
	private static NSMutableDictionary<String, AjaxSocialNetwork> _socialNetworks;
	static {
		_socialNetworks = new NSMutableDictionary<String, AjaxSocialNetwork>();
		_socialNetworks.setObjectForKey(new AjaxSocialNetwork.Delicious(), "delicious");
		_socialNetworks.setObjectForKey(new AjaxSocialNetwork.Digg(), "digg");
		_socialNetworks.setObjectForKey(new AjaxSocialNetwork.Facebook(), "facebook");
		_socialNetworks.setObjectForKey(new AjaxSocialNetwork.Furl(), "furl");
		_socialNetworks.setObjectForKey(new AjaxSocialNetwork.Newsvine(), "newsvine");
		_socialNetworks.setObjectForKey(new AjaxSocialNetwork.Netscape(), "netscape");
		_socialNetworks.setObjectForKey(new AjaxSocialNetwork.Reddit(), "reddit");
		_socialNetworks.setObjectForKey(new AjaxSocialNetwork.StumbleUpon(), "stumble");
		_socialNetworks.setObjectForKey(new AjaxSocialNetwork.Technorati(), "technorati");
		_socialNetworks.setObjectForKey(new AjaxSocialNetwork.Squidoo(), "squidoo");
		_socialNetworks.setObjectForKey(new AjaxSocialNetwork.WindowsLive(), "live");
		_socialNetworks.setObjectForKey(new AjaxSocialNetwork.YahooMyWeb(), "yahoo");
		_socialNetworks.setObjectForKey(new AjaxSocialNetwork.Ask(), "ask");
		_socialNetworks.setObjectForKey(new AjaxSocialNetwork.Google(), "google");
		_socialNetworks.setObjectForKey(new AjaxSocialNetwork.Magnolia(), "magnolia");
		_socialNetworks.setObjectForKey(new AjaxSocialNetwork.Ning(), "ning");
		_socialNetworks.setObjectForKey(new AjaxSocialNetwork.Rawsugar(), "rawsugar");
		_socialNetworks.setObjectForKey(new AjaxSocialNetwork.Spurl(), "spurl");
		_socialNetworks.setObjectForKey(new AjaxSocialNetwork.Tagtooga(), "tagtooga");
	}

	/**
	 * Returns the social network with the given name.  Supported names include:
	 * delicious, digg, facebook, furl, newsvine, netscape, reddit, stumble, technorati,
	 * squidoo, live, yahoo, ask, google, magnolia, ning, rawsugar, spurl, and tagtooga.
	 * 
	 * @param name the name of the social network site
	 * @return the matching Social Network
	 * @throws NoSuchElementException if there is no social network registered with that name
	 */
	public static AjaxSocialNetwork socialNetworkNamed(String name) {
		AjaxSocialNetwork socialNetwork = AjaxSocialNetwork._socialNetworks.objectForKey(name);
		if (socialNetwork == null) {
			throw new NoSuchElementException("There is no AjaxSocialNetwork named '" + name + "'.");
		}
		return socialNetwork;
	}

	/**
	 * Registers a new social network with the given name.
	 * 
	 * @param socialNetwork the social network
	 * @param name the lookup name
	 */
	public static void registerSocialNetworkNamed(AjaxSocialNetwork socialNetwork, String name) {
		AjaxSocialNetwork._socialNetworks.setObjectForKey(socialNetwork, name);
	}
	
	protected String _submissionUrl(String baseUrl, String urlKey, String targetUrl, String titleKey, String title, NSDictionary<String, String> additionalParams) {
		try {
			ERXMutableURL url = new ERXMutableURL(baseUrl);
			url.setQueryParameter(urlKey, targetUrl);
			if (titleKey != null && title != null) {
				url.setQueryParameter(titleKey, title);
			}
			if (additionalParams != null) {
				url.addQueryParameters(additionalParams);
			}
			return url.toString();
		}
		catch (Throwable t) {
			throw new RuntimeException("Failed to create a URL for '" + baseUrl + "' with the targetUrl '" + targetUrl + "'");
		}
	}

	/**
	 * Returns the display name of the social network.
	 *  
	 * @return the display name of the social network
	 */
	public String name() {
		return ERXStringUtilities.displayNameForKey(getClass().getSimpleName());
	}

	/**
	 * Returns the icon framework for the social network.
	 * @return the icon framework for the social network
	 */
	public String iconFramework() {
		return "Ajax";
	}
	
	/**
	 * Returns the icon path for the social network.
	 * 
	 * @return the icon path for the social network
	 */
	public String iconName() {
		return "SocialNetwork/" + getClass().getSimpleName() + ".png";
	}

	/**
	 * Returns the URL for submitting the given url and title to the social network.
	 * 
	 * @param url the URL of the page to submit
	 * @param title the title of the pageto submit
	 * @return the submission URL
	 */
	public abstract String submissionUrl(String url, String title);

	public static class Delicious extends AjaxSocialNetwork {
		@Override
		public String submissionUrl(String url, String title) {
			return _submissionUrl("http://del.icio.us/post", "url", url, "title", title, null);
		}
	}

	public static class Digg extends AjaxSocialNetwork {
		@Override
		public String submissionUrl(String url, String title) {
			return _submissionUrl("http://digg.com/submit", "url", url, "title", title, new NSDictionary<String, String>("2", "phase"));
		}
	}

	public static class Furl extends AjaxSocialNetwork {
		@Override
		public String iconName() {
			return "SocialNetwork/Furl.gif";
		}

		@Override
		public String submissionUrl(String url, String title) {
			return _submissionUrl("http://www.furl.net/store", "u", url, "ti", title, new NSDictionary<String, String>(new String[] { "f", "0" }, new String[] { "s", "to" }));
		}
	}

	public static class Newsvine extends AjaxSocialNetwork {
		@Override
		public String submissionUrl(String url, String title) {
			return _submissionUrl("http://www.newsvine.com/_tools/seed&save", "u", url, "T", title, null);
		}

	}

	public static class Netscape extends AjaxSocialNetwork {
		@Override
		public String submissionUrl(String url, String title) {
			return _submissionUrl("http://www.netscape.com/submit/", "U", url, "T", title, null);
		}

	}

	public static class Facebook extends AjaxSocialNetwork {
		@Override
		public String submissionUrl(String url, String title) {
			return _submissionUrl("http://www.facebook.com/sharer.php", "u", url, "t", title, null);
		}

		@Override
		public String iconName() {
			return "SocialNetwork/Facebook.gif";
		}
	}

	public static class Reddit extends AjaxSocialNetwork {
		@Override
		public String submissionUrl(String url, String title) {
			return _submissionUrl("http://reddit.com/submit", "url", url, "title", title, null);
		}

		@Override
		public String iconName() {
			return "SocialNetwork/Reddit.gif";
		}
	}

	public static class StumbleUpon extends AjaxSocialNetwork {
		@Override
		public String submissionUrl(String url, String title) {
			return _submissionUrl("http://www.stumbleupon.com/submit", "url", url, "title", title, null);
		}

		@Override
		public String iconName() {
			return "SocialNetwork/StumbleUpon.gif";
		}
	}

	public static class Technorati extends AjaxSocialNetwork {
		@Override
		public String submissionUrl(String url, String title) {
			return _submissionUrl("ttp://technorati.com/faves", "add", url, null, null, null);
		}

	}

	public static class Squidoo extends AjaxSocialNetwork {
		@Override
		public String submissionUrl(String url, String title) {
			return _submissionUrl("http://www.squidoo.com/lensmaster/bookmark", null, url, "title", title, null);
		}

	}

	public static class WindowsLive extends AjaxSocialNetwork {
		@Override
		public String submissionUrl(String url, String title) {
			return _submissionUrl("https://favorites.live.com/quickadd.aspx", "url", url, "title", title, new NSDictionary<String, String>(new String[] { "1", "en-us", "1" }, new String[] { "marklet", "mkt", "top" }));
		}

	}

	public static class YahooMyWeb extends AjaxSocialNetwork {
		@Override
		public String name() {
			return "Yahoo MyWeb";
		}

		@Override
		public String submissionUrl(String url, String title) {
			return _submissionUrl("http://myweb.yahoo.com/myresults/bookmarklet", "u", url, "t", title, new NSDictionary<String, String>("UTF", "ei"));
		}

	}

	public static class Ask extends AjaxSocialNetwork {
		@Override
		public String submissionUrl(String url, String title) {
			return _submissionUrl("http://myjeeves.ask.com/mysearch/BookmarkIt", "url", url, "title", title, new NSDictionary<String, String>(new String[] { "1.2", "webpages" }, new String[] { "v", "t" }));
		}

	}

	public static class Google extends AjaxSocialNetwork {
		@Override
		public String submissionUrl(String url, String title) {
			return _submissionUrl("http://www.google.com/bookmarks/mark", "bkmk", url, "title", title, new NSDictionary<String, String>(new String[] { "edit", "popup" }, new String[] { "op", "output" }));
		}

	}

	public static class Magnolia extends AjaxSocialNetwork {
		@Override
		public String submissionUrl(String url, String title) {
			return _submissionUrl("http://ma.gnolia.com/bookmarklet/snap/add", "url", url, "title", title, null);
		}

	}

	public static class Ning extends AjaxSocialNetwork {
		@Override
		public String submissionUrl(String url, String title) {
			return _submissionUrl("http://bookmarks.ning.com/addItem.php", "url", url, "T", title, null);
		}

	}

	public static class Rawsugar extends AjaxSocialNetwork {
		@Override
		public String submissionUrl(String url, String title) {
			return _submissionUrl("http://www.rawsugar.com/pages/tagger.faces", "url", url, "tttl", title, null);
		}

	}

	public static class Spurl extends AjaxSocialNetwork {
		@Override
		public String submissionUrl(String url, String title) {
			return _submissionUrl("http://www.spurl.net/spurl.php", "url", url, "title", title, null);
		}

	}

	public static class Tagtooga extends AjaxSocialNetwork {
		@Override
		public String submissionUrl(String url, String title) {
			return _submissionUrl("http://www.tagtooga.com/tapp/db.exe", "url", url, "title", title, new NSDictionary<String, String>(new String[] { "jsEntryForm", "fx" }, new String[] { "c", "b" }));
		}
	}
}