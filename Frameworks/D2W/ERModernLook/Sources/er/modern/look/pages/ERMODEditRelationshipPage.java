package er.modern.look.pages;

import com.webobjects.appserver.WOComponent;
import com.webobjects.appserver.WOContext;
import com.webobjects.appserver.WODisplayGroup;
import com.webobjects.directtoweb.D2W;
import com.webobjects.directtoweb.EditPageInterface;
import com.webobjects.directtoweb.NextPageDelegate;
import com.webobjects.directtoweb.SelectPageInterface;
import com.webobjects.eoaccess.EOUtilities;
import com.webobjects.eocontrol.EOClassDescription;
import com.webobjects.eocontrol.EODataSource;
import com.webobjects.eocontrol.EOEditingContext;
import com.webobjects.eocontrol.EOEnterpriseObject;
import com.webobjects.foundation.NSArray;

import er.directtoweb.pages.ERD2WEditRelationshipPage;
import er.directtoweb.pages.ERD2WPage;
import er.extensions.ERXExtensions;
import er.extensions.eof.ERXEC;
import er.extensions.eof.ERXEOControlUtilities;
import er.extensions.eof.ERXGenericRecord;
import er.modern.directtoweb.interfaces.ERMEditRelationshipPageInterface;

/**
 * An improved EditRelationshipPage that supports embedding and inline editing tasks.
 * 
 * @d2wKey editConfigurationName
 * @d2wKey isEntityEditable
 * @d2wKey readOnly
 * 
 * @author davidleber
 */
public class ERMODEditRelationshipPage extends ERD2WPage implements ERMEditRelationshipPageInterface, SelectPageInterface {
	
	public interface Keys extends ERD2WEditRelationshipPage.Keys {
		public static String parentPageConfiguration = "parentPageConfiguration";
		public static String inlineTask = "inlineTask";
		public static String inspectEmbeddedConfigurationName = "inspectEmbeddedConfigurationName";
		public static String editEmbeddedConfigurationName = "editEmbeddedConfigurationName";
		public static String createEmbeddedConfigurationName = "createEmbeddedConfigurationName";
		public static String queryEmbeddedConfigurationName = "queryEmbeddedConfigurationName";
		public static String localContext = "localContext";
	}
	
	private EOEnterpriseObject _masterObject;
	private EOEnterpriseObject _selectedObject;
	private EOEnterpriseObject _objectToAddToRelationship;
	private String _relationshipKey;
	private EODataSource _dataSource;
	private EODataSource _selectDataSource;
	private WODisplayGroup _relationshipDisplayGroup;
	public boolean isRelationshipToMany;
	public WOComponent nextPage;
	public NextPageDelegate nextPageDelegate;
	
    public ERMODEditRelationshipPage(WOContext context) {
        super(context);
    }
    
    @Override
    public void awake() {
    	_dataSource = null;
    	super.awake();
    }
	
	// ACTIONS
	
    /**
     * Perform the displayQueryAction. Sets the inline task to 'query'.
     */
	public WOComponent displayQueryAction() {
		setInlineTaskSafely("query");
		return null;
	}
	
	/**
	 * Performs the newObjectAction. Creates a new object and sets the inline task
	 * to 'create'
	 * @return
	 */
	public WOComponent newObjectAction() {
		EOEditingContext newEc = ERXEC.newEditingContext(masterObject().editingContext());
		EOClassDescription relatedObjectClassDescription = masterObject().classDescriptionForDestinationKey(relationshipKey());
		EOEnterpriseObject relatedObject = (EOEnterpriseObject)EOUtilities.createAndInsertInstance(newEc, relatedObjectClassDescription.entityName());
		EOEnterpriseObject localObj = EOUtilities.localInstanceOfObject(relatedObject.editingContext(), masterObject());
		if (localObj instanceof ERXGenericRecord) {
			((ERXGenericRecord)localObj).setValidatedWhenNested(false);
		}
		localObj.addObjectToBothSidesOfRelationshipWithKey(relatedObject, relationshipKey());
		setSelectedObject(relatedObject);
		setInlineTaskSafely("create");
		return null;
	}
	
	/**
	 * Performs the queryAction. Sets the inline task to 'list'
	 * @return
	 */
	public WOComponent queryAction() {
		if (inlineTask() != null) {
			setInlineTaskSafely("list");
		}
		return null;
	}

