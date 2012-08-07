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
 * @author ak on Tue Sep 02 2003
 */
public class ERDBannerComponent extends ERDCustomComponent {
	/**
	 * Do I need to update serialVersionUID?
	 * See section 5.6 <cite>Type Changes Affecting Serialization</cite> on page 51 of the 
	 * <a href="http://java.sun.com/j2se/1.4/pdf/serial-spec.pdf">Java Object Serialization Spec</a>
	 */
	private static final long serialVersionUID = 1L;

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
    @Override
    public boolean synchronizesVariablesWithBindings() { return false; }

    /** component is stateless */
    @Override
    public boolean isStateless() { return true; }

    public boolean showBanner() {
        return booleanValueForBinding("showBanner") && valueForBinding("bannerFileName") != null;
    }
}
