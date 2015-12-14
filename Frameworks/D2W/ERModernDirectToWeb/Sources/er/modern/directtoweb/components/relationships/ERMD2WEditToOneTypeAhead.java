package er.modern.directtoweb.components.relationships;

import org.apache.log4j.Logger;

import com.webobjects.appserver.WOActionResults;
import com.webobjects.appserver.WOContext;
import com.webobjects.directtoweb.D2W;
import com.webobjects.directtoweb.EditPageInterface;
import com.webobjects.eoaccess.EOEntity;
import com.webobjects.eoaccess.EOModelGroup;
import com.webobjects.eoaccess.EORelationship;
import com.webobjects.eoaccess.EOUtilities;
import com.webobjects.eocontrol.EODataSource;
import com.webobjects.eocontrol.EOEditingContext;
import com.webobjects.eocontrol.EOEnterpriseObject;
import com.webobjects.eocontrol.EOFetchSpecification;
import com.webobjects.eocontrol.EOQualifier;
import com.webobjects.eocontrol.EOSortOrdering;
import com.webobjects.foundation.NSArray;
import com.webobjects.foundation.NSDictionary;
import com.webobjects.foundation.NSNotification;
import com.webobjects.foundation.NSNotificationCenter;
import com.webobjects.foundation.NSSelector;

import er.directtoweb.ERDirectToWeb;
import er.directtoweb.components.ERDCustomEditComponent;
import er.extensions.eof.ERXConstant;
import er.extensions.eof.ERXEC;
import er.extensions.eof.ERXEOControlUtilities;
import er.extensions.eof.ERXGenericRecord;
import er.extensions.eof.ERXQ;
import er.extensions.eof.ERXS;
import er.extensions.foundation.ERXSimpleTemplateParser;
import er.extensions.foundation.ERXStringUtilities;
import er.extensions.foundation.ERXUtilities;
import er.extensions.foundation.ERXValueUtilities;
import er.modern.directtoweb.components.ERMDAjaxNotificationCenter;
import er.modern.directtoweb.components.buttons.ERMDActionButton;

/**
 * <p>A to-one relationship edit component that allows a user to select from a list by typing in the text field</p>
 * 
 * <p>It uses the 'keyWhenRelationship' from the d2w rules for this relationship to display and query. Because it uses an AjaxAutoComplet
 * the keyWhenRelationshp must be able to uniquely identify the found entity. It can either be a string attribute or a helper 
 * method that returns a constructed unique string.</p>
 * 
 * <p>If the keyWhenRelationship represents an attribute, this component will qualify the list of possible matches via a 
 * fetch. Otherwise it will load ALL of the available destinationEntities and qualify in memory. Keep this in mind when
 * deciding how to use this component.</p> 
 * 
 * @d2wKey restrictedChoiceKey - keypath off the component that returns a list of objects to be searched from (only used when keyWhenRelationship is NOT an attribute)
 * @d2wKey restrictingFetchSpecification - name of the model FetchSpec supplies the list of objects to be searched from (keyWhenRelationship is NOT an attribute) or that additionally qualifies the fetch
 * @d2wKey extraRestrictingQualifier - an additional qualifier (defined in the rules) that additionally qualifies the search
 * @d2wKey typeAheadSearchTemplate - a template that wraps the searchValue (for the inclusion of pre/post wildcards: i.e: "*@@searchValue@@*" )
 * @d2wKey typeAheadMinimumCharaceterCount - minimum number of characters before a search is performed
 * @d2wKey sortKey
 * @d2wKey isMandatory
 * @d2wKey propertyKey
 * @d2wKey destinationEntityName
 * @d2wKey sortCaseInsensitive
 * @d2wKey pageConfiguration
 * @d2wKey createConfigurationName
 * @d2wKey keyWhenRelationship
 * @d2wKey newButtonLabel
 * 
 * @author davidleber
 */

public class ERMD2WEditToOneTypeAhead extends ERDCustomEditComponent {
	
	public interface Keys extends ERDCustomEditComponent.Keys {
		public static final String newButtonLabel = "newButtonLabel";
		public static final String classForNewObjButton = "classForNewObjButton";
		public static final String pageConfiguration = "pageConfiguration";
		public static final String createConfigurationName = "createConfigurationName";
		public static final String propertyKey = "propertyKey";
		public static final String sortKey = "sortKey";
		public static final String destinationEntityName = "destinationEntityName";
		public static final String restrictedChoiceKey = "restrictedChoiceKey";
		public static final String restrictingFetchSpecification = "restrictingFetchSpecification";
		public static final String typeAheadSearchTemplate = "typeAheadSearchTemplate";
		public static final String extraRestrictingQualifier = "extraRestrictingQualifier";
		public static final String keyWhenRelationship = "keyWhenRelationship";
		public static final String typeAheadMinimumCharaceterCount = "typeAheadMinimumCharaceterCount";
	}
	
