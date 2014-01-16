/*
 * Copyright (C) NetStruxr, Inc. All rights reserved.
 *
 * This software is published under the terms of the NetStruxr
 * Public Software License version 0.5, a copy of which has been
 * included with this distribution in the LICENSE.NPL file.  */
package er.directtoweb.pages;

import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.lang.reflect.Constructor;
import java.lang.reflect.InvocationTargetException;

import org.apache.commons.lang.ObjectUtils;
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
import com.webobjects.foundation.NSKeyValueCoding;
import com.webobjects.foundation.NSMutableArray;
import com.webobjects.foundation.NSNotification;
import com.webobjects.foundation.NSNotificationCenter;
import com.webobjects.foundation.NSSelector;

import er.directtoweb.ERD2WFactory;
import er.directtoweb.delegates.ERDDeletionDelegate;
import er.directtoweb.interfaces.ERDEditObjectDelegate;
import er.directtoweb.interfaces.ERDListPageInterface;
import er.extensions.appserver.ERXComponentActionRedirector;
import er.extensions.appserver.ERXDisplayGroup;
import er.extensions.appserver.ERXSession;
import er.extensions.batching.ERXBatchingDisplayGroup;
import er.extensions.eof.ERXConstant;
import er.extensions.eof.ERXEOAccessUtilities;
import er.extensions.eof.ERXEOControlUtilities;
import er.extensions.foundation.ERXArrayUtilities;
import er.extensions.foundation.ERXValueUtilities;
import er.extensions.localization.ERXLocalizer;
import er.extensions.statistics.ERXStats;

/**
 * Reimplementation of the D2WListPage. Descends from ERD2WPage instead of
 * D2WList.
 * 
 * @author ak
 * @d2wKey useBatchingDisplayGroup
 * @d2wKey isEntityEditable
 * @d2wKey readOnly
 * @d2wKey alwaysRefetchList
 * @d2wKey pageConfiguration
 * @d2wKey defaultBatchSize
 * @d2wKey subTask
 * @d2wKey checkSortOrderingKeys
 * @d2wKey defaultSortOrdering
 * @d2wKey displayPropertyKeys
 * @d2wKey restrictingFetchSpecification
 * @d2wKey isEntityInspectable
 * @d2wKey isEntityPrintable
 * @d2wKey confirmDeleteConfigurationName
 * @d2wKey editConfigurationName
 * @d2wKey inspectConfigurationName
 * @d2wKey useNestedEditingContext
 * @d2wKey targetDictionary
 * @d2wKey shouldShowSelectAll
 * @d2wKey referenceRelationshipForBackgroupColor
 * @d2wKey showBatchNavigation
 */
public class ERD2WListPage extends ERD2WPage implements ERDListPageInterface, SelectPageInterface, ERXComponentActionRedirector.Restorable {
	/**
	 * Do I need to update serialVersionUID?
	 * See section 5.6 <cite>Type Changes Affecting Serialization</cite> on page 51 of the 
	 * <a href="http://java.sun.com/j2se/1.4/pdf/serial-spec.pdf">Java Object Serialization Spec</a>
	 */
	private static final long serialVersionUID = 1L;

	/** logging support */
	public final static Logger log = Logger.getLogger(ERD2WListPage.class);
    
    protected boolean _shouldRefetch;
    
    protected String _sessionID;

	/**
	 * Public constructor. Registers for
	 * {@link EOEditingContext#EditingContextDidSaveChangesNotification} so that
	 * component stays informed when objects are deleted and added.
	 * 
	 * @param c
	 *            current context
	 */
	public ERD2WListPage(WOContext c) {
		super(c);
		_sessionID = c.session().sessionID();
		NSNotificationCenter.defaultCenter().addObserver(this, new NSSelector<Void>("editingContextDidSaveChanges", ERXConstant.NotificationClassArray), EOEditingContext.EditingContextDidSaveChangesNotification, null);
	}

	/* Not necessary -- NSNotificationCenter uses weak references
	public void finalize() throws Throwable {
		NSNotificationCenter.defaultCenter().removeObserver(this);
		super.finalize();
	}
	*/

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
	 * Cached session ID, so we don't need to awake.
	 */
	@Override
	public String sessionID() {
	    return _sessionID;
	}
	
	/**
	 * Called when an {@link EOEditingContext} has changed. Sets
	 * {@link #_hasToUpdate} which in turn lets the group refetch on the next
	 * display.
	 */
	public void editingContextDidSaveChanges(NSNotification notif) {
	    if (ObjectUtils.equals(sessionID(), ERXSession.currentSessionID())) {
	        _hasToUpdate = true;
	    }
	}

