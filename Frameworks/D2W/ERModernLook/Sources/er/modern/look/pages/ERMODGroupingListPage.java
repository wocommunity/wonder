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
  /**
   * Do I need to update serialVersionUID?
   * See section 5.6 <cite>Type Changes Affecting Serialization</cite> on page 51 of the 
   * <a href="http://java.sun.com/j2se/1.4/pdf/serial-spec.pdf">Java Object Serialization Spec</a>
   */
  private static final long serialVersionUID = 1L;

	public ERMODGroupingListPage(WOContext wocontext) {
		super(wocontext);
	}
	
}
