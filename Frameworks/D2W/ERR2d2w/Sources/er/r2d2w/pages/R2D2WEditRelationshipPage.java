package er.r2d2w.pages;

import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.util.Objects;

import com.webobjects.appserver.WOComponent;
import com.webobjects.appserver.WOContext;
import com.webobjects.appserver.WODisplayGroup;
import com.webobjects.directtoweb.D2WContext;
import com.webobjects.directtoweb.EditRelationshipPageInterface;
import com.webobjects.directtoweb.SelectPageInterface;
import com.webobjects.eoaccess.EOEntity;
import com.webobjects.eoaccess.EORelationship;
import com.webobjects.eoaccess.EOUtilities;
import com.webobjects.eocontrol.EODataSource;
import com.webobjects.eocontrol.EODetailDataSource;
import com.webobjects.eocontrol.EOEditingContext;
import com.webobjects.eocontrol.EOEnterpriseObject;
import com.webobjects.eocontrol.EOSortOrdering;
import com.webobjects.foundation.NSArray;
import com.webobjects.foundation.NSMutableArray;
import com.webobjects.foundation.NSSelector;

import er.directtoweb.pages.ERD2WPage;
import er.extensions.appserver.ERXDisplayGroup;
import er.extensions.eof.ERXConstant;
import er.extensions.eof.ERXEC;
import er.extensions.eof.ERXEOAccessUtilities;
import er.extensions.eof.ERXEOControlUtilities;
import er.extensions.foundation.ERXArrayUtilities;
import er.extensions.foundation.ERXValueUtilities;

public class R2D2WEditRelationshipPage extends ERD2WPage implements EditRelationshipPageInterface, SelectPageInterface {
	/**
	 * Do I need to update serialVersionUID?
	 * See section 5.6 <cite>Type Changes Affecting Serialization</cite> on page 51 of the 
	 * <a href="http://java.sun.com/j2se/1.4/pdf/serial-spec.pdf">Java Object Serialization Spec</a>
	 */
	private static final long serialVersionUID = 2L;

	private transient NSArray<Object> masterObjectAndRelationshipKey;
	private EOEnterpriseObject masterObject, objectToAddToRelationship, objectInRelationship;
	private String relationshipKey;
	private boolean isRelationshipToMany;
	private ERXDisplayGroup<EOEnterpriseObject> relationshipDisplayGroup;
	private EODataSource selectDataSource;
	private String inspectConfigurationName;
	
	public static final String PARENT_RELATIONSHIP_KEY = "parentRelationship";
	public static final NSSelector<Void> OBJECTS_CHANGED = new NSSelector<Void>("objectsChangedInEditingContext", ERXConstant.NotificationClassArray);
	public static final NSSelector<Void> OBJECTS_INVALIDATED = new NSSelector<Void>("objectsInvalidatedInEditingContext", ERXConstant.NotificationClassArray);
	
	public R2D2WEditRelationshipPage(WOContext context) {
        super(context);
    }

	public boolean displayList() {
		return "list".equals(inlineTask());
	}
	
	public boolean displayInspect() {
		return "create".equals(inlineTask()) || "edit".equals(inlineTask()) || "inspect".equals(inlineTask());
	}
	
	public boolean displayQuery() {
		return "query".equals(inlineTask());
	}
	
	public String inlineTask() {
		return (String)d2wContext().valueForKey("inlineTask");
	}
	
	public void setInlineTask(String inlineTask) {
	}
	
	public void setInlineTaskSafely(String inlineTask) {
		d2wContext().takeValueForKey(inlineTask, "inlineTask");
	}
	
	public void setMasterObjectAndRelationshipKey(EOEnterpriseObject masterObject, String relationshipKey) {
		if(!ERXValueUtilities.isNull(masterObject) && relationshipKey != null && !relationshipKey.isBlank()) {
			if(!Objects.equals(masterObject(), masterObject) ||
					!Objects.equals(relationshipKey(), relationshipKey)) {
				
				/*
				 * Only validate if this is an inline edit relationship page.
				 * Otherwise, use a nested ec which is saved when _returnRelated
				 * is called, pushing changes into the parent ec.
				 */
				EOEditingContext ec = d2wContext().frame()
						?masterObject.editingContext()
						:ERXEC.newEditingContext(masterObject.editingContext(), false);
						
				this.masterObject = EOUtilities.localInstanceOfObject(ec, masterObject);
				this.relationshipKey = relationshipKey;
				EOEntity masterEntity = EOUtilities.entityForObject(ec, masterObject);
				EORelationship rel = masterEntity.relationshipNamed(relationshipKey);
				d2wContext().takeValueForKey(rel, PARENT_RELATIONSHIP_KEY);
				
				isRelationshipToMany = masterObject.isToManyKey(relationshipKey);
				
				WODisplayGroup dg = relationshipDisplayGroup();
				dg.setSelectsFirstObjectAfterFetch(!isRelationshipToMany);
				
				EODataSource ds = ERXEOControlUtilities.dataSourceForObjectAndKey(masterObject, relationshipKey);				
				setDataSource(ds);
				relationshipDisplayGroup().setDataSource(ds);
				
				dg.fetch();
				if(!isRelationshipToMany) {
					setObject(relationshipDisplayGroup().selectedObject());
				}
			}
		}
	}
	
