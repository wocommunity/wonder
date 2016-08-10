package er.extensions.components;

import com.webobjects.appserver.WOActionResults;
import com.webobjects.appserver.WOContext;

/**
 * Class for Wonder Component ERXMonthView.
 *
 * @binding cellWidth width if the date cells. Default is 20.
 *
 * @author ak on Thu Sep 04 2003
 */
public class ERXMonthView extends ERXStatelessComponent {
	/**
	 * Do I need to update serialVersionUID?
	 * See section 5.6 <cite>Type Changes Affecting Serialization</cite> on page 51 of the 
	 * <a href="http://java.sun.com/j2se/1.4/pdf/serial-spec.pdf">Java Object Serialization Spec</a>
	 */
	private static final long serialVersionUID = 1L;

    protected int 	_cellWidth = -1;
    protected String 	_cellAlign;
    /**
     * Public constructor
     * @param context the context
     */
    public ERXMonthView(WOContext context) {
        super(context);
    }
    
    @Override
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
