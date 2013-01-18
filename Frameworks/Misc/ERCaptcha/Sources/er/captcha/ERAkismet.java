package er.captcha;

import java.util.Map;

import net.sf.akismet.Akismet;

import com.webobjects.appserver.WOContext;
import com.webobjects.appserver.WORequest;
import com.webobjects.foundation.NSValidation;

import er.extensions.appserver.ERXApplication;
import er.extensions.components.ERXComponent;
import er.extensions.components.ERXSimpleSpamCheck;
import er.extensions.foundation.ERXProperties;

/**
 * ERAkismet is a component that behaves like ERXSimpleSpamCheck but uses the blog comment spam identification service
 * provided by http://akismet.com. If you are using this component in a commercial environment, you must abide by the
 * terms of service specified on Askimet's site.
 * 
 * @author mschrag
 * @property er.captcha.akismet.apiKey your Akismet API key
 * @property er.captcha.akismet.url your site's URL (defaults to http://request._serverName())
 * @binding remoteAddress (optional) the address of the remote user (defaults to request._remoteAddress())
 * @binding userAgent (optional) the user-agent of the remote user (defaults to request.headerForKey("user-agent"))
 * @binding referrer (optional) the referrer of this request (defaults to request.headerForKey("referer"))
 * @binding permalink (optional) the permalink of the page being commented on
 * @binding commentType (optional) the type of comment
 * @binding author (optional) the name of the author
 * @binding authorEmail (optional) the email address of the author
 * @binding authorURL (optional) the URL of the author
 * @binding content the comment content
 */
public class ERAkismet extends ERXComponent {
	/**
	 * Do I need to update serialVersionUID?
	 * See section 5.6 <cite>Type Changes Affecting Serialization</cite> on page 51 of the 
	 * <a href="http://java.sun.com/j2se/1.4/pdf/serial-spec.pdf">Java Object Serialization Spec</a>
	 */
	private static final long serialVersionUID = 1L;

	public ERAkismet(WOContext context) {
		super(context);
	}

	@Override
	public boolean isStateless() {
		return true;
	}

	@Override
	public void takeValuesFromRequest(WORequest request, WOContext context) {
		if (context.wasFormSubmitted()) {
			super.takeValuesFromRequest(request, context);

			String apiKey = ERXProperties.stringForKey("er.captcha.akismet.apiKey");
			String url = ERXProperties.stringForKeyWithDefault("er.captcha.akismet.url", "http://" + request._serverName());
			Akismet api = new Akismet(apiKey, url);
			if (ERXApplication.isDevelopmentModeSafe()) {
				if (!api.verifyAPIKey()) {
					throw new RuntimeException("The API key you provided is invalid. Please set a valid api key in the property 'er.captcha.akismet.apiKey'.");
				}
			}

			String ipAddress = stringValueForBinding("remoteAddress", request._remoteAddress());
			String userAgent = stringValueForBinding("userAgent", request.headerForKey("user-agent"));
			String referrer = stringValueForBinding("referrer", request.headerForKey("referer"));
			String permalink = stringValueForBinding("permalink");
			String commentType = stringValueForBinding("commentType");
			String author = stringValueForBinding("author");
			String authorEmail = stringValueForBinding("authorEmail");
			String authorURL = stringValueForBinding("authorURL");
			String content = stringValueForBinding("content");
			Map other = null;

			boolean isSpam = api.commentCheck(ipAddress, userAgent, referrer, permalink, commentType, author, authorEmail, authorURL, content, other);
			if (isSpam) {
				validationFailedWithException(new NSValidation.ValidationException("Spam check failed."), this, ERXSimpleSpamCheck.SPAM_CHECK_KEY);
				setValueForBinding(Boolean.FALSE, "valid");
			}
			else {
				setValueForBinding(Boolean.TRUE, "valid");
			}
		}
	}
}
