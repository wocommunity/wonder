package er.modern.directtoweb.components.query;

import com.webobjects.appserver.WOActionResults;
import com.webobjects.appserver.WOContext;
import com.webobjects.appserver.WOResponse;
import com.webobjects.eoaccess.EODatabaseDataSource;
import com.webobjects.eocontrol.EODataSource;
import com.webobjects.eocontrol.EOQualifier;

import er.ajax.AjaxUtils;
import er.directtoweb.components.ERDCustomQueryComponent;
import er.modern.directtoweb.delegates.ERMD2WAttributeQueryDelegate;
import er.modern.directtoweb.delegates.ERMD2WAttributeQueryDelegate.ERMD2WQueryComponent;

/**
 * An ajax search field for ad-hoc filtering of lists. Similar to
 * ERDAjaxSearchDisplayGroup, but enables filtering over multiple attributes.
 * 
 * Gets displayed when the searchKey D2W key is not null.
 * 
 * @d2wKey searchKey
 * @d2wKey minimumCharacterCount
 * 
 */
public class ERMD2WListFilter extends ERDCustomQueryComponent implements
        ERMD2WQueryComponent {

    private static final long serialVersionUID = 1L;
    
    public interface Keys extends ERDCustomQueryComponent.Keys {
        public static final String searchKey = "searchKey";
        public static final String typeAheadMinimumCharacterCount = "typeAheadMinimumCharacterCount";
    }

    private String _searchValue;

    public ERMD2WListFilter(WOContext context) {
        super(context);
    }

    @Override
    public boolean synchronizesVariablesWithBindings() {
        return false;
    }

    // actions
    public WOActionResults search() {
        EODataSource dataSource = displayGroup().dataSource();
        EOQualifier _qualifier = ERMD2WAttributeQueryDelegate.instance
                .buildQualifier(this);
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

    @Override
    public EODataSource dataSource() {
        return displayGroup().dataSource();
    }

}