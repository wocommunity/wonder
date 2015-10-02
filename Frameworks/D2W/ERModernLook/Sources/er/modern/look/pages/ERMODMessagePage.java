package er.modern.look.pages;

import com.webobjects.appserver.WOContext;

import er.directtoweb.pages.templates.ERD2WMessagePageTemplate;

/**
 * Used to present a message to the user with only one option, usually "OK".
 * 
 * @d2wKey okButtonLabel
 * @d2wKey cancelButtonLabel
 * @d2wKey pageWrapperName
 * @d2wKey explanationComponentName
 * @d2wKey explanationConfigurationName
 */
public class ERMODMessagePage extends ERD2WMessagePageTemplate {
  /**
   * Do I need to update serialVersionUID?
   * See section 5.6 <cite>Type Changes Affecting Serialization</cite> on page 51 of the 
   * <a href="http://java.sun.com/j2se/1.4/pdf/serial-spec.pdf">Java Object Serialization Spec</a>
   */
  private static final long serialVersionUID = 1L;

	public ERMODMessagePage(WOContext wocontext) {
		super(wocontext);
	}

}
