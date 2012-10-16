package er.ajax.look.components;

import org.apache.log4j.Logger;

import com.webobjects.appserver.WOContext;
import com.webobjects.directtoweb.D2WContext;
import com.webobjects.eocontrol.EOKeyValueCodingAdditions;
import com.webobjects.foundation.NSArray;
import com.webobjects.foundation.NSMutableArray;
import com.webobjects.foundation.NSMutableDictionary;
import com.webobjects.foundation.NSNotification;
import com.webobjects.foundation.NSNotificationCenter;
import com.webobjects.foundation.NSSelector;

import er.ajax.look.interfaces.PropertyChangedDelegate;
import er.directtoweb.components.ERDCustomComponent;
import er.extensions.appserver.ERXWOContext;
import er.extensions.eof.ERXConstant;
import er.extensions.eof.ERXKey;
import er.extensions.foundation.ERXArrayUtilities;
import er.extensions.foundation.ERXStringUtilities;

public class AjaxNotificationCenter extends ERDCustomComponent {
	/**
	 * Do I need to update serialVersionUID?
	 * See section 5.6 <cite>Type Changes Affecting Serialization</cite> on page 51 of the 
	 * <a href="http://java.sun.com/j2se/1.4/pdf/serial-spec.pdf">Java Object Serialization Spec</a>
	 */
	private static final long serialVersionUID = 1L;

	public static final ERXKey<String> AJAX_NOTIFICATION_CENTER_ID = new ERXKey<String>("ajaxNotificationCenterID");
	public static final ERXKey<String> PROPERTY_OBSERVER_ID = new ERXKey<String>("propertyObserverID");
	public static final ERXKey<String> PROPERTY_KEY = new ERXKey<String>("propertyKey");
	public static final ERXKey<PropertyChangedDelegate> PROPERTY_CHANGED_DELEGATE = new ERXKey<PropertyChangedDelegate>("propertyChangedDelegate");
		
	public static final String PropertyChangedNotification = "PropertyChangedNotification";
	public static final String RegisterPropertyObserverIDNotification = "RegisterPropertyObserverIDNotification";
		
	@SuppressWarnings("rawtypes")
	private static final NSSelector propertyChanged = new NSSelector("propertyChanged", ERXConstant.NotificationClassArray);
	@SuppressWarnings("rawtypes")
	private static final NSSelector registerPropertyObserverID = new NSSelector("registerPropertyObserverID", ERXConstant.NotificationClassArray);
	
	private String id;
	private NSMutableDictionary<String, String> propertyObserverIDs = new NSMutableDictionary<String, String>();
	private NSMutableArray<String> updateContainerIDs = new NSMutableArray<String>();
	
	private static final Logger log = Logger.getLogger(AjaxNotificationCenter.class);

	public String id() {
		if(id == null) {
			id = ERXWOContext.safeIdentifierName(context(), true);
			AJAX_NOTIFICATION_CENTER_ID.takeValueInObject(id, d2wContext());
		}
		return id;
	}

	public AjaxNotificationCenter(WOContext context) {
        super(context);
    }
		
	public void setD2wContext(D2WContext context) {
        if(context != null && !context.equals(d2wContext())) {
        	log.debug("Removing observers for old context");
        	NSNotificationCenter.defaultCenter().removeObserver(this, PropertyChangedNotification, null);
        	NSNotificationCenter.defaultCenter().removeObserver(this, RegisterPropertyObserverIDNotification, null);
        }
        NSNotificationCenter.defaultCenter().addObserver(this, propertyChanged, PropertyChangedNotification, context);
        NSNotificationCenter.defaultCenter().addObserver(this, registerPropertyObserverID, RegisterPropertyObserverIDNotification, context);
        log.debug("Notifications registered for context: " + context);
        super.setD2wContext(context);
	}

	public NSMutableArray<String> updateContainerIDs() {
		log.debug("Updating container IDs: " + updateContainerIDs.componentsJoinedByString(", "));
		return updateContainerIDs;
	}
	
	@SuppressWarnings("unchecked")
	public void propertyChanged(NSNotification n) {
		log.debug("Property changed for property key: " + PROPERTY_KEY.valueInObject(n.object()));
		PropertyChangedDelegate delegate = PROPERTY_CHANGED_DELEGATE.valueInObject(n.object());
		if(delegate != null) {
			log.debug("Updating container id list with propertyChangedDelegate");
			NSArray<String> updateProps = delegate.propertyChanged((D2WContext)n.object());
			NSArray updateIDs = EOKeyValueCodingAdditions.Utility.valuesForKeys(propertyObserverIDs, updateProps).allValues();
			updateIDs = ERXArrayUtilities.removeNullValues(updateIDs);
			updateContainerIDs.addObjectsFromArray(updateIDs);
			log.debug("Container ids to be updated: " + updateContainerIDs.componentsJoinedByString(", "));
		}
	}
	
	public void registerPropertyObserverID(NSNotification n) {
		String propKey = PROPERTY_KEY.valueInObject(n.object());
		String propID = PROPERTY_OBSERVER_ID.valueInObject(n.object());
		if(!ERXStringUtilities.stringIsNullOrEmpty(propKey) && !ERXStringUtilities.stringIsNullOrEmpty(propID)) {
			propertyObserverIDs.setObjectForKey(propID, propKey);
			log.debug("ID registered for property: (" + propKey + ", " + propID + ")" );
		}
	}
	
	/**
	 * Since this component uses synchronization to update observers when the
	 * d2wContext changes, it cannot be non-synchronizing. However, if we want
	 * to be able to drop this component anywhere, it needs to be able to
	 * accept any binding value. So this method simply returns value for key
	 * from the dynamicBindings dictionary.
	 */
	public Object handleQueryWithUnboundKey(String key) {
		if(log.isDebugEnabled()) {
			log.debug("Handling unbound key: " + key);
		}
		return dynamicBindings().objectForKey(key);
	}
	
	/**
	 * Since this component uses synchronization to update observers when the
	 * d2wContext changes, it cannot be non-synchronizing. However, if we want
	 * to be able to drop this component anywhere, it needs to be able to
	 * accept any binding value. So this method simply adds value for key
	 * to the dynamicBindings dictionary.
	 */
	@SuppressWarnings("unchecked")
	public void handleTakeValueForUnboundKey(Object value, String key) {
		if(log.isDebugEnabled()) {
			log.debug("Take value: " + value + " for unbound key: " + key);
		}
		dynamicBindings().setObjectForKey(value, key);
	}
}