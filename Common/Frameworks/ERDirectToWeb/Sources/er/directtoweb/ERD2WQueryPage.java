/*
 * Copyright (C) NetStruxr, Inc. All rights reserved.
 *
 * This software is published under the terms of the NetStruxr
 * Public Software License version 0.5, a copy of which has been
 * included with this distribution in the LICENSE.NPL file.  */
package er.directtoweb;

import com.webobjects.appserver.*;
import com.webobjects.directtoweb.*;
import com.webobjects.eoaccess.*;
import com.webobjects.eocontrol.*;
import com.webobjects.foundation.*;

import er.extensions.*;

/**
 * Superclass for all query pages.<br />
 * In addition to the rest of the goodies of ERD2WPage, it lets you 
 * save and restore the initial query bindings by supplying a NS(Mutable)Dictionary which
 * contains the keys "queryMin", "queryMax" etc from the respective fields of the WODisplayGroup.
 */

public class ERD2WQueryPage extends ERD2WPage implements QueryPageInterface  {

    public WODisplayGroup displayGroup = new WODisplayGroup();
    protected boolean didLoadQueryBindings;
    protected NSDictionary queryBindings;
    
    public ERD2WQueryPage(WOContext context) { super(context); }

    protected void pullQueryBindingsForName(String name) {
    	NSDictionary queryBindings = queryBindings();
    	if(queryBindings != null) {
    		NSDictionary source = (NSDictionary)queryBindings.objectForKey(name);
    		if(source != null) {
    			NSMutableDictionary destination = (NSMutableDictionary)NSKeyValueCoding.Utility.valueForKey(displayGroup, name);
    			destination.addEntriesFromDictionary(source);
    		}
    	}
    }
    
    protected void pushQueryBindingsForName(String name) {
    	NSDictionary queryBindings = queryBindings();
    	if(queryBindings != null && (queryBindings instanceof NSMutableDictionary)) {
    		NSMutableDictionary mutableQueryBindings = (NSMutableDictionary)queryBindings;
    		NSDictionary source = (NSDictionary)NSKeyValueCoding.Utility.valueForKey(displayGroup, name);
    		mutableQueryBindings.setObjectForKey(source.mutableClone(), name);
    	}
    }
    
    public void takeValuesFromRequest(WORequest request, WOContext context) {
    	super.takeValuesFromRequest(request, context);
    	saveQueryBindings();
    }
    
    public void appendToResponse(WOResponse arg0, WOContext arg1) {
        loadQueryBindings();
        super.appendToResponse(arg0, arg1);
    }
    
    protected void saveQueryBindings() {
    	NSDictionary queryBindings = queryBindings();
    	if(queryBindings != null) {
    		pushQueryBindingsForName("queryMin");
    		pushQueryBindingsForName("queryMax");
    		pushQueryBindingsForName("queryMatch");
    		pushQueryBindingsForName("queryOperator");
    		pushQueryBindingsForName("queryBindings"); 
    	}
    }
    
    protected void loadQueryBindings() {
    	if(!didLoadQueryBindings) {
    		NSDictionary queryBindings = queryBindings();
    		if(queryBindings != null) {
    			pullQueryBindingsForName("queryMin");
    			pullQueryBindingsForName("queryMax");
    			pullQueryBindingsForName("queryMatch");
    			pullQueryBindingsForName("queryOperator");
    			pullQueryBindingsForName("queryBindings"); 
    			didLoadQueryBindings = true;
    		}
    	}
    }
    
    public void awake() {
        super.awake();
    }
    
    public boolean isDeep() {
        return ERXValueUtilities.booleanValue(d2wContext().valueForKey("isDeep"));
    }

    public NSDictionary queryBindings() {
    	if(queryBindings == null) {
    		queryBindings = (NSDictionary)valueForBinding("queryBindings");
    	}
        return queryBindings;
    }
    
    public void setQueryBindings(NSDictionary dictionary) {
    	queryBindings = dictionary;
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

    // add the ability to AND the existing qualifier from the DG
    public EOQualifier qualifier() {
        EOQualifier q=displayGroup.qualifier();
        EOQualifier q2=displayGroup.qualifierFromQueryValues();
        return q==null ? q2 : (q2==null ? q : new EOAndQualifier(new NSArray(new Object[]{q,q2})));
    }

    // Used with branching delegates.
    protected NSDictionary branch;
    public String branchName() { return (String)branch.valueForKey("branchName"); }

    protected Boolean showResults = null;
    public boolean showResults() {
        if(showResults == null)
            return false;
        return showResults.booleanValue();
    }
    public void setShowResults(boolean value) {
        showResults = value ? Boolean.TRUE : Boolean.FALSE;
    }
    
    public WOComponent queryAction() {
        WOComponent nextPage = null;
        if(nextPageDelegate() == null) {
            if(ERXValueUtilities.booleanValue(d2wContext().valueForKey("showListInSamePage"))){
                setShowResults(true);
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

    public void setQueryDataSource(EODataSource datasource) {
        setDataSource(datasource);
    }
}
