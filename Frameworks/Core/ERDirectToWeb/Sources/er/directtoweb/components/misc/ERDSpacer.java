package er.directtoweb.components.misc;

import com.webobjects.appserver.WOContext;

import er.directtoweb.components.ERDCustomComponent;

/**
 * A silly simple component to show a horizontal rule
 */

public class ERDSpacer extends ERDCustomComponent {

    public ERDSpacer(WOContext context) { super(context); }

    public final boolean isStateless() { return true; }
    public final boolean synchronizesVariablesWithBindings() { return false; }
}
