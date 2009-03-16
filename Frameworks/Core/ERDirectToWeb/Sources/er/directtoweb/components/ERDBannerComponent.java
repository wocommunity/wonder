package er.directtoweb.components;

import org.apache.log4j.Logger;

import com.webobjects.appserver.WOContext;

/**
 * Shows an image header matching the page. If no image is bound, none is shown.
 *
 * @binding showBanner should we show the banner
 * @binding bannerFileName which image to show
 * @binding framework framework from where the image comes from
 *
 * @created ak on Tue Sep 02 2003
 * @project ERDirectToWeb
 */

public class ERDBannerComponent extends ERDCustomComponent {

    /** logging support */
    private static final Logger log = Logger.getLogger(ERDBannerComponent.class);
	
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
        return booleanValueForBinding("showBanner") && valueForBinding("bannerFileName") != null;
    }
}
