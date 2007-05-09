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
import com.webobjects.foundation.NSArray;

import er.corebusinesslogic.ERCPreference;
import er.corebusinesslogic.ERCoreBusinessLogic;
import er.corebusinesslogic.ERCoreUserInterface;
import er.extensions.ERXValueUtilities;

public class People extends _People implements ERCoreUserInterface {
    static final Logger log = Logger.getLogger(People.class);

    public People() {
        super();
    }

    public void awakeFromInsertion(EOEditingContext ec) {
        super.awakeFromInsertion(ec);
    }

    public TestItem createTestItemFromRequestWithDescription(Bug bug, Component component, String description) {
        bug = (Bug) localInstanceOf(bug);
        component = (Component) localInstanceOf(component);

        TestItem testItem = new TestItem();
        editingContext().insertObject(testItem);
        testItem.addToBothSidesOfComponent(component);
        testItem.setTextDescription(description);
        bug.addToBothSidesOfTestItems(testItem);

        return testItem;
    }

    // Class methods go here

    public static class PeopleClazz extends _PeopleClazz {
        
        private People verifier ;
        private People documenter;
        
        
        public People anyUser(EOEditingContext ec) {
            return (People) allObjects(ec).lastObject();
        }

        public People defaultDocumenter(EOEditingContext ec) {
            return null;
        }

        public People defaultVerifier(EOEditingContext ec) {
            return null;
        }

        public People userWithUsernamePassword(EOEditingContext ec, Object user, Object password) {
            NSArray users = loginWithUsernamePassword(ec, user, password);
            if (users.count() == 1)
                return (People) users.lastObject();
            return null;
        }

		public People currentUser(EOEditingContext ec) {
			return (People) ERCoreBusinessLogic.actor(ec);
		}

        public void setCurrentUser(People people) {
            ERCoreBusinessLogic.setActor(people);
        }
    }

    public static final PeopleClazz clazz = new PeopleClazz();

    public void newPreference(EOEnterpriseObject pref) {
        addToPreferences((ERCPreference) pref);
    }

    // FIXME ak: this is only here so that I don't have to change the generated
    // source in _People.java
    // but actually the templates are broken because they take an NSMutableArray
    // and make all sorts of strange
    // assumptions
    public void setPreferences(NSArray array) {
        super.setPreferences(array.mutableClone());
    }

    // make ERD2WPropertyName happy
    public boolean isDemoUser() {
        return false;
    }

    public boolean isEngineeringAsBoolean() {
        return ERXValueUtilities.booleanValue(isEngineering());
    }

    public boolean isActiveAsBoolean() {
        return ERXValueUtilities.booleanValue(isActive());
    }

    public boolean isAdminAsBoolean() {
        return ERXValueUtilities.booleanValue(isAdmin());
    }

    public NSArray openBugs() {
        return Bug.clazz.bugsOwnedWithUser(editingContext(), this);
    }

    public NSArray unreadBugs() {
        return Bug.clazz.unreadBugsWithUser(editingContext(), this);
    }

    public NSArray allRequirements() {
        if (isEngineeringAsBoolean()) {
            return Requirement.clazz.myTotalRequirementsEngineeringWithUser(editingContext(), this);
        } else {
            return Requirement.clazz.myTotalRequirementsWithUser(editingContext(), this);
        }
    }

    public NSArray openRequirements() {
        if (isEngineeringAsBoolean()) {
            return Requirement.clazz.requirementsInBuildEngineeringWithUser(editingContext(), this);
        } else {
            return Requirement.clazz.myRequirementsWithUser(editingContext(), this);
        }
    }

    public NSArray openTestItems() {
        return TestItem.clazz.unclosedTestItemsWithUser(editingContext(), this);
    }
}
