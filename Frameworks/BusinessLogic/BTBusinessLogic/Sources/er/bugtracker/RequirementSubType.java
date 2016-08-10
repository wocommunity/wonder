// RequirementSubType.java
// 
package er.bugtracker;

import com.webobjects.eocontrol.EOEditingContext;

public class RequirementSubType extends _RequirementSubType {
    public RequirementSubType() {
        super();
    }

    @Override
    public void init(EOEditingContext ec) {
        super.init(ec);
    }
    
    
    // Class methods go here
    
    public static class RequirementSubTypeClazz extends _RequirementSubTypeClazz {
        
    }

    public static final RequirementSubTypeClazz clazz = new RequirementSubTypeClazz();
}
