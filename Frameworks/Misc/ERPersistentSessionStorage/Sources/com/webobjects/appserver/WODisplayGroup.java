package com.webobjects.appserver;

import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.io.ObjectStreamField;
import java.io.Serializable;
import java.lang.reflect.Array;
import java.util.Enumeration;

import com.webobjects.eocontrol.EOAndQualifier;
import com.webobjects.eocontrol.EOArrayDataSource;
import com.webobjects.eocontrol.EOClassDescription;
import com.webobjects.eocontrol.EODataSource;
import com.webobjects.eocontrol.EODelayedObserverQueue;
import com.webobjects.eocontrol.EODetailDataSource;
import com.webobjects.eocontrol.EOEditingContext;
import com.webobjects.eocontrol.EOKeyValueArchiver;
import com.webobjects.eocontrol.EOKeyValueArchiving;
import com.webobjects.eocontrol.EOKeyValueQualifier;
import com.webobjects.eocontrol.EOKeyValueUnarchiver;
import com.webobjects.eocontrol.EOObjectStore;
import com.webobjects.eocontrol.EOObserverCenter;
import com.webobjects.eocontrol.EOObserverProxy;
import com.webobjects.eocontrol.EOQualifier;
import com.webobjects.eocontrol.EOSortOrdering;
import com.webobjects.eocontrol._EOFlatMutableDictionary;
import com.webobjects.eocontrol._EOMutableDefaultValueDictionary;
import com.webobjects.foundation.NSArray;
import com.webobjects.foundation.NSCoder;
import com.webobjects.foundation.NSComparator;
import com.webobjects.foundation.NSDictionary;
import com.webobjects.foundation.NSDisposable;
import com.webobjects.foundation.NSForwardException;
import com.webobjects.foundation.NSKeyValueCoding;
import com.webobjects.foundation.NSKeyValueCodingAdditions;
import com.webobjects.foundation.NSLog;
import com.webobjects.foundation.NSMutableArray;
import com.webobjects.foundation.NSMutableDictionary;
import com.webobjects.foundation.NSMutableSet;
import com.webobjects.foundation.NSNotification;
import com.webobjects.foundation.NSNotificationCenter;
import com.webobjects.foundation.NSSelector;
import com.webobjects.foundation.NSUndoManager;
import com.webobjects.foundation.NSValidation;
import com.webobjects.foundation._NSArrayUtilities;
import com.webobjects.foundation._NSDelegate;
import com.webobjects.foundation._NSUtilities;

/**
 * Reimplemented to fix NullPointerExceptions in deserialization.
 *
 */
