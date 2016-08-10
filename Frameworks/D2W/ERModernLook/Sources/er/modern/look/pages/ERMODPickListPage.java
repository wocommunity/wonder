package er.modern.look.pages;

import com.webobjects.appserver.WOContext;

import er.directtoweb.pages.templates.ERD2WPickListPageTemplate;

/**
 * A basic list but adding the ability to choose an arbitrary number of eos.  
 * Useful as a user-friendly replacement of the edit relationship page.
 * 
 * @d2wKey hasPrinterFriendlyVersion
 * @d2wKey bannerFileName
 * @d2wKey showBanner
 * @d2wKey emptyListComponentName
 * @d2wKey headerComponentName
 * @d2wKey repetitionComponentName
 * @d2wKey displayNameForEntity
 * @d2wKey textColor
 * @d2wKey backgroundColorForTable
 * @d2wKey batchNavigationBarComponentName
 * @d2wKey pageWrapperName
 * @d2wKey showActions
 * @d2wKey pickButtonLabel
 */
public class ERMODPickListPage extends ERD2WPickListPageTemplate {
  /**
   * Do I need to update serialVersionUID?
   * See section 5.6 <cite>Type Changes Affecting Serialization</cite> on page 51 of the 
   * <a href="http://java.sun.com/j2se/1.4/pdf/serial-spec.pdf">Java Object Serialization Spec</a>
   */
  private static final long serialVersionUID = 1L;

	public ERMODPickListPage(WOContext wocontext) {
		super(wocontext);
	}

}
