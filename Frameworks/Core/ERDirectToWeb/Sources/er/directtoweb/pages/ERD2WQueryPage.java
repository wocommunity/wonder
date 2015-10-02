/*
 * Copyright (C) NetStruxr, Inc. All rights reserved.
 *
 * This software is published under the terms of the NetStruxr
 * Public Software License version 0.5, a copy of which has been
 * included with this distribution in the LICENSE.NPL file.  */
package er.directtoweb.pages;

import java.util.Enumeration;

import com.webobjects.appserver.WOComponent;
import com.webobjects.appserver.WOContext;
import com.webobjects.appserver.WODisplayGroup;
import com.webobjects.appserver.WORequest;
import com.webobjects.appserver.WOResponse;
import com.webobjects.directtoweb.D2W;
import com.webobjects.directtoweb.D2WContext;
import com.webobjects.directtoweb.ListPageInterface;
import com.webobjects.directtoweb.NextPageDelegate;
import com.webobjects.eoaccess.EODatabaseDataSource;
import com.webobjects.eoaccess.EOEntity;
import com.webobjects.eocontrol.EOAndQualifier;
import com.webobjects.eocontrol.EODataSource;
import com.webobjects.eocontrol.EOFetchSpecification;
import com.webobjects.eocontrol.EOQualifier;
import com.webobjects.foundation.NSArray;
import com.webobjects.foundation.NSDictionary;
import com.webobjects.foundation.NSKeyValueCoding;
import com.webobjects.foundation.NSMutableArray;
import com.webobjects.foundation.NSMutableDictionary;
import com.webobjects.foundation.NSValidation;

import er.directtoweb.delegates.ERDQueryDataSourceDelegateInterface;
import er.directtoweb.delegates.ERDQueryValidationDelegate;
import er.directtoweb.interfaces.ERDQueryPageInterface;
import er.extensions.appserver.ERXDisplayGroup;
import er.extensions.appserver.ERXResponseRewriter;
import er.extensions.foundation.ERXValueUtilities;
import er.extensions.localization.ERXLocalizer;

/**
 * Superclass for all query pages.
 * <p>
 * In addition to the rest of the goodies of ERD2WPage, it lets you save and
 * restore the initial query bindings by supplying a NS(Mutable)Dictionary which
 * contains the keys "queryMin", "queryMax" etc from the respective fields of
 * the WODisplayGroup.
 * @d2wKey fetchSpecificationName
 * @d2wKey enableQueryForNullValues
 * @d2wKey isDeep
 * @d2wKey usesDistinct
 * @d2wKey refrehRefetchedObjects
 * @d2wKey fetchLimit
 * @d2wKey prefetchingRelationshipKeyPaths
 * @d2wKey showListInSamePage
 * @d2wKey listConfigurationName
 * @d2wKey queryDataSourceDelegate
 * @d2wKey queryValidationDelegate
 * @d2wKey enableQueryForNullValues
 * @d2wKey canQueryPropertyForNullValues
 */
public class ERD2WQueryPage extends ERD2WPage implements ERDQueryPageInterface {
	/**
	 * Do I need to update serialVersionUID?
	 * See section 5.6 <cite>Type Changes Affecting Serialization</cite> on page 51 of the 
	 * <a href="http://java.sun.com/j2se/1.4/pdf/serial-spec.pdf">Java Object Serialization Spec</a>
	 */
	private static final long serialVersionUID = 1L;

    protected WODisplayGroup displayGroup;

    protected boolean didLoadQueryBindings;
    protected NSDictionary queryBindings;

    protected EOFetchSpecification fetchSpecification;
    
    protected ERDQueryDataSourceDelegateInterface queryDataSourceDelegate;
    protected ERDQueryValidationDelegate queryValidationDelegate;

    protected NSArray _nullablePropertyKeys;
    protected NSMutableDictionary keysToQueryForNull = new NSMutableDictionary();

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
    
  /**
   * <span class="ja">
   * ディスプレイ・グループの全クエリ設定を取り除きます。
   * 
   * @return カレント・ページ
   * </span>
   */
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

    @Override
    public void takeValuesFromRequest(WORequest request, WOContext context) {
        super.takeValuesFromRequest(request, context);
        substituteValueForNullableQueryKeys();
        saveQueryBindings();
    }

