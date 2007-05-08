// Requirement.java
// 
package er.bugtracker;
import org.apache.log4j.Logger;

import com.webobjects.eocontrol.EOEditingContext;

public class Requirement extends _Requirement {
    static final Logger log = Logger.getLogger(Requirement.class);

    public Requirement() {
        super();
    }

    public void init(EOEditingContext ec) {
        super.init(ec);
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

    public static final RequirementClazz clazz = new RequirementClazz();
}
