package er.directtoweb;

import com.webobjects.appserver.WOComponent;
import com.webobjects.appserver.WOContext;
import com.webobjects.directtoweb.KeyValuePath;
import com.webobjects.directtoweb.NextPageDelegate;
import com.webobjects.directtoweb.QueryComponent;
import com.webobjects.eocontrol.EOEnterpriseObject;
import er.extensions.ERXLocalizer;

/**
 * A component for use on D2W query pages that allows the user to select a related object to be used in the query via a
 * "query relationship page."  Note that this component works only with relationships, and bad things will probably
 * happen if you try to use it with a regular attribute.
 * @author Travis Cripps
 */
public class ERD2WQueryToOneFault extends QueryComponent {

    public ERD2WQueryToOneFault(WOContext context) {
        super(context);
    }

    /**
     * Gets the string that will be displayed to represent the to-one value.
     * @return the display string
     */
    public String displayString() {
        EOEnterpriseObject eo = (EOEnterpriseObject)value();
        String result;
        if (eo != null) {
            String keyWhenRelationship = keyWhenRelationship();
            if (null == keyWhenRelationship || keyWhenRelationship.equals("userPresentableDescription")) {
                result = eo.userPresentableDescription();
            } else {
                result = eo.valueForKeyPath(keyWhenRelationship).toString();
            }
        } else {
            String noSelection = (String) d2wContext().valueForKey("noSelectionString");
            if (noSelection != null && noSelection.trim().length() > 0) {
                result = ERXLocalizer.currentLocalizer().localizedStringForKeyWithDefault(noSelection);
            } else {
                result = noSelection;
            }
        }
        return result;
    }

    /**
     * Produces the label text for the query button.
     * @return the text
     */
    public String queryButtonLabel() {
        ERXLocalizer localizer = ERXLocalizer.currentLocalizer();
        return value() != null ? localizer.localizedStringForKeyWithDefault("Change") : localizer.localizedStringForKeyWithDefault("Select");
    }

    /**
     * Produces a page where the user may query and select the object which should be used for this property in the
     * current query page.
     * @return the query relationship page.
     */
    public WOComponent queryRelationshipAction() {
        ERD2WQueryRelationshipPage queryRelationshipPage = (ERD2WQueryRelationshipPage)pageWithName("ERD2WQueryRelationshipPage");
        String propertyKey = propertyKey();
        // The value() eo will always be null unless it's previously been set by this action, so we need to tell the page
        // which entity it should use, as it will not have the object from which that information could normally be inferred. 
        queryRelationshipPage.setEntity(KeyValuePath.entityAtEndOfKeyPath(propertyKey, entity()));
        queryRelationshipPage.setMasterObjectAndRelationshipKey((EOEnterpriseObject)value(), keyWhenRelationship());
        queryRelationshipPage.setNextPageDelegate(new QueryRelationshipDelegate((ERD2WQueryPage)context().page(), propertyKey));
        return queryRelationshipPage;
    }

    /**
     * A {@link NextPageDelegate} implementation that assigns the object selected on the "query relationship page" to
     * the display group of a query page.
     */
    private static class QueryRelationshipDelegate implements NextPageDelegate {

        ERD2WQueryPage destinationPage;
        String propertyKey;

        public QueryRelationshipDelegate(ERD2WQueryPage destinationPage, String propertyKey) {
            this.destinationPage = destinationPage;
            this.propertyKey = propertyKey;
        }

        public WOComponent nextPage(WOComponent sender) {
            ERD2WQueryRelationshipPage queryRelationshipPage = (ERD2WQueryRelationshipPage)sender;
            EOEnterpriseObject selectedObject = queryRelationshipPage.selectedObject();
            if (selectedObject != null) {
                destinationPage.displayGroup().queryMatch().takeValueForKeyPath(selectedObject, propertyKey);
            } else {
                destinationPage.displayGroup().queryMatch().removeObjectForKey(propertyKey);
            }
            return destinationPage;
        }
        
    }

}
