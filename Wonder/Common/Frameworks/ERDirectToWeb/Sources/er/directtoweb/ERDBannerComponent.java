package er.directtoweb;

import com.webobjects.appserver.*;

import er.extensions.*;

/**
 * Class for DirectToWeb Component ERDBannerComponent.
 *
 * @binding sample sample binding explanation
 * @d2wKey sample sample d2w key
 *
 * @created ak on Tue Sep 02 2003
 * @project ERDirectToWeb
 */

public class ERDBannerComponent extends ERDCustomComponent {

    /** logging support */
    private static final ERXLogger log = ERXLogger.getLogger(ERDBannerComponent.class,"components");
	
    /**
     * Public constructor
     * @param context the context
     */
    public ERDBannerComponent(WOContext context) {
        super(context);
    }

    /** component does not synchronize it's variables */
    public boolean synchronizesVariablesWithBindings() { return false; }

    /** component is stateless */
    public boolean isStateless() { return true; }

    public boolean showBanner() {
        return booleanValueForBinding("showBanner");
    }
}
