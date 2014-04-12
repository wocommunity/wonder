package er.extensions.appserver;

import java.lang.reflect.Field;

import org.apache.commons.lang.ObjectUtils;
import org.apache.commons.lang.StringUtils;
import org.apache.log4j.Logger;

import com.webobjects.appserver.WODisplayGroup;
import com.webobjects.eoaccess.EODatabaseDataSource;
import com.webobjects.eocontrol.EOAndQualifier;
import com.webobjects.eocontrol.EODataSource;
import com.webobjects.eocontrol.EOFetchSpecification;
import com.webobjects.eocontrol.EOKeyValueUnarchiver;
import com.webobjects.eocontrol.EOQualifier;
import com.webobjects.eocontrol.EOSortOrdering;
import com.webobjects.foundation.NSArray;
import com.webobjects.foundation.NSDictionary;
import com.webobjects.foundation.NSForwardException;
import com.webobjects.foundation.NSMutableArray;
import com.webobjects.foundation.NSMutableDictionary;
import com.webobjects.foundation.NSMutableSet;
import com.webobjects.foundation.NSSet;
import com.webobjects.foundation._NSArrayUtilities;

import er.extensions.batching.ERXBatchingDisplayGroup;
import er.extensions.eof.ERXEOAccessUtilities;
import er.extensions.eof.ERXS;

/**
 * Extends {@link WODisplayGroup}
 * <ul>
 * <li>provide access to the filtered objects</li>
 * <li>allows you to add qualifiers to the final query qualifier (as opposed to just min/equals/max with the keys)</li>
 * <li>clears out the sort ordering when the datasource changes. This is a cure fix to prevent errors when using switch components.</li>
 * </ul>
 * @author ak
 * @param <T> data type of the displaygroup's objects
 */
public class ERXDisplayGroup<T> extends WODisplayGroup {
	private Field displayedObjectsField;
	
	/**
	 * Do I need to update serialVersionUID?
	 * See section 5.6 <cite>Type Changes Affecting Serialization</cite> on page 51 of the 
	 * <a href="http://java.sun.com/j2se/1.4/pdf/serial-spec.pdf">Java Object Serialization Spec</a>
	 */
	private static final long serialVersionUID = 1L;

	/** Logging support */
	private static final Logger log = Logger.getLogger(ERXDisplayGroup.class);

	public ERXDisplayGroup() {
		super();
		try {
			displayedObjectsField = WODisplayGroup.class.getDeclaredField("_displayedObjects");
			displayedObjectsField.setAccessible(true);
		}
		catch (SecurityException e) {
			throw NSForwardException._runtimeExceptionForThrowable(e);
		}
		catch (NoSuchFieldException e) {
			throw NSForwardException._runtimeExceptionForThrowable(e);
		}
	}
	
	/**
	 * Decodes an ERXDisplayGroup from the given unarchiver.
	 * 
	 * @param unarchiver the unarchiver to construct this display group with
	 * @return the corresponding batching display group
	 */
	public static Object decodeWithKeyValueUnarchiver(EOKeyValueUnarchiver unarchiver) {
		return new ERXDisplayGroup<Object>(unarchiver);
	}

