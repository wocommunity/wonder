// State.java
// 
package er.bugtracker;
import com.webobjects.foundation.*;
import com.webobjects.eocontrol.*;
import com.webobjects.eoaccess.*;
import er.extensions.*;

public class State extends _State {
    static final ERXLogger log = ERXLogger.getERXLogger(State.class);

    public static State ANALYZE;
    public static State BUILD;
    public static State VERIFY;
    public static State DOCUMENT;
    public static State CLOSED;

    public State() {
        super();
    }

    public void awakeFromInsertion(EOEditingContext ec) {
        super.awakeFromInsertion(ec);
    }


    // Class methods go here
    
    public static class StateClazz extends _StateClazz {
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

    public static final StateClazz clazz = (StateClazz)EOEnterpriseObjectClazz.clazzForEntityNamed("State");
}
