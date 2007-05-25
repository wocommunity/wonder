// TestItem.java
// 
package er.bugtracker;
import org.apache.log4j.Logger;

import com.webobjects.eocontrol.EOEditingContext;
import com.webobjects.foundation.NSArray;
import com.webobjects.foundation.NSTimestamp;

public class TestItem extends _TestItem {
    static final Logger log = Logger.getLogger(TestItem.class);

    public TestItem() {
        super();
    }

    public void init(EOEditingContext ec) {
        super.init(ec);
        setState(TestItemState.OPEN);
        setDateCreated(new NSTimestamp());
    }
    
    
    // Class methods go here
    
    public static class TestItemClazz extends _TestItemClazz {

        public NSArray unclosedTestItemsWithUser(EOEditingContext context, People people) {
            return objectsForUnclosedTestItems(context, people);
        }
    }

    public static final TestItemClazz clazz = new TestItemClazz();

    public void updateComponent(Component component) {
        addObjectToBothSidesOfRelationshipWithKey(component, Key.COMPONENT);
    }
}