    @Override
    public void appendToResponse(WOResponse response, WOContext context) {
        loadQueryBindings();
        super.appendToResponse(response, context);
        
        if (ERXValueUtilities.booleanValueWithDefault(d2wContext().valueForKey("enableQueryForNullValues"), false)) {
            ERXResponseRewriter.addScriptResourceInHead(response, context, "ERDirectToWeb", "ERD2WQueryPage.js");
        }
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

    @Override
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

    public NSArray prefetchingRelationshipKeyPaths(){
        return ERXValueUtilities.arrayValue(d2wContext().valueForKey("prefetchingRelationshipKeyPaths"));
    }

    // add the ability to AND the existing qualifier from the DG
    public EOQualifier qualifier() {
        EOQualifier q = displayGroup.qualifier();
        EOQualifier q2 = displayGroup.qualifierFromQueryValues();
        return q == null ? q2 : (q2 == null ? q : new EOAndQualifier(new NSArray(new Object[] { q, q2 })));
    }

    protected Boolean showResults = null;

    public boolean showResults() {
        return Boolean.TRUE.equals(showResults);
    }

    public void setShowResults(boolean value) {
        showResults = value;
    }

    public WOComponent queryAction() {
        WOComponent nextPage = null;

        // If we have a validation delegate, validate the query values before actually performing the query.
        ERDQueryValidationDelegate queryValidationDelegate = queryValidationDelegate();
        if (queryValidationDelegate != null) {
            clearValidationFailed();
            setErrorMessage(null);
            try {
                queryValidationDelegate.validateQuery(this);
            } catch (NSValidation.ValidationException ex) {
    			setErrorMessage(ERXLocalizer.currentLocalizer().localizedTemplateStringForKeyWithObject("CouldNotQuery", ex));
    			validationFailedWithException(ex, null, "queryExceptionKey");
    		}
            if (hasErrors()) {
                return context().page();
            }
        }

        if (ERXValueUtilities.booleanValue(d2wContext().valueForKey("showListInSamePage"))) {
            setShowResults(true);
        } else {
        	nextPage = nextPageFromDelegate();
            if (nextPage == null) {
                String listConfigurationName = (String) d2wContext().valueForKey("listConfigurationName");
                ListPageInterface listpageinterface;
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

    @Override
    public boolean showCancel() {
        return nextPage() != null;
    }

    /**
     * Assembles the data source for the search results page, configured for the current query.  If a
     * {@link #queryDataSourceDelegate()} is defined, the delegate's implementation is invoked. Otherwise,
     * the {@link #defaultQueryDataSource()} is returned.
     * @return the prepared data source
     */
    public EODataSource queryDataSource() {
        if (_wasCancelled) {
            return null;
        }
        
        ERDQueryDataSourceDelegateInterface delegate = queryDataSourceDelegate();
        if (delegate != null) {
            return delegate.queryDataSource(this);
        } else {
            return defaultQueryDataSource();
        }
    }

    /**
     * Sets the query data source.
     * @param datasource to be used as the query data source
     */
    public void setQueryDataSource(EODataSource datasource) {
        setDataSource(datasource);
    }
    
    /**
     * Default implementation of which assembles the data source for the search results page, configured
     * for the current query.
     * @return the prepared data source
     */
    public EODataSource defaultQueryDataSource() {
        EODataSource ds = dataSource();
        if (ds == null || !(ds instanceof EODatabaseDataSource)) {
            ds = new EODatabaseDataSource(session().defaultEditingContext(), entity().name());
            setDataSource(ds);
        }
        EOFetchSpecification fs = queryFetchSpecification();
        if (fs == null) {
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
        NSArray prefetchingRelationshipKeyPaths = prefetchingRelationshipKeyPaths();
        if (prefetchingRelationshipKeyPaths != null && prefetchingRelationshipKeyPaths().count() > 0) {
            fs.setPrefetchingRelationshipKeyPaths(prefetchingRelationshipKeyPaths);
        }
        return ds;
    }
    
    /**
     * Gets the query data source delegate.
     * @return the query data source delegate
     */
    public ERDQueryDataSourceDelegateInterface queryDataSourceDelegate() {
        if (queryDataSourceDelegate == null) {
            queryDataSourceDelegate = (ERDQueryDataSourceDelegateInterface)d2wContext().valueForKey("queryDataSourceDelegate");
        }
        return queryDataSourceDelegate;
    }
    
    /**
     * Sets the query data source delegate.
     * @param delegate to use as the query data source delegate
     */
    public void setQueryDataSourceDelegate(ERDQueryDataSourceDelegateInterface delegate) {
        queryDataSourceDelegate = delegate;
    }

    /**
     * <span class="en">
     * Gets the query validation delegate.
     * 
     * @return the query validation delegate
     * </span>
     * 
     * <span class="ja">
     * クエリ検証デリゲートを戻します。
     * 
     * @return クエリ検証デリゲート
     * </span>
     */
    public ERDQueryValidationDelegate queryValidationDelegate() {
        if (null == queryValidationDelegate) {
            queryValidationDelegate = (ERDQueryValidationDelegate)d2wContext().valueForKey("queryValidationDelegate");
        }
        return queryValidationDelegate;
    }

    /**
     * <span class="en">
     * Sets the query validation delegate.
     * 
     * @param delegate to use as the query validation delegate
     * </span>
     * 
     * <span class="ja">
     * クエリ検証デリゲートをセットします。
     * 
     * @param delegate -　クエリ検証デリゲート (@see ERDQueryValidationDelegate)
     * </span>
     */
    public void setQueryValidationDelegate(ERDQueryValidationDelegate delegate) {
        queryValidationDelegate = delegate;
    }

    /**
     * Gets the display group.
     * @return the display group
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
     * When operator is "&lt;" is uses <code>queryMatch()</code>, if it is "&gt;" is uses <code>queryMin()</code>,
     * so you can use it with the various date range components.
     * @param value to assign to the queryMatch dictionary for the given key
     * @param operator used for comparing the value
     * @param key to use
     */
    public void setQueryMatchForKey(Object value, String operator, String key) {
        NSMutableDictionary queryDict = displayGroup().queryMatch();
        NSMutableDictionary operatorDict = displayGroup().queryOperator();
        if(">".equals(operator)) {
            queryDict = displayGroup().queryMin();
            operatorDict = new NSMutableDictionary();
        } else if ("<".equals(operator)) {
            queryDict = displayGroup().queryMax();
            operatorDict = new NSMutableDictionary();
        }
        if(value != null) {
            queryDict.setObjectForKey(value, key);
            if(operator != null) {
                operatorDict.setObjectForKey(operator, key);
            } else {
                operatorDict.removeObjectForKey(key);
            }
        } else {
            queryDict.removeObjectForKey(key);
            operatorDict.removeObjectForKey(key);
        }
    }

    public void setCancelDelegate(NextPageDelegate cancelDelegate) {
        // FIXME not implemented!
        
    }

    /**
     * Discovers the property keys that can be queried for a NULL value.
     * @return the array of nullable and/or non-mandatory property keys
     */
    public NSArray nullablePropertyKeys() {
        if (null == _nullablePropertyKeys) {
            NSMutableArray array = new NSMutableArray();
            String preKey = propertyKey();
            D2WContext d2wContext = d2wContext();
            for (Enumeration keysEnum = displayPropertyKeys().objectEnumerator(); keysEnum.hasMoreElements();) {
                String key = (String)keysEnum.nextElement();
                setPropertyKey(key);

                Object isMandatory = d2wContext.valueForKey("isMandatory");
                if (isMandatory != null && !ERXValueUtilities.booleanValue(isMandatory)) {
                    array.addObject(key);
                }
            }
            _nullablePropertyKeys = array;
            setPropertyKey(preKey); // Restore the property key.
        }
        return _nullablePropertyKeys;
    }

    /**
     * Determines if the null query checkbox for the current D2W property key should be checked.
     * @return true if the checkbox should be checked
     */
    public boolean isNullQueryCheckedForCurrentProperty() {
        return Boolean.TRUE.equals(keysToQueryForNull.valueForKey(propertyKey()));
    }

    /**
     * Sets the flag denoting a property key is being queried for a null value.
     * @param value of the checkbox' checked attribute
     */
    public void setIsNullQueryCheckedForCurrentProperty(boolean value) {
        keysToQueryForNull.takeValueForKey(value, propertyKey());
    }

    /**
     * Determines if the null query checkbox can be shown for the current D2W property key should be checked.
     * @return true if the checkbox should be checked
     */
    public boolean canQueryCurrentPropertyForNullValue() {
        boolean enabled = ERXValueUtilities.booleanValueWithDefault(d2wContext().valueForKey("enableQueryForNullValues"), false);
        boolean propertyAllowsQuery = ERXValueUtilities.booleanValueWithDefault(d2wContext().valueForKey("canQueryPropertyForNullValues"), true);
        return (enabled && propertyAllowsQuery && nullablePropertyKeys().containsObject(propertyKey()));
    }

    /**
     * When querying for properties with a null value, and the null value checkbox for a property key is checked, this
     * method substitutes <code>NSKeyValueCoding.NullValue</code> into the display group's query dictionaries for that
     * property key.
     */
    protected void substituteValueForNullableQueryKeys() {
        WODisplayGroup displayGroup = displayGroup();
        for(Enumeration nullableKeysEnum = nullablePropertyKeys().objectEnumerator(); nullableKeysEnum.hasMoreElements();) {
            String key = (String)nullableKeysEnum.nextElement();
            Boolean value = (Boolean)keysToQueryForNull.objectForKey(key);
            if (Boolean.TRUE.equals(value)) {
                displayGroup.queryOperator().takeValueForKey(EOQualifier.stringForOperatorSelector(EOQualifier.QualifierOperatorEqual), key);
                if (displayGroup.queryBindings().valueForKey(key) != null) {
                    displayGroup.queryBindings().takeValueForKey(NSKeyValueCoding.NullValue, key);
                } else {
                    displayGroup.queryMatch().takeValueForKey(NSKeyValueCoding.NullValue, key);
                }
            }
        }
    }

}
