/*
 * Copyright (C) NetStruxr, Inc. All rights reserved.
 *
 * This software is published under the terms of the NetStruxr
 * Public Software License version 0.5, a copy of which has been
 * included with this distribution in the LICENSE.NPL file.  */
package er.bugtracker;

import er.corebusinesslogic.ERCoreBusinessLogic;
import er.extensions.ERXExtensions;
import er.extensions.ERXFrameworkPrincipal;

public class BTBusinessLogic extends ERXFrameworkPrincipal {

    public final static Class REQUIRES[] = new Class[] {ERXExtensions.class, ERCoreBusinessLogic.class};
    
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
        ERCoreBusinessLogic.sharedInstance().addPreferenceRelationshipToActorEntity("People", "id");
    }

    // Shared Data Init Point.  Keep alphabetical
    public void initializeSharedData() {
        State.clazz.initializeSharedData();
        Priority.clazz.initializeSharedData();
        TestItemState.clazz.initializeSharedData();
    }
}
