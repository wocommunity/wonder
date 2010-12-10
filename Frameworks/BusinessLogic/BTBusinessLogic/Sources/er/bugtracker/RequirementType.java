// RequirementType.java
// 
package er.bugtracker;

import com.webobjects.eocontrol.EOEditingContext;
import er.extensions.eof.EOEnterpriseObjectClazz;
import er.extensions.logging.ERXLogger;

public class RequirementType extends _RequirementType {
    static final ERXLogger log = ERXLogger.getERXLogger(RequirementType.class);

    public RequirementType() {
        super();
    }

    public void awakeFromInsertion(EOEditingContext ec) {
        super.awakeFromInsertion(ec);
    }
    
    
    // Class methods go here
    
    public static class RequirementTypeClazz extends _RequirementTypeClazz {
        
    }

    public static final RequirementTypeClazz clazz = (RequirementTypeClazz) EOEnterpriseObjectClazz.clazzForEntityNamed("RequirementType");
}
