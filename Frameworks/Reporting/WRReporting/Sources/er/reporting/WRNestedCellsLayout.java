package er.reporting;
import com.webobjects.appserver.WOContext;

/**
 * Class for Wonder Component WRNestedCellsLayout.
 *
 * @binding sample sample binding explanation
 *
 * @author ak on Mon Mar 17 2003
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
    @Override
    public boolean synchronizesVariablesWithBindings() { return false; }

}
