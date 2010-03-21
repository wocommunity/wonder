package er.diva.pages;

import com.webobjects.appserver.WOComponent;
import com.webobjects.appserver.WOContext;
import com.webobjects.appserver.WOResponse;
import com.webobjects.directtoweb.D2W;
import com.webobjects.foundation.NSDictionary;

import er.ajax.AjaxUtils;
import er.directtoweb.pages.ERD2WListPage;
import er.diva.ERDIVPageInterface;
import er.extensions.eof.ERXEOControlUtilities;
import er.extensions.eof.ERXGenericRecord;
import er.extensions.foundation.ERXProperties;
import er.extensions.foundation.ERXStringUtilities;
import er.extensions.foundation.ERXValueUtilities;

/**
 * An XHTML version of ERD2WListPage.
 * This component constitutes the only use of a HTML table in the entire Diva Look interface
 * 
 * Added features is allowing clickable/collapsing details for each row via the embedded ERXD2WInspect
 * 
 * @property er.prototaculous.useUnobtrusively Support for Unobtrusive Javascript programming.
 *
 * @author mendis
 */
public class ERDIVListPage extends ERD2WListPage implements ERDIVPageInterface {
	private static boolean useUnobtrusively = ERXProperties.booleanForKeyWithDefault("er.prototaculous.useUnobtrusively", true);

	public int index;
	
    public ERDIVListPage(WOContext context) {
        super(context);
    }
    
    // accessors
    public String stylesheet() {
    	return (String) d2wContext().valueForKey(ERDIVPageInterface.Keys.Stylesheet);
    }
    
    public String rowClass() {
    	return isEvenRow() ? null : "odd";
    }
    
    private boolean isEvenRow() {
    	return (index % 2 == 0);
    }
    
    public int colspan() {
    	if (isSelecting()) return 1;
    	else {
    		int colspan = 0;
    		if (!isEntityReadOnly() && isEntityEditable()) colspan++;
    		if (isEntityInspectable()) colspan++;
    		if (isDetailPage()) colspan++;
    		return colspan;
    	}
    }
    
	public boolean isEntityDeletable() {
		return ERXValueUtilities.booleanValueWithDefault(d2wContext().valueForKey("isEntityDeletable"), false);
	}
    
    // FIXME: turn into rule
    public String rowID() {
        String primaryKeyString = ERXEOControlUtilities.primaryKeyStringForObject(object());
        return ERXStringUtilities.safeIdentifierName(d2wContext().entity().name() + primaryKeyString);
    }
    
    public String style() {
    	return ((ERXGenericRecord) object()).isNewObject() ? "display:none;" : null;
    }
    
    // FIXME: turn into rule
    public String tableID() {
    	String pageConfiguration = (String) d2wContext().dynamicPage();
    	return (pageConfiguration != null && pageConfiguration.contains("Embedded")) ? null : "ListTable";
    }
    
    public boolean isDetailPage() {
    	String subTask = (String) d2wContext().valueForKey("subTask");
    	return subTask != null && subTask.equals("detail");
    }
    
    public int detailColspan() {
    	Integer count = (Integer) valueForKeyPath("d2wContext.displayPropertyKeys.count");
		if (isEntityDeletable()) count++;
    	return colspan() + count;
    }
    
    public String tbodyID() {
    	return rowID() + "_detail";
    }
    
    @SuppressWarnings("unchecked")
	public NSDictionary settings() {
        String pc = d2wContext().dynamicPage();
        if (pc != null) {
            return new NSDictionary(pc, "parentPageConfiguration");
        } else return null;
    }
    
    /*
     * detail toggle
     */
    public String onClick() {
    	return "Effect.toggle($('" + tbodyID() + "'), 'slide', {duration: 0.8}); return false;";
    }
    
    // R/R
    @Override
	public void appendToResponse(WOResponse response, WOContext context) {
    	super.appendToResponse(response, context);
    	
    	if (!useUnobtrusively) {
    		// prototype events
    		AjaxUtils.addScriptResourceInHead(context, response, "prototype.js");

    		// add page style sheet
    		if (stylesheet() != null) {
    			AjaxUtils.addStylesheetResourceInHead(context, response, "app", stylesheet());
    		}
    	}
    }
    
    // actions
    /*
     * An excel report of the list
     */
    public WOComponent reportListAction() {
    	ERD2WListPage excelListPage = (ERD2WListPage) D2W.factory().pageForConfigurationNamed("ListExcel" + entityName(), session());
    	excelListPage.setDataSource(dataSource());
    	return excelListPage;
    }
    
    @Override
    public WOComponent backAction() {
    	WOComponent result = nextPageFromDelegate();
    	if (result == null) {
    		result = nextPage();
    		if (result == null) {
    			result = (WOComponent) D2W.factory().pageForConfigurationNamed("AjaxQuery" + entity().name(), session());
    		}
    	}
    	return result;
    }
}
