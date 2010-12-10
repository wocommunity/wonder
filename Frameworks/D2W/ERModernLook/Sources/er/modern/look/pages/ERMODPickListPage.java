package er.modern.look.pages;

import com.webobjects.appserver.WOContext;

import er.directtoweb.pages.templates.ERD2WPickListPageTemplate;

/**
 * A basic list but adding the ability to choose an arbitrary number of eos.  
 * Useful as a user-friendly replacement of the edit relationship page.<br />
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
	
	public ERMODPickListPage(WOContext wocontext) {
		super(wocontext);
	}

}
