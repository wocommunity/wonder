package er.divalite.pages;

import com.webobjects.appserver.WOComponent;
import com.webobjects.appserver.WOContext;
import com.webobjects.directtoweb.D2W;

import er.directtoweb.pages.ERD2WListPage;
import er.extensions.eof.ERXEOControlUtilities;
import er.extensions.eof.ERXGenericRecord;
import er.extensions.foundation.ERXStringUtilities;
import er.extensions.foundation.ERXValueUtilities;

/**
 * Divalite list page
 * 
 * @author ravim
 *
 */
public class ERLITTablePage extends ERD2WListPage {
	public int index;
	
    public ERLITTablePage(WOContext context) {
        super(context);
    }
    
    // accessors   
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
    
    /*
     * detail toggle
     */
    public String onMouseOver() {
    	return "document.getElementById('" + tbodyID() + "').style.display = 'block';";
    }
    
    public String onMouseOut() {
    	return "document.getElementById('" + tbodyID() + "').style.display = 'none';";
    }
    
    public String createActionName() {
    	return "Create" + d2wContext().entity().name();
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
}
