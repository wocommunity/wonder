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
 * but enables filtering over multiple attributes and/or a pop-up list of choices.
 * 
 * Gets displayed when either or both of searchKey and restrictedChoiceKey D2W keys is not null.
 * 
 * @d2wKey searchKey - either a single target key as a string or an array with multiple keys
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
    private String _searchValue;

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
        EODataSource dataSource = displayGroup().dataSource();
        EOQualifier _qualifier = ERMD2WAttributeQueryDelegate.instance
                .buildQualifier(this);
        
        String filterChoicesKey = (String) d2wContext().valueForKey(Keys.restrictedChoiceTargetKey);
        if (!ERXStringUtilities.stringIsNullOrEmpty(filterChoicesKey)
                && filterChoice() != null) {
            if (d2wContext().valueForKey(Keys.restrictedChoiceRecursionKey) != null) {
                String recursionKey = (String) d2wContext()
                        .valueForKey(Keys.restrictedChoiceRecursionKey);
                NSMutableArray deepChoices = new NSMutableArray(filterChoice());
                deepChoices.addObjects(NSKeyValueCoding.Utility
                        .valueForKey(filterChoice(), recursionKey));
                _qualifier = ERXQ.and(_qualifier, ERXQ.in(filterChoicesKey, ERXArrayUtilities.flatten(deepChoices)));
            } else {
                _qualifier = ERXQ.and(_qualifier,
                        ERXQ.equals(filterChoicesKey, filterChoice()));
            }
        }
        
        ((EODatabaseDataSource) dataSource).setAuxiliaryQualifier(_qualifier);
        ((EODatabaseDataSource) displayGroup().dataSource()).fetchSpecification()
                .setUsesDistinct(true);
        displayGroup().fetch();
        displayGroup().setCurrentBatchIndex(1);
        return null;
    }

    public void appendToResponse(WOResponse response, WOContext context) {
        AjaxUtils.addScriptResourceInHead(context, response, "prototype.js");
        super.appendToResponse(response, context);
    }

    public void setSearchValue(String searchValue) {
        _searchValue = searchValue;
    }

    @Override
    public String searchValue() {
        return _searchValue;
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