package er.modern.look.pages;

import com.webobjects.appserver.WOContext;

import er.directtoweb.printerfriendly.ERD2WGroupingPrinterFriendlyListPageTemplate;

/**
 * Printer friendly version.
 * 
 * @d2wKey justification
 * @d2wKey componentName
 * @d2wKey propertyKey
 * @d2wKey displayPropertyKeys
 * @d2wKey displayNameForEntity
 * @d2wKey keyWhenGrouping
 * @d2wKey groupingOrderKey
 * @d2wKey headerComponentName
 * @d2wKey totallingKeys
 * @d2wKey displayNameForProperty
 * @d2wKey formatter
 * @d2wKey pageWrapperName
 */
public class ERMODGroupingPrinterFriendlyListPage extends ERD2WGroupingPrinterFriendlyListPageTemplate {
  /**
   * Do I need to update serialVersionUID?
   * See section 5.6 <cite>Type Changes Affecting Serialization</cite> on page 51 of the 
   * <a href="http://java.sun.com/j2se/1.4/pdf/serial-spec.pdf">Java Object Serialization Spec</a>
   */
  private static final long serialVersionUID = 1L;

	public ERMODGroupingPrinterFriendlyListPage(WOContext wocontext) {
		super(wocontext);
	}

}
