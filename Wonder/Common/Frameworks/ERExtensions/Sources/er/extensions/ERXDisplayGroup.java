package er.extensions;

import org.apache.log4j.Logger;

import com.webobjects.appserver.WODisplayGroup;
import com.webobjects.foundation.NSArray;

/**
 * Extends {@link WODisplayGroup}
 * <ul>
 * <li>provide access to the filtered objects</li>
 * </ul>
 * @author ak
 */
public class ERXDisplayGroup extends WODisplayGroup {


    /** Logging support */
    private static final Logger log = Logger.getLogger(ERXDisplayGroup.class);

    public NSArray filteredObjects() {
    	int old = numberOfObjectsPerBatch();
    	setNumberOfObjectsPerBatch(0);
    	NSArray result = displayedObjects();
    	setNumberOfObjectsPerBatch(old);
    	return result;
    }
}
