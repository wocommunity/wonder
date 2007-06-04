// State.java
// 
package er.bugtracker;
import org.apache.log4j.Logger;

import com.webobjects.eocontrol.EOEditingContext;
import com.webobjects.eocontrol.EOSharedEditingContext;
import com.webobjects.foundation.NSArray;

import er.corebusinesslogic.ERCMailState;
import er.extensions.ERXConstant;

/**
 * State is an example of a POJO. As it is a string in the DB, you need to provide a factory method.
 * @author ak
 *
 */
public class State extends ERXConstant.StringConstant {
    
    static final Logger log = Logger.getLogger(State.class);

    public static State ANALYZE = new State("anzl", "Analyze", 1);
    public static State BUILD = new State("buld", "Build", 2);
    public static State VERIFY = new State("vrfy", "Verify", 3);
    public static State DOCUMENT = new State("dcmt", "Document", 4);
    public static State CLOSED = new State("clsd", "Closed", 5);

    private int _sortOrder;
    
    public State(String value, String name, int sortOrder) {
        super(value, name);
        _sortOrder = sortOrder;
    }
    
    public int sortOrder() {
        return _sortOrder;
    }
    
    public String textDescription() {
        return name();
    }
    
    public static class StateClazz {

    	public NSArray allObjects(EOEditingContext ec) {
    		return new NSArray(new Object[] {ANALYZE, BUILD, VERIFY, DOCUMENT, CLOSED});
    	}

    	public State sharedStateForKey(String key) {
            return (State) ERXConstant.constantForClassNamed(key, State.class.getName());
        }

        public void initializeSharedData() {
            State.ANALYZE = sharedStateForKey("anzl");
            State.BUILD = sharedStateForKey("buld");
            State.VERIFY = sharedStateForKey("vrfy");
            State.DOCUMENT = sharedStateForKey("dcmt");
            State.CLOSED = sharedStateForKey("clsd");
        }
    }
    
    public static State state(String key) {
        return (State) constantForClassNamed(key, State.class.getName());
    }

    public static final StateClazz clazz = new StateClazz();
}
