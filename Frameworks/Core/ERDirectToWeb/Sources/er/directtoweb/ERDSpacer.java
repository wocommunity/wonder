package er.directtoweb;

import com.webobjects.appserver.WOContext;

/**
 * A silly simple component to show a horizontal rule
 */

public class ERDSpacer extends ERDCustomComponent {

    public ERDSpacer(WOContext context) { super(context); }

    public final boolean isStateless() { return true; }
    public final boolean synchronizesVariablesWithBindings() { return false; }
}
