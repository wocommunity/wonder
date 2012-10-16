package er.directtoweb.pages.templates;

import com.webobjects.appserver.WOContext;

import er.directtoweb.pages.ERD2WCalendarPage;

/**
 * Class for DirectToWeb Component ERD2WCalendarPageTemplate.
 * 
 * @author ak on Thu Sep 04 2003
 * 
 * @d2wKey bannerFileName
 * @d2wKey showBanner
 * @d2wKey emptyListComponentName
 * @d2wKey repetitionComponentName
 * @d2wKey headerComponentName
 * @d2wKey pageWrapperName
 */
public class ERD2WCalendarPageTemplate extends ERD2WCalendarPage {
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
    public ERD2WCalendarPageTemplate(WOContext context) {
        super(context);
    }
}
