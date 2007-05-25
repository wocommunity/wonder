/*
 * Copyright (C) NetStruxr, Inc. All rights reserved.
 *
 * This software is published under the terms of the NetStruxr
 * Public Software License version 0.5, a copy of which has been
 * included with this distribution in the LICENSE.NPL file.  */

package er.bugtracker.pages;

import java.util.Enumeration;

import com.webobjects.appserver.WOComponent;
import com.webobjects.appserver.WOContext;
import com.webobjects.foundation.NSArray;
import com.webobjects.foundation.NSMutableArray;

import er.bugtracker.Bug;
import er.bugtracker.Release;
import er.bugtracker.State;

public class PushRelease extends WOComponent {

    public PushRelease(WOContext aContext) {
        super(aContext);
    }

    public Release theRelease;

    private NSArray _bugsInBuild = null;

    public Bug currentBug;

    public NSArray bugsInBuild() {
        if (_bugsInBuild == null) {
            _bugsInBuild = Bug.clazz.bugsInBuildWithTargetRelease(session().defaultEditingContext(), null);
        }
        return _bugsInBuild;
    }
    
    public void setBugsInBuild(NSArray bugs) {
        _bugsInBuild = bugs;
    }

    public WOComponent push() {
        for (Enumeration e = bugsInBuild().objectEnumerator(); e.hasMoreElements();) {
            currentBug = (Bug) e.nextElement();
            currentBug.moveToVerification();
        }
        session().defaultEditingContext().saveChanges();
        return pageWithName("HomePage");
    }

    /** @TypeInfo Release */
    public Release targetRelease() {
        NSArray bugsInBuild = bugsInBuild();
        Release result = null;
        if (bugsInBuild != null && bugsInBuild.count() > 0) {
            Bug bug = (Bug) bugsInBuild.lastObject();
            result = bug.targetRelease();
        }
        return result;
    }

    /** @TypeInfo Release */
    public NSArray targetReleases() {
        NSMutableArray result = new NSMutableArray();
        NSArray bugsInBuild = bugsInBuild();
        if (bugsInBuild != null && bugsInBuild.count() > 0) {
            for (Enumeration e = bugsInBuild.objectEnumerator(); e.hasMoreElements();) {
                Bug bug = (Bug) e.nextElement();
                Release r = bug.targetRelease();
                if (!result.containsObject(r))
                    result.addObject(r);
            }
        }
        return result;
    }
}
