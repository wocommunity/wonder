/*
 * Copyright (C) NetStruxr, Inc. All rights reserved.
 *
 * This software is published under the terms of the NetStruxr
 * Public Software License version 0.5, a copy of which has been
 * included with this distribution in the LICENSE.NPL file.  */
package er.directtoweb;

import org.apache.log4j.Logger;

import com.webobjects.appserver.WOActionResults;
import com.webobjects.appserver.WOComponent;
import com.webobjects.appserver.WOContext;
import com.webobjects.appserver.WODisplayGroup;
import com.webobjects.appserver.WORequest;
import com.webobjects.appserver.WOResponse;
import com.webobjects.appserver.WOSession;
import com.webobjects.directtoweb.ConfirmPageInterface;
import com.webobjects.directtoweb.D2W;
import com.webobjects.directtoweb.D2WContext;
import com.webobjects.directtoweb.D2WListPage;
import com.webobjects.directtoweb.EditPageInterface;
import com.webobjects.directtoweb.InspectPageInterface;
import com.webobjects.directtoweb.ListPageInterface;
import com.webobjects.directtoweb.SelectPageInterface;
import com.webobjects.eoaccess.EODatabaseDataSource;
import com.webobjects.eoaccess.EOEntity;
import com.webobjects.eoaccess.EOModelGroup;
import com.webobjects.eoaccess.EORelationship;
import com.webobjects.eoaccess.EOUtilities;
import com.webobjects.eocontrol.EODataSource;
import com.webobjects.eocontrol.EOEditingContext;
import com.webobjects.eocontrol.EOEnterpriseObject;
import com.webobjects.eocontrol.EOFetchSpecification;
import com.webobjects.eocontrol.EOSharedEditingContext;
import com.webobjects.eocontrol.EOSortOrdering;
import com.webobjects.foundation.NSArray;
import com.webobjects.foundation.NSDictionary;
import com.webobjects.foundation.NSMutableArray;
import com.webobjects.foundation.NSNotification;
import com.webobjects.foundation.NSNotificationCenter;
import com.webobjects.foundation.NSSelector;

import er.extensions.ERXArrayUtilities;
import er.extensions.ERXBatchingDisplayGroup;
import er.extensions.ERXComponentActionRedirector;
import er.extensions.ERXConstant;
import er.extensions.ERXDisplayGroup;
import er.extensions.ERXEOAccessUtilities;
import er.extensions.ERXEOControlUtilities;
import er.extensions.ERXExtensions;
import er.extensions.ERXLocalizer;
import er.extensions.ERXValueUtilities;

/**
 * Reimplementation of the D2WListPage. Descendes from ERD2WPage instead of
 * D2WList.
 * 
 * @author ak
 */
public class ERD2WListPage extends ERD2WPage implements ERDListPageInterface, SelectPageInterface, ERXComponentActionRedirector.Restorable {

	/** logging support */
	public final static Logger log = Logger.getLogger(ERD2WListPage.class);
	
	protected boolean _shouldRefetch;

	/**
	 * Public constructor. Registers for
	 * {@link EOEditingContext.EditingContextDidSaveChangesNotification} so that
	 * component stays informed when objects are deleted and added.
	 * 
	 * @param c
	 *            current context
	 */
	public ERD2WListPage(WOContext c) {
		super(c);
		NSNotificationCenter.defaultCenter().addObserver(this, new NSSelector("editingContextDidSaveChanges", ERXConstant.NotificationClassArray), EOEditingContext.EditingContextDidSaveChangesNotification, null);
	}

	/** Override to un-register for stop obsevring notifcations. */
	public void finalize() throws Throwable {
		NSNotificationCenter.defaultCenter().removeObserver(this);
		super.finalize();
	}

	// reimplementation of D2WList stuff

	/** Holds the display group. */
	protected WODisplayGroup _displayGroup;

	public boolean _hasToUpdate = false;

	protected boolean _rowFlip = false;

	/** Returns the display group, creating one if there is none present. */
	public WODisplayGroup displayGroup() {
		if (_displayGroup == null) {
			createDisplayGroup();
			_displayGroup.setSelectsFirstObjectAfterFetch(false);
			if (ERD2WFactory.erFactory().defaultListPageDisplayGroupDelegate() != null) {
				_displayGroup.setDelegate(ERD2WFactory.erFactory().defaultListPageDisplayGroupDelegate());
			}
		}
		return _displayGroup;
	}

