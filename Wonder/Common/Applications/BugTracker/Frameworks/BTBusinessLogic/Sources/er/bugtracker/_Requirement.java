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

    public _Requirement() {
        super();
    }

    public static abstract class _RequirementClazz extends er.extensions.ERXGenericRecord.ERXGenericRecordClazz {

        public NSArray myRequirementsWithUser(EOEditingContext ec, Object user) {
            NSMutableDictionary _dict = new NSMutableDictionary(6);
            
            if(user != null) _dict.setObjectForKey( user, "user");
            return EOUtilities.objectsWithFetchSpecificationAndBindings(ec, "Requirement", "myRequirements", _dict);
        }

        public NSArray myTotalRequirementsWithUser(EOEditingContext ec, Object user) {
            NSMutableDictionary _dict = new NSMutableDictionary(6);
            
            if(user != null) _dict.setObjectForKey( user, "user");
            return EOUtilities.objectsWithFetchSpecificationAndBindings(ec, "Requirement", "myTotalRequirements", _dict);
        }

        public NSArray myTotalRequirementsEngineeringWithUser(EOEditingContext ec, Object user) {
            NSMutableDictionary _dict = new NSMutableDictionary(6);
            
            if(user != null) _dict.setObjectForKey( user, "user");
            return EOUtilities.objectsWithFetchSpecificationAndBindings(ec, "Requirement", "myTotalRequirementsEngineering", _dict);
        }

        public NSArray requirementsFiledRecentlyWithDateUser(EOEditingContext ec, Object date, Object user) {
            NSMutableDictionary _dict = new NSMutableDictionary(6);
            
            if(date != null) _dict.setObjectForKey( date, "date");
            if(user != null) _dict.setObjectForKey( user, "user");
            return EOUtilities.objectsWithFetchSpecificationAndBindings(ec, "Requirement", "requirementsFiledRecently", _dict);
        }

        public NSArray requirementsInBuild(EOEditingContext ec) {
            return EOUtilities.objectsWithFetchSpecificationAndBindings(ec, "Requirement", "requirementsInBuild", null);
        }

        public NSArray requirementsInBuildEngineeringWithUser(EOEditingContext ec, Object user) {
            NSMutableDictionary _dict = new NSMutableDictionary(6);
            
            if(user != null) _dict.setObjectForKey( user, "user");
            return EOUtilities.objectsWithFetchSpecificationAndBindings(ec, "Requirement", "requirementsInBuildEngineering", _dict);
        }

    }


    public er.bugtracker.Difficulty difficulty() {
        return (er.bugtracker.Difficulty)storedValueForKey("difficulty");
    }

    public void setDifficulty(er.bugtracker.Difficulty aValue) {
        takeStoredValueForKey(aValue, "difficulty");
    }
    public void addToBothSidesOfDifficulty(er.bugtracker.Difficulty object) {
        addObjectToBothSidesOfRelationshipWithKey(object, "difficulty");
    }
    public void removeFromBothSidesOfDifficulty(er.bugtracker.Difficulty object) {
        removeObjectFromBothSidesOfRelationshipWithKey(object, "difficulty");
    }


    public er.bugtracker.RequirementSubType requirementSubType() {
        return (er.bugtracker.RequirementSubType)storedValueForKey("requirementSubType");
    }

    public void setRequirementSubType(er.bugtracker.RequirementSubType aValue) {
        takeStoredValueForKey(aValue, "requirementSubType");
    }
    public void addToBothSidesOfRequirementSubType(er.bugtracker.RequirementSubType object) {
        addObjectToBothSidesOfRelationshipWithKey(object, "requirementSubType");
    }
    public void removeFromBothSidesOfRequirementSubType(er.bugtracker.RequirementSubType object) {
        removeObjectFromBothSidesOfRelationshipWithKey(object, "requirementSubType");
    }


    public er.bugtracker.RequirementType requirementType() {
        return (er.bugtracker.RequirementType)storedValueForKey("requirementType");
    }

    public void setRequirementType(er.bugtracker.RequirementType aValue) {
        takeStoredValueForKey(aValue, "requirementType");
    }
    public void addToBothSidesOfRequirementType(er.bugtracker.RequirementType object) {
        addObjectToBothSidesOfRelationshipWithKey(object, "requirementType");
    }
    public void removeFromBothSidesOfRequirementType(er.bugtracker.RequirementType object) {
        removeObjectFromBothSidesOfRelationshipWithKey(object, "requirementType");
    }

}
