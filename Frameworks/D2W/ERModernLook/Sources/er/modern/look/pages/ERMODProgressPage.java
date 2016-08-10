package er.modern.look.pages;

import com.webobjects.appserver.WOContext;

import er.directtoweb.pages.templates.ERD2WProgressPageTemplate;

/**
 * Class for DirectToWeb Component ERMODProgressPage.
 *
 * @d2wKey displayNameForPageConfiguration
 * @d2wKey pageWrapperName
 */
public class ERMODProgressPage extends ERD2WProgressPageTemplate {
  /**
   * Do I need to update serialVersionUID?
   * See section 5.6 <cite>Type Changes Affecting Serialization</cite> on page 51 of the 
   * <a href="http://java.sun.com/j2se/1.4/pdf/serial-spec.pdf">Java Object Serialization Spec</a>
   */
  private static final long serialVersionUID = 1L;
	
	public ERMODProgressPage(WOContext wocontext) {
		super(wocontext);
	}
}
