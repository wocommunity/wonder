package er.modern.directtoweb.components.query;

import com.webobjects.appserver.WOActionResults;
import com.webobjects.appserver.WOContext;
import com.webobjects.appserver.WOResponse;
import com.webobjects.eoaccess.EODatabaseDataSource;
import com.webobjects.eocontrol.EODataSource;
import com.webobjects.eocontrol.EOQualifier;
import com.webobjects.foundation.NSArray;
import com.webobjects.foundation.NSKeyValueCoding;
import com.webobjects.foundation.NSMutableArray;
import com.webobjects.foundation.NSMutableDictionary;

import er.ajax.AjaxUtils;
import er.directtoweb.components.ERDCustomQueryComponent;
import er.extensions.eof.ERXQ;
import er.extensions.foundation.ERXArrayUtilities;
import er.extensions.foundation.ERXStringUtilities;
import er.extensions.localization.ERXLocalizer;
import er.modern.directtoweb.delegates.ERMD2WAttributeQueryDelegate;
import er.modern.directtoweb.delegates.ERMD2WAttributeQueryDelegate.ERMD2WQueryComponent;

/**
 * Ajax-enabled ad-hoc filtering of lists. Similar to ERDAjaxSearchDisplayGroup,
 * but enables filtering over multiple attributes using either a single or
 * multiple search fields and/or a pop-up list of choices.
 * 
 * Gets displayed when either or both of searchKey and restrictedChoiceKey D2W
 * keys is not null. If the search key is an array of arrays of keys, a separate
 * search field will be rendered for each array of keys and the individual
 * qualifiers generated for each field will be ANDed.
 * 
 * @d2wKey searchKey - either a single target key as a string, an array with multiple keys or an array of arrays with keys
 * @d2wKey restrictedChoiceKey - key path that will return a list of filter choices, note that no "object" will be available!
 * @d2wKey keyWhenRelationship - specifies the display key on the choice
 * @d2wKey noSelectionString - "no selection" string to show on the restricted choice pop-up
 * @d2wKey restrictedChoiceTargetKey - target key path on which to qualify with the choice
 * @d2wKey restrictedChoiceRecursionKey - key path from which to retrieve descendant choices, think "allChildrenCategories"
 * @d2wKey typeAheadMinimumCharacterCount
 * 
 */
