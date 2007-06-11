/*
 * Copyright (C) NetStruxr, Inc. All rights reserved.
 *
 * This software is published under the terms of the NetStruxr
 * Public Software License version 0.5, a copy of which has been
 * included with this distribution in the LICENSE.NPL file.  */
package er.bugtracker;

import org.apache.log4j.Logger;

import com.webobjects.eocontrol.EOEditingContext;
import com.webobjects.eocontrol.EOEnterpriseObject;
import com.webobjects.eocontrol.EOGlobalID;
import com.webobjects.foundation.NSArray;

import er.corebusinesslogic.ERCoreBusinessLogic;
import er.corebusinesslogic.ERCoreUserInterface;

public class People extends _People implements ERCoreUserInterface {
    static final Logger log = Logger.getLogger(People.class);

    public interface Key extends _People.Key {
        public static final String PREFERENCES = "preferences";
    }
    
    
    public People() {
        super();
    }

    public void init(EOEditingContext ec) {
        super.init(ec);
        setIsActive(true);
        setIsAdmin(false);
        setIsCustomerService(false);
        setIsEngineering(false);
    }

    // Class methods go here

    public static class PeopleClazz extends _PeopleClazz {
        
        private EOGlobalID verifier;
        private EOGlobalID documenter;

        public People anyUser(EOEditingContext ec) {
            return (People) allObjects(ec).lastObject();
        }

        public People defaultDocumenter(EOEditingContext ec) {
            if(documenter != null) {
                return (People) ec.faultForGlobalID(documenter, ec);
            }
            return null;
        }

        public People defaultVerifier(EOEditingContext ec) {
            if(verifier != null) {
                return (People) ec.faultForGlobalID(verifier, ec);
            }
            return null;
        }

        public People userWithUsernamePassword(EOEditingContext ec, String user, String password) {
            NSArray users = loginWithUsernamePassword(ec, user, password);
            if (users.count() == 1)
                return (People) users.lastObject();
            return null;
        }

		private NSArray loginWithUsernamePassword(EOEditingContext ec, String user, String password) {
            // TODO Auto-generated method stub
            return objectsForLogin(ec, password, user);
        }

        public People currentUser(EOEditingContext ec) {
			return (People) ERCoreBusinessLogic.actor(ec);
		}

        public void setCurrentUser(People people) {
            ERCoreBusinessLogic.setActor(people);
        }

        public NSArray activeUsers(EOEditingContext context) {
            return objectsForActiveUsers(context);
        }
    }

    public static final PeopleClazz clazz = new PeopleClazz();

    public void newPreference(EOEnterpriseObject pref) {
        addObjectToBothSidesOfRelationshipWithKey(pref, Key.PREFERENCES);
    }

    public void setPreferences(NSArray array) {
        takeStoredValueForKey(array.mutableClone(), Key.PREFERENCES);
    }
    
    public NSArray preferences() {
        return (NSArray) storedValueForKey(Key.PREFERENCES);
    }
    
    // make ERD2WPropertyName happy
    public boolean isDemoUser() {
        return false;
    }

    public NSArray openBugs() {
        return Bug.clazz.bugsOwnedWithUser(editingContext(), this);
    }

    public NSArray unreadBugs() {
        return Bug.clazz.unreadBugsWithUser(editingContext(), this);
    }

    public NSArray allBugs() {
        return Bug.clazz.allBugsForUser(editingContext(), this);
    }
    
    public NSArray allRequirements() {
        if (isEngineering()) {
            return Requirement.clazz.myTotalRequirementsEngineeringWithUser(editingContext(), this);
        } else {
            return Requirement.clazz.myTotalRequirementsWithUser(editingContext(), this);
        }
    }

    public NSArray openRequirements() {
        if (isEngineering()) {
            return Requirement.clazz.requirementsInBuildEngineeringWithUser(editingContext(), this);
        } else {
            return Requirement.clazz.myRequirementsWithUser(editingContext(), this);
        }
    }

    public NSArray openTestItems() {
        return TestItem.clazz.unclosedTestItemsWithUser(editingContext(), this);
    }
}
