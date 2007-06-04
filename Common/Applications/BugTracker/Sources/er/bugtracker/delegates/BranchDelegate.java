package er.bugtracker.delegates;

import java.util.Enumeration;

import com.webobjects.appserver.WOComponent;
import com.webobjects.directtoweb.D2WContext;
import com.webobjects.directtoweb.InspectPageInterface;
import com.webobjects.eocontrol.EOEnterpriseObject;
import com.webobjects.foundation.NSArray;
import com.webobjects.foundation.NSDictionary;
import com.webobjects.foundation.NSMutableDictionary;

import er.bugtracker.Factory;
import er.bugtracker.Session;
import er.directtoweb.ERD2WMessagePage;
import er.directtoweb.ERDBranchDelegate;
import er.directtoweb.ERDDeletionDelegate;
import er.extensions.ERXDictionaryUtilities;
import er.extensions.ERXLocalizer;
import er.extensions.ERXStringUtilities;

public class BranchDelegate extends ERDBranchDelegate {

    protected Session session(WOComponent sender) {
        return (Session)sender.session();
    }

    protected NSArray defaultBranchChoices(D2WContext context) {
    	
        NSArray result = super.defaultBranchChoices(context);

        if(true) {
            result = choiceByRemovingKeys(new NSArray("createNew"), result);
        }
        if(context.task().equals("inspect")) {
            result = choiceByRemovingKeys(new NSArray("view"), result);
        }
        if(context.task().equals("edit")) {
            result = choiceByRemovingKeys(new NSArray("edit"), result);
        }
        return result;
    }
    
    public WOComponent createNew(WOComponent sender) {
        D2WContext context = d2wContext(sender);
        String pageName = (String)context.valueForKey("createConfigurationName");
        InspectPageInterface epi = Factory.bugTracker().editPageForNewObjectWithConfigurationNamed(pageName, session(sender));
        return (WOComponent) epi;
    }

    public WOComponent view(WOComponent sender) {
        EOEnterpriseObject eo = object(sender);
        D2WContext context = d2wContext(sender);
        String pageName = (String)context.valueForKey("inspectConfigurationName");
        InspectPageInterface epi = Factory.bugTracker().inspectPageNamed(pageName, eo);
        return (WOComponent) epi;
    }

    public WOComponent delete(WOComponent sender) {
        EOEnterpriseObject eo = object(sender);
        D2WContext context = d2wContext(sender);
        String pageName = (String)context.valueForKey("confirmDeleteConfigurationName");
        ERD2WMessagePage epi = (ERD2WMessagePage) Factory.bugTracker().pageForConfigurationNamed(pageName, session(sender));
        epi.setObject(eo);
        String message = ERXLocalizer.currentLocalizer().localizedTemplateStringForKeyWithObject("ERDTrashcan.confirmDeletionMessage", epi.d2wContext());
        epi.setMessage(message);
        epi.setCancelPage(sender.context().page());
        epi.setConfirmDelegate(new ERDDeletionDelegate(eo, sender.context().page()));
        return (WOComponent) epi;
    }

    public WOComponent edit(WOComponent sender) {
        EOEnterpriseObject eo = object(sender);
        D2WContext context = d2wContext(sender);
        String pageName = (String)context.valueForKey("editConfigurationName");
        InspectPageInterface epi = Factory.bugTracker().editPageNamed(pageName, eo);
        return (WOComponent) epi;
    }
}
