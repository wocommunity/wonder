/*
 * Copyright (C) NetStruxr, Inc. All rights reserved.
 *
 * This software is published under the terms of the NetStruxr
 * Public Software License version 0.5, a copy of which has been
 * included with this distribution in the LICENSE.NPL file.  */

/* ERXNavigation.java created by max on Thu 27-Jul-2000 */
package er.extensions;

import com.webobjects.foundation.*;
import com.webobjects.eocontrol.*;
import com.webobjects.eoaccess.*;
import com.webobjects.appserver.*;
import java.util.*;
import org.apache.log4j.Category;

public class ERXModelGroup extends EOModelGroup {
    static ERXLogger log  = ERXLogger.getLogger(ERXModelGroup.class);
    
    public ERXModelGroup() {
    }

    public static EOModelGroup modelGroupForLoadedBundles() {
        EOModelGroup eomodelgroup = new ERXModelGroup();
        NSArray nsarray = NSBundle.frameworkBundles();
        int i = nsarray.count() + 1;
        NSMutableArray nsmutablearray = new NSMutableArray(i);
        nsmutablearray.addObject(NSBundle.mainBundle());
        nsmutablearray.addObjectsFromArray(nsarray);
        for (int i6 = 0; i6 < i; i6++) {
            NSBundle nsbundle = (NSBundle) nsmutablearray.objectAtIndex(i6);
            NSArray nsarray7 = nsbundle.pathsForResources("eomodeld", null);
            int i8 = nsarray7.count();
            for (int i9 = 0; i9 < i8; i9++) {
                String string = (String) nsarray7.objectAtIndex(i9);
                String string10
                    = (NSPathUtilities.stringByDeletingPathExtension
                       (NSPathUtilities.lastPathComponent(string)));
                EOModel eomodel = eomodelgroup.modelNamed(string10);
                if (eomodel == null)
                    eomodelgroup.addModelWithPath(string);
                else if (NSLog.debugLoggingAllowedForLevelAndGroups(1, 32768L))
                    NSLog.debug.appendln
                        ("Ignoring model at path \"" + string
                         + "\" because the model group " + eomodelgroup
                         + " already contains the model from the path \""
                         + eomodel.path() + "\"");
            }
        }
        return eomodelgroup;
    }

    public void addModel(EOModel eomodel) {
        Enumeration enumeration = _modelsByName.objectEnumerator();
        String name = eomodel.name();
        if (_modelsByName.objectForKey(name) != null) {
            log.warn("The model '" + name + "' (path: " + eomodel.path()
             + ") cannot be added to model group " + this
             + " because it already contains a model with that name.");
            return;
        }
        NSMutableSet nsmutableset = new NSMutableSet(128);
        NSSet nsset = new NSSet(eomodel.entityNames());
        while (enumeration.hasMoreElements()) {
            EOModel eomodel1 = (EOModel) enumeration.nextElement();
            nsmutableset.addObjectsFromArray(eomodel1.entityNames());
        }
        NSSet intersection = nsmutableset.setByIntersectingSet(nsset);
        if(intersection.count() != 0) {
            log.warn("The model '" + name + "' (path: " + eomodel.path()
                     + ") has an entity name conflict with the entities "
                     + intersection + " already in the model group " + this);
            Enumeration e = intersection.objectEnumerator();
            while(e.hasMoreElements()) {
                String entityName = (String)e.nextElement();
                log.debug("Removing entity " + entityName + " from model " + name);
                eomodel.removeEntity(eomodel.entityNamed(entityName));
            }
        }
        eomodel.setModelGroup(this);
        _modelsByName.setObjectForKey(eomodel, eomodel.name());
        NSNotificationCenter.defaultCenter()
            .postNotification("EOModelAddedNotification", eomodel);
    }
}
