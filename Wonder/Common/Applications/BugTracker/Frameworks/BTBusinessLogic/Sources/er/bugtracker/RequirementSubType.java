// RequirementSubType.java
// 
package er.bugtracker;
import org.apache.log4j.Logger;

import com.webobjects.eocontrol.EOEditingContext;

import er.extensions.EOEnterpriseObjectClazz;

public class RequirementSubType extends _RequirementSubType {
    static final Logger log = Logger.getLogger(RequirementSubType.class);

    public RequirementSubType() {
        super();
    }

    public void awakeFromInsertion(EOEditingContext ec) {
        super.awakeFromInsertion(ec);
    }
    
    
    // Class methods go here
    
    public static class RequirementSubTypeClazz extends _RequirementSubTypeClazz {
        
    }

    public static final RequirementSubTypeClazz clazz = new RequirementSubTypeClazz();
}
