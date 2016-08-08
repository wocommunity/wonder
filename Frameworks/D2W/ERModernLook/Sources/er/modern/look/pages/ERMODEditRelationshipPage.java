package er.modern.look.pages;

import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;

import org.apache.commons.lang3.ObjectUtils;

import com.webobjects.appserver.WOComponent;
import com.webobjects.appserver.WOContext;
import com.webobjects.appserver.WODisplayGroup;
import com.webobjects.directtoweb.D2W;
import com.webobjects.directtoweb.ERD2WUtilities;
import com.webobjects.directtoweb.EditPageInterface;
import com.webobjects.directtoweb.NextPageDelegate;
import com.webobjects.directtoweb.SelectPageInterface;
import com.webobjects.eoaccess.EOEntity;
import com.webobjects.eoaccess.EORelationship;
import com.webobjects.eoaccess.EOUtilities;
import com.webobjects.eocontrol.EOClassDescription;
import com.webobjects.eocontrol.EODataSource;
import com.webobjects.eocontrol.EODetailDataSource;
import com.webobjects.eocontrol.EOEditingContext;
import com.webobjects.eocontrol.EOEnterpriseObject;
import com.webobjects.eocontrol.EOQualifier;
import com.webobjects.eocontrol.EOSortOrdering;
import com.webobjects.foundation.NSArray;
import com.webobjects.foundation.NSDictionary;
import com.webobjects.foundation.NSMutableArray;
import com.webobjects.foundation.NSNotification;
import com.webobjects.foundation.NSNotificationCenter;
import com.webobjects.foundation.NSSelector;

import er.directtoweb.pages.ERD2WEditRelationshipPage;
import er.directtoweb.pages.ERD2WPage;
import er.extensions.eof.ERXConstant;
import er.extensions.eof.ERXEC;
import er.extensions.eof.ERXEOAccessUtilities;
import er.extensions.eof.ERXEOControlUtilities;
import er.extensions.eof.ERXGenericRecord;
import er.extensions.foundation.ERXArrayUtilities;
import er.extensions.foundation.ERXStringUtilities;
import er.extensions.foundation.ERXValueUtilities;
import er.modern.directtoweb.components.ERMDAjaxNotificationCenter;
import er.modern.directtoweb.components.buttons.ERMDActionButton;
import er.modern.directtoweb.components.repetitions.ERMDInspectPageRepetition;
import er.modern.directtoweb.interfaces.ERMEditRelationshipPageInterface;

/**
 * An improved EditRelationshipPage that supports embedding and inline editing tasks.
 * 
 * @d2wKey editConfigurationName
 * @d2wKey isEntityEditable
 * @d2wKey checkSortOrderingKeys
 * @d2wKey defaultSortOrdering
 * @d2wKey readOnly
 * @d2wKey relationshipRestrictingQualifier - An additional qualifier that can be used to restrict the objects 
 * 									   		  shown in the relationship (see: ERDDelayedExtraQualifierAssignment).
 * 									          Useful if you have a value like: isDeleted that you wish to respect.
 * 
 * @author davidleber
 */
public class ERMODEditRelationshipPage extends ERD2WPage implements ERMEditRelationshipPageInterface, SelectPageInterface {
  /**
   * Do I need to update serialVersionUID?
   * See section 5.6 <cite>Type Changes Affecting Serialization</cite> on page 51 of the 
   * <a href="http://java.sun.com/j2se/1.4/pdf/serial-spec.pdf">Java Object Serialization Spec</a>
   */
  private static final long serialVersionUID = 1L;