	/**
	 * Creates the display group and sets the _displayGroup instance variable
	 */
	protected void createDisplayGroup() {
		boolean useBatchingDisplayGroup = useBatchingDisplayGroup();
		if (useBatchingDisplayGroup) {
			_displayGroup = new ERXBatchingDisplayGroup();
		} else {
			_displayGroup = new ERXDisplayGroup();
		}
	}

	/**
	 * Checks the d2wContext for useBatchingDisplayGroup and returns it.
	 * 
	 */
	public boolean useBatchingDisplayGroup() {
		return ERXValueUtilities.booleanValue(d2wContext().valueForKey("useBatchingDisplayGroup"));
	}

	/**
	 * Called when an {@link EOditingContext} has changed. Sets
	 * {@link #_hasToUpdate} which in turn lets the group refetch on the next
	 * display.
	 */
	// CHECKME ak is this really needed? I'd think it's kindo of overkill.
	public void editingContextDidSaveChanges(NSNotification notif) {
		_hasToUpdate = true;
	}

	/**
	 * Checks if the entity is read only, meaning that you can't edit it's
	 * objects.
	 */
	public boolean isEntityReadOnly() {
		boolean flag = super.isEntityReadOnly();
		flag = !ERXValueUtilities.booleanValueWithDefault(d2wContext().valueForKey("isEntityEditable"), !flag);
		flag = ERXValueUtilities.booleanValueWithDefault(d2wContext().valueForKey("readOnly"), flag);
		return flag;
	}

	public boolean isEntityEditable() {
		return ERXValueUtilities.booleanValueWithDefault(d2wContext().valueForKey("isEntityEditable"), false);
	}

	public boolean alwaysRefetchList() {
		return ERXValueUtilities.booleanValueWithDefault(d2wContext().valueForKey("alwaysRefetchList"), true);
	}

	/**
	 * Checks if the current task is select. We need this because this page
	 * implements the {@link SelectPageInterface} so we can't do an instanceof
	 * test.
	 */
	public boolean isSelecting() {
		return task().equals("select");
	}

	/** Checks if the current list is empty. */
	public boolean isListEmpty() {
		return listSize() == 0;
	}

	/** The number of objects in the list. */
	public int listSize() {
		return displayGroup().allObjects().count();
	}

	/**
	 * Utility to have alternating row colors. Override this to have more than
	 * one color.
	 */
	public String alternatingColorForRow() {
		_rowFlip = !_rowFlip;
		if (_rowFlip || !alternateRowColor())
			return backgroundColorForTable();
		else
			return backgroundColorForTableDark();
	}

	/**
	 * The background color for the current row. Override this to have more than
	 * one color.
	 */
	public String backgroundColorForRow() {
		return !isSelecting() || selectedObjects().containsObject(object()) ? alternatingColorForRow() : "#FFFF00";
	}

	/** Does nothing and exists only for KeyValueCoding. */
	public void setBackgroundColorForRow(String value) {
	}

	/** The currently selected object. */
	public EOEnterpriseObject selectedObject() {
		return (EOEnterpriseObject) displayGroup().selectedObject();
	}

	/**
	 * Sets currently selected object. Pushes the value to the display group,
	 * clearing the selection if needed.
	 */
	public void setSelectedObject(EOEnterpriseObject eo) {
		if (eo != null)
			displayGroup().selectObject(eo);
		else
			displayGroup().clearSelection();
	}

	/** The currently selected objects. */
	public NSArray selectedObjects() {
		return displayGroup().selectedObjects();
	}

	/**
	 * Sets currently selected objects. Pushes the values to the display group,
	 * clearing the selection if needed.
	 */
	public void setSelectedObjects(NSArray eos) {
		if (eos != null)
			displayGroup().setSelectedObjects(eos);
		else
			displayGroup().clearSelection();
	}

	/** Action method to select an object. */
	public WOComponent selectObjectAction() {
		setSelectedObject(object());
		WOComponent result = nextPageFromDelegate();
		return result;
	}

	public WOComponent backAction() {
		WOComponent result = nextPageFromDelegate();
		if (result == null) {
			result = nextPage();
			if (result == null) {
				result = (WOComponent) D2W.factory().queryPageForEntityNamed(entity().name(), session());
			}
		}
		return result;
	}

