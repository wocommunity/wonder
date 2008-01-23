/*
 * Copyright (C) NetStruxr, Inc. All rights reserved.
 *
 * This software is published under the terms of the NetStruxr
 * Public Software License version 0.5, a copy of which has been
 * included with this distribution in the LICENSE.NPL file.  */
package er.directtoweb;

import com.webobjects.appserver.WOComponent;
import com.webobjects.appserver.WOContext;
import com.webobjects.appserver.WODisplayGroup;
import com.webobjects.appserver.WORequest;
import com.webobjects.appserver.WOResponse;
import com.webobjects.directtoweb.D2W;
import com.webobjects.directtoweb.ListPageInterface;
import com.webobjects.directtoweb.NextPageDelegate;
import com.webobjects.directtoweb.QueryPageInterface;
import com.webobjects.eoaccess.EODatabaseDataSource;
import com.webobjects.eoaccess.EOEntity;
import com.webobjects.eocontrol.EOAndQualifier;
import com.webobjects.eocontrol.EODataSource;
import com.webobjects.eocontrol.EOFetchSpecification;
import com.webobjects.eocontrol.EOQualifier;
import com.webobjects.foundation.NSArray;
import com.webobjects.foundation.NSDictionary;
import com.webobjects.foundation.NSKeyValueCoding;
import com.webobjects.foundation.NSMutableDictionary;

import er.extensions.ERXDisplayGroup;
import er.extensions.ERXValueUtilities;

/**
 * Superclass for all query pages.<br />
 * In addition to the rest of the goodies of ERD2WPage, it lets you save and
 * restore the initial query bindings by supplying a NS(Mutable)Dictionary which
 * contains the keys "queryMin", "queryMax" etc from the respective fields of
 * the WODisplayGroup.
 */

public class ERD2WQueryPage extends ERD2WPage implements ERDQueryPageInterface {

    protected WODisplayGroup displayGroup;

    protected boolean didLoadQueryBindings;

    protected NSDictionary queryBindings;

    protected EOFetchSpecification fetchSpecification;

    public ERD2WQueryPage(WOContext context) {
        super(context);
        createDisplayGroup();
    }

    protected void createDisplayGroup() {
        displayGroup = new ERXDisplayGroup();
    }

    protected void pullQueryBindingsForName(String name) {
        NSDictionary queryBindings = queryBindings();
        if (queryBindings != null) {
            NSDictionary source = (NSDictionary) queryBindings.objectForKey(name);
            if (source != null) {
                NSMutableDictionary destination = (NSMutableDictionary) NSKeyValueCoding.Utility.valueForKey(displayGroup, name);
                destination.addEntriesFromDictionary(source);
            }
        }
    }
    
    public WOComponent clearAction() {
    	displayGroup().queryBindings().removeAllObjects();
    	displayGroup().queryMin().removeAllObjects();
    	displayGroup().queryMax().removeAllObjects();
    	displayGroup().queryOperator().removeAllObjects();
    	displayGroup().queryMatch().removeAllObjects();
       if (displayGroup() instanceof ERXDisplayGroup) {
            ERXDisplayGroup dg = (ERXDisplayGroup) displayGroup();
            dg.clearExtraQualifiers();
        }
    	return context().page();
    }
    
    public EOFetchSpecification fetchSpecification() {
        if(fetchSpecification == null) {
            String name = fetchSpecificationName();
            if(name != null) {
                fetchSpecification = entity().fetchSpecificationNamed(name);
            }
        }
    	return fetchSpecification; 
    }
    public void setFetchSpecification(EOFetchSpecification value) {
        fetchSpecification=value;
    	if(fetchSpecification != null) {
    		d2wContext().takeValueForKey(value.qualifier().bindingKeys(), "displayPropertyKeys");
    	}
    }

    public void setFetchSpecificationName(String value) {
        d2wContext().takeValueForKey(value,"fetchSpecificationName");
        //_fetchSpecificationName=name;
        EOEntity e=entity();
        setFetchSpecification(e.fetchSpecificationNamed(value));
    }

    public String fetchSpecificationName() {
        return (String)d2wContext().valueForKey("fetchSpecificationName");
    }

    public EOFetchSpecification queryFetchSpecification() {
        NSDictionary valuesFromBinding=displayGroup.queryMatch();
        if(fetchSpecification() != null) {
        	return fetchSpecification().fetchSpecificationWithQualifierBindings(valuesFromBinding);
        }
        return null;
    }

