package er.extensions;

import org.apache.log4j.Logger;

import com.webobjects.appserver.WODisplayGroup;
import com.webobjects.eoaccess.EODatabaseDataSource;
import com.webobjects.eocontrol.EOFetchSpecification;
import com.webobjects.eocontrol.EOQualifier;
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
}
