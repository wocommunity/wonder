package er.bugtracker.delegates;

import com.webobjects.appserver.WOComponent;
import com.webobjects.directtoweb.D2W;
import com.webobjects.directtoweb.D2WContext;
import com.webobjects.directtoweb.EditPageInterface;
import com.webobjects.eocontrol.EOEditingContext;
import com.webobjects.foundation.NSArray;

import er.bugtracker.Component;
import er.bugtracker.People;
import er.bugtracker.Requirement;
import er.bugtracker.Session;
import er.bugtracker.TestItem;
import er.extensions.ERXEC;
import er.extensions.ERXLocalizer;

public class RequirementDelegate extends BugDelegate {

    protected NSArray defaultBranchChoices(D2WContext context) {
        return super.defaultBranchChoices(context);
    }

    public WOComponent createTestItem(WOComponent sender) {
        Requirement requirement = (Requirement) object(sender);
        Session session = session(sender);
        ERXLocalizer localizer = ERXLocalizer.currentLocalizer();
        EOEditingContext peer = ERXEC.newEditingContext(requirement.editingContext().parentObjectStore());
        EditPageInterface epi = null;
        peer.lock();
        try {
            People user = People.clazz.currentUser(peer);
            requirement = (Requirement) requirement.localInstanceIn(peer);
            Component component = (Component) requirement.component();
            String description = localizer.localizedTemplateStringForKeyWithObject("CreateTestItemFromReq.templateString", requirement);
            TestItem testItem = user.createTestItemFromRequestWithDescription(requirement, component, description);
            epi=(EditPageInterface)D2W.factory().pageForConfigurationNamed("CreateNewTestItemFromReq",session);
            epi.setObject(testItem);
            epi.setNextPage(sender.context().page());
        } finally {
            peer.unlock();
        }

        return (WOComponent)epi;
    }
}
