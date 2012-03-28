package er.extensions.eof;

import java.util.Enumeration;

import com.webobjects.eoaccess.EOEntity;
import com.webobjects.eoaccess.EOUtilities;
import com.webobjects.eocontrol.EOEditingContext;
import com.webobjects.eocontrol.EOEnterpriseObject;
import com.webobjects.eocontrol.EOObjectStoreCoordinator;
import com.webobjects.foundation.NSArray;
import com.webobjects.foundation.NSDictionary;
import com.webobjects.foundation.NSMutableArray;
import com.webobjects.foundation.NSNotification;
import com.webobjects.foundation.NSNotificationCenter;
import com.webobjects.foundation.NSSelector;

import er.extensions.foundation.ERXSelectorUtilities;

/**
 * Listens to EOEditingContextDidSaveChanges notifications to track changes on a
 * given entity and calls the entitiesChanged method when the entity changes.
 * 
 * @author mschrag (mostly taken from ERXEnterpriseObjectCache, though)
 * @param <T> 
 */
public abstract class ERXEnterpriseObjectChangeListener<T extends EOEnterpriseObject> {
	public static String ClearCacheNotification = "ERXEnterpriseObjectChangeListener.ClearCache";

	private String _entityName;
	private boolean _trackAllChanges;
	private boolean _deep;

	/**
	 * Constructs an ERXEnterpriseChangeListener.
	 * 
	 * @param c
	 *            the class name of the entity to watch for changes
	 * @param trackAllChanges
	 *            if true, entitiesChanged will pass the array of all changed
	 *            EO's (slightly slower)
	 * @param deep
	 *            if true, subentities of the given entity will be considered
	 *            relevent to this change listener
	 */
	public ERXEnterpriseObjectChangeListener(Class c, boolean trackAllChanges, boolean deep) {
		this(entityNameForClass(c), trackAllChanges, deep);
	}

	/**
	 * Constructs an ERXEnterpriseChangeListener.
	 * 
	 * @param entityName
	 *            the entity name to watch for changes
	 * @param trackAllChanges
	 *            if true, entitiesChanged will pass the array of all changed
	 *            EO's (slightly slower)
	 * @param deep
	 *            if true, subentities of the given entity will be considered
	 *            relevent to this change listener
	 */
	public ERXEnterpriseObjectChangeListener(String entityName, boolean trackAllChanges, boolean deep) {
		_entityName = entityName;
		_trackAllChanges = trackAllChanges;
		_deep = deep;
		registerForNotifications();
	}

	private static String entityNameForClass(Class c) {
		EOEditingContext ec = ERXEC.newEditingContext();
		ec.lock();
		try {
			EOEntity entity = EOUtilities.entityForClass(ec, c);
			if (entity != null) {
				return entity.name();
			}
			throw new IllegalArgumentException("There is no entity for the class " + c + ".");
		}
		finally {
			ec.unlock();
		}
	}

	protected void registerForNotifications() {
		NSSelector selector = ERXSelectorUtilities.notificationSelector("editingContextDidSaveChanges");
		NSNotificationCenter.defaultCenter().addObserver(this, selector, EOEditingContext.EditingContextDidSaveChangesNotification, null);
		selector = ERXSelectorUtilities.notificationSelector("clearCache");
		NSNotificationCenter.defaultCenter().addObserver(this, selector, ERXEnterpriseObjectChangeListener.ClearCacheNotification, null);
	}

	/**
	 * Helper to check if an array of EOs contains the handled entity or its
	 * subclasses (if deep).
	 * 
	 * @param editingContext
	 *            the editingContext containing the changes
	 * @param dict
	 *            the notification's userInfo dictionary
	 * @param key
	 *            the inserted/updated/deleted key
	 * @return an array of changed EOs (if not trackAllChanges, this will only
	 *         ever return at most one)
	 */
	@SuppressWarnings("unchecked")
	protected NSArray<T> relevantChanges(EOEditingContext editingContext, NSDictionary dict, String key) {
		NSArray allObjects = (NSArray) dict.objectForKey(key);
		NSMutableArray<T> changedObjects = new NSMutableArray<T>();
		for (Enumeration enumeration = allObjects.objectEnumerator(); enumeration.hasMoreElements();) {
			EOEnterpriseObject eo = (EOEnterpriseObject) enumeration.nextElement();
			String changedEntityName = eo.entityName();
			if (isRelevant(editingContext, changedEntityName)) {
				changedObjects.addObject((T) eo);
				if (!_trackAllChanges) {
					break;
				}
			}
		}
		return changedObjects;
	}

