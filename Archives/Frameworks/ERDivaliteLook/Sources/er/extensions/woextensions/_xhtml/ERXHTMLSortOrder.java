package er.extensions.woextensions._xhtml;

import com.webobjects.appserver.WOContext;

import er.extensions.components.ERXSortOrder;

/**
 * 
 * @binding d2wContext
 * @binding displayGroup
 * @binding displayKey
 * @binding key
 */
public abstract class ERXHTMLSortOrder extends ERXSortOrder {
    public ERXHTMLSortOrder(WOContext context) {
        super(context);
    }
 
    // accessors
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