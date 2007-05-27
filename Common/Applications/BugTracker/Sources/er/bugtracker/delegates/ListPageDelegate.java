package er.bugtracker.delegates;

import java.util.Enumeration;

import com.webobjects.appserver.WOComponent;
import com.webobjects.directtoweb.D2WContext;
import com.webobjects.directtoweb.ERD2WUtilities;
import com.webobjects.directtoweb.InspectPageInterface;
import com.webobjects.eocontrol.EOEditingContext;
import com.webobjects.foundation.NSArray;

import er.bugtracker.Bug;
import er.bugtracker.Factory;
import er.bugtracker.Session;
import er.directtoweb.ERDBranchDelegate;
import er.directtoweb.ERDListPageInterface;
import er.extensions.ERXEC;

public class ListPageDelegate extends ERDBranchDelegate {

    protected Session session(WOComponent sender) {
        return (Session)sender.session();
    }

    protected NSArray defaultBranchChoices(D2WContext context) {
        NSArray result = super.defaultBranchChoices(context);
        return result;
    }
}
