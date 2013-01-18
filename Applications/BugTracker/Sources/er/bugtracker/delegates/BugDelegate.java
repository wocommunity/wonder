package er.bugtracker.delegates;

import com.webobjects.appserver.WOComponent;
import com.webobjects.directtoweb.D2WContext;
import com.webobjects.foundation.NSArray;

import er.bugtracker.Bug;
import er.bugtracker.Factory;
import er.bugtracker.State;

public class BugDelegate extends BranchDelegate {

    @Override
    protected NSArray defaultBranchChoices(D2WContext context) {
        NSArray result = super.defaultBranchChoices(context);
        log.debug("in: " + result);
        Bug bug = (Bug)object(context);
        // AK: this is just an illustration
        if(bug != null) {
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
        } else {
            result = choiceByLeavingKeys(new NSArray(new Object[] {"create"}), result);
        }
        return result;
    }

    public WOComponent resolve(WOComponent sender) {
        Bug bug = (Bug) object(sender);
        return Factory.bugTracker().resolveBug(bug);
    }

    public WOComponent comment(WOComponent sender) {
        Bug bug = (Bug) object(sender);
        return Factory.bugTracker().commentBug(bug);
    }

    public WOComponent reopen(WOComponent sender) {
        Bug bug = (Bug) object(sender);
        return Factory.bugTracker().reopenBug(bug);
    }


    public WOComponent reject(WOComponent sender) {
        Bug bug = (Bug) object(sender);
        return Factory.bugTracker().rejectBug(bug);
    }

    public WOComponent create(WOComponent sender) {
        return Factory.bugTracker().createBug();
    }

    public WOComponent createTestItem(WOComponent sender) {
        Bug bug = (Bug) object(sender);
        return Factory.bugTracker().createTestItemFromBug(bug);
    }
}
