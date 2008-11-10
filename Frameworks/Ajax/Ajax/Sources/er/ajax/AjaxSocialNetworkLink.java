package er.ajax;

import com.webobjects.appserver.WOContext;

import er.extensions.components.ERXComponent;

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