	/**
	 * Creates a new ERXBatchingDisplayGroup from an unarchiver.
	 * 
	 * @param unarchiver the unarchiver to construct this display group with
	 */
	@SuppressWarnings("unchecked")
	private ERXDisplayGroup(EOKeyValueUnarchiver unarchiver) {
		this();
		setCurrentBatchIndex(1);
		setNumberOfObjectsPerBatch(unarchiver.decodeIntForKey("numberOfObjectsPerBatch"));
		setFetchesOnLoad(unarchiver.decodeBoolForKey("fetchesOnLoad"));
		setValidatesChangesImmediately(unarchiver.decodeBoolForKey("validatesChangesImmediately"));
		setSelectsFirstObjectAfterFetch(unarchiver.decodeBoolForKey("selectsFirstObjectAfterFetch"));
		setLocalKeys((NSArray) unarchiver.decodeObjectForKey("localKeys"));
		setDataSource((EODataSource) unarchiver.decodeObjectForKey("dataSource"));
		setSortOrderings((NSArray) unarchiver.decodeObjectForKey("sortOrdering"));
		setQualifier((EOQualifier) unarchiver.decodeObjectForKey("qualifier"));
		setDefaultStringMatchFormat((String) unarchiver.decodeObjectForKey("formatForLikeQualifier"));
		NSDictionary insertedObjectDefaultValues = (NSDictionary) unarchiver.decodeObjectForKey("insertedObjectDefaultValues");
		if (insertedObjectDefaultValues == null) {
			insertedObjectDefaultValues = NSDictionary.EmptyDictionary;
		}
		setInsertedObjectDefaultValues(insertedObjectDefaultValues);
		finishInitialization();
	}

	
	/**
	 * Holds the extra qualifiers.
	 */
	private NSMutableDictionary<String, EOQualifier> _extraQualifiers = new NSMutableDictionary<String, EOQualifier>();

	public void setQualifierForKey(EOQualifier qualifier, String key) {
		if(qualifier != null) {
			_extraQualifiers.setObjectForKey(qualifier, key);
		} else {
			_extraQualifiers.removeObjectForKey(key);
		}
	}
	
	/**
	 * Will return the qualifier set by "setQualifierForKey()" if it exists. Null returns otherwise.
	 * @param key
	 * @return
	 */
	public EOQualifier qualifierForKey(String key) {
		EOQualifier qualifier = null;
		if (StringUtils.isNotBlank(key)) {
			qualifier = _extraQualifiers.objectForKey(key);
		}
		return qualifier;
	}

	/**
	 * Overridden to support extra qualifiers.
	 * @return the qualifier constructed
	 */
	@Override
	public EOQualifier qualifierFromQueryValues() {
		EOQualifier q1 = super.qualifierFromQueryValues();
		EOQualifier q2 = null;
		if(_extraQualifiers.allValues().count() > 1) {
			q2 = new EOAndQualifier(_extraQualifiers.allValues());
		} else if(_extraQualifiers.allValues().count() > 0) {
			q2 = _extraQualifiers.allValues().lastObject();
		}
		return q1 == null ? q2 : (q2 == null ? q1 : new EOAndQualifier(new NSArray<EOQualifier>(new EOQualifier[] {q1, q2})));
	}

	/**
	 * Overridden to localize the fetch specification if needed.
	 * @return <code>null</code> to force the page to reload
	 */
	@Override
	public Object fetch() {
		if(log.isDebugEnabled()) {
			log.debug("Fetching: " + toString(), new RuntimeException("Dummy for Stacktrace"));
		}
		Object result;
		// ak: we need to transform localized keys (foo.name->foo.name_de)
		// when we do a real fetch. This actually
		// belongs into ERXEC, but I'm reluctant to have this morphing done
		// every time a fetch occurs as it affects mainly sort ordering
		// from the display group
		if (dataSource() instanceof EODatabaseDataSource) {
			EODatabaseDataSource ds = (EODatabaseDataSource) dataSource();
			EOFetchSpecification old = ds.fetchSpecification();
			EOFetchSpecification fs = ERXEOAccessUtilities.localizeFetchSpecification(ds.editingContext(), old);
			ds.setFetchSpecification(fs);
			try {
				result = super.fetch();
			} finally {
				ds.setFetchSpecification(old);
			}
		} else {
			result = super.fetch();
		}
		return result;
	}

	/**
	 * Returns all objects, filtered by the qualifier().
	 * @return filtered objects
	 */
	public NSArray<T> filteredObjects() {
		// FIXME AK: need to cache here
		NSArray<T> result;
		EOQualifier q=qualifier();
		if (q!=null) {
			result=EOQualifier.filteredArrayWithQualifier(allObjects(),q);
		} else {
			result=allObjects();
		}
		return result;
	}