public class WODisplayGroup implements NSKeyValueCoding, NSKeyValueCoding.ErrorHandling, NSDisposable,
		EOKeyValueArchiving, EOKeyValueArchiving.Awaking, Serializable {
	/**
	 * Do I need to update serialVersionUID? See section 5.6 <cite>Type Changes
	 * Affecting Serialization</cite> on page 51 of the <a
	 * href="http://java.sun.com/j2se/1.4/pdf/serial-spec.pdf">Java Object
	 * Serialization Spec</a>
	 */
	private static final long serialVersionUID = 1L;

	private static final NSSelector<Void> SetQualifier = new NSSelector<Void>("setQualifier",
			new Class[] { EOQualifier.class });
	private static final NSSelector<Void> SetAuxiliaryQualifier = new NSSelector<Void>("setAuxiliaryQualifier",
			new Class[] { EOQualifier.class });
	private static final NSSelector<Void> SetQualifierBindings = new NSSelector<Void>("setQualifierBindings",
			new Class[] { NSDictionary.class });

	private static NSSelector<Boolean> _selectObjects = new NSSelector<Boolean>("selectObjectsIdenticalTo",
			new Class[] { NSArray.class });
	private static NSSelector<Void> _insertObject = new NSSelector<Void>("_insertObjectAtIndex",
			new Class[] { Object.class });
	private static NSSelector<Boolean> _deleteObject = new NSSelector<Boolean>("_deleteObject",
			new Class[] { Object.class });

	private static final String shouldRefetch = "displayGroupShouldRefetchForInvalidatedAllObjects";
	private static final String shouldChangeSelection = "displayGroupShouldChangeSelectionToIndexes";
	private static final String didChangeSelectedObjects = "displayGroupDidChangeSelectedObjects";
	private static final String didChangeSelection = "displayGroupDidChangeSelection";
	private static final String shouldFetch = "displayGroupShouldFetch";
	private static final String didFetch = "displayGroupDidFetchObjects";
	private static final String displayArray = "displayGroupDisplayArrayForObjects";
	private static final String createObjectFailed = "displayGroupCreateObjectFailedForDataSource";
	private static final String shouldInsertObject = "displayGroupShouldInsertObject";
	private static final String didInsertObject = "displayGroupDidInsertObject";
	private static final String shouldDeleteObject = "displayGroupShouldDeleteObject";
	private static final String didDeleteObject = "displayGroupDidDeleteObject";
	private static final String didChangeDataSource = "displayGroupDidChangeDataSource";
	private static final String DataSourceFieldKey = "dataSource";
	private static final String DelegateFieldKey = "delegate";
	private static final String SortOrderingFieldKey = "sortOrdering";
	private static final String QualifierFieldKey = "qualifier";
	private static final String FlagsFieldKey = "flags";
	private static final String NumberObjectsPerBatchFieldKey = "numObjectsPerBatch";
	private static final String BatchIndexFieldKey = "batchIndex";
	private static final String LocalKeysFieldKey = "localKeys";
	private static final String AllObjectsFieldKey = "allObjects";
	private static final String DisplayedObjectsFieldKey = "displayedObjects";
	private static final String SelectionFieldKey = "selection";
	private static final ObjectStreamField[] serialPersistentFields = {
			new ObjectStreamField(DataSourceFieldKey, EODataSource.class),
			new ObjectStreamField(DelegateFieldKey, _NSDelegate.class),
			new ObjectStreamField(SortOrderingFieldKey, NSArray.class),
			new ObjectStreamField(QualifierFieldKey, EOQualifier.class),
			new ObjectStreamField(FlagsFieldKey, boolean[].class),
			new ObjectStreamField(NumberObjectsPerBatchFieldKey, Integer.TYPE),
			new ObjectStreamField(BatchIndexFieldKey, Integer.TYPE),
			new ObjectStreamField(LocalKeysFieldKey, NSArray.class),
			new ObjectStreamField(AllObjectsFieldKey, NSMutableArray.class),
			new ObjectStreamField(DisplayedObjectsFieldKey, NSMutableArray.class),
			new ObjectStreamField(SelectionFieldKey, NSArray.class) };
	private EODataSource _dataSource;
	private transient boolean _customDataSourceClass;
	private NSMutableArray _allObjects;
	private NSMutableArray _displayedObjects;
	private _NSDelegate _delegate;
	private NSArray _selection;
	private NSArray<EOSortOrdering> _sortOrdering;
	private EOQualifier _qualifier;
	private NSArray<String> _localKeys;
	private transient NSMutableArray _selectedObjects;
	private transient EOObserverProxy _observerNotificationBeginProxy;
	private transient EOObserverProxy _observerNotificationEndProxy;
	private transient int _updatedObjectIndex;
	private transient NSDictionary _insertedObjectDefaultValues;
	private transient NSMutableArray _savedAllObjects;
	private transient _EOFlatMutableDictionary _queryMatch;
	private transient _EOFlatMutableDictionary _queryMin;
	private transient _EOFlatMutableDictionary _queryMax;
	private transient _EOMutableDefaultValueDictionary _queryOperator;
	private transient String _defaultStringMatchOperator;
	private transient char[] _defaultStringMatchFormat;
	private transient int _DSMFindexOfObject;
	private transient NSMutableDictionary _queryBindings;
	private boolean _flags_selectsFirstObjectAfterFetch;
	private boolean _flags_autoFetch;
	private transient boolean _flags_haveFetched;
	private boolean _flags_validateImmediately;
	private boolean _flags_queryMode;
	private transient boolean _flags_initialized;
	private transient boolean _wasDisposed = false;
	int _numberOfObjectsPerBatch;
	int _batchIndex = 1;
	private static NSArray _stringQualifierOperators;
	private static NSArray _allQualifierOperators;
	private static final String QUERYOPERATOR_DEFAULTSTRING = "";
	private static final char QUERYOPERATOR_WILDCARD = '*';
	private static final String QUERYMATCH_DEFAULTSTRING = "%@*";
	private static String _globalDefaultStringMatchFormat = QUERYMATCH_DEFAULTSTRING;

	private static String _globalDefaultStringMatchOperator = "caseInsensitiveLike";

	private static boolean _globalDefaultForValidatesChangesImmediately = false;
	public static final String DisplayGroupWillFetchNotification = "WODisplayGroupWillFetch";

	public static String globalDefaultStringMatchOperator() {
		return _globalDefaultStringMatchOperator;
	}

	public static void setGlobalDefaultStringMatchOperator(String op) {
		_globalDefaultStringMatchOperator = op;
	}

	public static String globalDefaultStringMatchFormat() {
		return _globalDefaultStringMatchFormat;
	}

	public static void setGlobalDefaultStringMatchFormat(String format) {
		_globalDefaultStringMatchFormat = format;
	}

	public static boolean globalDefaultForValidatesChangesImmediately() {
		return _globalDefaultForValidatesChangesImmediately;
	}

	public static void setGlobalDefaultForValidatesChangesImmediately(boolean yn) {
		_globalDefaultForValidatesChangesImmediately = yn;
	}

	private void _init(boolean forSerialization) {
		if (!forSerialization) {
			_selection = NSArray.EmptyArray;
			_allObjects = new NSMutableArray();
			_displayedObjects = new NSMutableArray();
			_delegate = new _NSDelegate(Delegate.class);

			_numberOfObjectsPerBatch = 0;
			setCurrentBatchIndex(1);
		}

		_queryMatch = new _EOFlatMutableDictionary();
		_queryMax = new _EOFlatMutableDictionary();
		_queryMin = new _EOFlatMutableDictionary();
		_queryOperator = new _EOMutableDefaultValueDictionary();
		_queryOperator.setDefaultValue(QUERYOPERATOR_DEFAULTSTRING);
		_queryBindings = new NSMutableDictionary();
		_insertedObjectDefaultValues = NSDictionary.EmptyDictionary;
		setDefaultStringMatchFormat(_globalDefaultStringMatchFormat);
		_defaultStringMatchOperator = _globalDefaultStringMatchOperator;
		setSelectsFirstObjectAfterFetch(true);
	}

	private boolean _isCustomDataSourceClass(Class c) {
		return (c != EODetailDataSource.class) && (c != EOArrayDataSource.class);
	}

	private void _setUpForNewDataSource() {
		if (_dataSource != null) {
			_customDataSourceClass = _isCustomDataSourceClass(_dataSource.getClass());
			EOEditingContext editingContext = _dataSource.editingContext();

			if (editingContext != null) {
				NSNotificationCenter.defaultCenter().addObserver(this,
						new NSSelector<Void>("objectsChangedInEditingContext", new Class[] { NSNotification._CLASS }),
						EOEditingContext.ObjectsChangedInEditingContextNotification, editingContext);

				NSNotificationCenter.defaultCenter().addObserver(
						this,
						new NSSelector<Void>("objectsInvalidatedInEditingContext",
								new Class[] { NSNotification._CLASS }),
						EOObjectStore.InvalidatedAllObjectsInStoreNotification, editingContext);
			}
		}
	}

	protected void finishInitialization() {
		if (_flags_initialized) {
			return;
		}

		_setUpForNewDataSource();

		_flags_initialized = true;
	}

	public WODisplayGroup() {
		_init(false);
		finishInitialization();
	}

	public void dispose() {
		if (!_wasDisposed) {
			_delegate = null;
			NSNotificationCenter.defaultCenter().removeObserver(this);
			EOObserverCenter.removeObserver(_observerNotificationBeginProxy, this);
			EODelayedObserverQueue.defaultObserverQueue().dequeueObserver(_observerNotificationBeginProxy);
			_observerNotificationBeginProxy = null;
			EOObserverCenter.removeObserver(_observerNotificationEndProxy, this);
			EODelayedObserverQueue.defaultObserverQueue().dequeueObserver(_observerNotificationEndProxy);
			_observerNotificationEndProxy = null;
			if (undoManager() != null)
				undoManager().removeAllActionsWithTarget(this);
			setDataSource(null);

			_selection = null;
			_selectedObjects = null;
			_sortOrdering = null;
			_allObjects = null;
			_displayedObjects = null;
			_insertedObjectDefaultValues = null;

			_queryMin = null;
			_queryMax = null;
			_queryMatch = null;
			_queryOperator = null;
			_queryBindings = null;
			_savedAllObjects = null;
			_defaultStringMatchOperator = null;
			_defaultStringMatchFormat = null;
			_localKeys = null;
		}
		_wasDisposed = true;
	}

	public Object initWithCoder(NSCoder coder) {
		_init(true);
		setDataSource((EODataSource) coder.decodeObject());
		setDelegate(coder.decodeObject());
		setSortOrderings((NSArray) coder.decodeObject());
		setQualifier((EOQualifier) coder.decodeObject());

		_flags_autoFetch = coder.decodeBoolean();
		_flags_validateImmediately = coder.decodeBoolean();
		_flags_selectsFirstObjectAfterFetch = coder.decodeBoolean();
		_flags_queryMode = coder.decodeBoolean();
		_numberOfObjectsPerBatch = coder.decodeInt();
		setCurrentBatchIndex(coder.decodeInt());
		setLocalKeys((NSArray) coder.decodeObject());

		_allObjects = null;
		_displayedObjects = null;
		_selection = null;
		_selectedObjects = null;

		_allObjects = ((NSMutableArray) coder.decodeObject());
		_displayedObjects = ((NSMutableArray) coder.decodeObject());
		_selection = ((NSArray) coder.decodeObject());

		return this;
	}

	private void writeObject(ObjectOutputStream out) throws IOException {
		ObjectOutputStream.PutField fields = out.putFields();		
		fields.put(DataSourceFieldKey, _dataSource);
		fields.put(DelegateFieldKey, _delegate);
		fields.put(SortOrderingFieldKey, _sortOrdering);
		fields.put(QualifierFieldKey, _qualifier);
		boolean[] flags = { _flags_autoFetch, _flags_validateImmediately, _flags_selectsFirstObjectAfterFetch,
				_flags_queryMode };
		fields.put(FlagsFieldKey, flags);
		fields.put(NumberObjectsPerBatchFieldKey, _numberOfObjectsPerBatch);
		fields.put(LocalKeysFieldKey, _localKeys);
		fields.put(AllObjectsFieldKey, _allObjects);
		fields.put(DisplayedObjectsFieldKey, _displayedObjects);
		/*
		 * Read/Set batchIndex AFTER displayed objects as setting batch index
		 * tries to read displayed objects for count
		 */
		fields.put(BatchIndexFieldKey, currentBatchIndex());
		fields.put(SelectionFieldKey, _selection);
		out.writeFields();
	}

	private void readObject(ObjectInputStream in) throws IOException, ClassNotFoundException {
		_init(true);

		ObjectInputStream.GetField fields = in.readFields();
		_dataSource = ((EODataSource) fields.get(DataSourceFieldKey, null));
		_delegate = ((_NSDelegate) fields.get(DelegateFieldKey, null));
		_sortOrdering = ((NSArray) fields.get(SortOrderingFieldKey, null));
		_qualifier = ((EOQualifier) fields.get(QualifierFieldKey, null));
		boolean[] flags = (boolean[]) fields.get(FlagsFieldKey, null);
		if (flags != null) {
			int i = 0;
			_flags_autoFetch = flags[(i++)];
			_flags_validateImmediately = flags[(i++)];
			_flags_selectsFirstObjectAfterFetch = flags[(i++)];
			_flags_queryMode = flags[(i++)];
		}
		_numberOfObjectsPerBatch = fields.get(NumberObjectsPerBatchFieldKey, 0);
		_localKeys = ((NSArray<String>) fields.get(LocalKeysFieldKey, null));
		_allObjects = ((NSMutableArray) fields.get(AllObjectsFieldKey, null));
		_displayedObjects = ((NSMutableArray) fields.get(DisplayedObjectsFieldKey, null));
		/*
		 * Read/Set batchIndex AFTER displayed objects as setting batch index
		 * tries to read displayed objects for count
		 */
		setCurrentBatchIndex(fields.get(BatchIndexFieldKey, 1));
		_selection = ((NSArray) fields.get(SelectionFieldKey, null));

		finishInitialization();
	}

	public void encodeWithCoder(NSCoder coder) {
		coder.encodeObject(_dataSource);
		coder.encodeObject(_delegate);
		coder.encodeObject(_sortOrdering);
		coder.encodeObject(_qualifier);
		coder.encodeBoolean(_flags_autoFetch);
		coder.encodeBoolean(_flags_validateImmediately);
		coder.encodeBoolean(_flags_selectsFirstObjectAfterFetch);
		coder.encodeBoolean(_flags_queryMode);
		coder.encodeInt(_numberOfObjectsPerBatch);
		coder.encodeInt(currentBatchIndex());
		coder.encodeObject(localKeys());
		coder.encodeObject(_allObjects);
		coder.encodeObject(_displayedObjects);
		coder.encodeObject(_selection);
	}

	public boolean selectsFirstObjectAfterFetch() {
		return _flags_selectsFirstObjectAfterFetch;
	}

	public void setSelectsFirstObjectAfterFetch(boolean yn) {
		_flags_selectsFirstObjectAfterFetch = yn;
	}

	public void setValidatesChangesImmediately(boolean yn) {
		_flags_validateImmediately = yn;
	}

	public boolean validatesChangesImmediately() {
		return _flags_validateImmediately;
	}

	public void setFetchesOnLoad(boolean yn) {
		_flags_autoFetch = yn;
	}

	public boolean fetchesOnLoad() {
		return _flags_autoFetch;
	}

	public NSArray allObjects() {
		return _allObjects;
	}

	public NSArray displayedObjects() {
		if (_numberOfObjectsPerBatch == 0) {
			return _displayedObjects;
		}
		NSMutableArray objectsOfCurrentBatch = new NSMutableArray(_numberOfObjectsPerBatch);
		int e = 0;
		int numberOfRecords = _displayedObjects.count();
		int currentBatchIndex = currentBatchIndex();

		if (numberOfRecords > currentBatchIndex * _numberOfObjectsPerBatch) {
			numberOfRecords = currentBatchIndex * _numberOfObjectsPerBatch;
		}

		for (int i = (currentBatchIndex - 1) * _numberOfObjectsPerBatch; i < numberOfRecords; i++) {
			objectsOfCurrentBatch.insertObjectAtIndex(_displayedObjects.objectAtIndex(i), e++);
		}

		return objectsOfCurrentBatch;
	}

	public void setQualifier(EOQualifier qualifier) {
		_qualifier = qualifier;
	}

	public EOQualifier qualifier() {
		return _qualifier;
	}

	public void setSortOrderings(NSArray<EOSortOrdering> keySortOrderArray) {
		_sortOrdering = keySortOrderArray;
	}

	public NSArray<EOSortOrdering> sortOrderings() {
		return _sortOrdering;
	}

	public void updateDisplayedObjects() {
		NSMutableArray oldObjectSelection = (NSMutableArray) selectedObjects();
		NSArray newDisplayArray = _allObjects;

		if (_delegate.respondsTo(displayArray)) {
			newDisplayArray = (NSArray) _delegate.perform(displayArray, this, newDisplayArray);
		} else {
			if (_qualifier != null) {
				newDisplayArray = EOQualifier.filteredArrayWithQualifier(newDisplayArray, _qualifier);
			}
			if (_sortOrdering != null) {
				newDisplayArray = EOSortOrdering.sortedArrayUsingKeyOrderArray(newDisplayArray, _sortOrdering);
			}
		}

		_displayedObjects = new NSMutableArray(newDisplayArray);

		selectObjectsIdenticalToSelectFirstOnNoMatch(oldObjectSelection, false);

		redisplay();
	}

	public void setObjectArray(NSArray array) {
		NSMutableArray oldObjectSelection = (NSMutableArray) selectedObjects();

		_allObjects = (array != null ? new NSMutableArray(array) : new NSMutableArray());

		updateDisplayedObjects();

		selectObjectsIdenticalToSelectFirstOnNoMatch(oldObjectSelection, selectsFirstObjectAfterFetch());

		redisplay();
	}

	public void setDataSource(EODataSource ds) {
		if (_dataSource == ds) {
			return;
		}
		if (_dataSource != null) {
			EOEditingContext editingContext = _dataSource.editingContext();

			if (editingContext != null) {
				editingContext.removeEditor(this);
				if (editingContext.messageHandler() == this) {
					editingContext.setMessageHandler(null);
				}
			}
		}
		_dataSource = ds;
		_setUpForNewDataSource();

		setObjectArray(null);

		_notifyWith(didChangeDataSource, this);
	}

	public EODataSource dataSource() {
		return _dataSource;
	}

	public void setDelegate(Object anObject) {
		_delegate.setDelegate(anObject);
	}

	public Object delegate() {
		return _delegate.delegate();
	}

	public NSArray<String> localKeys() {
		if (_localKeys == null) {
			_localKeys = NSArray.emptyArray();
		}
		return _localKeys;
	}

	public void setLocalKeys(NSArray<String> newKeySet) {
		_localKeys = newKeySet;
	}

	public void objectsChangedInEditingContext(NSNotification notification) {
		boolean should = true;

		NSArray deletedObjects = (NSArray) notification.userInfo().objectForKey("deleted");
		int deletedCount = deletedObjects != null ? deletedObjects.count() : 0;
		if ((deletedObjects != null) && (deletedCount != 0)) {
			NSMutableSet allHash = new NSMutableSet(_allObjects);
			NSMutableSet displayedHash = null;
			NSMutableSet selectedHash = null;
			boolean rebuildAll = false;
			boolean rebuildDisplayed = false;
			boolean rebuildSelected = false;
			for (int i = 0; i < deletedCount; i++) {
				Object deletedObject = deletedObjects.objectAtIndex(i);
				if (allHash.containsObject(deletedObject)) {
					allHash.removeObject(deletedObject);
					rebuildAll = true;
					if (displayedHash == null) {
						displayedHash = new NSMutableSet(_displayedObjects);
					}
					if (displayedHash.containsObject(deletedObject)) {
						displayedHash.removeObject(deletedObject);
						rebuildDisplayed = true;
						if (selectedHash == null) {
							/*
							 * Updated to use lazy loading accessor method since
							 * _selectedObjects is null after deserialization,
							 * resulting in a NullPointerException in the
							 * NSMutableSet constructor
							 */
							selectedHash = new NSMutableSet(selectedObjects());
						}
						if (selectedHash.containsObject(deletedObject)) {
							selectedHash.removeObject(deletedObject);
							rebuildSelected = true;
						}
					}
				}
			}

			if (rebuildAll) {
				_allObjects = new NSMutableArray(allHash.allObjects());
				if (rebuildDisplayed) {
					for (int i = _displayedObjects.count() - 1; i >= 0; i--) {
						Object displayedObject = _displayedObjects.objectAtIndex(i);
						if ((displayedHash == null) || (!displayedHash.containsObject(displayedObject))) {
							_displayedObjects.removeObjectAtIndex(i);
						}
					}
					if ((selectedHash != null) && (rebuildSelected)) {
						_selectedObjects = new NSMutableArray(selectedHash.allObjects());
					}
					_selection = _NSArrayUtilities.indexesForObjectsIndenticalTo(_displayedObjects, _selectedObjects);
				}

			}

		}

		if ((should) || (deletedCount != 0))
			redisplay();
	}

	public void objectsInvalidatedInEditingContext(NSNotification notification) {
		boolean should = true;

		if (_delegate.respondsTo(shouldRefetch)) {
			should = _delegate.booleanPerform(shouldRefetch, this, notification);
		}

		if (should) {
			fetch();
		}
	}

	public NSUndoManager undoManager() {
		return (_dataSource != null) && (_dataSource.editingContext() != null) ? _dataSource.editingContext()
				.undoManager() : null;
	}

	private Object _notifyWith(String sel, Object arg) {
		if ((_delegate != null) && (_delegate.respondsTo(sel))) {
			_delegate.perform(sel, arg);
		}
		return this;
	}

	private Object _notifyWithWith(String sel, Object arg1, Object arg2) {
		if ((_delegate != null) && (_delegate.respondsTo(sel))) {
			_delegate.perform(sel, arg1, arg2);
		}
		return this;
	}

	private void _notifyRowChanged(int index) {
		if (_updatedObjectIndex != index) {
			_updatedObjectIndex = (_updatedObjectIndex == -2 ? index : -1);
		}

		willChange();
	}

	private void _notifySelectionChanged() {
		if (_delegate.respondsTo(didChangeSelection)) {
			_delegate.perform(didChangeSelection, this);
		}

		willChange();
	}

	public void _beginObserverNotification(Object sender) {
		if ((!_flags_haveFetched) && (_flags_autoFetch))
			fetch();
	}

	public void _lastObserverNotified(Object sender) {
		_updatedObjectIndex = -2;
		EOObserverCenter.notifyObserversObjectWillChange(null);
	}

	private int _selectionIndex() {
		if (_selection.count() != 0) {
			return ((Integer) _selection.objectAtIndex(0)).intValue();
		}
		return -1;
	}

	public NSArray selectionIndexes() {
		return _selection;
	}

	public NSArray selectedObjects() {
		if (_selectedObjects == null) {
			_selectedObjects = ((NSMutableArray) _NSArrayUtilities.objectsAtIndexes(_displayedObjects, _selection));
		}
		return _selectedObjects;
	}

	public void setSelectedObjects(NSArray objects) {
		_selectedObjects = new NSMutableArray(objects);
		_selection = _NSArrayUtilities.indexesForObjectsIndenticalTo(_displayedObjects, _selectedObjects);
	}

	public Object selectedObject() {
		NSArray selectedObjects = selectedObjects();
		if (selectedObjects.count() != 0) {
			return selectedObjects.objectAtIndex(0);
		}
		return null;
	}

	public void setSelectedObject(Object anObject) {
		if (anObject != null)
			setSelectedObjects(new NSArray(anObject));
		else
			clearSelection();
	}

	@Deprecated
	public boolean endEditing() {
		return true;
	}

	public boolean setSelectionIndexes(NSArray s) {
		NSArray sortedArray = null;

		if (s.count() > 1)
			try {
				sortedArray = s.sortedArrayUsingComparator(NSComparator.AscendingNumberComparator);
			} catch (NSComparator.ComparisonException exception) {
				throw NSForwardException._runtimeExceptionForThrowable(exception);
			}
		else if (_displayedObjects.count() < 1)
			sortedArray = NSArray.EmptyArray;
		else {
			sortedArray = new NSArray(s);
		}

		NSMutableArray newSelectedObjects = (NSMutableArray) _NSArrayUtilities.objectsAtIndexes(_displayedObjects,
				sortedArray);
		boolean selectedObjectsChanged = !newSelectedObjects.equals(selectedObjects());

		boolean selectionIndexesChanged = !sortedArray.equals(_selection);

		if ((!selectedObjectsChanged) && (!selectionIndexesChanged)) {
			return true;
		}

		if (!endEditing()) {
			return false;
		}

		if ((_delegate.respondsTo(shouldChangeSelection))
				&& (!_delegate.booleanPerform(shouldChangeSelection, this, s))) {
			return false;
		}

		if (selectionIndexesChanged) {
			_selection = sortedArray;
		}

		if (selectedObjectsChanged) {
			_selectedObjects = newSelectedObjects;
		}

		if ((selectedObjectsChanged) && (_delegate.respondsTo(didChangeSelectedObjects))) {
			_delegate.perform(didChangeSelectedObjects, this);
		}

		if ((selectedObjectsChanged) || (selectionIndexesChanged)) {
			_notifySelectionChanged();
		}

		return true;
	}

	public boolean selectObject(Object object) {
		return selectObjectsIdenticalTo(new NSArray(object));
	}

	public boolean selectObjectsIdenticalTo(NSArray objectSelection) {
		NSArray newSelection = _NSArrayUtilities.indexesForObjectsIndenticalTo(_displayedObjects, objectSelection);
		boolean s = setSelectionIndexes(newSelection);
		if ((objectSelection.count() > 0) && (newSelection.count() == 0)) {
			return false;
		}
		return s;
	}

	public boolean selectObjectsIdenticalToSelectFirstOnNoMatch(NSArray objectSelection, boolean selectFirstOnNoMatch) {
		NSArray newSelection = _NSArrayUtilities.indexesForObjectsIndenticalTo(_displayedObjects, objectSelection);
		if (newSelection.count() == 0) {
			if ((selectFirstOnNoMatch) && (_displayedObjects.count() != 0)) {
				newSelection = new NSArray(Integer.valueOf(0));
			} else {
				newSelection = _NSArrayUtilities.closestMatchingIndexes(_selection, _displayedObjects.count(),
						selectFirstOnNoMatch);
			}
		}
		return setSelectionIndexes(newSelection);
	}

	public Object selectNext() {
		if (_displayedObjects.count() == 0)
			return null;
		Integer newIndex;
		if (_selection.count() == 0) {
			newIndex = _NSUtilities.IntegerForInt(0);
		} else {
			Integer firstIndex = (Integer) _selection.objectAtIndex(0);
			if (firstIndex.intValue() == _displayedObjects.count() - 1)
				newIndex = _NSUtilities.IntegerForInt(0);
			else {
				newIndex = _NSUtilities.IntegerForInt(firstIndex.intValue() + 1);
			}
		}
		NSArray newSelection = new NSArray(newIndex);
		setSelectionIndexes(newSelection);
		return null;
	}

	public Object selectPrevious() {
		if (_displayedObjects.count() == 0)
			return null;
		Integer newIndex;
		if (_selection.count() == 0) {
			newIndex = _NSUtilities.IntegerForInt(0);
		} else {
			Integer firstIndex = (Integer) _selection.objectAtIndex(0);
			if (firstIndex.intValue() == 0)
				newIndex = _NSUtilities.IntegerForInt(_displayedObjects.count() - 1);
			else {
				newIndex = _NSUtilities.IntegerForInt(firstIndex.intValue() - 1);
			}
		}
		NSArray newSelection = new NSArray(newIndex);

		setSelectionIndexes(newSelection);
		return null;
	}

	public boolean clearSelection() {
		return setSelectionIndexes(NSArray.EmptyArray);
	}

	@Deprecated
	public void redisplay() {
		_notifyRowChanged(-1);
	}

	public boolean _deleteObject(Object anObject) {
		if ((_delegate != null) && (_delegate.respondsTo(shouldDeleteObject))
				&& (!_delegate.booleanPerform(shouldDeleteObject, this, anObject))) {
			return false;
		}
		try {
			if (_dataSource != null)
				_dataSource.deleteObject(anObject);
		} catch (Throwable localException) {
			NSLog._conditionallyLogPrivateException(localException);
			_presentAlertWithTitleMessage("Error Deleting Object", localException.toString());
			return false;
		}

		if (undoManager() != null) {
			undoManager().registerUndoWithTarget(this, _selectObjects, selectedObjects());
			undoManager().registerUndoWithTarget(
					this,
					_insertObject,
					new Object[] { anObject,
							_NSUtilities.IntegerForInt(_displayedObjects.indexOfIdenticalObject(anObject)) });
		}

		_displayedObjects.removeIdenticalObject(anObject);
		_allObjects.removeIdenticalObject(anObject);

		selectObjectsIdenticalToSelectFirstOnNoMatch(selectedObjects(), false);
		_notifyWithWith(didDeleteObject, this, anObject);
		redisplay();

		return true;
	}

	private boolean _deleteObjectsAtIndexes(NSArray indexes) {
		int count = indexes.count();
		NSMutableArray objects = new NSMutableArray(count);
		for (int i = 0; i < count; i++) {
			objects.addObject(_displayedObjects.objectAtIndex(((Integer) indexes.objectAtIndex(i)).intValue()));
		}

		boolean delectionSucceeded = true;
		for (int i = 0; i < count; i++) {
			if (!_deleteObject(objects.objectAtIndex(i))) {
				delectionSucceeded = false;
			}
		}
		return delectionSucceeded;
	}

	public boolean deleteSelection() {
		endEditing();

		return _deleteObjectsAtIndexes(selectionIndexes());
	}

	public boolean deleteObjectAtIndex(int anIndex) {
		endEditing();

		return _deleteObject(_allObjects.objectAtIndex(anIndex));
	}

	public void setInsertedObjectDefaultValues(NSDictionary defaultValues) {
		if (defaultValues == null) {
			throw new IllegalArgumentException(
					"The inserted object default values dictionary on a WODisplayGroup may not be set to NULL.  Try NSDictionary.EmptyDictionary instead.");
		}
		_insertedObjectDefaultValues = defaultValues;
	}

	public NSDictionary insertedObjectDefaultValues() {
		return _insertedObjectDefaultValues;
	}

	public void _insertObjectAtIndex(Object args) {
		Object object = Array.get(args, 0);
		int index = ((Integer) Array.get(args, 1)).intValue();

		insertObjectAtIndex(object, index);
	}

	public void insertObjectAtIndex(Object createObject, int newIndex) {
		if (!endEditing()) {
			return;
		}

		if (newIndex > _displayedObjects.count()) {
			throw new IllegalArgumentException("WODisplayGroup::insertObjectAtIndex() " + newIndex
					+ " beyond the bounds of " + _displayedObjects.count());
		}

		if ((_delegate.respondsTo(shouldInsertObject))
				&& (!_delegate.booleanPerform(shouldInsertObject, this, createObject,
						_NSUtilities.IntegerForInt(newIndex)))) {
			return;
		}

		try {
			if (_dataSource != null)
				_dataSource.insertObject(createObject);
		} catch (Throwable localException) {
			NSLog._conditionallyLogPrivateException(localException);
			_presentAlertWithTitleMessage("Error Inserting Object", localException.toString());
			return;
		}

		if (undoManager() != null) {
			try {
				undoManager().registerUndoWithTarget(this, _selectObjects, selectedObjects());
				undoManager().registerUndoWithTarget(this, _deleteObject, createObject);
			} catch (Exception e) {
				throw NSForwardException._runtimeExceptionForThrowable(e);
			}
		}

		_displayedObjects.insertObjectAtIndex(createObject, newIndex);
		_allObjects.insertObjectAtIndex(createObject, newIndex);
		redisplay();

		_notifyWithWith(didInsertObject, this, createObject);

		selectObjectsIdenticalTo(new NSArray(createObject));
	}

	public Object insertNewObjectAtIndex(int newIndex) {
		if (!endEditing()) {
			return null;
		}

		Object createObject = _dataSource != null ? _dataSource.createObject() : null;

		if (createObject == null) {
			if (_delegate.respondsTo(createObjectFailed))
				_delegate.perform(createObjectFailed, this, _dataSource);
			else {
				_presentAlertWithTitleMessage("", "Data source unable to provide new object.");
			}
			return null;
		}
		NSArray keysWithDefaultValue = _insertedObjectDefaultValues.allKeys();

		int count = keysWithDefaultValue.count();

		while (count-- > 0) {
			String key = (String) keysWithDefaultValue.objectAtIndex(count);
			NSKeyValueCodingAdditions.Utility.takeValueForKeyPath(createObject,
					_insertedObjectDefaultValues.valueForKey(key), key);
		}

		insertObjectAtIndex(createObject, newIndex);
		return createObject;
	}

	public Object fetch() {
		_flags_haveFetched = true;

		if (_dataSource == null) {
			return null;
		}
		if (!endEditing()) {
			return null;
		}

		if ((_delegate.respondsTo(shouldFetch)) && (!_delegate.booleanPerform(shouldFetch, this))) {
			return null;
		}

		NSNotificationCenter.defaultCenter().postNotification("WODisplayGroupWillFetch", this);

		if (undoManager() != null) {
			undoManager().removeAllActionsWithTarget(this);
		}

		if ((_customDataSourceClass) && (SetQualifierBindings.implementedByObject(_dataSource))) {
			try {
				SetQualifierBindings.invoke(_dataSource, _queryBindings);
			} catch (Exception e) {
				throw NSForwardException._runtimeExceptionForThrowable(e);
			}

		}

		NSArray anArray = _dataSource.fetchObjects();

		setObjectArray(anArray);

		_notifyWithWith(didFetch, this, _allObjects);

		return null;
	}

	private EOQualifier _qualifierForKeyValueOperator(String key, Object value, NSSelector op) {
		Object aValue = value;
		try {
			EOClassDescription classDescription = _dataSource != null ? _dataSource.classDescriptionForObjects() : null;
			aValue = classDescription != null ? classDescription.validateValueForKey(aValue, key) : aValue;
		} catch (NSValidation.ValidationException e) {
			NSLog._conditionallyLogPrivateException(e);
		}
		NSSelector knownOp;
		if (op == EOQualifier.QualifierOperatorEqual) {
			String operatorString = (String) _queryOperator.objectForKey(key);

			if (operatorString == null) {
				operatorString = QUERYOPERATOR_DEFAULTSTRING;
			}

			if (!(aValue instanceof String)) {
				if (operatorString.compareTo(QUERYOPERATOR_DEFAULTSTRING) == 0)
					operatorString = "=";
			} else {
				String valueString = (String) aValue;
				int length = valueString.length();
				if (length == 0) {
					return null;
				}

				if (operatorString.compareTo("is") == 0) {
					operatorString = "=";
				} else {
					StringBuilder buffer = new StringBuilder(length + 4);

					if (operatorString.compareTo(QUERYOPERATOR_DEFAULTSTRING) == 0) {
						if (_DSMFindexOfObject > 0) {
							buffer.append(_defaultStringMatchFormat, 0, _DSMFindexOfObject);
						}
						buffer.append(valueString);
						int i = _DSMFindexOfObject + 2;
						if (i < _defaultStringMatchFormat.length) {
							buffer.append(_defaultStringMatchFormat, i, _defaultStringMatchFormat.length - i);
						}
						aValue = buffer.toString();
						operatorString = _defaultStringMatchOperator;
					} else if (operatorString.compareTo("starts with") == 0) {
						buffer.append(valueString);
						buffer.append(QUERYOPERATOR_WILDCARD);
						aValue = buffer.toString();
						operatorString = _defaultStringMatchOperator;
					} else if (operatorString.compareTo("ends with") == 0) {
						buffer.append(QUERYOPERATOR_WILDCARD);
						buffer.append(valueString);
						aValue = buffer.toString();
						operatorString = _defaultStringMatchOperator;
					} else if (operatorString.compareTo("contains") == 0) {
						buffer.append(QUERYOPERATOR_WILDCARD);
						buffer.append(valueString);
						buffer.append(QUERYOPERATOR_WILDCARD);
						aValue = buffer.toString();
						operatorString = _defaultStringMatchOperator;
					}

					buffer = null;
				}
			}
			knownOp = EOQualifier.operatorSelectorForString(operatorString);
		} else {
			knownOp = op;
		}

		if (knownOp == null) {
			NSLog.err.appendln("***Error: unknown operator. Discarding query parameter " + key + " "
					+ _queryOperator.objectForKey(key) + " " + aValue);
			return null;
		}

		return new EOKeyValueQualifier(key, knownOp, aValue);
	}

	private void _addQualifiersToArrayForValuesOperator(NSMutableArray qualifiers, NSDictionary values, NSSelector op) {
		Enumeration enumerator = values.keyEnumerator();

		while (enumerator.hasMoreElements()) {
			String key = (String) enumerator.nextElement();

			Object value = values.valueForKey(key);
			if (value != null) {
				EOQualifier qualifier = _qualifierForKeyValueOperator(key, value, op);
				if (qualifier != null)
					qualifiers.addObject(qualifier);
			}
		}
	}

	public String defaultStringMatchOperator() {
		return _defaultStringMatchOperator;
	}

	public void setDefaultStringMatchOperator(String op) {
		_defaultStringMatchOperator = op;
	}

	public String defaultStringMatchFormat() {
		return new String(_defaultStringMatchFormat);
	}

	public void setDefaultStringMatchFormat(String format) {
		String aFormat = format != null ? format : QUERYMATCH_DEFAULTSTRING;

		int i = aFormat.indexOf("%@");
		if (i < 0) {
			throw new IllegalArgumentException("DisplayGroup.setDefaultStringMatchFormat() : illegal format string \""
					+ aFormat + "\".  It must contain \"%@\".");
		}
		_defaultStringMatchFormat = aFormat.toCharArray();
		_DSMFindexOfObject = i;
	}

	public EOQualifier qualifierFromQueryValues() {
		NSMutableArray qualifiers = new NSMutableArray();

		_addQualifiersToArrayForValuesOperator(qualifiers, _queryMax, EOQualifier.QualifierOperatorLessThanOrEqualTo);
		_addQualifiersToArrayForValuesOperator(qualifiers, _queryMin, EOQualifier.QualifierOperatorGreaterThanOrEqualTo);
		_addQualifiersToArrayForValuesOperator(qualifiers, _queryMatch, EOQualifier.QualifierOperatorEqual);

		int count = qualifiers.count();
		if (count == 0)
			return null;
		if (count == 1) {
			return (EOQualifier) qualifiers.objectAtIndex(0);
		}
		return new EOAndQualifier(qualifiers);
	}

	public void qualifyDisplayGroup() {
		setInQueryMode(false);
		setQualifier(qualifierFromQueryValues());
		updateDisplayedObjects();
	}

	public void qualifyDataSource() {
		endEditing();
		setInQueryMode(false);

		NSSelector qualifierSetter = null;

		if (_customDataSourceClass) {
			if (SetAuxiliaryQualifier.implementedByObject(_dataSource))
				qualifierSetter = SetAuxiliaryQualifier;
			else if (SetQualifier.implementedByObject(_dataSource)) {
				qualifierSetter = SetQualifier;
			}
		}

		if (qualifierSetter != null) {
			try {
				qualifierSetter.invoke(_dataSource, qualifierFromQueryValues());
			} catch (Exception e) {
				throw NSForwardException._runtimeExceptionForThrowable(e);
			}
		}

		fetch();

		setCurrentBatchIndex(1);
	}

	@Deprecated
	public void setInQueryMode(boolean yn) {
		if (yn != inQueryMode())
			if (yn) {
				_savedAllObjects = _allObjects;
				setObjectArray(new NSArray(_queryMatch));
				selectObject(_queryMatch);
			} else {
				NSArray oldArray = _savedAllObjects;
				_savedAllObjects = null;
				setObjectArray(oldArray);
			}
	}

	@Deprecated
	public boolean inQueryMode() {
		return _savedAllObjects != null;
	}

	private void _presentAlertWithTitleMessage(String title, String message) {
		NSLog.err.appendln("<" + getClass().getName() + " " + title + ":" + message + "");
	}

	public void encodeWithKeyValueArchiver(EOKeyValueArchiver archiver) {
		archiver.encodeInt(_numberOfObjectsPerBatch, "numberOfObjectsPerBatch");
		archiver.encodeBool(fetchesOnLoad(), "fetchesOnLoad");
		archiver.encodeBool(validatesChangesImmediately(), "validatesChangesImmediately");
		archiver.encodeBool(selectsFirstObjectAfterFetch(), "selectsFirstObjectAfterFetch");
		archiver.encodeObject(localKeys(), LocalKeysFieldKey);
		archiver.encodeObject(_dataSource, DataSourceFieldKey);
		archiver.encodeObject(_sortOrdering, SortOrderingFieldKey);
		archiver.encodeObject(_qualifier, QualifierFieldKey);
		archiver.encodeObject(defaultStringMatchFormat(), "formatForLikeQualifier");
		archiver.encodeObject(_insertedObjectDefaultValues, "insertedObjectDefaultValues");
	}

	public static Object decodeWithKeyValueUnarchiver(EOKeyValueUnarchiver unarchiver) {
		return new WODisplayGroup(unarchiver);
	}

	private WODisplayGroup(EOKeyValueUnarchiver unarchiver) {
		_init(false);
		setNumberOfObjectsPerBatch(unarchiver.decodeIntForKey("numberOfObjectsPerBatch"));
		setFetchesOnLoad(unarchiver.decodeBoolForKey("fetchesOnLoad"));
		setValidatesChangesImmediately(unarchiver.decodeBoolForKey("validatesChangesImmediately"));
		setSelectsFirstObjectAfterFetch(unarchiver.decodeBoolForKey("selectsFirstObjectAfterFetch"));
		setLocalKeys((NSArray) unarchiver.decodeObjectForKey(LocalKeysFieldKey));
		_dataSource = ((EODataSource) unarchiver.decodeObjectForKey(DataSourceFieldKey));
		setSortOrderings((NSArray) unarchiver.decodeObjectForKey(SortOrderingFieldKey));
		setQualifier((EOQualifier) unarchiver.decodeObjectForKey(QualifierFieldKey));
		setDefaultStringMatchFormat((String) unarchiver.decodeObjectForKey("formatForLikeQualifier"));

		NSDictionary temp = (NSDictionary) unarchiver.decodeObjectForKey("insertedObjectDefaultValues");
		if (temp == null) {
			temp = NSDictionary.EmptyDictionary;
		}
		setInsertedObjectDefaultValues(temp);
		finishInitialization();
	}

	public void awakeFromKeyValueUnarchiver(EOKeyValueUnarchiver unarchiver) {
		if (_dataSource != null) {
			unarchiver.ensureObjectAwake(_dataSource);
		}
		if (fetchesOnLoad())
			fetch();
	}

	public NSArray relationalQualifierOperators() {
		return EOQualifier.relationalQualifierOperators();
	}

	public NSArray allQualifierOperators() {
		return _allQualifierOperators;
	}

	public NSArray stringQualifierOperators() {
		return _stringQualifierOperators;
	}

	public void setNumberOfObjectsPerBatch(int count) {
		if (count < 0) {
			throw new IllegalArgumentException("WODisplayGroup.setNumberOfObjectsPerBatch(): invalid count " + count);
		}
		if (_numberOfObjectsPerBatch != count) {
			clearSelection();
		}
		_numberOfObjectsPerBatch = count;

		_batchIndex = 1;
	}

	public int numberOfObjectsPerBatch() {
		return _numberOfObjectsPerBatch;
	}

	public Object displayNextBatch() {
		if (_numberOfObjectsPerBatch == 0) {
			return null;
		}
		setCurrentBatchIndex(_batchIndex + 1);
		clearSelection();

		return null;
	}

	public Object displayPreviousBatch() {
		if (_numberOfObjectsPerBatch == 0) {
			return null;
		}
		setCurrentBatchIndex(_batchIndex - 1);
		clearSelection();

		return null;
	}

	public int batchCount() {
		if (_numberOfObjectsPerBatch == 0) {
			return 0;
		}
		if (_displayedObjects.count() == 0) {
			return 1;
		}

		return (_displayedObjects.count() - 1) / _numberOfObjectsPerBatch + 1;
	}

	public boolean hasMultipleBatches() {
		return batchCount() > 1;
	}

	public int currentBatchIndex() {
		return _batchIndex;
	}

	public void setCurrentBatchIndex(int batchIndex) {
		if (_numberOfObjectsPerBatch == 0) {
			return;
		}
		int bc = batchCount();

		if (batchIndex > bc)
			_batchIndex = 1;
		else if (batchIndex < 1)
			_batchIndex = (bc > 0 ? bc : 1);
		else
			_batchIndex = batchIndex;
	}

	public int indexOfFirstDisplayedObject() {
		return _numberOfObjectsPerBatch * (currentBatchIndex() - 1) + 1;
	}

	public int indexOfLastDisplayedObject() {
		int computedEnd = _numberOfObjectsPerBatch * currentBatchIndex();
		int realEnd = allObjects().count();
		if (_numberOfObjectsPerBatch == 0)
			return realEnd;
		return realEnd < computedEnd ? realEnd : computedEnd;
	}

	public Object displayBatchContainingSelectedObject() {
		int batchIndex = 1;
		int index = _selectionIndex();

		if (batchCount() > 0) {
			batchIndex = index / _numberOfObjectsPerBatch + 1;
		}

		if (_batchIndex != batchIndex) {
			setCurrentBatchIndex(batchIndex);
			return null;
		}
		return "";
	}

	public NSMutableDictionary queryMatch() {
		return _queryMatch;
	}

	public NSMutableDictionary queryOperator() {
		return _queryOperator;
	}

	public NSMutableDictionary queryMax() {
		return _queryMax;
	}

	public NSMutableDictionary queryMin() {
		return _queryMin;
	}

	public NSMutableDictionary queryBindings() {
		return _queryBindings;
	}

	@Deprecated
	public void editingContextPresentErrorMessage(EOEditingContext editingContext, String message) {
		_presentAlertWithTitleMessage("EditingContext Error", message);
	}

	public Object insert() {
		int index = _selectionIndex();

		if (index < 0)
			insertNewObjectAtIndex(_displayedObjects.count());
		else {
			insertNewObjectAtIndex(index + 1);
		}
		displayBatchContainingSelectedObject();
		return null;
	}

	public Object delete() {
		deleteSelection();
		displayBatchContainingSelectedObject();
		return null;
	}

	public String detailKey() {
		if (hasDetailDataSource()) {
			return ((EODetailDataSource) _dataSource).detailKey();
		}
		return null;
	}

	public void setDetailKey(String detailKey) {
		if (hasDetailDataSource())
			((EODetailDataSource) _dataSource).setDetailKey(detailKey);
	}

	public Object masterObject() {
		if (hasDetailDataSource()) {
			return ((EODetailDataSource) _dataSource).masterObject();
		}
		return null;
	}

	public void setMasterObject(Object masterObject) {
		String detailKey = detailKey();
		if (detailKey != null) {
			_dataSource.qualifyWithRelationshipKey(detailKey, masterObject);
			if (fetchesOnLoad())
				fetch();
		}
	}

	public boolean hasDetailDataSource() {
		return _dataSource instanceof EODetailDataSource;
	}

	public void willChange() {
		EOObserverCenter.notifyObserversObjectWillChange(this);
		EOObserverCenter.notifyObserversObjectWillChange(null);
	}

	public Object valueForKey(String key) {
		return NSKeyValueCoding.DefaultImplementation.valueForKey(this, key);
	}

	public void takeValueForKey(Object value, String key) {
		NSKeyValueCoding.DefaultImplementation.takeValueForKey(this, value, key);
	}

	public Object handleQueryWithUnboundKey(String key) {
		return NSKeyValueCoding.DefaultImplementation.handleQueryWithUnboundKey(this, key);
	}

	public void handleTakeValueForUnboundKey(Object value, String key) {
		NSKeyValueCoding.DefaultImplementation.handleTakeValueForUnboundKey(this, value, key);
	}

	public void unableToSetNullForKey(String key) {
		if (key.equals("numberOfObjectsPerBatch"))
			setNumberOfObjectsPerBatch(0);
		else
			NSKeyValueCoding.DefaultImplementation.unableToSetNullForKey(this, key);
	}

	@Override
	public String toString() {
		StringBuilder sb = new StringBuilder();
		sb.append('<');
		sb.append(getClass().getName());
		sb.append(" dataSource=");
		sb.append(_dataSource);
		sb.append(" delegate=");
		sb.append(_delegate);
		sb.append(" sortOrdering=");
		sb.append(_sortOrdering);
		sb.append(" qualifier=");
		sb.append(_qualifier);
		sb.append(" localKeys=");
		sb.append(_localKeys);
		sb.append(" insertedObjectDefaultValues=");
		sb.append(_insertedObjectDefaultValues);
		sb.append(" numberOfObjectsPerBatch=");
		sb.append(_numberOfObjectsPerBatch);
		sb.append('>');
		return sb.toString();
	}

	static {
		_stringQualifierOperators = new NSArray(new String[] { "starts with", "contains", "ends with", "is", "like" });
		_allQualifierOperators = _stringQualifierOperators.arrayByAddingObjectsFromArray(EOQualifier
				.relationalQualifierOperators());
	}

	public static abstract interface Delegate {
		public abstract boolean displayGroupShouldRefetchForInvalidatedAllObjects(WODisplayGroup paramWODisplayGroup,
				NSNotification paramNSNotification);

		public abstract boolean displayGroupShouldChangeSelectionToIndexes(WODisplayGroup paramWODisplayGroup,
				NSArray paramNSArray);

		public abstract void displayGroupDidChangeSelectedObjects(WODisplayGroup paramWODisplayGroup);

		public abstract void displayGroupDidChangeSelection(WODisplayGroup paramWODisplayGroup);

		public abstract boolean displayGroupShouldFetch(WODisplayGroup paramWODisplayGroup);

		public abstract void displayGroupDidFetchObjects(WODisplayGroup paramWODisplayGroup, NSArray paramNSArray);

		public abstract NSArray displayGroupDisplayArrayForObjects(WODisplayGroup paramWODisplayGroup,
				NSArray paramNSArray);

		public abstract void displayGroupCreateObjectFailedForDataSource(WODisplayGroup paramWODisplayGroup,
				EODataSource paramEODataSource);

		public abstract boolean displayGroupShouldInsertObject(WODisplayGroup paramWODisplayGroup, Object paramObject,
				int paramInt);

		public abstract void displayGroupDidInsertObject(WODisplayGroup paramWODisplayGroup, Object paramObject);

		public abstract boolean displayGroupShouldDeleteObject(WODisplayGroup paramWODisplayGroup, Object paramObject);

		public abstract void displayGroupDidDeleteObject(WODisplayGroup paramWODisplayGroup, Object paramObject);

		public abstract void displayGroupDidChangeDataSource(WODisplayGroup paramWODisplayGroup);

		@Deprecated
		public abstract boolean displayGroupShouldDisplayAlert(WODisplayGroup paramWODisplayGroup, String paramString1,
				String paramString2);
	}
}