	/**
	 * Performs the saveAction. Called by inline edit or create pages.
	 * @return
	 */
	public WOComponent saveAction() {
		if ("create".equals(inlineTask())) {
			relationshipDisplayGroup().fetch();
			int count = relationshipDisplayGroup().allObjects().count();
			if (count > 0) {
				Object object = relationshipDisplayGroup().allObjects().objectAtIndex(relationshipDisplayGroup().allObjects().count() - 1);
				relationshipDisplayGroup().selectObject(object);
				relationshipDisplayGroup().displayBatchContainingSelectedObject();
			}
		}
 		setInlineTaskSafely(null);	
		return null;
	}
	
	/** 
	 * Perform the selectAction. Called by inline select page.
	 * @return
	 */
	public WOComponent selectAction() {
		EOEnterpriseObject selected = (objectToAddToRelationship() != null) ? EOUtilities.localInstanceOfObject(masterObject().editingContext(), objectToAddToRelationship()) : null;
		
		if (selected != null) {
			
			masterObject().addObjectToBothSidesOfRelationshipWithKey(selected, relationshipKey());
			
			relationshipDisplayGroup().fetch();
			relationshipDisplayGroup().selectObject(selected);
			relationshipDisplayGroup().displayBatchContainingSelectedObject();
		}
		
		return null;
	}
	
	/** 
	 * Perform the returnAction. Called when the page is a non embedded page is returning to the originating
	 * edit page.
	 * @return
	 */
	public WOComponent returnAction() {
		
		masterObject().editingContext().saveChanges();
		WOComponent result = (nextPageDelegate() != null) ? nextPageDelegate().nextPage(this) : super.nextPage();

		if (result != null) {
			return result;
		}

		result = (WOComponent)D2W.factory().editPageForEntityNamed(masterObject().entityName(), session());
		((EditPageInterface)result).setObject(masterObject());
		return result;
	}
	
	// COMPONENT DISPLAY CONTROLS
	
	/**
	 * Controls whether the inline query page is displayed.
	 */
	public boolean displayQuery() {
		return "query".equals(inlineTask());
	}
	
	/**
	 * Controls whether the inline eidt/create page is displayed.
	 */
	public boolean displayNew() {
		return "edit".equals(inlineTask()) || "inspect".equals(inlineTask()) || "create".equals(inlineTask());
	}
	
	/**
	 * Controls whether the inline list page is displayed.
	 */
	public boolean displayList() {
		return "list".equals(inlineTask());
	}
	
	/**
	 * Returns the name of the current inline page configuration
	 * @return
	 */
	public String inspectConfiguration() {
		String result = null;
		if ("create".equals(inlineTask())) {
			result = (String)d2wContext().valueForKey(Keys.createEmbeddedConfigurationName);
		} else if ("edit".equals(inlineTask())) {
			result = (String)d2wContext().valueForKey(Keys.editEmbeddedConfigurationName);
		} else {
			result = (String)d2wContext().valueForKey(Keys.inspectEmbeddedConfigurationName);
		}
		return result;
	}
	
	// SELECT PAGE INTERFACE
	
	/**
	 * Returns the current selected Object. Required by the SelectPageInterface
	 */
	public EOEnterpriseObject selectedObject() {
		return _selectedObject;
	}

	/**
	 * Sets the current selected Object. Required by the SelectPageInterface
	 */
	public void setSelectedObject(EOEnterpriseObject eo) {
		_selectedObject = eo;
	}
	
	// ERMEditRelationshipPageInterface
	
	/**
	 * Returns an array containing the master object (index 0) and relationship key (index 1).
	 * Required by the {@link ERMEditRelationshipPageInterface}
	 * 
	 * @return NSArray containing the master object (index 0) and relationship key (index 1).
	 */
	public NSArray<?> masterObjectAndRelationshipKey() {
		return new NSArray<Object>(new Object[] { masterObject(), relationshipKey() });
	}

	/**
	 * Sets the master object and relationship key.
	 * Takes an NSArray containing the master object (index 0) and relationship key (index 1).
	 * Required by the {@link ERMEditRelationhsipPageInterface}
	 * 
	 * @param NSArray containing the master object (index 0) and relationship key (index 1).
	 */
	public void setMasterObjectAndRelationshipKey(NSArray<?> a) {
		setMasterObjectAndRelationshipKey((EOEnterpriseObject)a.objectAtIndex(0), (String)a.objectAtIndex(1));
	}
	
