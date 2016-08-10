package er.directtoweb.pages.templates;

import com.webobjects.appserver.WOContext;

import er.directtoweb.pages.ERD2WQueryEntitiesPage;

/**
 * Class for DirectToWeb Component ERD2WQueryEntitiesPageTemplate.
 * 
 * @author ak on Mon Sep 01 2003
 * 
 * @d2wKey displayNameForEntity
 * @d2wKey textColor
 * @d2wKey backgroundColorForTable
 */
public class ERD2WQueryEntitiesPageTemplate extends ERD2WQueryEntitiesPage {
	/**
	 * Do I need to update serialVersionUID?
	 * See section 5.6 <cite>Type Changes Affecting Serialization</cite> on page 51 of the 
	 * <a href="http://java.sun.com/j2se/1.4/pdf/serial-spec.pdf">Java Object Serialization Spec</a>
	 */
	private static final long serialVersionUID = 1L;

    /**
     * Public constructor
     * @param context the context
     */
    public ERD2WQueryEntitiesPageTemplate(WOContext context) {
        super(context);
    }
}
