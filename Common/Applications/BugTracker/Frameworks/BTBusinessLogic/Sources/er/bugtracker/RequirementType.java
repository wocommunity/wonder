// RequirementType.java
// 
package er.bugtracker;
import com.webobjects.foundation.*;
import com.webobjects.eocontrol.*;
import com.webobjects.eoaccess.*;
import er.extensions.*;

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

    public static final RequirementTypeClazz clazz = (RequirementTypeClazz)EOEnterpriseObjectClazz.clazzForEntityNamed("RequirementType");
}
