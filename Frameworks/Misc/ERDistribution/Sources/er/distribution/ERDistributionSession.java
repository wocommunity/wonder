package er.distribution;

import com.webobjects.eoaccess.EOEntity;
import com.webobjects.eoaccess.EOModel;
import com.webobjects.eoaccess.EOModelGroup;
import com.webobjects.eocontrol.EOClassDescription;
import com.webobjects.eocontrol.EOEditingContext;
import com.webobjects.eocontrol.EOFetchSpecification;
import com.webobjects.eocontrol.EOGlobalID;
import com.webobjects.eodistribution.EODistributionContext;
import com.webobjects.foundation.NSArray;
import com.webobjects.foundation.NSData;
import com.webobjects.foundation.NSMutableArray;
import com.webobjects.foundation.NSNotification;
import com.webobjects.foundation.NSNotificationCenter;
import com.webobjects.foundation.NSSelector;

import er.extensions.appserver.ERXSession;

public abstract class ERDistributionSession extends ERXSession implements EODistributionContext.Delegate {

	private static final long serialVersionUID = 1L;
	private static final NSSelector _DistributionContextInstantiatedSelector = new NSSelector("_distributionContextInstantiated", new Class[] {NSNotification.class});

	public ERDistributionSession() {
        NSNotificationCenter.defaultCenter().addObserver(this, _DistributionContextInstantiatedSelector, EODistributionContext.DistributionContextInstantiatedNotification, null);
	}
	
    public void _distributionContextInstantiated(NSNotification notification) {
        // If this notification is received and the sender is for this session, get the editing context from the EODistributionContext and unregister from the notification.
        EODistributionContext distributionContext = (EODistributionContext)(notification.object());
        if (distributionContext.session() == this) {
            NSNotificationCenter.defaultCenter().removeObserver(this, EODistributionContext.DistributionContextInstantiatedNotification, null);
        }
    }

	public NSArray<EOClassDescription> clientSideRequestGetClassDescriptions() {
		NSMutableArray<EOClassDescription> result = new NSMutableArray<EOClassDescription>();
		
		NSArray<EOModel> models = EOModelGroup.defaultGroup().models();
		for (EOModel model : models) {
			if (!model.name().endsWith("Prototypes")) {
				for (EOEntity entity : model.entities()) {
					if (!entity.name().endsWith("Prototypes")) {
						result.add(entity.classDescriptionForInstances());
					}
				}
			}
		}
		
		return result;
	}

    public abstract boolean isUserAuthenticated();
    
    public abstract EOGlobalID clientSideRequestLogin(final String username, String password);
    
	public NSData distributionContextWillSendData(EODistributionContext distributionContext, NSData data) {
		return data;
	}

	public NSData distributionContextDidReceiveData(EODistributionContext distributionContext, NSData data) {
		return data;
	}

	public boolean distributionContextShouldFollowKeyPath(EODistributionContext distributionContext, String keyPath) {
		return "session".equals(keyPath) || isUserAuthenticated();
	}

	public boolean distributionContextShouldAllowInvocation(
			EODistributionContext distributionContext, Object receiver, NSSelector selector, Object[] arguments) {
		return isUserAuthenticated();
	}

	public boolean distributionContextShouldAllowAccessToClassDescription(
			EODistributionContext distributionContext, EOClassDescription classDescription) {
		return true;
	}

	public boolean distributionContextShouldFetchObjectsWithFetchSpecification(
			EODistributionContext distributionContext, EOFetchSpecification fetchSpec) {
		return isUserAuthenticated();
	}

	public boolean distributionContextShouldFetchObjectWithGlobalID(
			EODistributionContext distributionContext, EOGlobalID globalID, EOClassDescription classDescription) {
		return isUserAuthenticated();
	}

	public boolean distributionContextShouldSave(EODistributionContext distributionContext, EOEditingContext editingContext) {
		return isUserAuthenticated();
	}
	
}
