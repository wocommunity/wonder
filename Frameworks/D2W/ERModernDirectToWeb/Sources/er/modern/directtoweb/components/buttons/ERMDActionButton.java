package er.modern.directtoweb.components.buttons;

import com.webobjects.appserver.WOComponent;
import com.webobjects.appserver.WOContext;
import com.webobjects.appserver.WODisplayGroup;
import com.webobjects.directtoweb.D2WPage;
import com.webobjects.directtoweb.ERD2WUtilities;
import com.webobjects.directtoweb.EditPageInterface;
import com.webobjects.directtoweb.ListPageInterface;
import com.webobjects.directtoweb.QueryPageInterface;
import com.webobjects.directtoweb.SelectPageInterface;
import com.webobjects.eocontrol.EODataSource;
import com.webobjects.eocontrol.EOEnterpriseObject;
import com.webobjects.foundation.NSNotificationCenter;

import er.directtoweb.components.ERDCustomComponent;
import er.directtoweb.interfaces.ERDPickPageInterface;
import er.extensions.eof.ERXEOControlUtilities;
import er.extensions.foundation.ERXValueUtilities;

/**
 * Base Class for the ERM action buttons
 * 
 * @binding object
 * @binding displayGroup
 * @binding dataSource
 * 
 * @d2wKey task
 * @d2wKey allowInlineEditing
 * @d2wKey idForMainContainer
 * @d2wKey useAjaxControls
 * @d2wKey objectBeingEdited
 * @d2wKey inlineTask
 * @d2wKey useNestedEditingContext
 * @d2wKey isEntityEditable
 * @d2wKey pageConfiguration
 * 
 * @author davidleber
 *
 */
public abstract class ERMDActionButton extends ERDCustomComponent {
    public interface Keys extends ERDCustomComponent.Keys {
        public static final String object = "object";
        public static final String displayGroup = "displayGroup";
        public static final String dataSource = "dataSource";
        public static final String task = "task";
        public static final String allowInlineEditing = "allowInlineEditing";
        public static final String idForMainContainer = "idForMainContainer";
        public static final String useAjax = "useAjaxControls";
        public static final String objectBeingEdited = "objectBeingEdited";
        public static final String inlineTask = "inlineTask";
        public static final String useNestedEditingContext = "useNestedEditingContext";
        public static final String isEntityEditable = "isEntityEditable";
        public static final String pageConfiguration = "pageConfiguration";
    }
    
    public static final String BUTTON_PERFORMED_DELETE_ACTION = "ERMDActionButtonPerformedDelete";
    public static final String BUTTON_PERFORMED_EDIT_ACTION = "ERMDActionButtonPerformedEdit";
    public static final String BUTTON_PERFORMED_SELECT_ACTION = "ERMDActionButtonPerformedSelect";
    public static final String BUTTON_PERFORMED_INSPECT_ACTION = "ERMDActionButtonPerformedInspect";
    
	protected String _buttonLabel;
	protected String _buttonClass;
	protected String _updateContainer;
	protected Boolean _useAjax;
	
	public ERMDActionButton(WOContext context) {
		super(context);
	}
	
	/**
	 * ID of the update container for this button's ajax update.
	 * Defaults to the idForMainContainer value
	 */
	public String updateContainer() {
		if (_updateContainer == null) {
			_updateContainer = stringValueForBinding(Keys.idForMainContainer);
		}
		return _updateContainer;
	}
	
	/**
	 * Utility method to help post button action notification
	 * 
	 * @param note
	 */
	public void postNotification(String note) {
		NSNotificationCenter.defaultCenter().postNotification(note, this);
	}
	
    /** Action buttons do not synchronize their variables. */
	@Override
    public final boolean synchronizesVariablesWithBindings() { return false; }

    /** The current object. */
    public EOEnterpriseObject object() { return (EOEnterpriseObject)valueForBinding(Keys.object); }

    /** The current display group. */
    public WODisplayGroup displayGroup() { return (WODisplayGroup)valueForBinding(Keys.displayGroup); }

    /** The current data source. */
    public EODataSource dataSource() {
    	EODataSource ds =  (EODataSource)objectValueForBinding(Keys.dataSource, displayGroup() != null ? displayGroup().dataSource() : null); 
    	return ds;
    }
	
    /**
     * Utility method to return the local instance of the object as determined by the 
     * useNestedEditingContext binding
     */
    protected EOEnterpriseObject localInstanceOfObject() {
    	boolean createNestedContext = ERXValueUtilities.booleanValue(d2wContext().valueForKey(Keys.useNestedEditingContext));
    	return ERXEOControlUtilities.editableInstanceOfObject(object(), createNestedContext);
    }
    
    /**
     * Utility method returns the boolean value for the allowInlineEditing binding.
     * Used to determine whether inline behaviour is used.
     */
    public boolean shouldAllowInlineEditing() {
    	return ERXValueUtilities.booleanValue(d2wContextValueForBinding((Keys.allowInlineEditing)));
    }
    
    /**
     * Utility method, returns the boolean value for the useAjax binding
     */
    public boolean shouldUseAjax() {
    	return booleanValueForBinding(Keys.useAjax);
    }
    
    /**
     * Utility method, returns whether shouldAllowInlineEditing and shouldUseAjax are true
     */
	public Boolean useAjax() {
		if (_useAjax == null) {
			_useAjax = Boolean.valueOf(shouldUseAjax() && shouldAllowInlineEditing());
		}
		return _useAjax;
	}
	
    /** Utility to return the next page in the enclosing page. */
    public WOComponent nextPageInPage(D2WPage parent) {
        return ERD2WUtilities.nextPageInPage(parent);
    }
    
    /** Utility to return the enclosing select page, if there is one. */
    protected SelectPageInterface parentSelectPage() {
        return (SelectPageInterface)enclosingPageOfClass(SelectPageInterface.class);
    }
    
    /** Utility to return the first enclosing component that matches the given class, if there is one. */
    protected WOComponent enclosingPageOfClass(Class<?> c) {
        return ERD2WUtilities.enclosingPageOfClass(this, c);
    }
    
    /** Utility to return the enclosing list page, if there is one. */
    protected ListPageInterface parentListPage() {
        return (ListPageInterface)enclosingPageOfClass(ListPageInterface.class);
    }
    
    /** Utility to return the enclosing edit page, if there is one. */
    protected EditPageInterface parentEditPage() {
        return (EditPageInterface)enclosingPageOfClass(EditPageInterface.class);
    }
    
    /** Utility to return the enclosing query page, if there is one. */
    protected QueryPageInterface parentQueryPage() {
        return (QueryPageInterface)enclosingPageOfClass(QueryPageInterface.class);
    }

    /** Utility to return the enclosing pick page, if there is one. */
    protected ERDPickPageInterface parentPickPage() {
        return (ERDPickPageInterface)enclosingPageOfClass(ERDPickPageInterface.class);
    }

    /** Utility to return the enclosing D2W page, if there is one. */
    public D2WPage parentD2WPage() {
        return (D2WPage)enclosingPageOfClass(D2WPage.class);
    }

}
