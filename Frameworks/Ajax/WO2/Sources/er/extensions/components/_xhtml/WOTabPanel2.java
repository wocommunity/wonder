package er.extensions.components._xhtml;

import com.webobjects.appserver.*;
import er.extensions.components.ERXTabPanel;

/**
 * An XHTML based Tab Panel
 * 
 * @binding tabs: a list of objects representing the tabs
 * @binding tabNameKey: a string containing a key to apply to tabs to get the title of the tab
 * @binding selectedTab: contains the selected tab
 * @binding submitActionName: if this binding is non null, tabs will contain a submit button instead of a regular hyperlink and the action pointed to by the binding will be called
 */
public class WOTabPanel2 extends ERXTabPanel {
    public WOTabPanel2(WOContext context) {
        super(context);
    }
}