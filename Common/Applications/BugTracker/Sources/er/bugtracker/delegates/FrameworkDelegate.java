package er.bugtracker.delegates;

import com.webobjects.appserver.WOComponent;
import com.webobjects.directtoweb.D2WContext;
import com.webobjects.eocontrol.EOEditingContext;
import com.webobjects.foundation.NSArray;

import er.bugtracker.Framework;
import er.bugtracker.People;
import er.extensions.ERXEC;
import er.extensions.ERXEOControlUtilities;

public class FrameworkDelegate extends BranchDelegate {

    protected NSArray defaultBranchChoices(D2WContext context) {
        NSArray result = super.defaultBranchChoices(context);
        log.debug("in: " + result);
        Framework framework = (Framework)object(context);
        boolean ownerIsSelf = ERXEOControlUtilities.eoEquals(framework.owner(), People.clazz.currentUser(framework.editingContext()));
        if(framework == null || (framework.owner() != null && !ownerIsSelf)) {
            result = choiceByRemovingKeys(new NSArray("grabHat"), result);
            result = choiceByRemovingKeys(new NSArray("returnHat"), result);
        } else if(ownerIsSelf) {
            result = choiceByRemovingKeys(new NSArray("grabHat"), result);
        } else {
            result = choiceByRemovingKeys(new NSArray("returnHat"), result);
        }
        return result;
    }

    public WOComponent grabHat(WOComponent sender) {
        Framework framework = (Framework) object(sender);
        EOEditingContext peer = ERXEC.newEditingContext();
        peer.lock();
        try {
            framework = (Framework) framework.localInstanceIn(peer);
            framework.grabHat();
            peer.saveChanges();
        } finally {
            peer.unlock();
        }

        return sender.context().page();
    }


    public WOComponent returnHat(WOComponent sender) {
        Framework framework = (Framework) object(sender);
        EOEditingContext peer = ERXEC.newEditingContext();
        peer.lock();
        try {
            framework = (Framework) framework.localInstanceIn(peer);
            framework.releaseHat();
            peer.saveChanges();
        } finally {
            peer.unlock();
        }

        return sender.context().page();
    }
}