    protected void pushQueryBindingsForName(String name) {
        NSDictionary queryBindings = queryBindings();
        if (queryBindings != null && (queryBindings instanceof NSMutableDictionary)) {
            NSMutableDictionary mutableQueryBindings = (NSMutableDictionary) queryBindings;
            NSDictionary source = (NSDictionary) NSKeyValueCoding.Utility.valueForKey(displayGroup, name);
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
        if (queryBindings != null) {
            pushQueryBindingsForName("queryMin");
            pushQueryBindingsForName("queryMax");
            pushQueryBindingsForName("queryMatch");
            pushQueryBindingsForName("queryOperator");
            pushQueryBindingsForName("queryBindings");
        }
    }

    protected void loadQueryBindings() {
        if (!didLoadQueryBindings) {
            NSDictionary queryBindings = queryBindings();
            if (queryBindings != null) {
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
        if (queryBindings == null) {
            queryBindings = (NSDictionary) valueForBinding("queryBindings");
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
        EOQualifier q = displayGroup.qualifier();
        EOQualifier q2 = displayGroup.qualifierFromQueryValues();
        return q == null ? q2 : (q2 == null ? q : new EOAndQualifier(new NSArray(new Object[] { q, q2 })));
    }

    protected Boolean showResults = null;

    public boolean showResults() {
        if (showResults == null)
            return false;
        return showResults.booleanValue();
    }

    public void setShowResults(boolean value) {
        showResults = value ? Boolean.TRUE : Boolean.FALSE;
    }

    public WOComponent queryAction() {
        WOComponent nextPage = null;
        if (ERXValueUtilities.booleanValue(d2wContext().valueForKey("showListInSamePage"))) {
            setShowResults(true);
        } else {
        	nextPage = nextPageFromDelegate();
            if (nextPage == null) {
                String listConfigurationName = (String) d2wContext().valueForKey("listConfigurationName");
                ListPageInterface listpageinterface = null;
                if (listConfigurationName != null) {
                    listpageinterface = (ListPageInterface) D2W.factory().pageForConfigurationNamed(listConfigurationName, session());
                } else {
                    listpageinterface = D2W.factory().listPageForEntityNamed(entity().name(), session());
                }
                listpageinterface.setDataSource(queryDataSource());
                listpageinterface.setNextPage(context().page());
                nextPage = (WOComponent) listpageinterface;
            }
        }
        return nextPage;
    }

    // returning a null query data source if cancel was clicked
    private boolean _wasCancelled;
    
    public WOComponent cancelAction() {
    	WOComponent result = null;
    	try {
    		_wasCancelled = true;
    		result = nextPageFromDelegate();
    		if (result == null) {
    			// CHECKME AK: or return null?? no way of knowing...
    			result = nextPage();
    		}
    	} finally {
            _wasCancelled = false;
        }
        return result;
    }

    //CHECKME AK: this variable doesn't seem like such a good idea, in particular as there is no setter??
    public WOComponent returnPage;

    public WOComponent returnAction() {
        return returnPage != null ? returnPage : nextPage();
    }

    public boolean showCancel() {
        return nextPage() != null;
    }

    
    public EODataSource queryDataSource() {
        if (_wasCancelled) {
            return null;
        }
        EODataSource ds = dataSource();
        if (ds == null || !(ds instanceof EODatabaseDataSource)) {
        	ds = new EODatabaseDataSource(session().defaultEditingContext(), entity().name());
        	setDataSource(ds);
        }
        EOFetchSpecification fs = queryFetchSpecification();
        if(fs == null) {
        	fs = ((EODatabaseDataSource) ds).fetchSpecification();
        	fs.setQualifier(qualifier());
        	fs.setIsDeep(isDeep());
        	fs.setUsesDistinct(usesDistinct());
        	fs.setRefreshesRefetchedObjects(refreshRefetchedObjects());
        } else {
        	((EODatabaseDataSource) ds).setFetchSpecification(fs);
        }
        int limit = fetchLimit();
        if (limit != 0)
        	fs.setFetchLimit(limit);
        return ds;
    }

    public void setQueryDataSource(EODataSource datasource) {
        setDataSource(datasource);
    }

    /**
     * Returns the display group
     */
    public WODisplayGroup displayGroup() {
        return displayGroup;
    }
    
    public String headerTemplate() {
    	return fetchLimit() != 0 ? "ERD2WQueryPage.restrictedMessage" : "ERD2WQueryPage.plainMessage";
    }

    /**
     * Set a search value for the display group query match. When the value is null is gets removed from the 
     * dict, when the operator is null and the value isn't, "=" is chosen.
     * @param value
     * @param operator
     * @param key
     */
    public void setQueryMatchForKey(Object value, String operator, String key) {
        if(value != null) {
            displayGroup().queryMatch().setObjectForKey(value, key);
            if(operator != null) {
                displayGroup().queryOperator().setObjectForKey(operator, key);
            } else {
                displayGroup().queryOperator().removeObjectForKey(key);
            }
        } else {
            displayGroup().queryMatch().removeObjectForKey(key);
            displayGroup().queryOperator().removeObjectForKey(key);
        }
    }

    public void setCancelDelegate(NextPageDelegate cancelDelegate) {
        // FIXME not implemented!
        
    }
}
