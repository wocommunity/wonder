package er.bugtracker.delegates;

import com.webobjects.appserver.WOComponent;

public class BugDelegate extends BranchDelegate {
    
    public WOComponent resolve(WOComponent sender) {
        return sender.context().page();
    }

}
