/*
 * Copyright (C) NetStruxr, Inc. All rights reserved.
 *
 * This software is published under the terms of the NetStruxr
 * Public Software License version 0.5, a copy of which has been
 * included with this distribution in the LICENSE.NPL file.  */
package er.bugtracker;

import com.webobjects.foundation.*;
import com.webobjects.appserver.*;
import com.webobjects.eocontrol.*;
import com.webobjects.eoaccess.*;
import er.extensions.*;
import java.util.*;

public class BTBusinessLogic extends ERXFrameworkPrincipal {

    static {
        setUpFrameworkPrincipalClass(BTBusinessLogic.class);
    }

    BTBusinessLogic sharedInstance;
    public BTBusinessLogic sharedInstance() {
        if(sharedInstance == null) {
            sharedInstance = (BTBusinessLogic)ERXFrameworkPrincipal.sharedInstance(BTBusinessLogic.class);
        }
        return sharedInstance;
    }

    public void finishInitialization() {
        initializeSharedData();
        addPreferenceRelationship();
    }

    public void addPreferenceRelationship() {
        EOEntity people = EOModelGroup.defaultGroup().entityNamed("People");
        EOEntity preferences = EOModelGroup.defaultGroup().entityNamed("ERCPreference");

        EOJoin preferencesJoin = new EOJoin(people.attributeNamed("id"),preferences.attributeNamed("userID"));
        EORelationship preferencesRelationship = new EORelationship();

        preferencesRelationship.setName("preferences");
        people.addRelationship(preferencesRelationship);
        preferencesRelationship.addJoin(preferencesJoin);
        preferencesRelationship.setToMany(true);
        preferencesRelationship.setJoinSemantic(EORelationship.InnerJoin);
    }

    // Shared Data Init Point.  Keep alphabetical
    public void initializeSharedData() {
        State.stateClazz().initializeSharedData();
        Priority.priorityClazz().initializeSharedData();
        TestItemState.testItemStateClazz().initializeSharedData();
    }
}
