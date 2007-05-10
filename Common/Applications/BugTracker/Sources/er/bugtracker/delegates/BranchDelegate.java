package er.bugtracker.delegates;

import com.webobjects.appserver.WOComponent;
import com.webobjects.directtoweb.D2WContext;
import com.webobjects.directtoweb.InspectPageInterface;
import com.webobjects.eocontrol.EOEnterpriseObject;

import er.bugtracker.Factory;
import er.bugtracker.Session;
import er.directtoweb.ERD2WMessagePage;
import er.directtoweb.ERDBranchDelegate;
import er.directtoweb.ERDDeletionDelegate;

public class BranchDelegate extends ERDBranchDelegate {

    protected Session session(WOComponent sender) {
        return (Session)sender.session();
    }

    public WOComponent view(WOComponent sender) {
        EOEnterpriseObject eo = object(sender);
        D2WContext context = d2wContext(sender);
        String pageName = (String)d2wContext(sender).valueForKey("inspectConfigurationName");
        InspectPageInterface epi = Factory.bugTracker().inspectPageNamed(pageName, eo);
        return (WOComponent) epi;
    }

    public WOComponent delete(WOComponent sender) {
        EOEnterpriseObject eo = object(sender);
        D2WContext context = d2wContext(sender);
        String pageName = (String)d2wContext(sender).valueForKey("confirmDeleteConfigurationName");
        ERD2WMessagePage epi = (ERD2WMessagePage) Factory.bugTracker().pageForConfigurationNamed(pageName, session(sender));
        epi.setObject(eo);
        epi.setCancelPage(sender.context().page());
        epi.setConfirmDelegate(new ERDDeletionDelegate(eo, sender.context().page()));
        return (WOComponent) epi;
    }

    public WOComponent edit(WOComponent sender) {
        EOEnterpriseObject eo = object(sender);
        D2WContext context = d2wContext(sender);
        String pageName = (String)d2wContext(sender).valueForKey("editConfigurationName");
        InspectPageInterface epi = Factory.bugTracker().editPageNamed(pageName, eo);
        return (WOComponent) epi;
    }
}
