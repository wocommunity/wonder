package er.bugtracker.delegates;

import com.webobjects.appserver.WOComponent;
import com.webobjects.directtoweb.D2WContext;
import com.webobjects.foundation.NSArray;

import er.bugtracker.Bug;
import er.bugtracker.Factory;
import er.bugtracker.State;

public class BugDelegate extends BranchDelegate {

    protected NSArray defaultBranchChoices(D2WContext context) {
        NSArray result = super.defaultBranchChoices(context);
        log.debug("in: " + result);
         Bug bug = (Bug)object(context);
        // AK: this is just an illustration
        result = choiceByRemovingKeys(new NSArray("edit"), result);
        if(!bug.state().equals(State.ANALYZE)) {
            result = choiceByRemovingKeys(new NSArray("delete"), result);
        }
        if(!bug.state().equals(State.CLOSED)) {
        	result = choiceByRemovingKeys(new NSArray(new Object[] {"reopen"}), result);
        }
        if(bug.state().equals(State.CLOSED)) {
        	result = choiceByRemovingKeys(new NSArray(new Object[] {"resolve"}), result);
        }
        if(!bug.state().equals(State.VERIFY)) {
            result = choiceByRemovingKeys(new NSArray("reject"), result);
        }
        log.debug("out: " + result + " -> " + bug.state().textDescription());
        return result;
    }

    public WOComponent resolve(WOComponent sender) {
        Bug bug = (Bug) object(sender);
        return (WOComponent)Factory.bugTracker().resolveBug(bug);
    }

    public WOComponent comment(WOComponent sender) {
        Bug bug = (Bug) object(sender);
        return (WOComponent)Factory.bugTracker().commentBug(bug);
    }

    public WOComponent reopen(WOComponent sender) {
        Bug bug = (Bug) object(sender);
        return (WOComponent)Factory.bugTracker().reopenBug(bug);
    }


    public WOComponent reject(WOComponent sender) {
        Bug bug = (Bug) object(sender);
        return (WOComponent)Factory.bugTracker().rejectBug(bug);
    }


    public WOComponent createTestItem(WOComponent sender) {
        Bug bug = (Bug) object(sender);
        return (WOComponent)Factory.bugTracker().createTestItemFromBug(bug);
    }
}