	/** * end of reimplementation */

	public String urlForCurrentState() {
		return context().directActionURLForActionNamed(d2wContext().dynamicPage(), null).replaceAll("&amp;", "&");
	}

	protected void setSortOrderingsOnDisplayGroup(NSArray sortOrderings, WODisplayGroup dg) {
		sortOrderings = sortOrderings != null ? sortOrderings : NSArray.EmptyArray;
		dg.setSortOrderings(sortOrderings);
	}

	public static WOComponent printerFriendlyVersion(D2WContext d2wContext, WOSession session, EODataSource dataSource, WODisplayGroup displayGroup) {
		ListPageInterface result = (ListPageInterface) ERD2WFactory.erFactory().printerFriendlyPageForD2WContext(d2wContext, session);
		result.setDataSource(dataSource);
		WODisplayGroup dg = null;
		if (result instanceof D2WListPage) {
			dg = ((D2WListPage) result).displayGroup();
		} else if (result instanceof ERDListPageInterface) {
			dg = ((ERDListPageInterface) result).displayGroup();
		} else {
			try {
				dg = (WODisplayGroup) ((WOComponent) result).valueForKey("displayGroup");
			} catch (Exception ex) {
				log.warn("Can't get displayGroup from page of class: " + result.getClass().getName());
			}
		}
		if (dg != null) {
			dg.setSortOrderings(displayGroup.sortOrderings());
			dg.setNumberOfObjectsPerBatch(displayGroup.allObjects().count());
			dg.updateDisplayedObjects();
		}
		return (WOComponent) result;
	}

	public WOComponent printerFriendlyVersion() {
		return ERD2WListPage.printerFriendlyVersion(d2wContext(), session(), dataSource(), displayGroup());
	}

	// This will allow d2w pages to be listed on a per configuration basis in
	// stats collecting.
	public String descriptionForResponse(WOResponse aResponse, WOContext aContext) {
		String descriptionForResponse = (String) d2wContext().valueForKey("pageConfiguration");
		/*
		 * if (descriptionForResponse == null) log.info("Unable to find
		 * pageConfiguration in d2wContext: " + d2wContext());
		 */
		return descriptionForResponse != null ? descriptionForResponse : super.descriptionForResponse(aResponse, aContext);
	}

	private boolean _hasBeenInitialized = false;

	private Number _batchSize = null;

	public int numberOfObjectsPerBatch() {
		if (_batchSize == null) {
			int batchSize = ERXValueUtilities.intValueWithDefault(d2wContext().valueForKey("defaultBatchSize"), 0);
			Object batchSizePref = userPreferencesValueForPageConfigurationKey("batchSize");
			if (batchSizePref != null) {
				if (log.isDebugEnabled()) {
					log.debug("batchSize User Prefererence: " + batchSizePref);
				}
				batchSize = ERXValueUtilities.intValueWithDefault(batchSizePref, batchSize);
			}
			_batchSize = ERXConstant.integerForInt(batchSize);
		}
		return _batchSize.intValue();
	}

	// this can be overridden by subclasses for which sorting has to be fixed
	// (i.e. Grouping Lists)
	public boolean userPreferencesCanSpecifySorting() {
		return !"printerFriendly".equals(d2wContext().valueForKey("subTask"));
	}

	public boolean checkSortOrderingKeys() {
		return ERXValueUtilities.booleanValueWithDefault(d2wContext().valueForKey("checkSortOrderingKeys"), false);
	}

