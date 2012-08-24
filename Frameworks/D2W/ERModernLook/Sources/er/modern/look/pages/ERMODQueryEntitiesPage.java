package er.modern.look.pages;

import com.webobjects.appserver.WOContext;

import er.directtoweb.pages.templates.ERD2WQueryEntitiesPageTemplate;
import er.extensions.foundation.ERXStringUtilities;
import er.extensions.localization.ERXLocalizer;

/**
 * Modernized QueryEntities page
 * 
 * @author davidleber
 *
 */
public class ERMODQueryEntitiesPage extends ERD2WQueryEntitiesPageTemplate {
  /**
   * Do I need to update serialVersionUID?
   * See section 5.6 <cite>Type Changes Affecting Serialization</cite> on page 51 of the 
   * <a href="http://java.sun.com/j2se/1.4/pdf/serial-spec.pdf">Java Object Serialization Spec</a>
   */
  private static final long serialVersionUID = 1L;

	public int index;
	
//	private ERMODPageHelper _helper;
	
	public ERMODQueryEntitiesPage(WOContext wocontext) {
		super(wocontext);
	}
	
	// LINE
	/**
	 * Returns the css class for the current line in the query form
	 */
	public String lineDivClass() {
		String lineBase = (String)d2wContext().valueForKey("baseClassForLine");
		String eveness = "Even" + lineBase;
		if (index %2 == 0) {
			eveness = "Odd" + lineBase;
		}
		String type = (String)d2wContext().valueForKey("pageType");
		return lineBase + " " + eveness + " " + type + lineBase + " " + type + entityName() + lineBase;
	}
	
	// DISPLAY NAME FOR ENTITY;
	
	/**
	 * Returns the display name for the entity, or a localized version of it's name
	 * if there isn't one defined
	 */
	public String displayNameForEntity() {
		Object result = d2wContext().valueForKey("displayNameForEntity");
		if (result == null) {
			String temp = ERXStringUtilities.displayNameForKey(entityName());
			result = ERXLocalizer.currentLocalizer().localizedValueForKeyWithDefault(temp);
		}
		return (String)result;
	}
}
