package er.reporting;
import com.webobjects.appserver.*;

/**
 * Class for Wonder Component WRVerticalRowsLayout.
 *
 * @binding sample sample binding explanation
 *
 * @created ak on Mon Mar 17 2003
 * @project WRReporting
 */

public class WRVerticalRowsLayout extends WRReport {
	
    /**
     * Public constructor
     * @param context the context
     */
    public WRVerticalRowsLayout(WOContext context) {
        super(context);
    }

    /** component does not synchronize it's variables */
    public boolean synchronizesVariablesWithBindings() { return false; }

}
