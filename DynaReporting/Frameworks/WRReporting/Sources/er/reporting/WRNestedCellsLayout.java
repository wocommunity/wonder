package er.reporting;
import com.webobjects.appserver.*;

/**
 * Class for Wonder Component WRNestedCellsLayout.
 *
 * @binding sample sample binding explanation
 *
 * @created ak on Mon Mar 17 2003
 * @project WRReporting
 */

public class WRNestedCellsLayout extends WRReport {
	
    /**
     * Public constructor
     * @param context the context
     */
    public WRNestedCellsLayout(WOContext context) {
        super(context);
    }

    /** component does not synchronize it's variables */
    public boolean synchronizesVariablesWithBindings() { return false; }

}
