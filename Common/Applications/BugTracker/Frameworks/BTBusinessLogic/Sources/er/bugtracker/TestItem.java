// TestItem.java
// 
package er.bugtracker;
import org.apache.log4j.Logger;

import com.webobjects.eocontrol.EOEditingContext;
import com.webobjects.foundation.NSTimestamp;

public class TestItem extends _TestItem {
    static final Logger log = Logger.getLogger(TestItem.class);

    public TestItem() {
        super();
    }

    public void awakeFromInsertion(EOEditingContext ec) {
        super.awakeFromInsertion(ec);
        setState(TestItemState.OPEN);
        setDateCreated(new NSTimestamp());
    }
    
    
    // Class methods go here
    
    public static class TestItemClazz extends _TestItemClazz {
        
    }

    public static final TestItemClazz clazz = new TestItemClazz();
}
