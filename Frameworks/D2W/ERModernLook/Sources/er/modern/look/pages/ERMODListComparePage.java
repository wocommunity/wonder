package er.modern.look.pages;

import com.webobjects.appserver.WOContext;

import er.directtoweb.pages.templates.ERD2WListComparePageTemplate;

/**
 * Basically a list page flipped vertical.  Useful for comparing from a left to right fashion.
 * 
 * @d2wKey componentName
 * @d2wKey hasPrinterFriendlyVersion
 * @d2wKey bannerFileName
 * @d2wKey showBanner
 * @d2wKey emptyListComponentName
 * @d2wKey displayNameForEntity
 * @d2wKey textColor
 * @d2wKey pageWrapperName
 */
public class ERMODListComparePage extends ERD2WListComparePageTemplate {
  /**
   * Do I need to update serialVersionUID?
   * See section 5.6 <cite>Type Changes Affecting Serialization</cite> on page 51 of the 
   * <a href="http://java.sun.com/j2se/1.4/pdf/serial-spec.pdf">Java Object Serialization Spec</a>
   */
  private static final long serialVersionUID = 1L;

	public ERMODListComparePage(WOContext wocontext) {
		super(wocontext);
	}

}
