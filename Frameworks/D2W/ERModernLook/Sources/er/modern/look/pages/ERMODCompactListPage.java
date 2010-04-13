package er.modern.look.pages;

import com.webobjects.appserver.WOContext;

import er.directtoweb.pages.templates.ERD2WCompactListPageTemplate;

/**
 * Compact list page.  Doesn't have any of the navigation at the top.<br />
 * 
 * @d2wKey emptyListComponentName
 * @d2wKey repetitionComponentName
 * @d2wKey displayNameForEntity
 * @d2wKey batchNavigationBarComponentName
 * @d2wKey showBatchNavigation
 * @d2wKey displayNameForEntity
 */
public class ERMODCompactListPage extends ERD2WCompactListPageTemplate {
	
	public ERMODCompactListPage(WOContext wocontext) {
		super(wocontext);
	}
	
}