	public interface Keys extends ERD2WEditRelationshipPage.Keys {
		public static String parentPageConfiguration = "parentPageConfiguration";
		public static String inlineTask = "inlineTask";
		public static String inspectEmbeddedConfigurationName = "inspectEmbeddedConfigurationName";
		public static String editEmbeddedConfigurationName = "editEmbeddedConfigurationName";
		public static String createEmbeddedConfigurationName = "createEmbeddedConfigurationName";
		public static String queryEmbeddedConfigurationName = "queryEmbeddedConfigurationName";
		public static String localContext = "localContext";
		public static String relationshipRestrictingQualifier ="relationshipRestrictingQualifier";
		public static String checkSortOrderingKeys = "checkSortOrderingKeys";
		public static String defaultSortOrdering = "defaultSortOrdering";
		public static String userPreferencesSortOrdering	= "sortOrdering";
		public static String displayPropertyKeys = "displayPropertyKeys";
		public static String subTask = "subTask";
		public static String isEntityCreatable = "isEntityCreatable";
        public static String shouldShowQueryRelatedButton = "shouldShowQueryRelatedButton";
		
	}
	
	private EOEnterpriseObject _masterObject;
	private EOEnterpriseObject _selectedObject;
	private EOEnterpriseObject _objectToAddToRelationship;
	private String _relationshipKey;
	private EODataSource _dataSource;
	private EODataSource _selectDataSource;
	private WODisplayGroup _relationshipDisplayGroup;
    private Integer _batchSize = null;
	public boolean isRelationshipToMany;
	public WOComponent nextPage;
	public NextPageDelegate nextPageDelegate;
	
	public ERMODEditRelationshipPage(WOContext context) {
        super(context);
    }
    
    @Override
    public void awake() {
    	_dataSource = null;
    	NSNotificationCenter.defaultCenter().addObserver(this, new NSSelector<Void>("relatedObjectDidChange", ERXConstant.NotificationClassArray), ERMDActionButton.BUTTON_PERFORMED_DELETE_ACTION, null);
    	super.awake();
    }
    
