package er.extensions;

import org.apache.log4j.Logger;

import com.webobjects.appserver.WODisplayGroup;
import com.webobjects.eoaccess.EODatabaseDataSource;
import com.webobjects.eocontrol.EOAndQualifier;
import com.webobjects.eocontrol.EODataSource;
import com.webobjects.eocontrol.EOFetchSpecification;
import com.webobjects.eocontrol.EOQualifier;
import com.webobjects.foundation.NSArray;
import com.webobjects.foundation.NSMutableDictionary;

/**
 * Extends {@link WODisplayGroup}
 * <ul>
 * <li>provide access to the filtered objects</li>
 * <li>allows you to add qualifiers to the final query qualifier (as opposed to just min/equals/max with the keys)</li>
 * <li>clears out the sort ordering when the datasource changes. This is a cure fix to prevent errors when using switch components.
 * </ul>
 * @author ak
 */
public class ERXDisplayGroup extends WODisplayGroup {

	/** Logging support */
	private static final Logger log = Logger.getLogger(ERXDisplayGroup.class);

	public ERXDisplayGroup() {
		super();
	}

	/**
	 * Holds the extra qualifiers.
	 */
	private NSMutableDictionary _extraQualifiers = new NSMutableDictionary();

	public void setQualifierForKey(EOQualifier qualifier, String key) {
		if(qualifier != null) {
			_extraQualifiers.setObjectForKey(qualifier, key);
		} else {
			_extraQualifiers.removeObjectForKey(key);
		}
	}

	/**
	 * Overridden to support extra qualifiers.
	 */
	public EOQualifier qualifierFromQueryValues() {
		EOQualifier q1 = super.qualifierFromQueryValues();
		EOQualifier q2 = null;
		if(_extraQualifiers.allValues().count() > 1) {
			q2 = new EOAndQualifier(_extraQualifiers.allValues());
		} else if(_extraQualifiers.allValues().count() > 0) {
			q2 = (EOQualifier)_extraQualifiers.allValues().lastObject();
		}
		return q1 == null ? q2 : (q2 == null ? q1 : new EOAndQualifier(new NSArray(new Object[] {q1, q2})));
	}

	/**
	 * Overridden to localize the fetch specification if needed.
	 */
	/* TODO: Changes were too great to easily bring in the localizing code.  Unncomment later when we can update the ERXLocalizer.
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
	*/

	/**
	 * Returns all objects, filtered by the qualifier().
	 */
	public NSArray filteredObjects() {
		// FIXME AK: need to cache here
		NSArray result;
		EOQualifier q=qualifier();
		if (q!=null) {
			result=EOQualifier.filteredArrayWithQualifier(allObjects(),q);
		} else {
			result=allObjects();
		}
		return result;
	}

	/**
	 * Overridden to track selection changes.
	 */
	public NSArray selectedObjects() {
		if(log.isDebugEnabled()) {
			log.debug("selectedObjects@" + hashCode() +  ":" + super.selectedObjects().count());
		}
		return super.selectedObjects();
	}

	/**
	 * Overridden to track selection changes.
	 */
	public void setSelectedObjects(NSArray nsarray) {
		if(log.isDebugEnabled()) {
			log.debug("setSelectedObjects@" + hashCode()  + ":" + nsarray.count());
		}
		super.setSelectedObjects(nsarray);
	}

	/**
	 * Overridden to track selection changes.
	 */
	public boolean setSelectionIndexes(NSArray nsarray) {
		if(log.isDebugEnabled()) {
			log.debug("setSelectionIndexes@" + hashCode()  + ":" + nsarray.count(), new RuntimeException("Dummy for Stacktrace"));
		}
		return super.setSelectionIndexes(nsarray);
	}

	/**
	 * Overriden to re-set the selection. Why is this cleared in the super class?
	 */
	public void setNumberOfObjectsPerBatch(int i) {
		NSArray oldSelection = selectedObjects();
		super.setNumberOfObjectsPerBatch(i);
		setSelectedObjects(oldSelection);
	}

	/**
	 * Overridden to clear out the sort ordering if it is no longer applicable.
	 */
	public void setDataSource(EODataSource eodatasource) {
		EODataSource old = dataSource();
		super.setDataSource(eodatasource);
		if(old != null && eodatasource != null && ERXExtensions.safeDifferent(old.classDescriptionForObjects(), eodatasource.classDescriptionForObjects())) {
			setSortOrderings(NSArray.EmptyArray);
		}
	}

	/**
	 * Overriden to re-set the selection. Why is this cleared in the super class?
	 */
	public Object displayNextBatch() {
		NSArray oldSelection = selectedObjects();
		Object result = super.displayNextBatch();
		setSelectedObjects(oldSelection);
		return result;
	}

	/**
	 * Overriden to re-set the selection. Why is this cleared in the super class?
	 */
	public Object displayPreviousBatch() {
		NSArray oldSelection = selectedObjects();
		Object result = super.displayPreviousBatch();
		setSelectedObjects(oldSelection);
		return result;
	}
	
	/**
	 * Selects the visible objects.
	 *
	 */
	public Object selectFilteredObjects() {
		setSelectedObjects(filteredObjects());
		return null;
	}

	/**
	 * Overridden to log a message when more than one sort order exists. Useful to track down errors.
	 */
	public void setSortOrderings(NSArray nsarray) {
		super.setSortOrderings(nsarray);
		if(nsarray != null && nsarray.count() > 1) {
			if(log.isDebugEnabled()) {
				log.debug("More than one sort order: " + nsarray);
			}
		}
	}

	public void clearExtraQualifiers() {
		_extraQualifiers.removeAllObjects();
	}
}
