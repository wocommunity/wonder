package er.directtoweb;

import com.webobjects.appserver.WOComponent;
import com.webobjects.appserver.WOContext;
import com.webobjects.directtoweb.D2WContext;
import com.webobjects.directtoweb.D2WEditRelationshipPage;
import com.webobjects.eoaccess.EOUtilities;
import com.webobjects.eocontrol.EODataSource;
import com.webobjects.eocontrol.EOEditingContext;
import com.webobjects.eocontrol.EOEnterpriseObject;
import er.extensions.ERXEC;

/**
 * <p>This page is an adaptation of the superclass, intended for use as a "query relationship" page, coupled with the
 * {@link ERD2WQueryToOneFault} component.  It allows the user to select a related object for use as a value in the
 * originating query page.</p>
 *
 * <p>Consumers of this component should be aware of a few provisos...  Since, in most cases, the originating query page
 * will not have a value for the object to hand to this page, it's also very important to to manually set the entity so
 * the query and list components know what they're supposed to work with.  Lastly, you should finish initializing the
 * page by calling the {@link #setMasterObjectAndRelationshipKey(com.webobjects.eocontrol.EOEnterpriseObject, String)}
 * method.</p>
 *
 * <p>If you access this component via the {@link ERD2WQueryToOneFault} component, these things are all done for you.</p>
 *
 * @author Travis Cripps 
 */
public class ERD2WQueryRelationshipPage extends D2WEditRelationshipPage {

    protected String _displayKey;
    public EODataSource selectDataSource = null;

    protected EOEditingContext _editingContext;
    protected EOEnterpriseObject _eoToUse;
    protected EOEnterpriseObject _selectedEO;

    private PageState _pageState = PageState.Query;

    private enum PageState {
        Query,
        List
    }

    public ERD2WQueryRelationshipPage(WOContext context) {
        super(context);
        // This component needs a D2W context, but, currently, it isn't created by the D2W factory, so we manually
        // create a new context and assign the task ("editRelationship")--things the factory would normally do.
        setLocalContext(new D2WContext(context.session()));
        setTask("editRelationship");
    }

    /**
     * Get the key that is used when showing the selected EO.
     * @return the display key
     */
    public String displayKey() {
        return _displayKey;
    }

    /**
     * Returns a description of the selected EO, using the {@link #displayKey}, if not selected EO is not null.
     * @return a string description.
     */
    public String toOneDescription() {
        EOEnterpriseObject selectedEO = selectedObject();
        if (selectedEO != null) {
            return selectedEO.valueForKeyPath(displayKey()).toString();
        } else {
            return null;
        }
    }

    /**
     * Gets the selected object.
     * @return the selected object
     */
    public EOEnterpriseObject selectedObject() {
        return _selectedEO;
    }

    /**
     * Determines if the query component appears in the lower half of the page.
     * @return true if the query component should appear
     */
    public boolean displayQuery() {
        return _pageState == PageState.Query;
    }

    /**
     * Determines if the select component appears in the lower half of the page.
     * @return true if the select component should appear
     */
    public boolean displayList() {
        return _pageState == PageState.List;
    }

    /**
     * Returns the current component with a list of results for the query.
     * @return the current component, showing a list of results
     */
    @Override
    public WOComponent queryAction() {
        _pageState = PageState.List;
        return context().page();
    }

    /**
     * Returns the current component, using the selected EO as the value to be set in the return action.
     * @return the current component
     */
    @Override
    public WOComponent selectAction() {
        EOEnterpriseObject eoToUse = _eoToUse != null ? EOUtilities.localInstanceOfObject(_editingContext, _eoToUse) : null;
        if (eoToUse != null) {
            _selectedEO = eoToUse;
        } else {
            _pageState = PageState.Query;
        }
        return context().page();
    }

    /**
     * Removes the selection of the object.
     * @return the current page
     */
    @Override
    public WOComponent removeFromToOneRelationshipAction() {
        _selectedEO = null;
        return context().page();
    }

    /**
     * Returns the current component with a query form for searching for the desired, related object.
     * @return the current component, showing a query form
     */
    public WOComponent displayQueryAction() {
        _pageState = PageState.Query;
        return context().page();
    }

    /**
     * Returns a destination component, usually the originating query page.
     * @return the destination component
     */
    @Override
    public WOComponent returnAction() {
        return nextPageDelegate().nextPage(this);
    }

    /**
     * This is a badly twisted version of this method.  This page uses the eo as the actual selected object rather
     * than as a "master" object, and it uses the relationship key as the display key.
     * @param eo to use as the selected object
     * @param relationshipKey to use as the display key for the selected object
     */
    @Override
    public void setMasterObjectAndRelationshipKey(EOEnterpriseObject eo, String relationshipKey) {
        _editingContext = ERXEC.newEditingContext();
        _editingContext.setDelegate(this);

        if (eo != null) {
            _selectedEO = EOUtilities.localInstanceOfObject(_editingContext, eo);
            setObject(_selectedEO);
            setEntityName(_selectedEO.entityName());
        }

        _displayKey = relationshipKey;
        setPropertyKey(displayKey());
    }

    @Override
    public boolean isEntityReadOnly() {
        return true;
    }

}
