/*
 * Copyright (C) NetStruxr, Inc. All rights reserved.
 *
 * This software is published under the terms of the NetStruxr
 * Public Software License version 0.5, a copy of which has been
 * included with this distribution in the LICENSE.NPL file.  */
package er.bugtracker;

import com.webobjects.foundation.*;
import com.webobjects.eocontrol.*;
import er.extensions.*;
import er.corebusinesslogic.*;

public class People extends _People implements ERCoreUserInterface {
    static final ERXLogger log = ERXLogger.getERXLogger(People.class);

    public People() {
        super();
    }

    public void awakeFromInsertion(EOEditingContext ec) {
        super.awakeFromInsertion(ec);
    }


    public TestItem createTestItemFromRequestWithDescription(Bug bug, Component component, String description) {
        bug = (Bug)localInstanceOf(bug);
        component = (Component)localInstanceOf(component);

        TestItem testItem = new TestItem();
        editingContext().insertObject(testItem);
        testItem.addToBothSidesOfComponent(component);
        testItem.setTextDescription(description);
        bug.addToBothSidesOfTestItems(testItem);

        return testItem;
    }
    
    // Class methods go here

    public static class PeopleClazz extends _PeopleClazz {
        public People anyUser(EOEditingContext ec) {
            return (People)allObjects(ec).lastObject();
        }
        public People defaultDocumenter(EOEditingContext ec) {
            return anyUser(ec);
        }
        public People defaultVerifier(EOEditingContext ec) {
            return anyUser(ec);
        }
        public People userWithUsernamePassword(EOEditingContext ec, Object user, Object password) {
            NSArray users = loginWithUsernamePassword(ec,user,password);
            if(users.count() == 1)
                return (People)users.lastObject();
            return null;
        }
    }

    public static final PeopleClazz clazz = (PeopleClazz)EOEnterpriseObjectClazz.clazzForEntityNamed("People");

    public void newPreference(EOEnterpriseObject pref) {
        addToPreferences((ERCPreference)pref);
    }

    // make ERD2WPropertyName happy
    public boolean isDemoUser() { return false; }
    public boolean isEngineeringAsBoolean() { return ERXValueUtilities.booleanValue(isEngineering()); }
    public boolean isActiveAsBoolean() { return ERXValueUtilities.booleanValue(isActive()); }
    public boolean isAdminAsBoolean() { return ERXValueUtilities.booleanValue(isAdmin()); }

    public NSArray openBugs() {
        return Bug.clazz.bugsOwnedWithUser(editingContext(), this);
    }

    public NSArray bugs() {
        return openBugs();
    }

    public NSArray unreadBugs() {
        return Bug.clazz.unreadBugsWithUser(editingContext(), this);
    }

    public NSArray allRequirements() {
        if(isEngineeringAsBoolean()) {
            return Requirement.clazz.myTotalRequirementsEngineeringWithUser(editingContext(), this);
        } else {
            return Requirement.clazz.myTotalRequirementsWithUser(editingContext(), this);
        }
    }
    
    public NSArray openRequirements() {
        if(isEngineeringAsBoolean()) {
            return Requirement.clazz.requirementsInBuildEngineeringWithUser(editingContext(), this);
        } else {
            return Requirement.clazz.myRequirementsWithUser(editingContext(), this);
        }
    }

    public NSArray openTestItems() {
        return TestItem.clazz.unclosedTestItemsWithUser(editingContext(), this);
    }
}