	public static Logger log = Logger.getLogger(ERMD2WEditToOneTypeAhead.class);
	private String _searchValue;
	private String _destinationEntityName;
	private String _sortKey;
	private String _propertyKey;
	private String _keyWhenRelationship;
	private String _safeElementID;
	private EOFetchSpecification _restrictingFetchSpec;
	private String _restrictedChoiceKey;
	private String _restrictingFetchSpecification;
	private NSArray<EOEnterpriseObject> _allItems;
	private String _template;
	private EOQualifier _extraQualifier;
	private Integer _minimumCharacterCount;
	private EOEnterpriseObject _currentSelection;
	private String _newButtonClass;
	private String _newButtonLabel;
	
	public EOEnterpriseObject item;
	
    public ERMD2WEditToOneTypeAhead(WOContext context) {
        super(context);
    }
	
    @Override
    public void awake() {
    	NSNotificationCenter.defaultCenter().addObserver(this, new NSSelector("relatedObjectDidChange", ERXConstant.NotificationClassArray), ERMDActionButton.BUTTON_PERFORMED_DELETE_ACTION, null);
    	super.awake();
    }
    
    @Override
    public void sleep() {
    	NSNotificationCenter.defaultCenter().removeObserver(this, ERMDActionButton.BUTTON_PERFORMED_DELETE_ACTION, null);
    	super.sleep();
    }
    
	/**
	 * Called when an {@link ERMDActionButton} changes the related object. Nulls
	 * {@link #_searchValue} which in turn lets it rebuild on the next display
	 */
	@SuppressWarnings("unchecked")
	public void relatedObjectDidChange(NSNotification notif) {
		NSDictionary<String, Object>userInfo = notif.userInfo();
		if (userInfo != null) {
			Object key = userInfo.valueForKey("propertyKey");
			EOEnterpriseObject obj = (EOEnterpriseObject)userInfo.valueForKey("object");
			if (propertyKey() != null && propertyKey().equals(key) && ERXEOControlUtilities.eoEquals(object(), obj)) {
				_searchValue = null;
				_currentSelection = null;
			}
		}
	}
	
    
    @Override
    public boolean synchronizesVariablesWithBindings() {
    	return false;
    }
    
    /**
     * Value displayed by the AjaxAutoFill field, if nothing is entered in the
     * field it will return either the kvc value of 'keyWhenRelationship' on the related
     * entity or the kvc value of 'userPresentableDescription'
     */
    public String searchValue() {
    	if (ERXStringUtilities.stringIsNullOrEmpty(_searchValue) && currentSelection() != null) {
    		_searchValue = currentSelection().valueForKey(keyWhenRelationship()).toString();
    	} 
		return _searchValue;
	}
    
    /**
     * Sets the searchValue
     * 
     * @param value
     */
	public void setSearchValue(String value) {
		_searchValue = value;
	}
	
	/**
	 * Returns the currently selected destination entity
	 */
	public EOEnterpriseObject currentSelection() {
		if (_currentSelection == null)
//			NSLog.out.appendln("***ERMD2WEditToOneTypeAhead.currentSelection: "
//					+ _currentSelection + " ***");
			_currentSelection = (EOEnterpriseObject)objectPropertyValue();
		return _currentSelection;
	}
	
	/**
	 * Returns the array of available matching destination entities
	 */
    public NSArray<EOEnterpriseObject> currentObjects() {
		NSArray<EOEnterpriseObject> result = null;
		String value = searchValue();
    	if (value != null) {
    		if (searchTemplate() != null) {
    			value = ERXSimpleTemplateParser.parseTemplatedStringWithObject(searchTemplate(), this);
    		}
    		EOQualifier qual = ERXQ.likeInsensitive(keyWhenRelationship(), value);
    		result = destinationObjectsWithQualifier(qual);
    	}
    	return result;
    }
	
    /**
     * Returns the display value for the available matching destination entities in the
     * drop down list.
     */
	public String itemDisplayString() {
		return (String)item.valueForKey(keyWhenRelationship());
	}
	
