package er.directtoweb;

import com.webobjects.appserver.*;

import er.extensions.*;

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
    private static final ERXLogger log = ERXLogger.getLogger(ERD2WProgressPageTemplate.class,"components");
	
    /**
     * Public constructor
     * @param context the context
     */
    public ERD2WProgressPageTemplate(WOContext context) {
        super(context);
    }
}
