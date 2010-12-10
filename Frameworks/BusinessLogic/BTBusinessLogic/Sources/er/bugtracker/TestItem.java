// TestItem.java
// 
package er.bugtracker;

import com.webobjects.eocontrol.EOEditingContext;
import com.webobjects.foundation.NSTimestamp;
import er.extensions.eof.EOEnterpriseObjectClazz;
import er.extensions.logging.ERXLogger;

public class TestItem extends _TestItem {
    static final ERXLogger log = ERXLogger.getERXLogger(TestItem.class);

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

    public static final TestItemClazz clazz = (TestItemClazz) EOEnterpriseObjectClazz.clazzForEntityNamed("TestItem");
}
