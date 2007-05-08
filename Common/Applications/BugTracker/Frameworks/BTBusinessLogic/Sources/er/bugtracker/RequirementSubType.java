// RequirementSubType.java
// 
package er.bugtracker;
import org.apache.log4j.Logger;

import com.webobjects.eocontrol.EOEditingContext;

public class RequirementSubType extends _RequirementSubType {
    static final Logger log = Logger.getLogger(RequirementSubType.class);

    public RequirementSubType() {
        super();
    }

    public void init(EOEditingContext ec) {
        super.init(ec);
    }
    
    
    // Class methods go here
    
    public static class RequirementSubTypeClazz extends _RequirementSubTypeClazz {
        
    }

    public static final RequirementSubTypeClazz clazz = new RequirementSubTypeClazz();
}
