// TestItem.java
// 
package er.bugtracker;
import com.webobjects.foundation.*;
import com.webobjects.eocontrol.*;
import com.webobjects.eoaccess.*;
import er.extensions.*;

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

    public static final TestItemClazz clazz = (TestItemClazz)EOEnterpriseObjectClazz.clazzForEntityNamed("TestItem");
}
