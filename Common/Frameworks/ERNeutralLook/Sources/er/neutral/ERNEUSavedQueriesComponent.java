package er.neutral;

import com.webobjects.appserver.WOComponent;
import com.webobjects.appserver.WOContext;
import com.webobjects.appserver.WODisplayGroup;
import com.webobjects.directtoweb.D2WContext;
import com.webobjects.directtoweb.D2WModel;
import com.webobjects.foundation.NSMutableArray;
import er.directtoweb.ERDSavedQueriesComponent;
import com.webobjects.eocontrol.*;
import com.webobjects.foundation.*;
import er.extensions.foundation.*;
import er.directtoweb.ERD2WQueryPage;
import er.extensions.foundation.ERXProperties;
import er.extensions.logging.ERXLogger;

import java.util.Enumeration;

/**
 * This Component will store the forms values in the displayGroup of a Query Page into user preferences
 * under a saved name.  The list of saved queries will be available in the popup for future use.
 *
 * Preference key = "SavedQueryFor"+pageConfiguration
 *
 * The ERNEUQueryPage has been modified to switch in the value of the rule keyPath = "savedQueryComponentName"
 * So, if you want this component to appear on your query page, create a rule defining savedQueryComponentName="ERNEUSavedQueriesComponent"
 *
 * User: dscheck
 */

public class ERNEUSavedQueriesComponent extends ERDSavedQueriesComponent {
    public static final ERXLogger log = ERXLogger.getERXLogger(ERNEUSavedQueriesComponent.class);

    //  For backward compatibility we need er.neutral.ERNEUSavedQueriesComponent.SavedQuery
    //  So that previously saved preferences can still be decoded
    //  The EOKeyValueUnarchiver uses the class name stored in the saved dictionary to instantiate the destination class

    static class SavedQuery extends ERDSavedQueriesComponent.SavedQuery {
        public SavedQuery() {
            super();
        }

        public SavedQuery(String name, WODisplayGroup displayGroup) {
            super(name, displayGroup);
        }
    }

    public ERNEUSavedQueriesComponent(WOContext context) {
        super(context);
    }

    private NSMutableArray _savedQueries=null;
    public ERDSavedQueriesComponent.SavedQuery aSavedQuery=null;
    public ERDSavedQueriesComponent.SavedQuery selectedSavedQuery=null;
    public String newQueryName=null;
    public final String DEFAULT_QUERY_NONE="";
    public boolean needsAutoSubmit = false;

    public String userPreferenceNameForPageConfiguration(String pageConfiguration) {
        return "SavedQueryFor:"+pageConfiguration;
    }

    public String userPreferenceNameForDefaultQueryWithPageConfiguration(String pageConfiguration) {
        return "DefaultQueryNameFor:"+pageConfiguration;
    }

    public String userPreferenceNameForAutoSubmitWithPageConfiguration(String pageConfiguration) {
        return "AutoSubmitQueryFor:"+pageConfiguration;
    }

    public NSMutableArray loadSavedQueriesForPageConfigurationNamed(String pageConfigurationName) {
        NSArray savedQueries=null;

        EOKeyValueArchiving.Support.setSupportForClass(newEOKVArchiningTimestampSupport, NSTimestamp._CLASS);

        try {
            savedQueries = (NSArray) userPreferences().valueForKey(userPreferenceNameForPageConfiguration(pageConfigurationName));
        }
        finally {
            EOKeyValueArchiving.Support.setSupportForClass(originalEOKVArchiningTimestampSupport, NSTimestamp._CLASS);
        }

        if (log.isDebugEnabled()) log.debug("loadSavedQueriesForPageConfigurationNamed("+pageConfigurationName+"): queries = "+savedQueries);

        return savedQueries == null ? new NSMutableArray(): savedQueries.mutableClone();
    }

    public void saveQueriesForPageConfigurationNamed(NSArray queries, String pageConfigurationName) {
        if (log.isDebugEnabled()) log.debug("saveQueriesForPageConfigurationNamed("+pageConfigurationName+"): queries = "+queries);


        EOKeyValueArchiving.Support.setSupportForClass(newEOKVArchiningTimestampSupport, NSTimestamp._CLASS);

        try {
            userPreferences().takeValueForKey(queries,userPreferenceNameForPageConfiguration(pageConfigurationName));
        }
        finally {
            EOKeyValueArchiving.Support.setSupportForClass(originalEOKVArchiningTimestampSupport, NSTimestamp._CLASS);
        }
    }

    /** component does not synchronize variables */
    public boolean synchronizesVariablesWithBindings() {
        return false;
    }

    /** component is not stateless */
    public boolean isStateless() {
        return false;
    }

    public void sleep() {
        needsAutoSubmit=false;
        super.sleep();
    }

    public D2WContext d2wContext() {
        return (D2WContext) valueForBinding("localContext");
    }

    public WODisplayGroup displayGroup() {
        return (WODisplayGroup) valueForBinding("displayGroup");
    }

    public String pageConfiguration() {
        return (String) d2wContext().valueForKey(D2WModel.DynamicPageKey);
    }

