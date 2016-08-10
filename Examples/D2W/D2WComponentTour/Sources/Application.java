import com.webobjects.eoaccess.EOUtilities;
import com.webobjects.eocontrol.EOEditingContext;
import com.webobjects.eocontrol.EOFetchSpecification;
import com.webobjects.eocontrol.EOObjectStore;
import com.webobjects.foundation.NSArray;
import com.webobjects.foundation.NSLog;
import com.webobjects.foundation.NSMutableArray;

import er.extensions.appserver.ERXApplication;
import er.extensions.eof.ERXEC;

public class Application extends ERXApplication {
	
    public static void main(String argv[]) {
        ERXApplication.main(argv, Application.class);
    }

    public Application() {
        NSLog.out.appendln("Welcome to " + name() + " !");
        // AK: I'm *way* to lazy to figure out how to set up a real DB so everyone can use it
        // as we only need a bit of reference data anyway, we just fake we fetched...
        ERXEC.setFactory(new ERXEC.DefaultFactory() {

			@Override
			protected EOEditingContext _createEditingContext(EOObjectStore parent) {
				return new ERXEC(parent) {

					@Override
					public NSArray objectsWithFetchSpecification(EOFetchSpecification eofetchspecification, EOEditingContext eoeditingcontext) {
						NSMutableArray result = new NSMutableArray();
						for (int i = 0; i < 5; i++) {
							result.addObject(EOUtilities.createAndInsertInstance(this, eofetchspecification.entityName()));
						}
						return result;
					}
					
				};
			}
        	
        });
     }
}
