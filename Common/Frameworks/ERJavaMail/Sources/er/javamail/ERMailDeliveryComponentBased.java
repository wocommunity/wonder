/*
 $Id$

 ERMailDeliveryComponentBased.java - Camille Troillard - tuscland@mac.com
 */

package er.javamail;

import com.webobjects.appserver.WOComponent;
import com.webobjects.appserver.WOContext;
import com.webobjects.appserver.WOMessage;
import com.webobjects.foundation.NSDictionary;

/**
 * This abstract class is the basis for all WOComponetn based deliverers.
 * 
 * @author Camille Troillard <tuscland@mac.com>
 */
public abstract class ERMailDeliveryComponentBased extends ERMailDelivery {
	/** WOComponent used to render the HTML message. */
	protected WOComponent _component;
	protected WOComponent _alternativeComponent;

	/**
	 * Variable that stores the state of the session. In the case the component was instanciated with
	 * ERMailUtils.instanciatePage, the session may be new and hence, would lack its dictionary properties.
	 */
	protected NSDictionary _sessionDictionary = NSDictionary.EmptyDictionary;

	/**
	 * Sets the WOComponent used to render the HTML message.
	 * 
	 * @deprecated use setComponent instead.
	 */
	public void setWOComponentContent(WOComponent component) {
		this.setComponent(component);
	}

	/** Sets the WOComponent used to render the HTML message. */
	public void setComponent(WOComponent component) {
		_component = component;
	}

	public WOComponent component() {
		return _component;
	}
	
	/**
	 * Sets the alternative view component for rendering a different mime type (text/plain, etc)
	 */
	public void setAlternativeComponent(WOComponent alternativeComponent) {
		_alternativeComponent = alternativeComponent;
	}
	
	public WOComponent alternativeComponent() {
		return _alternativeComponent;
	}

	/** Accessor for the sessionDictionary property */
	public NSDictionary sessionDictionary() {
		return _sessionDictionary;
	}

	/** Accessor for the sessionDictionary property */
	public void setSessionDictionary(NSDictionary dict) {
		_sessionDictionary = dict;
	}

	/** Generates the output string used in messages */
	protected String componentContentString() {
		return _componentContentString(component());
	}

	/** Generates the output string used in messages */
	protected String alternativeComponentContentString() {
		return _componentContentString(alternativeComponent());
	}

	/** Generates the output string used in messages */
	protected String _componentContentString(WOComponent component) {
		String contentString = null;
		if (component != null) {
			WOContext context = component.context();

			// CHECKME: It's probably not a good idea to do this here
			// since the context could also have been generating relative URLs
			// unless the context is created from scratch
			context._generateCompleteURLs();
			WOMessage response = component.generateResponse();
			contentString = response.contentString();
		}
		return contentString;
	}

}
