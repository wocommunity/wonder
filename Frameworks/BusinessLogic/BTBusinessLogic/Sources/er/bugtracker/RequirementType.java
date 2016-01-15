// RequirementType.java
// 
package er.bugtracker;

import com.webobjects.eocontrol.EOEditingContext;

public class RequirementType extends _RequirementType {
    public RequirementType() {
        super();
    }

    @Override
    public void init(EOEditingContext ec) {
        super.init(ec);
    }
    
    
    // Class methods go here
    
    public static class RequirementTypeClazz extends _RequirementTypeClazz {
        
    }

    public static final RequirementTypeClazz clazz = new RequirementTypeClazz();
}
