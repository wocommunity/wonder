package er.extensions.woextensions._xhtml;

import org.apache.log4j.Logger;

import com.webobjects.appserver.*;
import com.webobjects.eocontrol.*;
import com.webobjects.foundation.*;

import er.extensions.localization.ERXLocalizer;
import er.extensions.woextensions.WOSortOrder;

/**
 * 
 * @binding d2wContext
 * @binding displayGroup
 * @binding displayKey
 * @binding key
 */
public abstract class ERXSortOrder2 extends WOSortOrder {
    public ERXSortOrder2(WOContext context) {
        super(context);
    }
    
    
    /** logging support */
    public final static Logger log = Logger.getLogger(ERXSortOrder2.class);

    //////////////////////////////////////////////// Notification Hooks //////////////////////////////////////////
    public final static String SortOrderingChanged = "SortOrderingChanged";

    //////////////////////////////////////////////// States //////////////////////////////////////////////////////
    public final static int Reset = -1;
    public final static int Unsorted = 0;
    public final static int SortedAscending = 1;
    public final static int SortedDescending = 2;

    public boolean synchronizesVariablesWithBindings() { return false; }
    public void reset() {
        super.reset();
        _currentState = Reset;
    }

    protected int _currentState = Reset;
    public int currentState() {
        if (_currentState == Reset) {
            _currentState = Unsorted;
            if (_isCurrentKeyPrimary()) {
                NSSelector aCurrentState = _primaryKeySortOrderingSelector();
                if (aCurrentState.equals(EOSortOrdering.CompareAscending) 
                		|| aCurrentState.equals(EOSortOrdering.CompareCaseInsensitiveAscending)) {
                    _currentState = SortedAscending;
                } else if (aCurrentState.equals(EOSortOrdering.CompareDescending) 
                		|| aCurrentState.equals(EOSortOrdering.CompareCaseInsensitiveDescending)) {
                    _currentState = SortedDescending;
                }
            }
        }
        return _currentState;
    }
    
    public String displayString() {
        String displayString = null;
        switch(currentState()) {
            case Unsorted:
            	displayString = "-"; break;
            case SortedAscending:
            	displayString = "&darr;"; break;
            case SortedDescending:
            	displayString = "&uarr;"; break;
        }
        return displayString;
    }
    
    // FIXME: Should post a notification even if d2wContext isn't bound.
    @SuppressWarnings("unchecked")
	public WOComponent toggleClicked() {
        super.toggleClicked();
        if (log.isDebugEnabled()) log.debug("toggleClicked "+valueForBinding("d2wContext"));
        if (valueForBinding("d2wContext") != null) {
            NSNotificationCenter.defaultCenter().postNotification(SortOrderingChanged,
                                                                  displayGroup().sortOrderings(),
                                                                  new NSDictionary(valueForBinding("d2wContext"), "d2wContext"));
        }
        return null;
    }
    
    

    public String helpString() {
       return ERXLocalizer.currentLocalizer().localizedTemplateStringForKeyWithObject("ERXSortOrder.sortBy", this);
    }

    // These come right out of WOSortOrder, but have protected access instead of private.
    @SuppressWarnings("unchecked")
	protected EOSortOrdering _primarySortOrdering() {
        NSArray nsarray = displayGroup().sortOrderings();
        if (nsarray != null && nsarray.count() > 0) {
            EOSortOrdering eosortordering = (EOSortOrdering)nsarray.objectAtIndex(0);
            return eosortordering;
        } else {
            return null;
        }
    }

    protected NSSelector _primaryKeySortOrderingSelector() {
        EOSortOrdering eosortordering = _primarySortOrdering();
        NSSelector nsselector = null;
        if(eosortordering != null)
            nsselector = eosortordering.selector();
        return nsselector;
    }

    protected boolean _isCurrentKeyPrimary() {
        EOSortOrdering eosortordering = _primarySortOrdering();
        return eosortordering != null &&  eosortordering.key() != null &&  eosortordering.key().equals(key());
    }
    
    public String styleClass() {
        String styleClass = null;
        switch(currentState()) {
            case Unsorted:
            	styleClass = "ERXSortOrder2_None"; break;
            case SortedAscending:
            	styleClass = "ERXSortOrder2_Down"; break;
            case SortedDescending:
            	styleClass = "ERXSortOrder2_Up"; break;
        }
        return styleClass;
    }
}