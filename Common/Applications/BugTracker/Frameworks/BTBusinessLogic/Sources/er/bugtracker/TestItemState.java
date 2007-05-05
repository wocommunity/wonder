// TestItemState.java
// 
package er.bugtracker;
import org.apache.log4j.Logger;

import com.webobjects.eocontrol.EOEditingContext;
import com.webobjects.eocontrol.EOSharedEditingContext;

public class TestItemState extends _TestItemState {
    static final Logger log = Logger.getLogger(TestItemState.class);


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

    public static final TestItemStateClazz clazz = new TestItemStateClazz();
}
