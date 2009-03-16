package er.directtoweb.pages.templates;

import org.apache.log4j.Logger;

import com.webobjects.appserver.WOContext;

import er.directtoweb.pages.ERD2WProgressPage;

/**
 * Class for DirectToWeb Component ERD2WProgressPageTemplate.
 *
 * @binding sample sample binding explanation
 * @d2wKey sample sample d2w key
 *
 * @created ak on Wed Feb 04 2004
 * @project ERDirectToWeb
 */

public class ERD2WProgressPageTemplate extends ERD2WProgressPage {

    /** logging support */
    private static final Logger log = Logger.getLogger(ERD2WProgressPageTemplate.class);
	
    /**
     * Public constructor
     * @param context the context
     */
    public ERD2WProgressPageTemplate(WOContext context) {
        super(context);
    }
}
