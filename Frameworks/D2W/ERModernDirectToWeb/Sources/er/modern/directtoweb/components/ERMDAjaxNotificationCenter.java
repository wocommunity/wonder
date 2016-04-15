package er.modern.directtoweb.components;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.webobjects.appserver.WOContext;
import com.webobjects.directtoweb.D2WContext;
import com.webobjects.foundation.NSArray;
import com.webobjects.foundation.NSDictionary;
import com.webobjects.foundation.NSMutableArray;
import com.webobjects.foundation.NSMutableDictionary;
import com.webobjects.foundation.NSNotification;
import com.webobjects.foundation.NSNotificationCenter;
import com.webobjects.foundation.NSSelector;

import er.ajax.AjaxUpdateContainer;
import er.directtoweb.components.ERDCustomComponent;
import er.extensions.appserver.ERXWOContext;
import er.extensions.eof.ERXConstant;
import er.extensions.eof.ERXKey;
import er.extensions.foundation.ERXArrayUtilities;
import er.extensions.foundation.ERXStringUtilities;
import er.modern.directtoweb.components.buttons.ERMDDeleteButton;

/**
 * ERMDAjaxNotificationCenter makes it easy to observe properties for changes
 * and update dependent property keys. You just specify a dependency structure
 * via the propertyDependencies D2W key. It takes a dictionary with property
 * keys to be observed as keys and an array of the dependents to be updated as
 * the value. Example:
 * 
 * <pre>
 * 100 : ((task = 'create' or task = 'edit') and entity.name = 'Person') => propertyDependencies = {"isFemale" = ("salutation"); "dateOfBirth" = ("discount", "parentEmail"); } [com.webobjects.directtoweb.Assignment]
 * </pre>
 * 
 * This will observe the property keys "isFemale" and "dateOfBirth". If
 * "isFemale" changes, the "salutation" property will be updated. If
 * "dateOfBirth" changes, the "discount" and "parentEmail" properties will be
 * updated. You can then hide and show properties on the fly by using the
 * displayVariant key:
 * 
 * <pre>
 * 100 : ((task = 'create' or task = 'edit') and entity.name = 'Person' and propertyKey = 'parentEmail' and object.isAdult = '1') => displayVariant = "omit" [com.webobjects.directtoweb.Assignment]
 * </pre>
 * 
 * 
 * By default, ERMDAjaxNotificationCenter will be included in the
 * aboveDisplayPropertyKeys repetition when propertyDependencies is not null. If
 * you set aboveDisplayPropertyKeys yourself, you have to include the
 * "ajaxNotificationCenter" property key.
 * 
 * Unlike the original version by Ramsey, this implementation depends on
 * ERMDInspectPageRepetition to insert AjaxObserveField and AjaxUpdateContainer
 * components where required.
 * 
 * @d2wKey ajaxNotificationCenter
 * @d2wKey propertyDependencies
 * 
 * @author rgurley@mac.com
 * @author fpeters
 * 
 */
public class ERMDAjaxNotificationCenter extends ERDCustomComponent {

    private static final long serialVersionUID = 1L;

    public static final ERXKey<String> AJAX_NOTIFICATION_CENTER_ID = new ERXKey<String>(
            "ajaxNotificationCenterID");

    public static final ERXKey<String> PROPERTY_OBSERVER_ID = new ERXKey<String>(
            "propertyObserverID");

    public static final ERXKey<String> PROPERTY_KEY = new ERXKey<String>("propertyKey");

    public static final ERXKey<NSDictionary<String, NSArray<String>>> PROPERTY_DEPENDENCIES = new ERXKey<NSDictionary<String, NSArray<String>>>(
            "propertyDependencies");

    public static final String PropertyChangedNotification = "PropertyChangedNotification";

    public static final String RegisterPropertyObserverIDNotification = "RegisterPropertyObserverIDNotification";

    @SuppressWarnings("rawtypes")
    private NSSelector propertyChanged = new NSSelector("propertyChanged",
            ERXConstant.NotificationClassArray);

    private String id;

    private NSMutableArray<String> updateContainerIDs = new NSMutableArray<String>();

    private static final Logger log = LoggerFactory.getLogger(ERMDAjaxNotificationCenter.class);

    public String id() {
        if (id == null) {
            id = ERXWOContext.safeIdentifierName(context(), true);
            AJAX_NOTIFICATION_CENTER_ID.takeValueInObject(id, d2wContext());
        }
        return id;
    }

    public ERMDAjaxNotificationCenter(WOContext context) {
        super(context);
    }

    public void setD2wContext(D2WContext context) {
        if (context != null && !context.equals(d2wContext())) {
            log.debug("Removing observers for old context");
            NSNotificationCenter.defaultCenter().removeObserver(this,
                    PropertyChangedNotification, null);
        }
        NSNotificationCenter.defaultCenter().addObserver(this, propertyChanged,
                PropertyChangedNotification, context);
        log.debug("Notifications registered for context: {}", context);
        super.setD2wContext(context);
    }