	public NSArray sortOrderings() {
		NSArray sortOrderings = null;
		if (userPreferencesCanSpecifySorting()) {
			sortOrderings = (NSArray) userPreferencesValueForPageConfigurationKey("sortOrdering");
			if (log.isDebugEnabled())
				log.debug("Found sort Orderings in user prefs " + sortOrderings);
		}
		if (sortOrderings == null) {
			NSArray sortOrderingDefinition = (NSArray) d2wContext().valueForKey("defaultSortOrdering");
			if (sortOrderingDefinition != null) {
				NSMutableArray so = new NSMutableArray();
				NSArray displayPropertyKeys = (NSArray) d2wContext().valueForKey("displayPropertyKeys");
				for (int i = 0; i < sortOrderingDefinition.count();) {
					String sortKey = (String) sortOrderingDefinition.objectAtIndex(i++);
					String sortSelectorKey = (String) sortOrderingDefinition.objectAtIndex(i++);
					if (checkSortOrderingKeys()) {
						if (displayPropertyKeys.containsObject(sortKey) || entity().anyAttributeNamed(sortKey) != null || ERXEOAccessUtilities.attributePathForKeyPath(entity(), sortKey).count() > 0) {
							EOSortOrdering sortOrdering = new EOSortOrdering(sortKey, ERXArrayUtilities.sortSelectorWithKey(sortSelectorKey));
							so.addObject(sortOrdering);
						} else {
							log.warn("Sort key '" + sortKey + "' is not in display keys, attributes or non-flattened key paths");
						}
					} else {
						EOSortOrdering sortOrdering = new EOSortOrdering(sortKey, ERXArrayUtilities.sortSelectorWithKey(sortSelectorKey));
						so.addObject(sortOrdering);
					}
				}
				sortOrderings = so;
				if (log.isDebugEnabled())
					log.debug("Found sort Orderings in rules " + sortOrderings);
			}
		}
		return sortOrderings;
	}

	public String defaultSortKey() {
		// the default D2W mechanism is completely disabled
		return null;
	}

	public void takeValuesFromRequest(WORequest r, WOContext c) {
		setupPhase();
		super.takeValuesFromRequest(r, c);
	}

	public WOActionResults invokeAction(WORequest r, WOContext c) {
		setupPhase();
		return super.invokeAction(r, c);
	}

	public void appendToResponse(WOResponse r, WOContext c) {
		setupPhase();
		_rowFlip = true;
		if (_hasToUpdate) {
			willUpdate();
			displayGroup().fetch();
			_hasToUpdate = false;
			didUpdate();
		}

		// GN: reset the displayed batch if it is out of range
		if (this.displayGroup() != null && this.displayGroup().currentBatchIndex() > this.displayGroup().batchCount()) {
			this.displayGroup().setCurrentBatchIndex(1);
		}
		super.appendToResponse(r, c);
	}

	protected Object dataSourceState;

	public void setDataSource(EODataSource eodatasource) {
		EODatabaseDataSource ds = (eodatasource instanceof EODatabaseDataSource) ? (EODatabaseDataSource) eodatasource : null;
		Object newDataSourceState = null;
		if (ds != null) {
			newDataSourceState = ds.fetchSpecification().toString().replaceAll("\\n", "") + ":" + ds.fetchSpecificationForFetch().toString().replaceAll("\\n", "") + " fetchLimit: " + ds.fetchSpecification().fetchLimit() + ", " + ds.fetchSpecificationForFetch().fetchLimit();
		}
		EODataSource old = displayGroup().dataSource();
		super.setDataSource(eodatasource);
		displayGroup().setDataSource(eodatasource);
		if (ds == null || (dataSourceState == null) || (dataSourceState != null && !dataSourceState.equals(newDataSourceState)) || alwaysRefetchList()) {
			log.debug("updating:\n" + dataSourceState + " vs\n" + newDataSourceState);
			dataSourceState = newDataSourceState;
			_hasToUpdate = true;

			// AK: when you use the page in a embedded component and have a few
			// of them in a tab
			// page, WO reuses the component for a new dataSource. If this DS
			// doesn't have the
			// sort order keys required it leads to a KVC error later on. We fix
			// this here to re-init
			// the sort ordering from the rules.
			if (old != null && eodatasource != null && ERXExtensions.safeDifferent(eodatasource.classDescriptionForObjects(), old.classDescriptionForObjects())) {
				setSortOrderingsOnDisplayGroup(sortOrderings(), displayGroup());
			}
		}
		
	}

	protected void willUpdate() {
	}

	protected void didUpdate() {
	}

