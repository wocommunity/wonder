package er.modern.look.pages;

import com.webobjects.appserver.WOContext;

import er.directtoweb.pages.templates.ERD2WListComparePageTemplate;

/**
 * Basically a list page flipped vertical.  Useful for comparing from a left to right fashion.<br />
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

	public ERMODListComparePage(WOContext wocontext) {
		super(wocontext);
	}

}
