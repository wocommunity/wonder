// TestItemState.java
// 
package er.bugtracker;
import com.webobjects.foundation.*;
import com.webobjects.eocontrol.*;
import com.webobjects.eoaccess.*;
import er.extensions.*;

public class TestItemState extends _TestItemState {
    static final ERXLogger log = ERXLogger.getLogger(TestItemState.class);


    public static TestItemState OPEN;
    public static TestItemState BUG;
    public static TestItemState CLOSED;
    public static TestItemState REQ;

    public TestItemState() {
        super();
    }

    public void awakeFromInsertion(EOEditingContext ec) {
        super.awakeFromInsertion(ec);
    }
    
    
    // Class methods go here
    
    public static class TestItemStateClazz extends _TestItemStateClazz {
        public TestItemState sharedStateForKey(String key) {
            return (TestItemState)objectWithPrimaryKeyValue(EOSharedEditingContext.defaultSharedEditingContext(), key);
        }

        public void initializeSharedData() {
            TestItemState.OPEN = sharedStateForKey("open");
            TestItemState.BUG = sharedStateForKey("bug ");
            TestItemState.CLOSED = sharedStateForKey("clsd");
            TestItemState.REQ = sharedStateForKey("rqmt");
        }
        
    }

    public static TestItemStateClazz testItemStateClazz() { return (TestItemStateClazz)EOGenericRecordClazz.clazzForEntityNamed("TestItemState"); }
}