	protected void setupPhase() {
		WODisplayGroup dg = displayGroup();
		if (dg != null) {
			NSArray sortOrderings = dg.sortOrderings();
			EODataSource ds = dataSource();
			if (!_hasBeenInitialized) {
				log.debug("Initializing display group");
				String fetchspecName = (String) d2wContext().valueForKey("restrictingFetchSpecification");
				if (fetchspecName != null) {
					if (ds instanceof EODatabaseDataSource) {
						EOFetchSpecification fs = ((EODatabaseDataSource) ds).entity().fetchSpecificationNamed(fetchspecName);
						if (fs != null) {
							fs = (EOFetchSpecification) fs.clone();
						}
						((EODatabaseDataSource) ds).setFetchSpecification(fs);
					}
				}
				if (sortOrderings == null) {
					sortOrderings = sortOrderings();
					setSortOrderingsOnDisplayGroup(sortOrderings, dg);
				}
				dg.setNumberOfObjectsPerBatch(numberOfObjectsPerBatch());
				dg.fetch();
				dg.updateDisplayedObjects();
				_hasBeenInitialized = true;
				_hasToUpdate = false;
			}
			// AK: if we have a DB datasource, then we might want to refetch if
			// the sort ordering changed
			// because if we have a fetch limit then the displayed matches on
			// the first page come from the
			// results, not from the real order in the DB. Set
			// "alwaysRefetchList" to false in your
			// rules to prevent that.
			// In addition, we need to refetch if we use a batching display
			// group, as the sort ordering is
			// always applied from the DB.
			if ((sortOrderings != null) && (ds instanceof EODatabaseDataSource)) {
				EOFetchSpecification fs = ((EODatabaseDataSource) ds).fetchSpecification();
				if (!fs.sortOrderings().equals(sortOrderings) && (fs.fetchLimit() != 0 || useBatchingDisplayGroup())) {
					fs.setSortOrderings(sortOrderings);
					_hasToUpdate = _hasToUpdate ? true : alwaysRefetchList();
				}
			}
			// this will have the side effect of resetting the batch # to sth
			// correct, in case
			// the current index if out of range
			log.debug("dg.currentBatchIndex() " + dg.currentBatchIndex());
			dg.setCurrentBatchIndex(dg.currentBatchIndex());
			if (listSize() > 0) {
				d2wContext().takeValueForKey(dg.allObjects().objectAtIndex(0), "object");
			}
		}
	}

	public boolean isEntityInspectable() {
		return ERXValueUtilities.booleanValueWithDefault(d2wContext().valueForKey("isEntityInspectable"), isEntityReadOnly());
		// return isEntityReadOnly() && (isEntityInspectable!=null &&
		// isEntityInspectable.intValue()!=0);
	}

	public boolean isEntityPrintable() {
		return ERXValueUtilities.booleanValueWithDefault(d2wContext().valueForKey("isEntityPrintable"), false);
	}

	public WOComponent deleteObjectAction() {
		String confirmDeleteConfigurationName = (String) d2wContext().valueForKey("confirmDeleteConfigurationName");
		ConfirmPageInterface nextPage;
		if (confirmDeleteConfigurationName == null) {
			log.warn("Using default delete template: ERD2WConfirmPageTemplate, set the 'confirmDeleteConfigurationName' key to something more sensible");
			nextPage = (ConfirmPageInterface) pageWithName("ERD2WConfirmPageTemplate");
		} else {
			nextPage = (ConfirmPageInterface) D2W.factory().pageForConfigurationNamed(confirmDeleteConfigurationName, session());
		}
		nextPage.setConfirmDelegate(new ERDDeletionDelegate(object(), dataSource(), context().page()));
		nextPage.setCancelDelegate(new ERDDeletionDelegate(null, null, context().page()));
		if (nextPage instanceof InspectPageInterface) {
			((InspectPageInterface) nextPage).setObject(object());
		} else {
			String message = ERXLocalizer.currentLocalizer().localizedTemplateStringForKeyWithObject("ERD2WList.confirmDeletionMessage", d2wContext());
			nextPage.setMessage(message);
		}
		return (WOComponent) nextPage;
	}

	public WOComponent editObjectAction() {
		EditPageInterface epi;
		String editConfigurationName = (String) d2wContext().valueForKey("editConfigurationName");
		log.debug("editConfigurationName: " + editConfigurationName);
		if (editConfigurationName != null) {
			epi = (EditPageInterface) D2W.factory().pageForConfigurationNamed(editConfigurationName, session());
		} else {
			epi = D2W.factory().editPageForEntityNamed(object().entityName(), session());
		}
		EOEnterpriseObject leo = localInstanceOfObject();
		epi.setObject(leo);
		epi.setNextPage(context().page());
		return (WOComponent) epi;
	}

