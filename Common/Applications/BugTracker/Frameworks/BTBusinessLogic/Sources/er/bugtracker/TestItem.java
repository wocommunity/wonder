// TestItem.java
// 
package er.bugtracker;
import org.apache.log4j.Logger;

import com.webobjects.eocontrol.EOEditingContext;
import com.webobjects.eocontrol.EOQualifier;
import com.webobjects.foundation.NSArray;
import com.webobjects.foundation.NSTimestamp;

import er.bugtracker.People.Key;
import er.extensions.ERXQ;

public class TestItem extends _TestItem {
    static final Logger log = Logger.getLogger(TestItem.class);

    public void init(EOEditingContext ec) {
        super.init(ec);
        setState(TestItemState.OPEN);
        setDateCreated(new NSTimestamp());
    }

    public void open() {
        setState(TestItemState.OPEN);
    }

    public void close() {
        setState(TestItemState.CLOSED);
    }
    
  // Class methods go here
    
    public static class TestItemClazz extends _TestItemClazz {

        public NSArray unclosedTestItemsWithUser(EOEditingContext ec, People people) {
            EOQualifier q = ERXQ.and(ERXQ.equals(Key.OWNER, people), ERXQ.notEquals(Key.STATE, TestItemState.CLOSED));
            return objectsMatchingQualifier(ec, q);
        }
    }

    public static final TestItemClazz clazz = new TestItemClazz();
}
