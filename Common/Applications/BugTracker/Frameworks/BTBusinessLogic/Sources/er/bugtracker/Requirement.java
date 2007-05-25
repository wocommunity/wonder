// Requirement.java
// 
package er.bugtracker;
import org.apache.log4j.Logger;

import com.webobjects.eocontrol.EOEditingContext;
import com.webobjects.foundation.NSArray;

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
        markUnread();
        super.setState(newState);
    }
    
    // Class methods go here
    
    public static class RequirementClazz extends _RequirementClazz {

        public NSArray myTotalRequirementsEngineeringWithUser(EOEditingContext context, People people) {
            return objectsForMyTotalRequirementsEngineering(context, people);
        }

        public NSArray myTotalRequirementsWithUser(EOEditingContext context, People people) {
            return objectsForMyTotalRequirements(context, people);
        }

        public NSArray requirementsInBuildEngineeringWithUser(EOEditingContext context, People people) {
            return objectsForRequirementsInBuildEngineering(context, people);
        }

        public NSArray myRequirementsWithUser(EOEditingContext context, People people) {
            return objectsForMyRequirements(context, people);
        }
        
    }

    public static final RequirementClazz clazz = new RequirementClazz();
}