	public WOComponent inspectObjectAction() {
		InspectPageInterface ipi;
		String inspectConfigurationName = (String) d2wContext().valueForKey("inspectConfigurationName");
		log.debug("inspectConfigurationName: " + inspectConfigurationName);
		if (inspectConfigurationName != null) {
			ipi = (InspectPageInterface) D2W.factory().pageForConfigurationNamed(inspectConfigurationName, session());
		} else {
			ipi = D2W.factory().inspectPageForEntityNamed(object().entityName(), session());
		}
		ipi.setObject(object());
		ipi.setNextPage(context().page());
		return (WOComponent) ipi;
	}

	protected EOEnterpriseObject localInstanceOfObject() {
		Object value = d2wContext().valueForKey("useNestedEditingContext");
		boolean createNestedContext = ERXValueUtilities.booleanValue(value);
		return ERXEOControlUtilities.editableInstanceOfObject(object(), createNestedContext);
	}

	/**
	 * Should we show the cancel button? It's only visible when we have a
	 * nextPage set up.
	 */
	public boolean showCancel() {
		return nextPage() != null;
	}

	/**
	 * Returns true of we are selecting, but not the top-level page.
	 * 
	 */
	public boolean isSelectingNotTopLevel() {
		boolean result = false;
		if (isSelecting() && (context().page() != this)) {
			result = true;
		}
		return result;
	}

	private String _formTargetJavaScriptUrl;

	public String formTargetJavaScriptUrl() {
		if (_formTargetJavaScriptUrl == null) {
			_formTargetJavaScriptUrl = application().resourceManager().urlForResourceNamed("formTarget.js", "ERDirectToWeb", null, context().request());
		}
		return _formTargetJavaScriptUrl;
	}

	public String targetString() {
		String result = "";
		NSDictionary targetDictionary = (NSDictionary) d2wContext().valueForKey("targetDictionary");
		if (targetDictionary != null) {
			StringBuffer buffer = new StringBuffer();
			buffer.append(targetDictionary.valueForKey("targetName") != null ? targetDictionary.valueForKey("targetName") : "foobar");
			buffer.append(":width=");
			buffer.append(targetDictionary.valueForKey("width") != null ? targetDictionary.valueForKey("width") : "{window.screen.width/2}");
			buffer.append(", height=");
			buffer.append(targetDictionary.valueForKey("height") != null ? targetDictionary.valueForKey("height") : "{myHeight}");
			buffer.append(",");
			buffer.append((targetDictionary.valueForKey("scrollbars") != null && targetDictionary.valueForKey("scrollbars") == "NO") ? " " : "scrollbars");
			buffer.append(", {(isResizable)?'resizable':''}, status");
			result = buffer.toString();
		} else {
			result = "foobar:width={window.screen.width/2}, height={myHeight}, scrollbars, {(isResizable)?'resizable':''}, status";
		}
		return result;
	}

	public boolean shouldShowSelectAll() {
		return listSize() > 10 || ERXValueUtilities.booleanValue(d2wContext().valueForKey("shouldShowSelectAll"));
	}

	public void warmUpForDisplay() {
		// default implementation does nothing
	}

	public String colorForRow() {
		String result = null;
		if (d2wContext().valueForKey("referenceRelationshipForBackgroupColor") != null) {
			String path = (String) d2wContext().valueForKey("referenceRelationshipForBackgroupColor") + ".backgroundColor";
			result = (String) object().valueForKeyPath(path);
		}
		return result;
	}

	public EOEnterpriseObject referenceEO;

	private NSArray _referenceEOs;

	public NSArray referenceEOs() {
		if (_referenceEOs == null) {
			String relationshipName = (String) d2wContext().valueForKey("referenceRelationshipForBackgroupColor");
			if (relationshipName != null) {
				EOEntity entity = EOModelGroup.defaultGroup().entityNamed(entityName());
				EORelationship relationship = entity.relationshipNamed(relationshipName);
				_referenceEOs = EOUtilities.objectsForEntityNamed(EOSharedEditingContext.defaultSharedEditingContext(), relationship.destinationEntity().name());
				_referenceEOs = ERXArrayUtilities.sortedArraySortedWithKey(_referenceEOs, "ordering", EOSortOrdering.CompareAscending);
			}
		}
		return _referenceEOs;
	}

}
