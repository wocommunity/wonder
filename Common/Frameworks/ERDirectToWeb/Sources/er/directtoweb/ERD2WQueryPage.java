/*
 * Copyright (C) NetStruxr, Inc. All rights reserved.
 *
 * This software is published under the terms of the NetStruxr
 * Public Software License version 0.5, a copy of which has been
 * included with this distribution in the LICENSE.NPL file.  */
package er.directtoweb;

import com.webobjects.foundation.*;
import com.webobjects.eocontrol.*;
import com.webobjects.eoaccess.*;
import com.webobjects.appserver.*;
import com.webobjects.directtoweb.*;
import com.webobjects.directtoweb.generation.*;
import er.extensions.*;

/**
 * Superclass for all query pages.<br />
 * 
 */

public class ERD2WQueryPage extends ERD2WPage implements QueryPageInterface, DTWGeneration  {

    public WODisplayGroup displayGroup = new WODisplayGroup();

    public ERD2WQueryPage(WOContext context) { super(context); }

    public boolean isDeep() {
        return ERXValueUtilities.booleanValue(d2wContext().valueForKey("isDeep"));
    }

    public boolean usesDistinct() {
        return ERXValueUtilities.booleanValue(d2wContext().valueForKey("usesDistinct"));
    }

    public boolean refreshRefetchedObjects() {
        return ERXValueUtilities.booleanValue(d2wContext().valueForKey("refreshRefetchedObjects"));
    }

    public int fetchLimit() {
        return ERXValueUtilities.intValueWithDefault(d2wContext().valueForKey("fetchLimit"), 0);
    }

    // debug helpers
    public boolean d2wComponentNameDebuggingEnabled() {
        return ERDirectToWeb.d2wComponentNameDebuggingEnabled(session());
    }
    public String d2wCurrentComponentName() {
        String name = (String)d2wContext().valueForKey("componentName");
        if(name.indexOf("CustomComponent")>=0) {
            name = (String)d2wContext().valueForKey("customComponentName");
        }
        return name;
    }

    // add the ability to AND the existing qualifier from the DG
    public EOQualifier qualifier() {
        EOQualifier q=displayGroup.qualifier();
        EOQualifier q2=displayGroup.qualifierFromQueryValues();
        return q==null ? q2 : (q2==null ? q : new EOAndQualifier(new NSArray(new Object[]{q,q2})));
    }

    // Used with branching delegates.
    protected NSDictionary branch;
    public String branchName() { return (String)branch.valueForKey("branchName"); }

    public boolean showResults = false;

    public WOComponent queryAction() {
        WOComponent nextPage = null;
        if(nextPageDelegate() == null) {
            if(ERXValueUtilities.booleanValue(d2wContext().valueForKey("showListInSamePage"))){
                showResults = true;
            } else {
                String listConfigurationName=(String)d2wContext().valueForKey("listConfigurationName");
                ListPageInterface listpageinterface = null;
                if(listConfigurationName!=null){
                    listpageinterface = (ListPageInterface)D2W.factory().pageForConfigurationNamed(listConfigurationName, session());
                } else {
                    listpageinterface = D2W.factory().listPageForEntityNamed(entity().name(), session());
                }
                listpageinterface.setDataSource(queryDataSource());
                listpageinterface.setNextPage(context().page());
                nextPage = (WOComponent) listpageinterface;
            }
        } else {
            NextPageDelegate nextpagedelegate = this.nextPageDelegate();
            nextPage = nextpagedelegate.nextPage(this);
        }
        return nextPage;
    }

    // returning a null query data source if cancel was clicked
    private boolean _wasCancelled;
    public WOComponent cancelAction() {
        WOComponent result=null;
        try {
            _wasCancelled=true;
            result=nextPageDelegate().nextPage(this);
        } finally {
            _wasCancelled=false;
        }
        return result;
    }

    public WOComponent returnPage;
    public WOComponent returnAction(){
        return returnPage;
    }

    public boolean showCancel() {
        return returnPage != null;
    }

    public EODataSource queryDataSource() {
        if(_wasCancelled) {
            return null;
        }
        EODataSource ds = dataSource();
        if (ds == null || !(ds instanceof EODatabaseDataSource)) {
            ds = new EODatabaseDataSource(session().defaultEditingContext(), entity().name());
            setDataSource(ds);
        }
        EOFetchSpecification fs = ((EODatabaseDataSource)ds).fetchSpecification();
        fs.setQualifier(qualifier());
        fs.setIsDeep(isDeep());
        fs.setUsesDistinct(usesDistinct());
        fs.setRefreshesRefetchedObjects(refreshRefetchedObjects());
        int limit = fetchLimit();
        if (limit != 0)
            fs.setFetchLimit(limit);
        return ds;
    }

    public String fetchSpecOptions() {
        String fetchSpecOptions
        = ("_queryDataSource.fetchSpecification().setIsDeep(" + isDeep()
           + ");\n\t_queryDataSource.fetchSpecification().setUsesDistinct("
           + usesDistinct()
           + ");\n\t_queryDataSource.fetchSpecification().setRefreshesRefetchedObjects("
           + refreshRefetchedObjects() + ");\n");
        if (fetchLimit() != 0)
            fetchSpecOptions += ("\t_queryDataSource.fetchSpecification().setFetchLimit("
                       + fetchLimit() + ");\n");
        return fetchSpecOptions;
    }

}
