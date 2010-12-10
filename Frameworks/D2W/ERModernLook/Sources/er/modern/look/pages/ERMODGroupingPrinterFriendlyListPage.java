package er.modern.look.pages;

import com.webobjects.appserver.WOContext;
import er.directtoweb.printerfriendly.ERD2WGroupingPrinterFriendlyListPageTemplate;

/**
 * Printer friendly version.<br />
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
	
	public ERMODGroupingPrinterFriendlyListPage(WOContext wocontext) {
		super(wocontext);
	}

}
