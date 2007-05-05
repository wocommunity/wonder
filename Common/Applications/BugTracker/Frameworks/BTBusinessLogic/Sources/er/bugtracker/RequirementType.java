// RequirementType.java
// 
package er.bugtracker;
import org.apache.log4j.Logger;

import com.webobjects.eocontrol.EOEditingContext;

import er.extensions.EOEnterpriseObjectClazz;

public class RequirementType extends _RequirementType {
    static final Logger log = Logger.getLogger(RequirementType.class);

    public RequirementType() {
        super();
    }

    public void awakeFromInsertion(EOEditingContext ec) {
        super.awakeFromInsertion(ec);
    }
    
    
    // Class methods go here
    
    public static class RequirementTypeClazz extends _RequirementTypeClazz {
        
    }

    public static final RequirementTypeClazz clazz = new RequirementTypeClazz();
}
