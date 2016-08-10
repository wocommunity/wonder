package er.extensions.eof;

import java.lang.reflect.InvocationTargetException;

import com.webobjects.eoaccess.EOAttribute;
import com.webobjects.eoaccess.EODatabase;
import com.webobjects.eoaccess.EODatabaseContext;
import com.webobjects.eoaccess.EODatabaseOperation;
import com.webobjects.eoaccess.EOEntity;
import com.webobjects.eoaccess.ERXEOAccessHelper;
import com.webobjects.eocontrol.EOEditingContext;
import com.webobjects.eocontrol.EOFetchSpecification;
import com.webobjects.eocontrol.EOGlobalID;
import com.webobjects.foundation.NSArray;
import com.webobjects.foundation.NSDictionary;
import com.webobjects.foundation.NSKeyValueCoding;
import com.webobjects.foundation.NSLog;

public class ERXDatabaseContext extends EODatabaseContext {
	private static ThreadLocal _fetching = new ThreadLocal();
	protected static Class<? extends ERXDatabase> _dbClass = null;
	
	public ERXDatabaseContext( EODatabase database ) throws InstantiationException, IllegalAccessException, IllegalArgumentException, InvocationTargetException, NoSuchMethodException, SecurityException {
		super( _dbClass != null ? _dbClass.getConstructor( EODatabase.class ).newInstance( database ) : new ERXDatabase( database ) );
	}

	public static void setDatabaseContextClass( Class<? extends ERXDatabase> cls ) {
		NSLog.out.appendln( "Setting ERXDatabase subclass to " + cls.getName() );
		_dbClass = cls;
	}
	
	public static boolean isFetching() {
		Boolean fetching = (Boolean) _fetching.get();
		// System.out.println("ERXDatabaseContext.isFetching: " +
		// Thread.currentThread() + ", " + fetching);
		return fetching != null && fetching.booleanValue();
	}

	public static void setFetching(boolean fetching) {
		// System.out.println("ERXDatabaseContext.setFetching: " +
		// Thread.currentThread() + ", " + fetching);
		_fetching.set(Boolean.valueOf(fetching));
	}

	@Override
	public NSArray objectsForSourceGlobalID(EOGlobalID gid, String name, EOEditingContext context) {
		NSArray results;
		boolean fetching = isFetching();
		if (!fetching) {
			setFetching(true);
		}
		try {
			results = super.objectsForSourceGlobalID(gid, name, context);
		}
		finally {
			if (!fetching) {
				setFetching(false);
			}
		}
		return results;
	}

	@Override
	public NSArray _objectsWithFetchSpecificationEditingContext(EOFetchSpecification fetchSpec, EOEditingContext context) {
		NSArray results;
		boolean fetching = isFetching();
		if (!fetching) {
			setFetching(!fetchSpec.refreshesRefetchedObjects());
		}
		try {
			results = super._objectsWithFetchSpecificationEditingContext(fetchSpec, context);
		}
		finally {
			if (!fetching) {
				setFetching(false);
			}
		}
		return results;
	}
	
	@Override
	public void _followFetchSpecification(EOFetchSpecification fetchSpec, String relationshipName, NSArray sourceObjects, EOEditingContext context) {
		fetchSpec = ERXEOAccessHelper.adjustPrefetching(this, fetchSpec, relationshipName, sourceObjects, context);
		super._followFetchSpecification(fetchSpec, relationshipName, sourceObjects, context);
	}

	@Override
	public void _verifyNoChangesToReadonlyEntity(EODatabaseOperation dbOp) {
		EOEntity entity = dbOp.entity();
		if (entity.isReadOnly()) {
			switch (dbOp.databaseOperator()) {
			case 0: // '\0'
				return;

			case 1: // '\001'
				throw new IllegalStateException("cannot insert object:" + dbOp.object() + " that corresponds to read-only entity: " + entity.name() + " in databaseContext " + this);

			case 3: // '\003'
				throw new IllegalStateException("cannot delete object:" + dbOp.object() + " that corresponds to read-only entity:" + entity.name() + " in databaseContext " + this);

			case 2: // '\002'
				if (!dbOp.dbSnapshot().equals(dbOp.newRow())) {
					throw new IllegalStateException("cannot update '" + dbOp.rowDiffsForAttributes(entity.attributes()).allKeys() + "' keys on object:" + dbOp.object() + " that corresponds to read-only entity: " + entity.name() + " in databaseContext " + this);
				}
				return;
			}
		}
		// HACK: ak these methods are protected, so we call them via KVC
		if (dbOp.databaseOperator() == 2 && ((Boolean) NSKeyValueCoding.Utility.valueForKey(entity, "_hasNonUpdateableAttributes")).booleanValue()) {
			NSArray keys = (NSArray) NSKeyValueCoding.Utility.valueForKey(entity, "dbSnapshotKeys");
			NSDictionary dbSnapshot = dbOp.dbSnapshot();
			NSDictionary newRow = dbOp.newRow();
			for (int i = keys.count() - 1; i >= 0; i--) {
				String key = (String) keys.objectAtIndex(i);
				EOAttribute att = entity.attributeNamed(key);
				// FIX: ak when you have single-table inheritance and in the
				// child there are foreign keys that are not in the parent
				// THEN, if the entity _hasNonUpdateableAttributes (public PK or
				// read only props) the DB op is checked
				// against the attributes. BUT this dictionary has all entries,
				// even from the child (most likely NULL values)
				// and the current implementation doesn't check against the case
				// when the attribute isn't present in the first place.
				// SO we add this check and live happily ever after
				if (att != null && att._isNonUpdateable() && !dbSnapshot.objectForKey(key).equals(newRow.objectForKey(key))) {
					if (att.isReadOnly()) {
						throw new IllegalStateException("cannot update read-only key '" + key + "' on object:" + dbOp.object() + " of entity: " + entity.name() + " in databaseContext " + this);
					}
					throw new IllegalStateException("cannot update primary-key '" + key + "' from '" + dbSnapshot.objectForKey(key) + "' to '" + newRow.objectForKey(key) + "' on object:" + dbOp.object() + " of entity: " + entity.name() + " in databaseContext " + this);
				}
			}

		}
	}
}
