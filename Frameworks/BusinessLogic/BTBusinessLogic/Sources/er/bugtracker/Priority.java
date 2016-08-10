// Priority.java
// 
package er.bugtracker;

import com.webobjects.eocontrol.EOEditingContext;
import com.webobjects.eocontrol.EOSharedEditingContext;
import com.webobjects.foundation.NSArray;

public class Priority extends _Priority {
    public static Priority CRITICAL;
    public static Priority HIGH;
    public static Priority MEDIUM;
    public static Priority LOW;

    public Priority() {
        super();
    }

    @Override
    public void init(EOEditingContext ec) {
        super.init(ec);
    }
    
    
    // Class methods go here
    
    public static class PriorityClazz extends _PriorityClazz {

    	@Override
    	public NSArray allObjects(EOEditingContext ec) {
    		return new NSArray(new Object[] {CRITICAL, HIGH, MEDIUM, LOW});
    	}
    	
        public Priority sharedStateForKey(String key) {
            return objectWithPrimaryKeyValue(EOSharedEditingContext.defaultSharedEditingContext(), key);
        }

        public void initializeSharedData() {
            Priority.CRITICAL = sharedStateForKey("crtl");
            Priority.HIGH = sharedStateForKey("high");
            Priority.MEDIUM = sharedStateForKey("medm");
            Priority.LOW = sharedStateForKey("low ");
        }
    }

    public static final PriorityClazz clazz = new PriorityClazz();
}
