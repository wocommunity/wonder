// State.java
// 
package er.bugtracker;
import org.apache.log4j.Logger;

import com.webobjects.eocontrol.EOEditingContext;
import com.webobjects.eocontrol.EOSharedEditingContext;
import com.webobjects.foundation.NSArray;

public class State extends _State {
    static final Logger log = Logger.getLogger(State.class);

    public static State ANALYZE;
    public static State BUILD;
    public static State VERIFY;
    public static State DOCUMENT;
    public static State CLOSED;

    public State() {
        super();
    }

    public void init(EOEditingContext ec) {
        super.init(ec);
    }


    // Class methods go here
    
    public static class StateClazz extends _StateClazz {

    	public NSArray allObjects(EOEditingContext ec) {
    		return new NSArray(new Object[] {ANALYZE, BUILD, VERIFY, DOCUMENT, CLOSED});
    	}

    	public State sharedStateForKey(String key) {
            return (State)objectWithPrimaryKeyValue(EOSharedEditingContext.defaultSharedEditingContext(), key);
        }

        public void initializeSharedData() {
            State.ANALYZE = sharedStateForKey("anzl");
            State.BUILD = sharedStateForKey("buld");
            State.VERIFY = sharedStateForKey("vrfy");
            State.DOCUMENT = sharedStateForKey("dcmt");
            State.CLOSED = sharedStateForKey("clsd");
        }
    }

    public static final StateClazz clazz = new StateClazz();
}