    public NSMutableArray savedQueries() {
        if (_savedQueries == null) {
            String pageConfiguration=pageConfiguration();

            _savedQueries=loadSavedQueriesForPageConfigurationNamed(pageConfiguration);

            String defaultName=defaultQueryNameForPageConfiguration(pageConfiguration);

            selectedSavedQuery=null;

            // Try to setup the default query if it is set.
            if (_savedQueries.count() > 0 && !DEFAULT_QUERY_NONE.equals(defaultName)) {
                for (Enumeration queryEnum=_savedQueries.objectEnumerator(); queryEnum.hasMoreElements();) {
                    ERDSavedQueriesComponent.SavedQuery aQuery=(ERDSavedQueriesComponent.SavedQuery) queryEnum.nextElement();

                    if (defaultName.equals(aQuery.name()))  {
                        selectedSavedQuery=aQuery;

                        // since this is the inital population dont loose anything that is already set there from the query setup code
                        selectedSavedQuery.sendValuesToDisplayGroup(displayGroup(), false);

                        newQueryName=selectedSavedQuery.name();

                        break;
                    }

                }
            }
        }

        return _savedQueries;
    }


    private static ERDSavedQueriesComponent.SavedQuery emptySavedQueryForDeletes=new ERNEUSavedQueriesComponent.SavedQuery();

    public WOComponent popupChangedSelection() {
        if (selectedSavedQuery != null) {
            selectedSavedQuery.sendValuesToDisplayGroup(displayGroup(), true);
            newQueryName=selectedSavedQuery.name();

            needsAutoSubmit=autoSubmitEnabled();
        }
        else  {
            clearForm();
        }

        return null;
    }

    public WOComponent refresh() {
        return context().page();
    }

    public boolean autoSubmitEnabled() {
        return ERXValueUtilities.booleanValue(userPreferences().valueForKey(userPreferenceNameForAutoSubmitWithPageConfiguration(pageConfiguration())));
    }

    public void setAutoSubmitEnabled(boolean b) {
        userPreferences().takeValueForKey((b?"1":"0"), userPreferenceNameForAutoSubmitWithPageConfiguration(pageConfiguration()));
    }

    public WOComponent addNewQuery() {
        ERDSavedQueriesComponent.SavedQuery newQuery = new ERNEUSavedQueriesComponent.SavedQuery(newQueryName, displayGroup());

        _savedQueries.addObject(newQuery);

        selectedSavedQuery=newQuery;

        saveQueriesForPageConfigurationNamed(_savedQueries, pageConfiguration());

        return null;
    }

    public WOComponent updateCurrentQuery() {
        if (newQueryName != null && newQueryName.trim().length() >0) {
            if (isDefaultQuery(selectedSavedQuery))
                setDefaultQueryNameForPageConfiguration(newQueryName, pageConfiguration());

            selectedSavedQuery.setName(newQueryName);
        }

        selectedSavedQuery.getValuesFromDisplayGroup(displayGroup());

        if (ERXProperties.booleanForKeyWithDefault("er.neutral.ERDSavedQueriesComponent.saveAfterUpdating", true))
            saveQueriesForPageConfigurationNamed(_savedQueries, pageConfiguration());

        return null;
    }

    public WOComponent deleteCurrentQuery() {
        if (selectedSavedQuery != null) {
            _savedQueries.removeObject(selectedSavedQuery);

            saveQueriesForPageConfigurationNamed(_savedQueries, pageConfiguration());

            if (isDefaultQuery(selectedSavedQuery))
                setDefaultQueryNameForPageConfiguration("", pageConfiguration());

            selectedSavedQuery=null;
        }

        return clearForm();
    }

    public WOComponent deleteAllSavedQueries() {
        _savedQueries.removeAllObjects();

        saveQueriesForPageConfigurationNamed(_savedQueries, pageConfiguration());

        selectedSavedQuery=null;

        return clearForm();
    }

    public WOComponent clearForm() {
        selectedSavedQuery=null;
        newQueryName=null;

        emptySavedQueryForDeletes.sendValuesToDisplayGroup(displayGroup(), true);

        return null;
    }

    public WOComponent makeDefaultSavedQuery() {
        String aQueryName=(selectedSavedQuery != null) ?selectedSavedQuery.name():DEFAULT_QUERY_NONE;

        setDefaultQueryNameForPageConfiguration(aQueryName, pageConfiguration());

        return null;
    }

    public String defaultQueryNameForPageConfiguration(String pageConfigurationName) {
        String defaultQueryName=(String) userPreferences().valueForKey(userPreferenceNameForDefaultQueryWithPageConfiguration(pageConfigurationName));

        if (log.isDebugEnabled()) log.debug("defaultQueryNameForPageConfiguration("+pageConfigurationName+"): defaultQueryName = "+defaultQueryName);

        return (defaultQueryName != null)?defaultQueryName:DEFAULT_QUERY_NONE;
    }

    public void setDefaultQueryNameForPageConfiguration(String aName, String pageConfigurationName) {
        userPreferences().takeValueForKey(aName,userPreferenceNameForDefaultQueryWithPageConfiguration(pageConfigurationName));

        if (log.isDebugEnabled()) log.debug("setDefaultQueryNameForPageConfiguration("+pageConfigurationName+"): defaultQueryName = "+aName);
    }

    public boolean isSelectedQueryTheDefault() {
        return isDefaultQuery(selectedSavedQuery);
    }

    public boolean isNoSelectedQuery() {
        return selectedSavedQuery == null;
    }

    public boolean hasNoSavedQueries() {
        return _savedQueries.count() == 0;
    }

    public boolean isDefaultQuery(ERDSavedQueriesComponent.SavedQuery aQuery) {
        String queryName=(aQuery != null)?aQuery.name():DEFAULT_QUERY_NONE;
        return queryName.equals(defaultQueryNameForPageConfiguration(pageConfiguration()));
    }

}
