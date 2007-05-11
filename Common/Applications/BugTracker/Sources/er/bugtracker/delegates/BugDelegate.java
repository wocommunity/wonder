package er.bugtracker.delegates;

import com.webobjects.appserver.WOComponent;
import com.webobjects.directtoweb.D2W;
import com.webobjects.directtoweb.D2WContext;
import com.webobjects.directtoweb.EditPageInterface;
import com.webobjects.eocontrol.EOEditingContext;
import com.webobjects.foundation.NSArray;

import er.bugtracker.Bug;
import er.bugtracker.Session;
import er.bugtracker.State;
import er.extensions.ERXEC;

public class BugDelegate extends BranchDelegate {

    protected NSArray defaultBranchChoices(D2WContext context) {
        NSArray result = super.defaultBranchChoices(context);
        log.info("in: " + result);
         Bug bug = (Bug)object(context);
        // AK: this is just an illustration
        if(!bug.state().equals(State.ANALYZE)) {
            result = choiceByRemovingKeys(new NSArray("delete"), result);
        }
        if(!bug.state().equals(State.CLOSED)) {
        	result = choiceByRemovingKeys(new NSArray(new Object[] {"reopen", "view"}), result);
        }
        if(bug.state().equals(State.CLOSED)) {
        	result = choiceByRemovingKeys(new NSArray(new Object[] {"resolve", "edit"}), result);
        }
        if(!bug.state().equals(State.VERIFY)) {
            result = choiceByRemovingKeys(new NSArray("reject"), result);
        }
        log.info("out: " + result + " -> " + bug.state().textDescription());
        return result;
    }

    public WOComponent resolve(WOComponent sender) {
        Bug bug = (Bug) object(sender);
        Session session = session(sender);
        EOEditingContext peer = ERXEC.newEditingContext(bug.editingContext().parentObjectStore());
        EditPageInterface epi = null;
        peer.lock();
        try {
            bug = (Bug) bug.localInstanceIn(peer);
            bug.close();
            epi=(EditPageInterface)D2W.factory().pageForConfigurationNamed("EditBugToClose",session);
            epi.setObject(bug);
            epi.setNextPage(sender.context().page());
        } finally {
            peer.unlock();
        }

        return (WOComponent)epi;
    }

    public WOComponent reopen(WOComponent sender) {
        Bug bug = (Bug) object(sender);
        Session session = session(sender);
        EOEditingContext peer = ERXEC.newEditingContext(bug.editingContext().parentObjectStore());
        EditPageInterface epi = null;
        peer.lock();
        try {
            bug = (Bug) bug.localInstanceIn(peer);
            bug.reopen();
            epi=(EditPageInterface)D2W.factory().pageForConfigurationNamed("EditBugToReopen",session);
            epi.setObject(bug);
            epi.setNextPage(sender.context().page());
        } finally {
            peer.unlock();
        }

        return (WOComponent)epi;
    }


    public WOComponent reject(WOComponent sender) {
        Bug bug = (Bug) object(sender);
        Session session = session(sender);
        EOEditingContext peer = ERXEC.newEditingContext(bug.editingContext().parentObjectStore());
        EditPageInterface epi = null;
        peer.lock();
        try {
            bug = (Bug) bug.localInstanceIn(peer);
            bug.rejectVerification();
            epi=(EditPageInterface)D2W.factory().pageForConfigurationNamed("EditBugToRejectVerification",session);
            epi.setObject(bug);
            epi.setNextPage(sender.context().page());
        } finally {
            peer.unlock();
        }

        return (WOComponent)epi;
    }

}