	/**
	 * Action called when the user makes a selection from the AjaxAutoComplete
	 */
	public WOActionResults selectObject() {
//		log.debug("selectobject called: " + item);
		EOQualifier qual = ERXQ.equals(keyWhenRelationship(), searchValue());
		NSArray<EOEnterpriseObject> objs = destinationObjectsWithQualifier(qual);
		if (objs != null && objs.count() > 0) {
			EOEnterpriseObject localEO = ERXEOControlUtilities.localInstanceOfObject(object().editingContext(), objs.objectAtIndex(0));
			object().addObjectToBothSidesOfRelationshipWithKey(localEO, propertyKey());
		} else {
			EOEnterpriseObject existingObj = (EOEnterpriseObject)object().valueForKey(propertyKey());
			if (existingObj != null) {
				object().removeObjectFromBothSidesOfRelationshipWithKey(existingObj, propertyKey());
			}
		}
        // support for ERMDAjaxNotificationCenter
        if (ERXValueUtilities.booleanValueWithDefault(d2wContext().valueForKey("shouldObserve"), false)) {
            NSNotificationCenter.defaultCenter().postNotification(
                    ERMDAjaxNotificationCenter.PropertyChangedNotification,
                    parent().valueForKeyPath("d2wContext"));
        }
//		NSLog.out.appendln("Select Object Called: " + object().valueForKey(propertyKey()) + " " + searchValue());
		return null;
	}
	
	/**
	 * Action called when user clicks the Add button
	 */
	@SuppressWarnings("unchecked")
	public WOActionResults addObject() {
		String currentPageConfiguration = stringValueForBinding(Keys.pageConfiguration);
		
		NSDictionary extraValues = currentPageConfiguration != null ? new NSDictionary(currentPageConfiguration, Keys.pageConfiguration) : null;
        String createPageConfigurationName = (String)ERDirectToWeb.d2wContextValueForKey(Keys.createConfigurationName, destinationEntityName(), extraValues);
        
		EditPageInterface epi = (EditPageInterface)D2W.factory().pageForConfigurationNamed(createPageConfigurationName, session());
		EOEditingContext newEc = ERXEC.newEditingContext(object().editingContext());
		EOEnterpriseObject relatedObject = EOUtilities.createAndInsertInstance(newEc, destinationEntityName());
		EOEnterpriseObject localObj = ERXEOControlUtilities.localInstanceOfObject(relatedObject.editingContext(), object());
		if (localObj instanceof ERXGenericRecord) {
			((ERXGenericRecord)localObj).setValidatedWhenNested(false);
		}
		localObj.addObjectToBothSidesOfRelationshipWithKey(relatedObject, propertyKey());
		
		epi.setNextPage(context().page());
		epi.setObject(relatedObject);
		
		// Null out the current searchValue so when we come back, it regenerates
		_searchValue = null;
		
		return (WOActionResults) epi;
	}
	
	public EODataSource dataSource() {
		return ERXEOControlUtilities.dataSourceForObjectAndKey(object(), propertyKey());
	}
	
	/**
	 * Should this component use a fetch to qualify the list of available destination entities
	 * 
	 * @return true if the 'keyWhenRelationship' is the name of an attribute
	 */
	public boolean useFetch() {
		EOEntity entity = EOUtilities.entityNamed(ec(), destinationEntityName());
		return (entity.attributeNamed(keyWhenRelationship()) != null);
	}
	
	@SuppressWarnings("unchecked")
	public NSArray<EOEnterpriseObject> destinationObjectsWithQualifier(EOQualifier qual) {
		NSArray<EOEnterpriseObject> result = null;
		NSArray<EOSortOrdering> orderings = null;
		if (!ERXStringUtilities.stringIsNullOrEmpty(sortKey())) {
			orderings = ERXS.ascs(sortKey());
		}
		if (extraQualifier() != null) {
			qual = ERXQ.and(qual, extraQualifier());
		}
		if (useFetch() && ERXStringUtilities.stringIsNullOrEmpty(restrictedChoiceKey())) {
	        if(restrictingFetchSpecificationName() != null) {
	        	qual = ERXQ.and(qual, restrictingFetchSpec().qualifier());
	        }
	        EOFetchSpecification fetchSpec = new EOFetchSpecification(destinationEntityName(), qual, orderings);
			fetchSpec.setIsDeep(true);
			EOEditingContext ec = ERXEC.newEditingContext();
			result = ec.objectsWithFetchSpecification(fetchSpec);
		} else {
			result = ERXQ.filtered(allItems(), qual);
		}
		return result;
	}
	
	private EOEditingContext ec() {
    	return object().editingContext();
    }
	
	// ACCESSORS
	
	public String destinationEntityName() {
		if (_destinationEntityName == null) {
			_destinationEntityName = stringValueForBinding(Keys.destinationEntityName);
			
		}
		return _destinationEntityName;
	}

	public String sortKey() {
		if (_sortKey == null) {
			_sortKey = stringValueForBinding(Keys.sortKey);
		}
		return _sortKey;
	}