	/**
	 * Returns allObjects(), first filtered by the qualifier(), then sorted by the sortOrderings().
	 * @return sorted filtered objects
	 */
	public NSArray<T> sortedObjects() {
		return ERXS.sorted(filteredObjects(), sortOrderings());
	}

	@Override
	public NSArray<T> selectedObjects() {
		if(log.isDebugEnabled()) {
			log.debug("selectedObjects@" + hashCode() +  ":" + super.selectedObjects().count());
		}
		return super.selectedObjects();
	}

	@Override
	public void setSelectedObjects(NSArray objects) {
		if(log.isDebugEnabled()) {
			log.debug("setSelectedObjects@" + hashCode()  + ":" + (objects != null ? objects.count() : "0"));
		}
		if (this instanceof ERXBatchingDisplayGroup) {
			// keep previous behavior
			// CHECKME a batching display group has its own _displayedObjects variable so setSelectionIndexes won't work
			super.setSelectedObjects(objects);
		} else {
			// jw: don't call super as it does not call setSelectionIndexes as advertised in its
			// javadocs and thus doesn't invoke events on the delegate
			// we need to access the private field _displayedObjects directly as we would get
			// wrong indexes when calling displayedObjects()
			NSMutableArray displayedObjects;
			try {
				displayedObjects = (NSMutableArray) displayedObjectsField.get(this);
			}
			catch (IllegalArgumentException e) {
				throw NSForwardException._runtimeExceptionForThrowable(e);
			}
			catch (IllegalAccessException e) {
				throw NSForwardException._runtimeExceptionForThrowable(e);
			}
			NSArray<Integer> newSelection = _NSArrayUtilities.indexesForObjectsIndenticalTo(displayedObjects, objects);
			setSelectionIndexes(newSelection);
		}
	}

	@Override
	public boolean setSelectionIndexes(NSArray nsarray) {
		if(log.isDebugEnabled()) {
			log.debug("setSelectionIndexes@" + hashCode()  + ":" + (nsarray != null ? nsarray.count() : "0"),
					new RuntimeException("Dummy for Stacktrace"));
		}
		return super.setSelectionIndexes(nsarray);
	}

	/**
	 * Extends the current selection by the given object.
	 * @param object object to add to the selection
	 * @return <code>true</code> if the object was added or <code>false</code> otherwise
	 */
	public boolean addToSelection(T object) {
		if (object == null) {
			return false;
		}
		return addToSelection(new NSArray<T>(object));
	}
	
	/**
	 * Extends the current selection by the given objects.
	 * @param objects objects to add to the selection
	 * @return <code>true</code> if at least one object was added or <code>false</code> otherwise
	 */
	public boolean addToSelection(NSArray<T> objects) {
		if (objects == null || objects.isEmpty()) {
			return false;
		}
		NSMutableSet<T> selection = new NSMutableSet<T>(selectedObjects());
		int selectionCountBefore = selection.count();
		selection.addObjectsFromArray(objects);
		setSelectedObjects(selection.allObjects());
		return selection.count() != selectionCountBefore;
	}
	
	/**
	 * Removes the given object from the current selection.
	 * @param object object to remove from the selection
	 * @return <code>true</code> if the object was removed or <code>false</code> otherwise
	 */
	public boolean removeFromSelection(T object) {
		if (object == null) {
			return false;
		}
		return removeFromSelection(new NSArray<T>(object));
	}
	
	/**
	 * Removes the given objects from the current selection.
	 * @param objects objects to remove from the selection
	 * @return <code>true</code> if at least one object was removed or <code>false</code> otherwise
	 */
	public boolean removeFromSelection(NSArray<T> objects) {
		if (objects == null || objects.isEmpty()) {
			return false;
		}
		NSMutableSet<T> selection = new NSMutableSet<T>(selectedObjects());
		int selectionCountBefore = selection.count();
		NSSet<T> objectsToRemove = new NSSet<T>(objects);
		selection.subtractSet(objectsToRemove);
		setSelectedObjects(selection.allObjects());
		return selection.count() != selectionCountBefore;
	}

