// _Requirement.java
// 
// Created by eogenerator
// DO NOT EDIT.  Make changes to Requirement.java instead.
package er.bugtracker;

import com.webobjects.eoaccess.EOUtilities;
import com.webobjects.eocontrol.EOEditingContext;
import com.webobjects.foundation.NSArray;
import com.webobjects.foundation.NSMutableDictionary;
import er.extensions.eof.ERXGenericRecord;

public abstract class _Requirement extends Bug {

    public _Requirement() {
        super();
    }

    public static abstract class _RequirementClazz extends ERXGenericRecord.ERXGenericRecordClazz {

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


    public Difficulty difficulty() {
        return (Difficulty)storedValueForKey("difficulty");
    }

    public void setDifficulty(Difficulty aValue) {
        takeStoredValueForKey(aValue, "difficulty");
    }
    public void addToBothSidesOfDifficulty(Difficulty object) {
        addObjectToBothSidesOfRelationshipWithKey(object, "difficulty");
    }
    public void removeFromBothSidesOfDifficulty(Difficulty object) {
        removeObjectFromBothSidesOfRelationshipWithKey(object, "difficulty");
    }


    public RequirementSubType requirementSubType() {
        return (RequirementSubType)storedValueForKey("requirementSubType");
    }

    public void setRequirementSubType(RequirementSubType aValue) {
        takeStoredValueForKey(aValue, "requirementSubType");
    }
    public void addToBothSidesOfRequirementSubType(RequirementSubType object) {
        addObjectToBothSidesOfRelationshipWithKey(object, "requirementSubType");
    }
    public void removeFromBothSidesOfRequirementSubType(RequirementSubType object) {
        removeObjectFromBothSidesOfRelationshipWithKey(object, "requirementSubType");
    }


    public RequirementType requirementType() {
        return (RequirementType)storedValueForKey("requirementType");
    }

    public void setRequirementType(RequirementType aValue) {
        takeStoredValueForKey(aValue, "requirementType");
    }
    public void addToBothSidesOfRequirementType(RequirementType object) {
        addObjectToBothSidesOfRelationshipWithKey(object, "requirementType");
    }
    public void removeFromBothSidesOfRequirementType(RequirementType object) {
        removeObjectFromBothSidesOfRelationshipWithKey(object, "requirementType");
    }

}
