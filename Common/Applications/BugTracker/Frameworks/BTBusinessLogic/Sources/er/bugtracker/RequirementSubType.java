// RequirementSubType.java
// 
package er.bugtracker;
import com.webobjects.foundation.*;
import com.webobjects.eocontrol.*;
import com.webobjects.eoaccess.*;
import er.extensions.*;

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

    public static final RequirementSubTypeClazz clazz = (RequirementSubTypeClazz)EOEnterpriseObjectClazz.clazzForEntityNamed("RequirementSubType");
}