	/**
	 * Returns true if the changed entity name matches the watched entity name,
	 * or if this change listener is "deep," if the changed entity name is a
	 * 
	 * @param editingContext
	 *            the editing context containing the changes
	 * @param changedEntityName
	 *            the name of the changed entity
	 * @return true if this change is relevant to this change listener
	 */
	@SuppressWarnings( { "unchecked" })
	protected boolean isRelevant(EOEditingContext editingContext, String changedEntityName) {
		boolean relevant = false;
		if (changedEntityName.equals(_entityName)) {
			relevant = true;
		}
		else if (_deep) {
			EOEntity changedEntity = ERXEOAccessUtilities.entityNamed(editingContext, changedEntityName);
			for (EOEntity parent = changedEntity.parentEntity(); !relevant && parent != null; parent = parent.parentEntity()) {
				relevant = changedEntityName.equals(parent.name());
			}
		}
		return relevant;
	}

	/**
	 * Handler for the editingContextDidSaveChanges notification. Calls
	 * entitiesChanged if an object of the given entity (or its subentities)
	 * were changed.
	 * 
	 * @param n
	 */
	public void editingContextDidSaveChanges(NSNotification n) {
		EOEditingContext ec = (EOEditingContext) n.object();
		if (ec.parentObjectStore() instanceof EOObjectStoreCoordinator) {
			NSArray<T> entitiesInserted = relevantChanges(ec, n.userInfo(), EOEditingContext.InsertedKey);
			NSArray<T> entitiesUpdated = null;
			NSArray<T> entitiesDeleted = null;
			if (entitiesInserted.count() == 0 || _trackAllChanges) {
				entitiesUpdated = relevantChanges(ec, n.userInfo(), EOEditingContext.UpdatedKey);
				if (entitiesUpdated.count() == 0 || _trackAllChanges) {
					entitiesDeleted = relevantChanges(ec, n.userInfo(), EOEditingContext.DeletedKey);
					if (entitiesDeleted.count() == 0) {
						return;
					}
				}
			}
			if (entitiesInserted.count() > 0 || entitiesUpdated.count() > 0 || entitiesDeleted.count() > 0) {
				if (_trackAllChanges) {
					entitiesChanged(entitiesInserted, entitiesUpdated, entitiesDeleted);
				}
				else {
					entitiesChanged(null, null, null);
				}
			}
		}
	}

	/**
	 * Handler for the clearCaches notification. Calls reset if n.object is the
	 * entity name.
	 * 
	 * @param n
	 */
	public void clearCache(NSNotification n) {
		if (n.object() == null || entityName().equals(n.object())) {
			clearCache();
		}
	}

	/**
	 * Returns the name of the entity this cache is watching.
	 * 
	 * @return the name of the entity this cache is watching
	 */
	protected String entityName() {
		return _entityName;
	}

	/**
	 * Called when the entity being listened to changes. If trackAllChanges is
	 * false, all of the arrays will be null.
	 * 
	 * @param entitiesInserted
	 *            entities of this type were inserted, if null, it was not
	 *            checked
	 * @param entitiesUpdated
	 *            entities of this type were updated, if null, it was not
	 *            checked
	 * @param entitiesDeleted
	 *            entities of this type were deleted, if null, it was not
	 *            checked
	 */
	public abstract void entitiesChanged(NSArray<T> entitiesInserted, NSArray<T> entitiesUpdated, NSArray<T> entitiesDeleted);

	/**
	 * Called when a clear cache request has been received.
	 */
	public abstract void clearCache();
}