	public ERXDisplayGroup<EOEnterpriseObject> relationshipDisplayGroup() {
		if(relationshipDisplayGroup == null) {
			relationshipDisplayGroup = new ERXDisplayGroup<EOEnterpriseObject>();
			setSortOrderingsOnDisplayGroup(sortOrderings(), relationshipDisplayGroup);
			relationshipDisplayGroup.setNumberOfObjectsPerBatch(numberOfObjectsPerBatch());
		}
		return relationshipDisplayGroup;
	}
	
	public EOEnterpriseObject selectedObject() {
		return relationshipDisplayGroup().selectedObject();
	}
	
	public void setSelectedObject(EOEnterpriseObject selectedObject) {
		relationshipDisplayGroup().setSelectedObject(selectedObject);
	}
	
	public int numberOfObjectsPerBatch() {
		int batchSize = 0;
		if (shouldShowBatchNavigation()) {
			batchSize = ERXValueUtilities.intValueWithDefault(d2wContext().valueForKey("defaultBatchSize"), 0);
			Object batchSizePref = userPreferencesValueForPageConfigurationKey("batchSize");
			if (batchSizePref != null) {
				if (log.isDebugEnabled()) {
						log.debug("batchSize User Preference: " + batchSizePref);
				}
				batchSize = ERXValueUtilities.intValueWithDefault(batchSizePref, batchSize);
			}
		}
		return batchSize;
	}

	/**
	 * Determines if the batch navigation should be shown.	It can be explicitly disabled by setting the D2W key 
	 * <code>showBatchNavigation</code> to false.
	 * @return true if the batch navigation should be shown
	 */
	public boolean shouldShowBatchNavigation() {
		return ERXValueUtilities.booleanValueWithDefault(d2wContext().valueForKey("showBatchNavigation"), true);
	}

