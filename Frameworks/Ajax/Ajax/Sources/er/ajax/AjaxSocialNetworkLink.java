package er.ajax;

import com.webobjects.appserver.WOContext;

import er.extensions.components.ERXComponent;

/**
 * AjaxSocialNetworkLink creates a link to the submission URL for 
 * a social network around the social network's icon.
 * 
 * @author mschrag
 * @binding name the name of the social network (@see er.ajax.AjaxSocialNetwork.socialNetworkNamed)
 * @binding url the URL to submit
 * @binding title the title to submit
 * @binding alt the alt tag (defaults to the name of the network)
 * @binding target the target of the link
 */
public class AjaxSocialNetworkLink extends ERXComponent {
	/**
	 * Do I need to update serialVersionUID?
	 * See section 5.6 <cite>Type Changes Affecting Serialization</cite> on page 51 of the 
	 * <a href="http://java.sun.com/j2se/1.4/pdf/serial-spec.pdf">Java Object Serialization Spec</a>
	 */
	private static final long serialVersionUID = 1L;

	public AjaxSocialNetworkLink(WOContext context) {
		super(context);
	}

	@Override
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