	/**
	 * Set the master object and relationship key.
	 * 
	 * @param EOEnterpriseObject the master object
	 * @param String the relationship key
	 */
	public void setMasterObjectAndRelationshipKey(EOEnterpriseObject eo, String relationshipKey) {
		// only do this if the eo and relationshipKey have changed;
		if (relationshipKey != null && eo != null) {
			if (ERXExtensions.safeDifferent(relationshipKey(), relationshipKey) ||
					(masterObject() != null && !ERXEOControlUtilities.eoEquals(masterObject(), eo))) {
//				NSLog.out.appendln("***ERMODEditRelationshipPage.setMasterObjectAndRelationshipKey: "
//								+ "HAS CHANGES; " + eo + " - " + masterObject() + "  " + relationshipKey + " - " + relationshipKey() +"***");
		        _dataSource = null;
				setMasterObject(eo);
		        
		        setEditingContext(eo.editingContext());
		        setRelationshipKey(relationshipKey); 
		        
		        if (masterObject().isToManyKey(relationshipKey))
		            isRelationshipToMany = true;
		        else
		            relationshipDisplayGroup().setSelectsFirstObjectAfterFetch(true);
		        
		        relationshipDisplayGroup().setDataSource(dataSource());
		        relationshipDisplayGroup().fetch();
		        setPropertyKey(keyWhenRelationship());

			}
		}
	}
    
    // ACCESSORS
    
	public String inlineTask() {
		return (String)d2wContext().valueForKey(Keys.inlineTask);
	}
	
	public void setInlineTask(String task) {
		// noop
	}
	
	public void setInlineTaskSafely(String task) {
		d2wContext().takeValueForKey(task, Keys.inlineTask);
	}
    
    public String relationshipKey() {
    	return _relationshipKey;
    }
    
    public void setRelationshipKey(String key) {
    	_relationshipKey = key;
    }
    
    /**
     * DataSource for the relationship.
     * 
     * @return EODataSource an EODetailDataSource created from the masterObject and relationshipKey.
     */
    public EODataSource dataSource() {
    	if (_dataSource == null) {
			_dataSource = ERXEOControlUtilities.dataSourceForObjectAndKey(masterObject(), relationshipKey());
		}
		return _dataSource;
    }
    
    public void setDataSource(EODataSource ds) {
    	_dataSource = ds;
    }

	public EOEnterpriseObject objectToAddToRelationship() {
		return _objectToAddToRelationship;
	}
	
	public void setObjectToAddToRelationship( EOEnterpriseObject objectToAddTorRelationship) {
		_objectToAddToRelationship = objectToAddTorRelationship;
	}

	/**
	 * Display group for the related objects
	 * 
	 * @return WODisplayGroup lazily instantiated display group.
	 */
	public WODisplayGroup relationshipDisplayGroup() {
		if (_relationshipDisplayGroup == null) {
			_relationshipDisplayGroup = new WODisplayGroup();
			String count = (String)d2wContext().valueForKey("defaultBatchSize");
			if (count != null) {
				int intCount = Integer.parseInt(count);
				_relationshipDisplayGroup.setNumberOfObjectsPerBatch(intCount);
			}
			
		}
		return _relationshipDisplayGroup;
	}

	public void setRelationshipDisplayGroup(WODisplayGroup relationshipDisplayGroup) {
		_relationshipDisplayGroup = relationshipDisplayGroup;
	}
	
	public EODataSource selectDataSource() {
		return _selectDataSource;
	}

	public void setSelectDataSource(EODataSource selectDataSource) {
		_selectDataSource = selectDataSource;
	}

	public EOEnterpriseObject masterObject() {
		return _masterObject;
	}

	public void setMasterObject(EOEnterpriseObject masterObject) {
		this._masterObject = masterObject;
	}
	
	/** Checks if the current list is empty. */
	public boolean isListEmpty() {
		return listSize() == 0;
	}

	/** The number of objects in the list. */
	public int listSize() {
		return relationshipDisplayGroup().allObjects().count();
	}
	
}