	@SuppressWarnings("unchecked")
	public NSArray<EOSortOrdering> sortOrderings() {
		NSArray<EOSortOrdering> sortOrderings = null;
			sortOrderings = (NSArray<EOSortOrdering>) userPreferencesValueForPageConfigurationKey("sortOrdering");
			if (log.isDebugEnabled()) {
			  log.debug("Found sort Orderings in user prefs " + sortOrderings);
			}
		if (sortOrderings == null) {
			NSArray<String> sortOrderingDefinition = (NSArray<String>) d2wContext().valueForKey("defaultSortOrdering");
			if (sortOrderingDefinition != null) {
				NSMutableArray<EOSortOrdering> validatedSortOrderings = new NSMutableArray<EOSortOrdering>();
				NSArray<String> displayPropertyKeys = (NSArray<String>) d2wContext().valueForKey("displayPropertyKeys");
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
		return ERXValueUtilities.booleanValueWithDefault(d2wContext().valueForKey("checkSortOrderingKeys"), false);
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
		} catch (IllegalArgumentException e) {
			// ERXEOAccessUtilities.attributePathForKeyPath throws IllegalArgumentException for a bogus key path
			validSortOrdering = false;
		}
		
		if (!validSortOrdering) {
			log.warn("Sort key '" + sortKey + "' is not in display keys, attributes or non-flattened key paths for the entity '" + entity().name() + "'.");
			validSortOrdering = false;
		}
		return validSortOrdering;
	}
	
	protected void setSortOrderingsOnDisplayGroup(NSArray<EOSortOrdering> sortOrderings, WODisplayGroup dg) {
		if(sortOrderings == null) {
			sortOrderings = NSArray.emptyArray();
		}
		dg.setSortOrderings(sortOrderings);
	}
			
	public boolean isRelationshipToMany() {
		return isRelationshipToMany;
	}
	
	public EOEnterpriseObject masterObject() {
		return masterObject;
	}
	
	public void setMasterObject(EOEnterpriseObject masterObject) {
		setMasterObjectAndRelationshipKey(masterObject, relationshipKey());
		this.masterObject = masterObject;
	}

	public NSArray<Object> masterObjectAndRelationshipKey() {
		return masterObjectAndRelationshipKey;
	}

	public void setMasterObjectAndRelationshipKey(NSArray<Object> masterObjectAndRelationshipKey) {
		this.masterObjectAndRelationshipKey = masterObjectAndRelationshipKey;
		if(d2wContext() != null) {
			EOEnterpriseObject eo = (EOEnterpriseObject) masterObjectAndRelationshipKey.objectAtIndex(0);
			String relationshipKey = (String) masterObjectAndRelationshipKey.objectAtIndex(1);
			setMasterObjectAndRelationshipKey(eo, relationshipKey);
		}
	}
	
	public String relationshipKey() {
		return relationshipKey;
	}
	
	public void setRelationshipKey(String relationshipKey) {
		setMasterObjectAndRelationshipKey(masterObject(), relationshipKey);
		this.relationshipKey = relationshipKey;
	}
	
	public EODataSource selectDataSource() {
		return selectDataSource;
	}
	
	public void setSelectDataSource(EODataSource selectDataSource) {
		this.selectDataSource = selectDataSource;
	}
	
	/**
	 * Holds the object edited/inspected in the embedded inspect page.
	 * @return the object to inspect
	 */
	public EOEnterpriseObject objectToAddToRelationship() {
		return objectToAddToRelationship;
	}
	
	/**
	 * Holds the object edited/inspected in the embedded inspect page.
	 * @param objectToAddTorRelationship the object to inspect
	 */
	public void setObjectToAddToRelationship( EOEnterpriseObject objectToAddTorRelationship) {
		this.objectToAddToRelationship = objectToAddTorRelationship;
	}
	
	/**
	 * Holds the object selected by the embedded select page
	 * @return the selected object
	 */
	public EOEnterpriseObject objectInRelationship() {
		return objectInRelationship;
	}
	
	/**
	 * Sets the object selected by the embedded select page
	 * @param objectInRelationship the object selected
	 */
	public void setObjectInRelationship(EOEnterpriseObject objectInRelationship) {
		this.objectInRelationship = objectInRelationship;
	}

	public void setLocalContext(D2WContext c) {
		super.setLocalContext(c);
		if(masterObjectAndRelationshipKey() != null) {
			EOEnterpriseObject eo = (EOEnterpriseObject) masterObjectAndRelationshipKey().objectAtIndex(0);
			String relationshipKey = (String) masterObjectAndRelationshipKey().objectAtIndex(1);
			setMasterObjectAndRelationshipKey(eo, relationshipKey);
		}
	}
	
	/**
	 * Performs the queryAction. Sets the inline task to 'list'
	 * @return
	 */
	public WOComponent queryAction() {
		setInlineTaskSafely("list");
		return context().page();
	}

	/** 
	 * Perform the selectAction. Called by inline select page.
	 * @return
	 */
	public WOComponent selectAction() {
		EOEnterpriseObject selected = null;
		if(objectToAddToRelationship() != null) {
			selected = EOUtilities.localInstanceOfObject(masterObject().editingContext(), objectToAddToRelationship());
		}
		if (selected != null) {
			masterObject().addObjectToBothSidesOfRelationshipWithKey(selected, relationshipKey());
			relationshipDisplayGroup().fetch();
			relationshipDisplayGroup().selectObject(selected);
			relationshipDisplayGroup().displayBatchContainingSelectedObject();
		}
		return context().page();
	}
	
	/**
	 * Performs the saveAction. Called by inline edit and create pages.
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
 		setInspectConfigurationName(null);
		return context().page();
	}

	/**
	 * @return the inspectConfigurationName
	 */
	public String inspectConfigurationName() {
		if(inspectConfigurationName == null) {
			inspectConfigurationName = (String)d2wContext().valueForKey("inspectConfigurationName");
		}
		return inspectConfigurationName;
	}

	/**
	 * @param inspectConfigurationName the inspectConfigurationName to set
	 */
	public void setInspectConfigurationName(String inspectConfigurationName) {
		this.inspectConfigurationName = inspectConfigurationName;
	}

	private void writeObject(ObjectOutputStream out) throws IOException {
		out.writeObject(masterObject);
		out.writeObject(objectToAddToRelationship);
		out.writeObject(objectInRelationship);
		out.writeObject(relationshipKey);
		out.writeBoolean(isRelationshipToMany);
		out.writeObject(relationshipDisplayGroup.dataSource());
		out.writeObject(relationshipDisplayGroup);
		out.writeObject(selectDataSource);
		out.writeObject(inspectConfigurationName);
		out.writeObject(d2wContext().valueForKey("inlineTask"));
	}
	
	private void readObject(ObjectInputStream in) throws IOException, ClassNotFoundException {
		masterObject = (EOEnterpriseObject) in.readObject();
		objectToAddToRelationship = (EOEnterpriseObject) in.readObject();
		objectInRelationship = (EOEnterpriseObject) in.readObject();
		relationshipKey = (String) in.readObject();
		isRelationshipToMany = in.readBoolean();
		EODetailDataSource ds = (EODetailDataSource) in.readObject();
		ds.qualifyWithRelationshipKey(relationshipKey, masterObject);
		relationshipDisplayGroup = (ERXDisplayGroup<EOEnterpriseObject>) in.readObject();
		selectDataSource = (EODataSource) in.readObject();
		inspectConfigurationName = (String) in.readObject();
		String inlineTask = (String) in.readObject();
		if(inlineTask != null) {
			d2wContext().takeValueForKey(inlineTask, "inlineTask");
		}
		
		masterObjectAndRelationshipKey = new NSArray<Object>(masterObject, relationshipKey);
		
		if(masterObject != null && relationshipKey != null) {
			EOEntity masterEntity = EOUtilities.entityForObject(masterObject.editingContext(), masterObject);
			EORelationship rel = masterEntity.relationshipNamed(relationshipKey);
			d2wContext().takeValueForKey(rel, PARENT_RELATIONSHIP_KEY);
		}
	}
}