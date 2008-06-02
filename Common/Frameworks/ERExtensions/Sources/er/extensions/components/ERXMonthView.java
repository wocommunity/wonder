package er.extensions.components;
import org.apache.log4j.Logger;

import com.webobjects.appserver.WOActionResults;
import com.webobjects.appserver.WOContext;

/**
* Class for Wonder Component ERXMonthView.
 *
 * @binding cellWidth width if the date cells. Default is 20.
 *
 * @created ak on Thu Sep 04 2003
 * @project ERExtensions
 */

public class ERXMonthView extends ERXStatelessComponent {

    /** logging support */
    private static final Logger log = Logger.getLogger(ERXMonthView.class);
    protected int 	_cellWidth = -1;
    protected String 	_cellAlign;
    /**
     * Public constructor
     * @param context the context
     */
    public ERXMonthView(WOContext context) {
        super(context);
    }
    public void reset() {
        super.reset();
        _cellWidth = -1;
        _cellAlign = null;
    }
    public int cellWidth() {
        if(_cellWidth == -1) {
            _cellWidth = intValueForBinding("cellWidth", 20);
        }
        return _cellWidth;
    }
    public String cellAlign() {
        if(_cellAlign == null) {
            _cellAlign = stringValueForBinding("cellAlign", "center");
        }
        return _cellAlign;
    }
    public WOActionResults selectDateAction() {
        String action = stringValueForBinding("action");
        WOActionResults nextPage = context().page();
        if(action == null) {
            ERXDateGrouper grouper = (ERXDateGrouper)valueForBinding("grouper");
            grouper.setSelectedDate(grouper.currentDate());
        } else {
            nextPage = performParentAction(action);
        }
        return nextPage;
    }
}
