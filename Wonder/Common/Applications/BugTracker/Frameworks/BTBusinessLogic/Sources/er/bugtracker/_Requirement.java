// _Requirement.java
// 
// Created by eogenerator
// DO NOT EDIT.  Make changes to Requirement.java instead.
package er.bugtracker;
import com.webobjects.foundation.*;
import com.webobjects.eocontrol.*;
import com.webobjects.eoaccess.*;
import er.extensions.*;
import java.util.*;
import java.math.BigDecimal;

public abstract class _Requirement extends er.bugtracker.Bug {

    public static final String ENTITY = "Requirement";

    public interface Key extends er.bugtracker.Bug.Key {
        public static final String TYPE = "type";
        public static final String TEST_ITEMS = "testItems";
        public static final String TARGET_RELEASE = "targetRelease";
        public static final String SUBJECT = "subject";
        public static final String STATE = "state";
        public static final String REQUIREMENT_TYPE = "requirementType";
        public static final String REQUIREMENT_SUB_TYPE = "requirementSubType";
        public static final String PRIORITY = "priority";
        public static final String PREVIOUS_OWNER = "previousOwner";
        public static final String OWNER = "owner";
        public static final String ORIGINATOR = "originator";
        public static final String IS_READ = "isRead";
        public static final String IS_FEATURE_REQUEST = "isFeatureRequest";
        public static final String DIFFICULTY = "difficulty";
        public static final String DATE_SUBMITTED = "dateSubmitted";
        public static final String DATE_MODIFIED = "dateModified";
        public static final String COMPONENT = "component";
        public static final String COMMENTS = "comments";  
    }

    public static abstract class _RequirementClazz extends Bug.BugClazz {
    
    	public Requirement createRequirement(EOEditingContext editingContext, Boolean isFeatureRequest, Boolean isRead, er.bugtracker.State state, String subject, er.bugtracker.Component component, er.bugtracker.People originator, er.bugtracker.People owner, er.bugtracker.Priority priority, er.bugtracker.Release targetRelease) {
	   		Requirement eo = (Requirement)EOUtilities.createAndInsertInstance(editingContext, Requirement.ENTITY);
	    	eo.setIsFeatureRequest(isFeatureRequest);
	    	eo.setIsRead(isRead);
	    	eo.setState(state);
	    	eo.setSubject(subject);
	    	eo.setComponent(component);
	    	eo.setOriginator(originator);
	    	eo.setOwner(owner);
	    	eo.setPriority(priority);
	    	eo.setTargetRelease(targetRelease);
	    	return eo;
 		}


        public NSArray objectsForMyRequirements(EOEditingContext context, er.bugtracker.People userBinding) {
            EOFetchSpecification spec = EOFetchSpecification.fetchSpecificationNamed("myRequirements", "Requirement");

            NSMutableDictionary bindings = new NSMutableDictionary();

            if (userBinding != null)
                bindings.setObjectForKey(userBinding, "user");
            spec = spec.fetchSpecificationWithQualifierBindings(bindings);

            return context.objectsWithFetchSpecification(spec);
        }

        public NSArray objectsForMyTotalRequirements(EOEditingContext context, er.bugtracker.People userBinding) {
            EOFetchSpecification spec = EOFetchSpecification.fetchSpecificationNamed("myTotalRequirements", "Requirement");

            NSMutableDictionary bindings = new NSMutableDictionary();

            if (userBinding != null)
                bindings.setObjectForKey(userBinding, "user");
            spec = spec.fetchSpecificationWithQualifierBindings(bindings);

            return context.objectsWithFetchSpecification(spec);
        }

        public NSArray objectsForMyTotalRequirementsEngineering(EOEditingContext context, er.bugtracker.People userBinding) {
            EOFetchSpecification spec = EOFetchSpecification.fetchSpecificationNamed("myTotalRequirementsEngineering", "Requirement");

            NSMutableDictionary bindings = new NSMutableDictionary();

            if (userBinding != null)
                bindings.setObjectForKey(userBinding, "user");
            spec = spec.fetchSpecificationWithQualifierBindings(bindings);

            return context.objectsWithFetchSpecification(spec);
        }

        public NSArray objectsForRequirementsFiledRecently(EOEditingContext context, NSTimestamp dateBinding, er.bugtracker.People userBinding) {
            EOFetchSpecification spec = EOFetchSpecification.fetchSpecificationNamed("requirementsFiledRecently", "Requirement");

            NSMutableDictionary bindings = new NSMutableDictionary();

            if (dateBinding != null)
                bindings.setObjectForKey(dateBinding, "date");
            if (userBinding != null)
                bindings.setObjectForKey(userBinding, "user");
            spec = spec.fetchSpecificationWithQualifierBindings(bindings);

            return context.objectsWithFetchSpecification(spec);
        }

        public NSArray objectsForRequirementsInBuild(EOEditingContext context) {
            EOFetchSpecification spec = EOFetchSpecification.fetchSpecificationNamed("requirementsInBuild", "Requirement");

            return context.objectsWithFetchSpecification(spec);
        }

        public NSArray objectsForRequirementsInBuildEngineering(EOEditingContext context, er.bugtracker.People userBinding) {
            EOFetchSpecification spec = EOFetchSpecification.fetchSpecificationNamed("requirementsInBuildEngineering", "Requirement");

            NSMutableDictionary bindings = new NSMutableDictionary();

            if (userBinding != null)
                bindings.setObjectForKey(userBinding, "user");
            spec = spec.fetchSpecificationWithQualifierBindings(bindings);

            return context.objectsWithFetchSpecification(spec);
        }

    }


    public er.bugtracker.Difficulty difficulty() {
        return (er.bugtracker.Difficulty)storedValueForKey(Key.DIFFICULTY);
    }
    public void setDifficulty(er.bugtracker.Difficulty object) {
        takeStoredValueForKey(object, Key.DIFFICULTY);
    }


    public er.bugtracker.RequirementSubType requirementSubType() {
        return (er.bugtracker.RequirementSubType)storedValueForKey(Key.REQUIREMENT_SUB_TYPE);
    }
    public void setRequirementSubType(er.bugtracker.RequirementSubType object) {
        takeStoredValueForKey(object, Key.REQUIREMENT_SUB_TYPE);
    }


    public er.bugtracker.RequirementType requirementType() {
        return (er.bugtracker.RequirementType)storedValueForKey(Key.REQUIREMENT_TYPE);
    }
    public void setRequirementType(er.bugtracker.RequirementType object) {
        takeStoredValueForKey(object, Key.REQUIREMENT_TYPE);
    }

}