	public String propertyKey() {
		if (_propertyKey == null) {
			_propertyKey = stringValueForBinding(Keys.propertyKey);
		}
		return _propertyKey;
	}

	public void setPropertyKey(String key) {
		_propertyKey = key;
	}
	
	@SuppressWarnings("unchecked")
    public NSArray<EOEnterpriseObject> allItems() {
    	if (_allItems == null) {
    		_allItems = (NSArray<EOEnterpriseObject>)restrictedChoiceList();
    		if (_allItems == null) {
    			EOFetchSpecification fetchSpec = new EOFetchSpecification(destinationEntityName(), null, null);
    			_allItems = ec().objectsWithFetchSpecification(fetchSpec);
    		}
		}
		return _allItems;
	}
	
	public EOFetchSpecification restrictingFetchSpec() {
		if (_restrictingFetchSpec == null) {
			
			_restrictingFetchSpec = EOModelGroup.defaultGroup().fetchSpecificationNamed(restrictingFetchSpecificationName(), destinationEntityName());;
		}
		return _restrictingFetchSpec;
	}

	public String restrictedChoiceKey() {
		if (_restrictedChoiceKey == null) {
			_restrictedChoiceKey = stringValueForBinding(Keys.restrictedChoiceKey);
		}
		return _restrictedChoiceKey;
	}

	public String restrictingFetchSpecificationName() {
		if (_restrictingFetchSpecification == null) {
			_restrictingFetchSpecification = stringValueForBinding(Keys.restrictingFetchSpecification);
		}
		return _restrictingFetchSpecification;
	}

	public String searchTemplate() {
		if (_template == null) {
			_template = stringValueForBinding(Keys.typeAheadSearchTemplate);
		}
		return _template;
	}

	public EOQualifier extraQualifier() {
		if (_extraQualifier == null) {
			_extraQualifier = (EOQualifier)valueForBinding(Keys.extraRestrictingQualifier);
		}
		return _extraQualifier;
	}

	public Integer minimumCharacterCount() {
		if (_minimumCharacterCount == null) {
			_minimumCharacterCount = ERXValueUtilities.IntegerValueWithDefault(stringValueForBinding(Keys.typeAheadMinimumCharaceterCount), 1);
		}
		return _minimumCharacterCount;
	}

	public String keyWhenRelationship() {
		if (_keyWhenRelationship == null) {
			_keyWhenRelationship = stringValueForBinding(Keys.keyWhenRelationship);
		}
		return _keyWhenRelationship;
	}
	
    public Object restrictedChoiceList() {
        String restrictedChoiceKey = stringValueForBinding(Keys.restrictedChoiceKey);
        if( restrictedChoiceKey!=null &&  restrictedChoiceKey.length()>0 )
            return valueForKeyPath(restrictedChoiceKey);
        String fetchSpecName = stringValueForBinding(Keys.restrictingFetchSpecification);
        if(fetchSpecName != null) {
            EORelationship relationship = ERXUtilities.relationshipWithObjectAndKeyPath(object(),
                                                                                        (String)d2wContext().valueForKey(Keys.propertyKey));
            return EOUtilities.objectsWithFetchSpecificationAndBindings(object().editingContext(), relationship.destinationEntity().name(),fetchSpecName,null);
        }
        return null;
    }
    
	public String newButtonClass() {
		if (_newButtonClass == null) {
			_newButtonClass = stringValueForBinding(Keys.classForNewObjButton, "Button ObjButton NewObjButton");
		}
		return _newButtonClass;
	}
	
	public String newButtonLabel() {
		if (_newButtonLabel == null) {
			_newButtonLabel = stringValueForBinding(Keys.newButtonLabel, "New");
		}
		return _newButtonLabel;
	}

	// AJAX IDs
	
	public String searchTermSelectedFunctionName() {
		if (_safeElementID == null) {
			_safeElementID =ERXStringUtilities.safeIdentifierName(context().elementID());
		}
		return "ermdtorlu_" + _safeElementID + "CompleteFunction";
	}

	public String searchTermSelectedFunction() {
		return "function(e) { " + searchTermSelectedFunctionName() + "(); }";
	}
    
	/** Should the 'new' button be displayed? */
	public boolean isEntityCreatable() {
		return ERXValueUtilities.booleanValueWithDefault(d2wContext()
				.valueForKey("isDestinationEntityCreatable"), true);
	}

	/** Should the 'inspect' button be displayed? */
	public boolean isEntityInspectable() {
		return ERXValueUtilities.booleanValueWithDefault(d2wContext()
				.valueForKey("isDestinationEntityInspectable"), true);
	}
}
