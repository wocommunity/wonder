package er.modern.look.pages;

import com.webobjects.appserver.WOContext;

import er.directtoweb.pages.templates.ERD2WMessagePageTemplate;

/**
 * Used to present a message to the user with only one option, usually "OK".<br />
 * 
 * @d2wKey okButtonLabel
 * @d2wKey cancelButtonLabel
 * @d2wKey pageWrapperName
 * @d2wKey explanationComponentName
 * @d2wKey explanationConfigurationName
 */
public class ERMODMessagePage extends ERD2WMessagePageTemplate {
	
	public ERMODMessagePage(WOContext wocontext) {
		super(wocontext);
	}

}
