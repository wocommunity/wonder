// Requirement.java
// 
package er.bugtracker;
import org.apache.log4j.Logger;

import com.webobjects.eocontrol.EOEditingContext;
import com.webobjects.eocontrol.EOFetchSpecification;
import com.webobjects.foundation.NSArray;

public class Requirement extends _Requirement {
    static final Logger log = Logger.getLogger(Requirement.class);

    public Requirement() {
        super();
    }

    public void init(EOEditingContext ec) {
        super.init(ec);
    }
    
    // Class methods go here
    
    public static class RequirementClazz extends _RequirementClazz {
        // owner, not(closed)

        public NSArray myTotalRequirementsWithUser(EOEditingContext context, People people) {
            // originator or owner, not(build)
            EOFetchSpecification fs = newFetchSpecification(
                    andQualifier(
                            qualifierForPerson(people), 
                            negateQualifier(qualifierForState(State.BUILD))));
            return context.objectsWithFetchSpecification(fs);
        }

        public NSArray myTotalRequirementsEngineeringWithUser(EOEditingContext context, People people) {
            // owner, build
            EOFetchSpecification fs = newFetchSpecification(
                    andQualifier(
                            qualifierForOwner(people), 
                            qualifierForState(State.BUILD)));
            return context.objectsWithFetchSpecification(fs);
        }

        public NSArray requirementsInBuildEngineeringWithUser(EOEditingContext context, People people) {
            // originator or owner, unread, (build)
            EOFetchSpecification fs = newFetchSpecification(
                    andQualifier(
                            qualifierForPerson(people), 
                            andQualifier(
                                    qualifierForRead(false),
                                    qualifierForState(State.BUILD))));
            return context.objectsWithFetchSpecification(fs);
        }

        public NSArray myRequirementsWithUser(EOEditingContext context, People people) {
            // originator or owner, unread, (analyze or verify)
            EOFetchSpecification fs = newFetchSpecification(
                    andQualifier(
                            qualifierForPerson(people), 
                            andQualifier(
                                    qualifierForRead(false),
                                    qualifierForStates(new State[]{State.BUILD, State.ANALYZE}))));
            return context.objectsWithFetchSpecification(fs);
        }
        
    }

    public static final RequirementClazz clazz = new RequirementClazz();
}
