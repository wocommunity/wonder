// RequirementSubType.java
// 
package er.bugtracker;

import com.webobjects.eocontrol.EOEditingContext;
import er.extensions.eof.EOEnterpriseObjectClazz;
import er.extensions.logging.ERXLogger;

public class RequirementSubType extends _RequirementSubType {
    static final ERXLogger log = ERXLogger.getERXLogger(RequirementSubType.class);

    public RequirementSubType() {
        super();
    }

    public void awakeFromInsertion(EOEditingContext ec) {
        super.awakeFromInsertion(ec);
    }
    
    
    // Class methods go here
    
    public static class RequirementSubTypeClazz extends _RequirementSubTypeClazz {
        
    }

    public static final RequirementSubTypeClazz clazz = (RequirementSubTypeClazz) EOEnterpriseObjectClazz.clazzForEntityNamed("RequirementSubType");
}