	/**
	 * Checks if the entity is read only, meaning that you can't edit it's
	 * objects.
	 */
	@Override
	public boolean isEntityReadOnly() {
		boolean flag = super.isEntityReadOnly();
		flag = !ERXValueUtilities.booleanValueWithDefault(d2wContext().valueForKey("isEntityEditable"), !flag);
		flag = ERXValueUtilities.booleanValueWithDefault(d2wContext().valueForKey("readOnly"), flag);
		return flag;
	}

	@Override
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
	@Override
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
	@Override
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
			if (shouldShowBatchNavigation()) {
			int batchSize = ERXValueUtilities.intValueWithDefault(d2wContext().valueForKey("defaultBatchSize"), 0);
			Object batchSizePref = userPreferencesValueForPageConfigurationKey("batchSize");
			if (batchSizePref != null) {
				if (log.isDebugEnabled()) {
						log.debug("batchSize User Preference: " + batchSizePref);
				}
				batchSize = ERXValueUtilities.intValueWithDefault(batchSizePref, batchSize);
			}
			_batchSize = ERXConstant.integerForInt(batchSize);
			} else {
				// We are not showing the batch nav, so we need to display all results.
				_batchSize = ERXConstant.ZeroInteger;
			}
		}
		return _batchSize.intValue();
	}

	// this can be overridden by subclasses for which sorting has to be fixed
	// (i.e. Grouping Lists)
	public boolean userPreferencesCanSpecifySorting() {
		return !"printerFriendly".equals(d2wContext().valueForKey("subTask"));
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

	@SuppressWarnings("unchecked")
	public NSArray<EOSortOrdering> sortOrderings() {
		NSArray<EOSortOrdering> sortOrderings = null;
		if (userPreferencesCanSpecifySorting()) {
			sortOrderings = (NSArray<EOSortOrdering>) userPreferencesValueForPageConfigurationKey("sortOrdering");
			if (log.isDebugEnabled()) {
			  log.debug("Found sort Orderings in user prefs " + sortOrderings);
			}
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

	public String defaultSortKey() {
		// the default D2W mechanism is completely disabled
		return null;
	}

	@Override
	public void takeValuesFromRequest(WORequest r, WOContext c) {
		setupPhase();
		super.takeValuesFromRequest(r, c);
	}

	protected void _fetchDisplayGroup(WODisplayGroup dg) {
        	String statsKey = super.makeStatsKey("DisplayGroup Fetch");
		ERXStats.markStart(ERXStats.Group.SQL, statsKey);
		try {
			dg.fetch();
		}
		catch (NSKeyValueCoding.UnknownKeyException e) {
			if (dg.sortOrderings() != null && dg.sortOrderings().count() > 0) {
				log.error("Fetching display group failed. Resetting potentially bogus sort orderings and trying again.", e);
				dg.setSortOrderings(null);
				dg.fetch();
			}
			else {
				throw e;
			}
		}
        	ERXStats.markEnd(ERXStats.Group.SQL, statsKey);
	}
		
	protected void fetchIfNecessary() {
		if (_hasToUpdate) {
			willUpdate();
			_fetchDisplayGroup(displayGroup());
			_hasToUpdate = false;
			didUpdate();
		}
	}
	
	@Override
	public WOActionResults invokeAction(WORequest r, WOContext c) {
		setupPhase();
		fetchIfNecessary();
		return super.invokeAction(r, c);
	}

	@Override
	public void appendToResponse(WOResponse r, WOContext c) {
		setupPhase();
		_rowFlip = true;
		fetchIfNecessary();

		// GN: reset the displayed batch if it is out of range
		if (displayGroup() != null && displayGroup().currentBatchIndex() > displayGroup().batchCount()) {
			displayGroup().setCurrentBatchIndex(1);
		}
		super.appendToResponse(r, c);
	}

	protected Object dataSourceState;

	@Override
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
			if (old != null && eodatasource != null && ObjectUtils.notEqual(eodatasource.classDescriptionForObjects(), old.classDescriptionForObjects())) {
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
				_fetchDisplayGroup(dg);
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
			if (listSize() > 0 && displayGroup().selectsFirstObjectAfterFetch()) {
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
	    WOComponent result = null;
		EditPageInterface epi;
		ERDEditObjectDelegate editObjectDelegate = null;
		String editConfigurationName = (String) d2wContext().valueForKey("editConfigurationName");
		EOEnterpriseObject leo = localInstanceOfObject();
		log.debug("editConfigurationName: " + editConfigurationName);
		if ((editObjectDelegate  = editObjectDelegateInstance()) != null) {
            result = editObjectDelegate.editObject(leo, context().page());
        }
		else {
    		if (editConfigurationName != null) {
    			epi = (EditPageInterface) D2W.factory().pageForConfigurationNamed(editConfigurationName, session());
    		} 
    		else {
    			epi = D2W.factory().editPageForEntityNamed(object().entityName(), session());
    		}
    		
    		epi.setObject(leo);
    		epi.setNextPage(context().page());
    		result = (WOComponent) epi;
		}
		return result;
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
	@Override
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
			StringBuilder buffer = new StringBuilder();
			buffer.append(targetDictionary.valueForKey("targetName") != null ? targetDictionary.valueForKey("targetName") : "foobar");
			buffer.append(":width=");
			buffer.append(targetDictionary.valueForKey("width") != null ? targetDictionary.valueForKey("width") : "{window.screen.width/2}");
			buffer.append(", height=");
			buffer.append(targetDictionary.valueForKey("height") != null ? targetDictionary.valueForKey("height") : "{myHeight}");
			buffer.append(',');
			buffer.append((targetDictionary.valueForKey("scrollbars") != null && targetDictionary.valueForKey("scrollbars") == "NO") ? " " : "scrollbars");
			buffer.append(", {(isResizable)?'resizable':''}, status");
			result = buffer.toString();
		} else {
			result = "foobar:width={window.screen.width/2}, height={myHeight}, scrollbars, {(isResizable)?'resizable':''}, status";
		}
		return result;
	}

	public boolean shouldShowSelectAll() {
		return ERXValueUtilities.booleanValueWithDefault(d2wContext().valueForKey("shouldShowSelectAll"), listSize() > 10);
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

	/**
	 * Determines if the batch navigation should be shown.	It can be explicitly disabled by setting the D2W key 
	 * <code>showBatchNavigation</code> to false.
	 * @return true if the batch navigation should be shown
	 */
	public boolean shouldShowBatchNavigation() {
		return ERXValueUtilities.booleanValueWithDefault(d2wContext().valueForKey("showBatchNavigation"), true);
	}
	
    /**
     * Attempts to instantiate the custom edit object delegate subclass, if one has been specified.
     */
    private ERDEditObjectDelegate editObjectDelegateInstance() {
        ERDEditObjectDelegate delegate = null;
        String delegateClassName = (String)d2wContext().valueForKey("editObjectDelegateClass");
        if (delegateClassName != null) { 
            try {
                Class delegateClass = Class.forName(delegateClassName);
                Constructor delegateClassConstructor = delegateClass.getConstructor(WOContext.class);
                delegate = (ERDEditObjectDelegate)delegateClassConstructor.newInstance(context());
            } catch (LinkageError le) {
                if (le instanceof ExceptionInInitializerError) {
                    log.warn("Could not initialize edit object delegate class: " + delegateClassName);
                } else {
                    log.warn("Could not load  delegate class: " + delegateClassName + " due to: " + le.getMessage());
                }
            } catch (ClassNotFoundException cnfe) {
                log.warn("Could not find class for edit object delegate: " + delegateClassName);
            } catch (NoSuchMethodException nsme) {
                log.warn("Could not find constructor for edit object delegate class: " + delegateClassName);
            } catch (SecurityException se) {
                log.warn("Insufficient privileges to access edit object delegate constructor: " + delegateClassName);
            } catch (IllegalAccessException iae) {
                log.warn("Insufficient access to create edit object delegate instance: " + iae.getMessage());
            } catch (IllegalArgumentException iae) {
                log.warn("Used an illegal argument when creating edit object delegate instance: " + iae.getMessage());
            } catch (InstantiationException ie) {
                log.warn("Could not instantiate edit object delegate instance: " + ie.getMessage());
            } catch (InvocationTargetException ite) {
                log.warn("Exception while invoking edit object delegate constructor: " + ite.getMessage());
            }
        }

        return delegate;
    }
    
	private void readObject(ObjectInputStream in) throws IOException, ClassNotFoundException {
		in.defaultReadObject();
		NSNotificationCenter.defaultCenter().addObserver(this, new NSSelector<Void>("editingContextDidSaveChanges", ERXConstant.NotificationClassArray), EOEditingContext.EditingContextDidSaveChangesNotification, null);
	}
	
	private void writeObject(ObjectOutputStream out) throws IOException {
		out.defaultWriteObject();
		NSNotificationCenter.defaultCenter().removeObserver(this, EOEditingContext.EditingContextDidSaveChangesNotification, null);
	}
}
