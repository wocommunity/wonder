package er.reporting;
import com.webobjects.appserver.WOContext;

/**
 * Class for Wonder Component WRVerticalRowsLayout.
 *
 * @binding sample sample binding explanation
 *
 * @author ak on Mon Mar 17 2003
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
    @Override
    public boolean synchronizesVariablesWithBindings() { return false; }

}