public class ERMD2WListFilter extends ERDCustomQueryComponent implements
        ERMD2WQueryComponent {

    private static final long serialVersionUID = 1L;
    
    public interface Keys extends ERDCustomQueryComponent.Keys {
        public static final String restrictedChoiceKey = "restrictedChoiceKey";
        public static final String keyWhenRelationship = "keyWhenRelationship";
        public static final String noSelectionString = "noSelectionString";
        public static final String restrictedChoiceTargetKey = "restrictedChoiceTargetKey";
        public static final String restrictedChoiceRecursionKey = "restrictedChoiceRecursionKey";
        public static final String searchKey = "searchKey";
        public static final String typeAheadMinimumCharacterCount = "typeAheadMinimumCharacterCount";
    }

    private Object _filterChoice;
    
    private NSMutableDictionary<String, String> _searchValues = new NSMutableDictionary<>();

    private NSMutableDictionary<String, EOQualifier> _qualifiers = new NSMutableDictionary<>();

    public Integer searchFieldIndex = 0;

    public Object filterChoiceItem;

    public ERMD2WListFilter(WOContext context) {
        super(context);
    }

    @Override
    public boolean synchronizesVariablesWithBindings() {
        return false;
    }
    
    /**
     * @return true if searchKey or filterChoices is not null
     */
    public boolean shouldShow() {
        return d2wContext().valueForKey(Keys.searchKey) != null || d2wContext().valueForKey(Keys.restrictedChoiceKey) != null;
    }

    // actions
    @SuppressWarnings({ "rawtypes", "unchecked" })
    public WOActionResults search() {
        // get the qualifier for the current search field
        EOQualifier qualifier = ERMD2WAttributeQueryDelegate.instance
                .buildQualifier(this, searchFieldIndex);

        // store the qualifier for this search field
        _qualifiers.takeValueForKey(qualifier, searchFieldIndex.toString());
        
        // "and" active qualifiers from all search fields
        qualifier = ERXQ.and(_qualifiers.allValues());
        
        String filterChoicesKey = (String) d2wContext().valueForKey(Keys.restrictedChoiceTargetKey);
        if (!ERXStringUtilities.stringIsNullOrEmpty(filterChoicesKey)
                && filterChoice() != null) {
            if (d2wContext().valueForKey(Keys.restrictedChoiceRecursionKey) != null) {
                String recursionKey = (String) d2wContext()
                        .valueForKey(Keys.restrictedChoiceRecursionKey);
                NSMutableArray deepChoices = new NSMutableArray(filterChoice());
                deepChoices.addObjects(NSKeyValueCoding.Utility
                        .valueForKey(filterChoice(), recursionKey));
                qualifier = ERXQ.and(qualifier, ERXQ.in(filterChoicesKey, ERXArrayUtilities.flatten(deepChoices)));
            } else {
                qualifier = ERXQ.and(qualifier,
                        ERXQ.equals(filterChoicesKey, filterChoice()));
            }
        }
        
        // qualify on the data source if it's a DB data source
        if (displayGroup().dataSource() instanceof EODatabaseDataSource) {
            EODatabaseDataSource dbds = (EODatabaseDataSource) displayGroup().dataSource();
            dbds.setAuxiliaryQualifier(qualifier);
            dbds.fetchSpecification().setUsesDistinct(true);
        } else {
            displayGroup().setQualifier(qualifier);
        }
        displayGroup().fetch();
        displayGroup().setCurrentBatchIndex(1);
        return null;
    }

    public void appendToResponse(WOResponse response, WOContext context) {
        AjaxUtils.addScriptResourceInHead(context, response, "prototype.js");
        super.appendToResponse(response, context);
    }
    
    /**
     * @return true if multiple search fields have been defined
     */
    public boolean hasMultipleFields() {
        boolean hasMultipleFields = false;
        if (d2wContext().valueForKey("searchKey") instanceof NSArray<?>) {
            NSArray<?> rawValue = (NSArray<?>) d2wContext().valueForKey("searchKey");
            if (rawValue.objectAtIndex(0) instanceof NSArray<?>) {
                hasMultipleFields = true;
            }
        }
        return hasMultipleFields;
    }
    
    @SuppressWarnings("unchecked")
    public NSArray<NSArray<String>> searchKeyGroups() {
        NSArray<NSArray<String>> searchKeyGroups = null;
        if ((d2wContext().valueForKey("searchKey") instanceof NSArray<?>)) {
            NSArray<?> rawValue = (NSArray<?>) d2wContext().valueForKey("searchKey");
            if (rawValue.objectAtIndex(0) instanceof NSArray<?>) {
                searchKeyGroups = (NSArray<NSArray<String>>) d2wContext().valueForKey("searchKey");
            } else {
                NSArray<String> searchKey = (NSArray<String>) d2wContext()
                        .valueForKey("searchKey");
                searchKeyGroups = new NSArray<NSArray<String>>(searchKey);
            }
        }
        return searchKeyGroups;
    }
    
    public int searchKeyGroupCount() {
        return searchKeyGroups().count();
    }
    
    public NSArray<String> searchKey() {
        return ERMD2WAttributeQueryDelegate.instance.searchKey(this, searchFieldIndex);
    }
    
    public String localizedSearchKey() {
        String searchKeyList = searchKey().componentsJoinedByString(",");
        return ERXLocalizer.currentLocalizer()
                .localizedStringForKeyWithDefault("ERMD2WListFilter.searchKey."
                        + searchKeyList);
    }
    
    /**
     * @return a class list that allows targeting of individual search fields
     */
    public String searchFieldCssClass() {
        String searchFieldCssClass = "ListFilterSearch";
        if (hasMultipleFields()) {
            searchFieldCssClass = searchFieldCssClass.concat(" ListFilterSearch_" + searchFieldIndex);
        }
        return searchFieldCssClass;
    }
    
    public void setSearchValue(String searchValue) {
        // set the search value for the current field
        if (searchValue == null) {
            _searchValues.removeObjectForKey(searchFieldIndex.toString());
        } else {
            _searchValues.setObjectForKey(searchValue, searchFieldIndex.toString());
        }
    }

    @Override
    public String searchValue() {
        // retrieve the search value for the current field
        return _searchValues.objectForKey(searchFieldIndex.toString());
    }

    public void setFilterChoice(Object filterChoice) {
        _filterChoice = filterChoice;
    }

    public Object filterChoice() {
        return _filterChoice;
    }
    
    public String filterChoicesDisplayString() {
        String displayKey = (String) d2wContext().valueForKey(Keys.keyWhenRelationship);
        return (String) NSKeyValueCoding.Utility.valueForKey(filterChoiceItem, displayKey);
    }

    public String noSelectionString() {
        String noSelectionString = (String) d2wContext().valueForKey(Keys.noSelectionString);
        noSelectionString = ERXLocalizer.currentLocalizer().localizedStringForKey(noSelectionString);
        return noSelectionString;
    }

    @SuppressWarnings({ "rawtypes" })
    public NSArray filterChoices() {
        NSArray filterChoices = NSArray.emptyArray();
        if (d2wContext().valueForKey(Keys.restrictedChoiceKey) != null) {
            String filterChoicesKey = (String) d2wContext()
                    .valueForKey(Keys.restrictedChoiceKey);
            filterChoices = (NSArray) valueForKeyPath(filterChoicesKey);
        }
        return filterChoices;
    }

    @Override
    public EODataSource dataSource() {
        return displayGroup().dataSource();
    }

    /**
     * @return dynamic CSS class attribute, depending on whether both searchKey
     *         and filterChoices are to be shown
     */
    public String wrapperClass() {
        String wrapperClass = "ListFilter";
        if (d2wContext().valueForKey(Keys.searchKey) != null && d2wContext().valueForKey(Keys.restrictedChoiceKey) != null) {
            wrapperClass = wrapperClass.concat(" ComboListFilter");
        }
        return wrapperClass;
    }
}