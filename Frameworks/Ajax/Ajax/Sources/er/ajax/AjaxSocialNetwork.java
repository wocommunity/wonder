package er.ajax;

import java.util.NoSuchElementException;

import com.webobjects.foundation.NSDictionary;
import com.webobjects.foundation.NSMutableDictionary;

import er.extensions.foundation.ERXMutableURL;
import er.extensions.foundation.ERXStringUtilities;

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

	public static AjaxSocialNetwork socialNetworkNamed(String name) {
		AjaxSocialNetwork socialNetwork = AjaxSocialNetwork._socialNetworks.objectForKey(name);
		if (socialNetwork == null) {
			throw new NoSuchElementException("There is no AjaxSocialNetwork named '" + name + "'.");
		}
		return socialNetwork;
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

	public String name() {
		return ERXStringUtilities.displayNameForKey(getClass().getSimpleName());
	}

	public String iconName() {
		return "SocialNetwork/" + getClass().getSimpleName() + ".png";
	}

	public abstract String submissionUrl(String url, String title);

	public static class Delicious extends AjaxSocialNetwork {
		public String submissionUrl(String url, String title) {
			return _submissionUrl("http://del.icio.us/post", "url", url, "title", title, null);
		}
	}

	public static class Digg extends AjaxSocialNetwork {
		public String submissionUrl(String url, String title) {
			return _submissionUrl("http://digg.com/submit", "url", url, "title", title, new NSDictionary<String, String>("2", "phase"));
		}
	}

	public static class Furl extends AjaxSocialNetwork {
		public String iconName() {
			return "SocialNetwork/Furl.gif";
		}

		public String submissionUrl(String url, String title) {
			return _submissionUrl("http://www.furl.net/store", "u", url, "ti", title, new NSDictionary<String, String>(new String[] { "f", "0" }, new String[] { "s", "to" }));
		}
	}

	public static class Newsvine extends AjaxSocialNetwork {
		public String submissionUrl(String url, String title) {
			return _submissionUrl("http://www.newsvine.com/_tools/seed&save", "u", url, "T", title, null);
		}

	}

	public static class Netscape extends AjaxSocialNetwork {
		public String submissionUrl(String url, String title) {
			return _submissionUrl("http://www.netscape.com/submit/", "U", url, "T", title, null);
		}

	}

	public static class Facebook extends AjaxSocialNetwork {
		public String submissionUrl(String url, String title) {
			return _submissionUrl("http://www.facebook.com/sharer.php", "u", url, "t", title, null);
		}

		public String iconName() {
			return "SocialNetwork/Facebook.gif";
		}
	}

	public static class Reddit extends AjaxSocialNetwork {
		public String submissionUrl(String url, String title) {
			return _submissionUrl("http://reddit.com/submit", "url", url, "title", title, null);
		}

		public String iconName() {
			return "SocialNetwork/Reddit.gif";
		}
	}

	public static class StumbleUpon extends AjaxSocialNetwork {
		public String submissionUrl(String url, String title) {
			return _submissionUrl("http://www.stumbleupon.com/submit", "url", url, "title", title, null);
		}

		public String iconName() {
			return "SocialNetwork/StumbleUpon.gif";
		}
	}

	public static class Technorati extends AjaxSocialNetwork {
		public String submissionUrl(String url, String title) {
			return _submissionUrl("ttp://technorati.com/faves", "add", url, null, null, null);
		}

	}

	public static class Squidoo extends AjaxSocialNetwork {
		public String submissionUrl(String url, String title) {
			return _submissionUrl("http://www.squidoo.com/lensmaster/bookmark", null, url, "title", title, null);
		}

	}

	public static class WindowsLive extends AjaxSocialNetwork {
		public String submissionUrl(String url, String title) {
			return _submissionUrl("https://favorites.live.com/quickadd.aspx", "url", url, "title", title, new NSDictionary<String, String>(new String[] { "1", "en-us", "1" }, new String[] { "marklet", "mkt", "top" }));
		}

	}

	public static class YahooMyWeb extends AjaxSocialNetwork {
		public String name() {
			return "Yahoo MyWeb";
		}

		public String submissionUrl(String url, String title) {
			return _submissionUrl("http://myweb.yahoo.com/myresults/bookmarklet", "u", url, "t", title, new NSDictionary<String, String>("UTF", "ei"));
		}

	}

	public static class Ask extends AjaxSocialNetwork {
		public String submissionUrl(String url, String title) {
			return _submissionUrl("http://myjeeves.ask.com/mysearch/BookmarkIt", "url", url, "title", title, new NSDictionary<String, String>(new String[] { "1.2", "webpages" }, new String[] { "v", "t" }));
		}

	}

	public static class Google extends AjaxSocialNetwork {
		public String submissionUrl(String url, String title) {
			return _submissionUrl("http://www.google.com/bookmarks/mark", "bkmk", url, "title", title, new NSDictionary<String, String>(new String[] { "edit", "popup" }, new String[] { "op", "output" }));
		}

	}

	public static class Magnolia extends AjaxSocialNetwork {
		public String submissionUrl(String url, String title) {
			return _submissionUrl("http://ma.gnolia.com/bookmarklet/snap/add", "url", url, "title", title, null);
		}

	}

	public static class Ning extends AjaxSocialNetwork {
		public String submissionUrl(String url, String title) {
			return _submissionUrl("http://bookmarks.ning.com/addItem.php", "url", url, "T", title, null);
		}

	}

	public static class Rawsugar extends AjaxSocialNetwork {
		public String submissionUrl(String url, String title) {
			return _submissionUrl("http://www.rawsugar.com/pages/tagger.faces", "url", url, "tttl", title, null);
		}

	}

	public static class Spurl extends AjaxSocialNetwork {
		public String submissionUrl(String url, String title) {
			return _submissionUrl("http://www.spurl.net/spurl.php", "url", url, "title", title, null);
		}

	}

	public static class Tagtooga extends AjaxSocialNetwork {
		public String submissionUrl(String url, String title) {
			return _submissionUrl("http://www.tagtooga.com/tapp/db.exe", "url", url, "title", title, new NSDictionary<String, String>(new String[] { "jsEntryForm", "fx" }, new String[] { "c", "b" }));
		}
	}
}