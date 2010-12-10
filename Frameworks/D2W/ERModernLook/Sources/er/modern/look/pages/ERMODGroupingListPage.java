package er.modern.look.pages;

import com.webobjects.appserver.WOContext;

import er.directtoweb.pages.templates.ERD2WGroupingListPageTemplate;

/**
 * Displays a groups of objects grouped by a key.<br />
 * Actually, this component uses none of the variables and methods defined here,
 * as all the work is done by the ERDGroupingListPageRepetition that should be set
 * in the rules when a "ListGroupSomeEntity" page configuration is called up.
 * 
 * @d2wKey returnButtonLabel
 * @d2wKey printerButtonComponentName
 * @d2wKey emptyListComponentName
 * @d2wKey headerComponentName
 * @d2wKey entity
 * @d2wKey allowsFiltering
 * @d2wKey repetitionComponentName
 * @d2wKey displayNameForEntity
 * @d2wKey backgroundColorForTable
 * @d2wKey batchNavigationBarComponentName
 * @d2wKey pageWrapperName
 */
public class ERMODGroupingListPage extends ERD2WGroupingListPageTemplate {
	
	public ERMODGroupingListPage(WOContext wocontext) {
		super(wocontext);
	}
	
}
