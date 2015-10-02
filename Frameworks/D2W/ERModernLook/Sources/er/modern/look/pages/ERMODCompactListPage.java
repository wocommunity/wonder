package er.modern.look.pages;

import com.webobjects.appserver.WOContext;

import er.directtoweb.pages.templates.ERD2WCompactListPageTemplate;

/**
 * Compact list page.  Doesn't have any of the navigation at the top.
 * 
 * @d2wKey emptyListComponentName
 * @d2wKey repetitionComponentName
 * @d2wKey displayNameForEntity
 * @d2wKey batchNavigationBarComponentName
 * @d2wKey showBatchNavigation
 * @d2wKey displayNameForEntity
 */
public class ERMODCompactListPage extends ERD2WCompactListPageTemplate {
  /**
   * Do I need to update serialVersionUID?
   * See section 5.6 <cite>Type Changes Affecting Serialization</cite> on page 51 of the 
   * <a href="http://java.sun.com/j2se/1.4/pdf/serial-spec.pdf">Java Object Serialization Spec</a>
   */
  private static final long serialVersionUID = 1L;

	public ERMODCompactListPage(WOContext wocontext) {
		super(wocontext);
	}
	
}