    public NSMutableArray<String> updateContainerIDs() {
        log.debug("Updating container IDs: {}", updateContainerIDs.componentsJoinedByString(", "));
        return updateContainerIDs;
    }

    public void propertyChanged(NSNotification n) {
        log.debug("Property changed for property key: {}", PROPERTY_KEY.valueInObject(n.object()));

        NSArray<String> updateProps = propertyChanged((D2WContext) n.object());
        if (updateProps != null && updateProps.count() > 0) {

            refreshRelationships(updateProps);
            
            // collect the corresponding update container IDs
            NSMutableArray<String> attributeLineUCs = new NSMutableArray<String>();
            D2WContext c = (D2WContext) n.object();
            String pageConfiguration = (String) c.valueForKey("pageConfiguration");
            for (String aPropertyName : updateProps) {
                String lineUC = pageConfiguration;
                lineUC = lineUC.concat(ERXStringUtilities.capitalize(aPropertyName));
                lineUC = lineUC.concat("LineUC");
                attributeLineUCs.addObject(lineUC);
            }

            ERXArrayUtilities.addObjectsFromArrayWithoutDuplicates(updateContainerIDs,
                    attributeLineUCs);

            // force update of notification center UC
            AjaxUpdateContainer.safeUpdateContainerWithID(id, context());
            log.debug("Container ids to be updated: {}", updateContainerIDs.componentsJoinedByString(", "));
        }
    }

    public NSDictionary<String, NSArray<String>> propertyDependencies(D2WContext context) {
        NSDictionary<String, NSArray<String>> propertyDependencies = PROPERTY_DEPENDENCIES
                .valueInObject(context);
        return propertyDependencies;
    }

    /**
     * @param context
     *            The d2wContext of the changed property level component
     * @return a list of property keys to be updated
     */
    @SuppressWarnings("unchecked")
    public NSArray<String> propertyChanged(D2WContext context) {
        String prop = context.propertyKey();
        NSArray<String> dependants = NSArray.EmptyArray;
        NSDictionary<String, NSArray<String>> propertyDependencies = PROPERTY_DEPENDENCIES
                .valueInObject(context);
        if (propertyDependencies.containsKey(prop)) {
            dependants = (NSArray<String>) propertyDependencies.valueForKey(prop);
        }
        return dependants;
    }

    /**
     * Sends out a notification to instances of
     * {@link ERMODEditRelationshipPage} for any relationships that have to be
     * updated, causing them to be refetched.
     * 
     * @param updateProps
     */
    private void refreshRelationships(NSArray<String> updateProps) {
        for (String aPropertyKey : updateProps) {
            // TODO handle key paths to different entities
            if (d2wContext().entity().relationshipNamed(aPropertyKey) != null) {
                // this is a relationship, so we'll send out a notification
                Object obj = context().page();
                String OBJECT_KEY = "object";
                NSMutableDictionary<String, Object> userInfo = new NSMutableDictionary<String, Object>(
                        obj, OBJECT_KEY);
                userInfo.setObjectForKey(d2wContext().valueForKey("object"), OBJECT_KEY);
                userInfo.setObjectForKey(aPropertyKey, "propertyKey");
                userInfo.setObjectForKey(id, "ajaxNotificationCenterId");
                // HACK: the delete action notification is the only way to
                // trigger a relationship component update for now
                NSNotificationCenter.defaultCenter().postNotification(
                        ERMDDeleteButton.BUTTON_PERFORMED_DELETE_ACTION, obj, userInfo);
                log.debug("Sent update notification for relationship: {}", aPropertyKey);
            }
        }
    }

    /**
     * Since this component uses synchronization to update observers when the
     * d2wContext changes, it cannot be non-synchronizing. However, if we want
     * to be able to drop this component anywhere, it needs to be able to accept
     * any binding value. So this method simply returns value for key from the
     * dynamicBindings dictionary.
     */
    public Object handleQueryWithUnboundKey(String key) {
        log.debug("Handling unbound key: {}", key);
        return dynamicBindings().objectForKey(key);
    }

    /**
     * Since this component uses synchronization to update observers when the
     * d2wContext changes, it cannot be non-synchronizing. However, if we want
     * to be able to drop this component anywhere, it needs to be able to accept
     * any binding value. So this method simply adds value for key to the
     * dynamicBindings dictionary.
     */
    @SuppressWarnings("unchecked")
    public void handleTakeValueForUnboundKey(Object value, String key) {
        log.debug("Take value: {} for unbound key: {}", value, key);
        dynamicBindings().setObjectForKey(value, key);
    }

}
