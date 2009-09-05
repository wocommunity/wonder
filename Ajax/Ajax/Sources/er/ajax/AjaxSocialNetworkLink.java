package er.ajax;

import com.webobjects.appserver.WOContext;

import er.extensions.ERXComponent;

/**
 * AjaxSocialNetworkLink creates a link to the submission URL for 
 * a social network around the social network's icon.
 * 
 * @author mschrag
 * @binding name the name of the social network (@see er.ajax.AjaxSocialNetwork.socialNetworkNamed)
 * @binding url the URL to submit
 * @binding title the title to submit
 * @binding alt the alt tag (defaults to the name of the network)
 */
public class AjaxSocialNetworkLink extends ERXComponent {
	public AjaxSocialNetworkLink(WOContext context) {
		super(context);
	}

	public boolean synchronizesVariablesWithBindings() {
		return false;
	}

	public String submissionUrl() {
		return socialNetwork().submissionUrl(stringValueForBinding("url"), stringValueForBinding("title"));
	}

	public AjaxSocialNetwork socialNetwork() {
		return AjaxSocialNetwork.socialNetworkNamed(stringValueForBinding("name"));
	}

	public String alt() {
		return stringValueForBinding("alt", socialNetwork().name());
	}
}