    @Override
    public void sleep() {
    	NSNotificationCenter.defaultCenter().removeObserver(this, ERMDActionButton.BUTTON_PERFORMED_DELETE_ACTION, null);
    	super.sleep();
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
	 */
	public WOComponent newObjectAction() {
		EOEditingContext newEc = ERXEC.newEditingContext(masterObject().editingContext());
		EOClassDescription relatedObjectClassDescription = masterObject().classDescriptionForDestinationKey(relationshipKey());
		EOEnterpriseObject relatedObject = EOUtilities.createAndInsertInstance(newEc, relatedObjectClassDescription.entityName());
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
	 */
	public WOComponent queryAction() {
		if (inlineTask() != null) {
			setInlineTaskSafely("list");
		}
		return null;
	}

	/**
	 * Performs the saveAction. Called by inline edit or create pages.
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
        // support for ERMDAjaxNotificationCenter
        postChangeNotification();
		return null;
	}
	
	/** 
	 * Perform the selectAction. Called by inline select page.
	 */
	public WOComponent selectAction() {
		EOEnterpriseObject selected = (objectToAddToRelationship() != null) ? EOUtilities.localInstanceOfObject(masterObject().editingContext(), objectToAddToRelationship()) : null;
		
		if (selected != null) {
			
			masterObject().addObjectToBothSidesOfRelationshipWithKey(selected, relationshipKey());
			
			relationshipDisplayGroup().fetch();
			relationshipDisplayGroup().selectObject(selected);
			relationshipDisplayGroup().displayBatchContainingSelectedObject();
		}
		
        // support for ERMDAjaxNotificationCenter
        postChangeNotification();
		return null;
	}
	
	/** 
	 * Perform the returnAction. Called when the page is a non embedded page is returning to the originating
	 * edit page.
	 */
	public WOComponent returnAction() {
		
		masterObject().editingContext().saveChanges();
		WOComponent result = (nextPageDelegate() != null) ? nextPageDelegate().nextPage(this) : super.nextPage();

		if (result != null) {
			return result;
		}

		result = (WOComponent)D2W.factory().editPageForEntityNamed(masterObject().entityName(), session());
		((EditPageInterface)result).setObject(masterObject());

        // support for ERMDAjaxNotificationCenter
        postChangeNotification();
		return result;
	}
	
	/**
	 * Called when an {@link ERMDActionButton} changes the related object. 
	 * Forces the displayGroup to fetch.
	 */
	@SuppressWarnings("unchecked")
	public void relatedObjectDidChange(NSNotification notif) {
		NSDictionary<String, Object>userInfo = notif.userInfo();
		if (userInfo != null) {
			Object key = userInfo.valueForKey("propertyKey");
			EOEnterpriseObject obj = (EOEnterpriseObject)userInfo.valueForKey("object");
			if (relationshipKey() != null && relationshipKey().equals(key) && ERXEOControlUtilities.eoEquals(masterObject(), obj)) {
				relationshipDisplayGroup().fetch();
				// when the last object of the last batch gets removed, select the new last batch
				if (relationshipDisplayGroup().currentBatchIndex() > relationshipDisplayGroup().batchCount()) {
				    relationshipDisplayGroup().setCurrentBatchIndex(relationshipDisplayGroup().batchCount());
				}
			}
		}
        if (notif.userInfo().valueForKey("ajaxNotificationCenterId") == null) {
            // the change notification was not sent from ERMDAjaxNotificationCenter
            postChangeNotification();
        }
	}
	   
    private void postChangeNotification() {
        ERMDInspectPageRepetition parent = ERD2WUtilities.enclosingComponentOfClass(this,
                ERMDInspectPageRepetition.class);
        if (ERXValueUtilities.booleanValueWithDefault(
                parent.valueForKeyPath("d2wContext.shouldObserve"), false)) {
            NSNotificationCenter.defaultCenter().postNotification(
                    ERMDAjaxNotificationCenter.PropertyChangedNotification,
                    parent.valueForKeyPath("d2wContext"));
        }
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
	 * Required by the {@link ERMEditRelationshipPageInterface}
	 * 
	 * @param a an NSArray containing the master object (index 0) and relationship key (index 1).
	 */
	public void setMasterObjectAndRelationshipKey(NSArray<?> a) {
        EOEnterpriseObject masterObject = (EOEnterpriseObject) a.objectAtIndex(0);
        String relationshipKey = (String) a.objectAtIndex(1);
        if (masterObject != null
                && !ERXStringUtilities.stringIsNullOrEmpty(relationshipKey)) {
            EOEntity masterEntity = EOUtilities.entityForObject(
                    masterObject.editingContext(), masterObject);
            EORelationship rel = masterEntity.relationshipNamed(relationshipKey);
            // set currentRelationship key to allow unique ID creation
            // (wonder-140)
            d2wContext().takeValueForKey(rel, "currentRelationship");
        }
        setMasterObjectAndRelationshipKey(masterObject, relationshipKey);
	}
	
	/**
	 * Set the master object and relationship key.
	 * 
	 * @param eo the master object, an EOEnterpriseObject
	 * @param relationshipKey
	 */
	public void setMasterObjectAndRelationshipKey(EOEnterpriseObject eo, String relationshipKey) {
		// only do this if the eo and relationshipKey have changed;
		if (relationshipKey != null && eo != null) {
			if (ObjectUtils.notEqual(relationshipKey(), relationshipKey) ||
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
		        relationshipDisplayGroup().setSortOrderings(sortOrderings());
		        relationshipDisplayGroup().fetch();
		        EOQualifier extraQualifier = (EOQualifier)d2wContext().valueForKey(Keys.relationshipRestrictingQualifier);
		        if (extraQualifier != null) {
		        	relationshipDisplayGroup().setQualifier(extraQualifier);
		        	relationshipDisplayGroup().qualifyDataSource();
		        }
		        setPropertyKey(keyWhenRelationship());
			}
		}
	}
	
    /*
     * Overridden to set the parentRelationship key.
     * 
     * @see er.directtoweb.pages.ERD2WPage#settings()
     */
    @Override
    public NSDictionary<String,Object> settings() {
        String pc = d2wContext().dynamicPage();
        if (pc != null) {
            if (d2wContext().valueForKey("currentRelationship") != null) {
                // set parentRelationship key to allow subcomponents to
                // reference the correct ID (wonder-140)
                return new NSDictionary<String,Object>(new Object[] { pc,
                        d2wContext().valueForKey("currentRelationship") }, new String[] {
                        "parentPageConfiguration", "parentRelationship" });
            } else {
                return new NSDictionary<String,Object>(pc, "parentPageConfiguration");
            }
        }
        return null;
    }

	// SORT ORDERING
	
	@SuppressWarnings("unchecked")
	public NSArray<EOSortOrdering> sortOrderings() {
		NSArray<EOSortOrdering> sortOrderings = null;
		if (userPreferencesCanSpecifySorting()) {
			sortOrderings = (NSArray<EOSortOrdering>) userPreferencesValueForPageConfigurationKey(Keys.userPreferencesSortOrdering);
			if (log.isDebugEnabled()) {
			  log.debug("Found sort Orderings in user prefs " + sortOrderings);
			}
		}
		if (sortOrderings == null) {
			NSArray<String> sortOrderingDefinition = (NSArray<String>) d2wContext().valueForKey(Keys.defaultSortOrdering);
			if (sortOrderingDefinition != null) {
				NSMutableArray<EOSortOrdering> validatedSortOrderings = new NSMutableArray<EOSortOrdering>();
				NSArray<String> displayPropertyKeys = (NSArray<String>) d2wContext().valueForKey(Keys.displayPropertyKeys);
				for (int i = 0; i < sortOrderingDefinition.count();) {
					String sortKey = sortOrderingDefinition.objectAtIndex(i++);
					String sortSelectorKey = sortOrderingDefinition.objectAtIndex(i++);
					if (!checkSortOrderingKeys() || isValidSortKey(displayPropertyKeys, sortKey)) {
					  EOSortOrdering sortOrdering = new EOSortOrdering(sortKey, ERXArrayUtilities.sortSelectorWithKey(sortSelectorKey));
					  validatedSortOrderings.addObject(sortOrdering);
					}
				}
				sortOrderings = validatedSortOrderings;
				if (log.isDebugEnabled()) {
					log.debug("Found sort Orderings in rules " + sortOrderings);
				}
			}
		}
		return sortOrderings;
	}
	
	/**
	 * Returns whether or not sort orderings should be validated (based on the checkSortOrderingKeys rule).
	 * @return whether or not sort orderings should be validated
	 */
	public boolean checkSortOrderingKeys() {
		return ERXValueUtilities.booleanValueWithDefault(d2wContext().valueForKey(Keys.checkSortOrderingKeys), false);
	}
	
	/**
	 * Validates the given sort key (is it a display key, an attribute, or a valid attribute path). 
	 * 
	 * @param displayPropertyKeys the current display properties
	 * @param sortKey the sort key to validate
	 * @return true if the sort key is valid, false if not
	 */
	protected boolean isValidSortKey(NSArray<String> displayPropertyKeys, String sortKey) {
	  boolean validSortOrdering = false;
	  try {
	    if (displayPropertyKeys.containsObject(sortKey) || entity().anyAttributeNamed(sortKey) != null || ERXEOAccessUtilities.attributePathForKeyPath(entity(), sortKey).count() > 0) {
	      validSortOrdering = true;
	    }
	  }
	  catch (IllegalArgumentException e) {
	    // MS: ERXEOAccessUtilities.attributePathForKeyPath throws IllegalArgumentException for a bogus key path
	    validSortOrdering = false;
	  }
	  
	  if (!validSortOrdering) {
	    log.warn("Sort key '" + sortKey + "' is not in display keys, attributes or non-flattened key paths for the entity '" + entity().name() + "'.");
	    validSortOrdering = false;
	  }
	  return validSortOrdering;
  }
	
	// this can be overridden by subclasses for which sorting has to be fixed
	// (i.e. Grouping Lists)
	public boolean userPreferencesCanSpecifySorting() {
		return !"printerFriendly".equals(d2wContext().valueForKey(Keys.subTask));
	}
	
	// BATCH SIZE
	
    /**
     * @return the batch size as set via ERCPreference or the rules
     */
    public int numberOfObjectsPerBatch() {
        if (_batchSize == null) {
            Integer batchSize = ERXValueUtilities.IntegerValueWithDefault(d2wContext()
                    .valueForKey("defaultBatchSize"), 5);
            Object batchSizePref = userPreferencesValueForPageConfigurationKey("batchSize");
            if (batchSizePref != null) {
                if (log.isDebugEnabled()) {
                    log.debug("Found batch size in user prefs " + batchSizePref);
                }
                batchSize = ERXValueUtilities.IntegerValueWithDefault(batchSizePref,
                        batchSize);
            }
            _batchSize = batchSize;
        }
        return _batchSize.intValue();
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
    @Override
    public EODataSource dataSource() {
    	if (_dataSource == null) {
			_dataSource = ERXEOControlUtilities.dataSourceForObjectAndKey(masterObject(), relationshipKey());
		}
		return _dataSource;
    }
    
    @Override
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
			_relationshipDisplayGroup.setNumberOfObjectsPerBatch(numberOfObjectsPerBatch());
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
		_masterObject = masterObject;
	}
	
	/** Checks if the current list is empty. */
	public boolean isListEmpty() {
		return listSize() == 0;
	}

	/** The number of objects in the list. */
	public int listSize() {
		return relationshipDisplayGroup().displayedObjects().count();
	}
	
	/** Should the 'new' button be displayed? */
	public boolean isEntityCreatable() {
		return ERXValueUtilities.booleanValue(d2wContext().valueForKey(Keys.isEntityCreatable)) && !isEntityReadOnly();
	}
	
    public boolean shouldShowQueryRelatedButton() {
        boolean shouldShowQueryRelatedButton = ERXValueUtilities
                .booleanValue(d2wContext().valueForKey(Keys.shouldShowQueryRelatedButton));
        if (isRelationshipOwned()) {
            // if the relationship is owned, search makes no sense
            shouldShowQueryRelatedButton = false;
        }
        return shouldShowQueryRelatedButton;
    }

    public boolean isRelationshipOwned() {
        boolean isRelationshipOwned = false;
        if (masterObject().allPropertyKeys().contains(relationshipKey())) {
            isRelationshipOwned = masterObject().classDescription().ownsDestinationObjectsForRelationshipKey(relationshipKey());
        }
        return isRelationshipOwned;
    }

	private void writeObject(ObjectOutputStream out) throws IOException {
		out.writeObject(_masterObject);
		out.writeObject(_objectToAddToRelationship);
		out.writeObject(_selectedObject);
		out.writeObject(_relationshipKey);
		out.writeBoolean(isRelationshipToMany);
		out.writeObject(_dataSource);
		out.writeObject(_relationshipDisplayGroup.dataSource());
		out.writeObject(_relationshipDisplayGroup);
		out.writeObject(_selectDataSource);
		out.writeObject(d2wContext().valueForKey("inlineTask"));
	}
	
	private void readObject(ObjectInputStream in) throws IOException, ClassNotFoundException {
		_masterObject = (EOEnterpriseObject) in.readObject();
		_objectToAddToRelationship = (EOEnterpriseObject) in.readObject();
		_selectedObject = (EOEnterpriseObject) in.readObject();
		_relationshipKey = (String) in.readObject();
		isRelationshipToMany = in.readBoolean();
		_dataSource = (EODataSource) in.readObject();
		EODetailDataSource ds = (EODetailDataSource) in.readObject();
		ds.qualifyWithRelationshipKey(_relationshipKey, _masterObject);
		_relationshipDisplayGroup = (WODisplayGroup) in.readObject();
		_selectDataSource = (EODataSource) in.readObject();
		String inlineTask = (String) in.readObject();
		if(inlineTask != null) {
			d2wContext().takeValueForKey(inlineTask, "inlineTask");
		}
	}

    /**
     * @return a unique ID for the repetition container
     */
    public String idForRepetitionContainer() {
        String repetitionContainerID = (String) d2wContext().valueForKey(
                "idForRepetitionContainer");
        // use master object to generate globally unique ID
        // - allows for nesting of relationship components
        repetitionContainerID = repetitionContainerID.concat("_"
                + masterObject().hashCode());
        return repetitionContainerID;
    }

}
