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
import com.webobjects.appserver.WOResponse;
import com.webobjects.directtoweb.D2W;
import com.webobjects.directtoweb.ListPageInterface;
import com.webobjects.eoaccess.EODatabaseDataSource;
import com.webobjects.eocontrol.EOArrayDataSource;
import com.webobjects.eocontrol.EOClassDescription;
import com.webobjects.eocontrol.EOEnterpriseObject;
import com.webobjects.eocontrol.EOSortOrdering;
import com.webobjects.foundation.NSArray;
import com.webobjects.foundation.NSMutableArray;

import er.bugtracker.Bug;
import er.bugtracker.People;

public class BugsPerUser extends WOComponent {

    public BugsPerUser(WOContext aContext) {
        super(aContext);
    }

    protected EODatabaseDataSource bugsDataSource;

    protected NSArray bugs;
    protected Bug currentBug;
    protected EOEnterpriseObject theRelease;

    /** @TypeInfo Bug */
    protected NSArray bugsPerOwner;
    protected People user;

    /** @TypeInfo Release */
    protected NSArray targetReleases() {
        NSMutableArray result=new NSMutableArray();
        if (bugs!=null && bugs.count()>0) {
            for (Enumeration e=bugs.objectEnumerator(); e.hasMoreElements();) {
                Bug bug=(Bug)e.nextElement();
                EOEnterpriseObject r=(EOEnterpriseObject)bug.valueForKey("targetRelease");
                if (!result.containsObject(r)) result.addObject(r);
            }
        }
        return result;
    }

    private static final NSArray LAST_NAME_ORDERING = new NSArray(EOSortOrdering.sortOrderingWithKey("owner.name",                                                                                                     EOSortOrdering.CompareCaseInsensitiveAscending));
    protected EOEnterpriseObject release;
    public void appendToResponse(WOResponse r, WOContext c) {
        // we refresh the bug list
        bugs=EOSortOrdering.sortedArrayUsingKeyOrderArray(bugsDataSource.fetchObjects(),
                                                          LAST_NAME_ORDERING);
        super.appendToResponse(r,c);
    }

    public WOComponent viewBugs() {
        ListPageInterface lpi=(ListPageInterface)D2W.factory().listPageForEntityNamed("Bug",session());
        EOArrayDataSource ads=new EOArrayDataSource(EOClassDescription.classDescriptionForEntityName("Bug"),
                                                    session().defaultEditingContext());
        ads.setArray(bugsPerOwner);
        lpi.setDataSource(ads);
        lpi.setNextPage(context().page());
        return (WOComponent)lpi;
    }

}
