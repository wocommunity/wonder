package er.extensions;

import com.webobjects.eoaccess.EODatabase;
import com.webobjects.eoaccess.EODatabaseContext;
import com.webobjects.eocontrol.EOEditingContext;
import com.webobjects.eocontrol.EOFetchSpecification;
import com.webobjects.eocontrol.EOGlobalID;
import com.webobjects.foundation.NSArray;

public class ERXDatabaseContext extends EODatabaseContext {
	private static ThreadLocal _fetching = new ThreadLocal();

	public ERXDatabaseContext(EODatabase database) {
		super(new ERXDatabase(database));
	}

	public static boolean isFetching() {
		Boolean fetching = (Boolean) _fetching.get();
		//System.out.println("ERXDatabaseContext.isFetching: " + Thread.currentThread() + ", " + fetching);
		return fetching != null && fetching.booleanValue();
	}

	public static void setFetching(boolean fetching) {
		//System.out.println("ERXDatabaseContext.setFetching: " + Thread.currentThread() + ", " + fetching);
		_fetching.set(Boolean.valueOf(fetching));
	}

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
}
