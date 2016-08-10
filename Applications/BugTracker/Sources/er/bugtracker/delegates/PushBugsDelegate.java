package er.bugtracker.delegates;

import java.util.Enumeration;

import com.webobjects.appserver.WOComponent;
import com.webobjects.directtoweb.D2WContext;
import com.webobjects.directtoweb.ERD2WUtilities;
import com.webobjects.eocontrol.EOEditingContext;
import com.webobjects.foundation.NSArray;

import er.bugtracker.Bug;
import er.bugtracker.Session;
import er.directtoweb.interfaces.ERDListPageInterface;
import er.extensions.eof.ERXEC;

public class PushBugsDelegate extends BranchDelegate {

    @Override
    protected Session session(WOComponent sender) {
        return (Session)sender.session();
    }

    @Override
    protected NSArray defaultBranchChoices(D2WContext context) {
        NSArray result = super.defaultBranchChoices(context);
        return result;
    }
    
    public WOComponent pushBugsToVerification(WOComponent sender) {
        ERDListPageInterface lpi = (ERDListPageInterface) ERD2WUtilities.parentListPage(sender);
        NSArray bugsInBuild = lpi.displayGroup().allObjects();
        EOEditingContext ec = ERXEC.newEditingContext();
        ec.lock();
        try {
            for (Enumeration e = bugsInBuild.objectEnumerator(); e.hasMoreElements();) {
                Bug currentBug = (Bug) e.nextElement();
                currentBug.moveToVerification();
            }
            ec.saveChanges();
        } finally {
            ec.unlock();
        }
        return sender.context().page();
    }
}