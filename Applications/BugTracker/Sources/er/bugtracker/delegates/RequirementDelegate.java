package er.bugtracker.delegates;

import com.webobjects.appserver.WOComponent;
import com.webobjects.directtoweb.D2WContext;
import com.webobjects.foundation.NSArray;

import er.bugtracker.Factory;
import er.bugtracker.Requirement;

public class RequirementDelegate extends BugDelegate {

    @Override
    protected NSArray defaultBranchChoices(D2WContext context) {
        return super.defaultBranchChoices(context);
    }

    @Override
    public WOComponent createTestItem(WOComponent sender) {
        Requirement requirement = (Requirement) object(sender);
        return Factory.bugTracker().createTestItemFromBug(requirement);
    }
}