	/**
	 * Overridden to preserve the current selection.
	 * @param count the proposed number of objects the WODisplayGroup should display at a time
	 */
	@Override
	public void setNumberOfObjectsPerBatch(int count) {
		NSArray<T> oldSelection = selectedObjects();
		super.setNumberOfObjectsPerBatch(count);
		setSelectedObjects(oldSelection);
	}

	/**
	 * Overridden to clear out the sort ordering if it is no longer applicable.
	 * @param ds the proposed EODataSource
	 */
	@Override
	public void setDataSource(EODataSource ds) {
		EODataSource old = dataSource();
		super.setDataSource(ds);
		if(old != null && ds != null && ObjectUtils.notEqual(old.classDescriptionForObjects(), ds.classDescriptionForObjects())) {
			setSortOrderings(NSArray.EmptyArray);
		}
	}

	/**
	 * Overridden to preserve the current selection.
	 * @return <code>null</code> to force the page to reload
	 */
	@Override
	public Object displayNextBatch() {
		NSArray<T> oldSelection = selectedObjects();
		Object result = super.displayNextBatch();
		setSelectedObjects(oldSelection);
		return result;
	}

	/**
	 * Overridden to preserve the current selection.
	 * @return <code>null</code> to force the page to reload
	 */
	@Override
	public Object displayPreviousBatch() {
		NSArray<T> oldSelection = selectedObjects();
		Object result = super.displayPreviousBatch();
		setSelectedObjects(oldSelection);
		return result;
	}
	
	/**
	 * Selects the visible objects.
	 * @return <code>null</code> to force the page to reload
	 */
	public Object selectFilteredObjects() {
		setSelectedObjects(filteredObjects());
		return null;
	}

	/**
	 * Overridden to log a message when more than one sort order exists. Useful to track down errors.
	 * @param sortOrderings the proposed EOSortOrdering objects
	 */
	@Override
	public void setSortOrderings(NSArray<EOSortOrdering> sortOrderings) {
		super.setSortOrderings(sortOrderings);
		if (sortOrderings != null && sortOrderings.count() > 1) {
			if (log.isDebugEnabled()) {
				log.debug("More than one sort order: " + sortOrderings);
			}
		}
	}

	public void clearExtraQualifiers() {
		_extraQualifiers.removeAllObjects();
	}
	
	/* Generified methods */
	
	@Override
	public NSArray<T> allObjects() {
		return super.allObjects();
	}
	
	@Override
	public NSArray<String> allQualifierOperators() {
		return super.allQualifierOperators();
	}
	
	@Override
	public NSArray<T> displayedObjects() {
		return super.displayedObjects();
	}
	
	@Override
	public T selectedObject() {
		return (T) super.selectedObject();
	}
	
	@Override
	public NSArray<EOSortOrdering> sortOrderings() {
		return super.sortOrderings();
	}
	
	/**
	 * Overridden to return correct result when no objects are displayed
	 * @return the index of the first object displayed by the current batch
	 */
	@Override
	public int indexOfFirstDisplayedObject() {
		if (currentBatchIndex() == 1 && displayedObjects().count() == 0)
			return 0;
		return super.indexOfFirstDisplayedObject();
	}

	/**
	 * Overridden to return correct index if the number of filtered objects
	 * is not a multiple of <code>numberOfObjectsPerBatch</code> and we are
	 * on the last batch index. The superclass incorrectly uses allObjects
	 * instead of displayedObjects to determine the index value.
	 * @return the index of the last object displayed by the current batch
	 */
	@Override
	public int indexOfLastDisplayedObject() {
        int computedEnd = numberOfObjectsPerBatch() * currentBatchIndex();
        int realEnd = displayedObjects().count();
        if(numberOfObjectsPerBatch() == 0) {
            return realEnd;
        }
        if (currentBatchIndex() > 1) {
        	realEnd += numberOfObjectsPerBatch() * (currentBatchIndex() - 1);
        }
        return realEnd >= computedEnd ? computedEnd : realEnd;
    }
}
