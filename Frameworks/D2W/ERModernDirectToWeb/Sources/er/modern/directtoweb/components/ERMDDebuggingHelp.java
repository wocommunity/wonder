package er.modern.directtoweb.components;

import com.webobjects.appserver.WOContext;

import er.directtoweb.components.ERDDebuggingHelp;
import com.webobjects.appserver.WOActionResults;

public class ERMDDebuggingHelp extends ERDDebuggingHelp {

    private static final long serialVersionUID = 1L;

    public Boolean showDetails = false;

    public ERMDDebuggingHelp(WOContext context) {
        super(context);
    }

    public Object debugValueForKey() {
        if (key != null && !"".equals(key)) {
            try {
                return d2wContext().valueForKeyPath(key);
            } catch (Exception e) {
            }
        }
        return null;
    }

    public String debugHelpUC() {
        return "debugHelpUC" + d2wContext().valueForKeyPath("pageConfiguration");
    }

    public String d2wKeyResultUC() {
        return "d2wKeyResultUC" + d2wContext().valueForKeyPath("pageConfiguration");
    }

    public WOActionResults toggleDetails() {
        showDetails = !showDetails;
        return null;
    }

}
