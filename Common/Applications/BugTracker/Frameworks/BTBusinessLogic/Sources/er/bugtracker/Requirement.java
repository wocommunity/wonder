// Requirement.java
// 
package er.bugtracker;
import com.webobjects.foundation.*;
import com.webobjects.eocontrol.*;
import com.webobjects.eoaccess.*;
import er.extensions.*;

public class Requirement extends _Requirement {
    static final ERXLogger log = ERXLogger.getERXLogger(Requirement.class);

    public Requirement() {
        super();
    }

    public void awakeFromInsertion(EOEditingContext ec) {
        super.awakeFromInsertion(ec);
    }


    public void setState(State newState) {
        willChange();
        State oldState=state();
        markUnread();
        super.setState(newState);
    }
    
    // Class methods go here
    
    public static class RequirementClazz extends _RequirementClazz {
        
    }

    public static final RequirementClazz clazz = (RequirementClazz)EOEnterpriseObjectClazz.clazzForEntityNamed("Requirement");
}
