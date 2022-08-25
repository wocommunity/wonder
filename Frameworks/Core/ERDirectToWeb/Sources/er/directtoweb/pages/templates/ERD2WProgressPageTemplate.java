package er.directtoweb.pages.templates;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.webobjects.appserver.WOContext;

import er.directtoweb.pages.ERD2WProgressPage;

/**
 * Class for DirectToWeb Component ERD2WProgressPageTemplate.
 *
 * @binding sample sample binding explanation
 * @d2wKey sample sample d2w key
 * @d2wKey displayNameForPageConfiguration
 * @d2wKey pageWrapperName
 *
 * @author ak on Wed Feb 04 2004
 */
public class ERD2WProgressPageTemplate extends ERD2WProgressPage {
	/**
	 * Do I need to update serialVersionUID?
	 * See section 5.6 <cite>Type Changes Affecting Serialization</cite> on page 51 of the 
	 * <a href="http://java.sun.com/j2se/1.4/pdf/serial-spec.pdf">Java Object Serialization Spec</a>
	 */
	private static final long serialVersionUID = 1L;

    /** logging support */
    private static final Logger log = LoggerFactory.getLogger(ERD2WProgressPageTemplate.class);
	
    /**
     * Public constructor
     * @param context the context
     */
    public ERD2WProgressPageTemplate(WOContext context) {
        super(context);
    }
}
