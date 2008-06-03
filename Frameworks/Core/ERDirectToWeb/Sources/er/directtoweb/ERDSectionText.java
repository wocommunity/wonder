package er.directtoweb;

import com.webobjects.appserver.WOContext;

/**
 * Used to display sections as text.<br />
 * 
 * @binding displayNameForSectionKey
 */

public class ERDSectionText extends ERDCustomComponent {
    public ERDSectionText(WOContext context) { super(context); }

    public boolean isStateless() { return true; }
    public boolean synchronizesVariablesWithBindings() { return false; }
}
