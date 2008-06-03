package er.bugtracker.delegates;

import com.webobjects.appserver.WOComponent;
import com.webobjects.directtoweb.D2WContext;
import com.webobjects.foundation.NSArray;

import er.bugtracker.Session;
import er.directtoweb.ERDBranchDelegate;

public class ListPageDelegate extends ERDBranchDelegate {

    protected Session session(WOComponent sender) {
        return (Session)sender.session();
    }

    protected NSArray defaultBranchChoices(D2WContext context) {
        NSArray result = super.defaultBranchChoices(context);
        return result;
    }
}
