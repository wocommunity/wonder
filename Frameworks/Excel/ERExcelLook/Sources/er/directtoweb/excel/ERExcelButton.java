package er.directtoweb.excel;

import com.webobjects.appserver.WOApplication;
import com.webobjects.appserver.WOComponent;
import com.webobjects.appserver.WOContext;
import com.webobjects.appserver.WODisplayGroup;
import com.webobjects.appserver.WOSession;
import com.webobjects.directtoweb.D2WContext;
import com.webobjects.directtoweb.D2WListPage;
import com.webobjects.directtoweb.D2WPage;
import com.webobjects.directtoweb.ERD2WContext;
import com.webobjects.directtoweb.EditPageInterface;
import com.webobjects.directtoweb.ListPageInterface;
import com.webobjects.eocontrol.EODataSource;

import er.directtoweb.ERD2WFactory;
import er.directtoweb.components.buttons.ERDActionButton;
import er.directtoweb.interfaces.ERDListPageInterface;
import er.extensions.batching.ERXBatchingDisplayGroup;
import er.extensions.eof.ERXEOControlUtilities;

public class ERExcelButton extends ERDActionButton {
	/**
	 * Do I need to update serialVersionUID?
	 * See section 5.6 <cite>Type Changes Affecting Serialization</cite> on page 51 of the 
	 * <a href="http://java.sun.com/j2se/1.4/pdf/serial-spec.pdf">Java Object Serialization Spec</a>
	 */
	private static final long serialVersionUID = 1L;

    public ERExcelButton(WOContext context) {
        super(context);
    }

    public boolean show() {
        return booleanValueForBinding("showExcelExport", true);
    }
    
    public WOComponent printerFriendlyVersion() {
        return listPrinterFriendlyVersion();
    }

    public WOComponent editPrinterFriendlyVersion() {
        WOComponent result=ERD2WFactory.erFactory().printerFriendlyPageForD2WContext(d2wContext(),session());
        ((EditPageInterface)result).setObject(object());
        return result;
    }

    public WOComponent listPrinterFriendlyVersion() {
        D2WContext d2wContext = d2wContext();
        WOSession session = session();
        WODisplayGroup displayGroup = displayGroup();
        EODataSource dataSource = dataSource();
        if(dataSource == null) {
        	if (displayGroup instanceof ERXBatchingDisplayGroup) {
				ERXBatchingDisplayGroup dg = (ERXBatchingDisplayGroup) displayGroup;
	            dataSource = ERXEOControlUtilities.dataSourceForArray(displayGroup.displayedObjects());
			} else {
	            dataSource = ERXEOControlUtilities.dataSourceForArray(displayGroup.allObjects());
			}
        }
        D2WContext newContext = ERD2WContext.newContext(session);
        String newTask = d2wContext.task().equals("edit") ? "inspect" : d2wContext.task();
        // for editable list pages...
        if("list".equals(d2wContext().valueForKey("subTask"))) {
            newTask = "list";
        }
        newContext.takeValueForKey(newTask, "task");
        // not using subTask directly here because the cache mechanism relies on
        // being able to compute wether this key
        // is 'computable' (subTask is since a rule can fire to give a default)
        // or an external output
        //        newContext.takeValueForKey("excel","subTask");
        newContext.takeValueForKey("excel", "forcedSubTask");
        newContext.takeValueForKey(d2wContext.valueForKey("pageName"), "existingPageName");
        newContext.takeValueForKey(d2wContext.valueForKey("subTask"), "existingSubTask");
        newContext.takeValueForKey(d2wContext.valueForKey("pageConfiguration"), "pageConfiguration");
        newContext.takeValueForKey(d2wContext.entity(), "entity");
        ListPageInterface result = (ListPageInterface) WOApplication.application().pageWithName((String) newContext.valueForKey("pageName"), session.context());
        ((D2WPage) result).setLocalContext(newContext);

        result.setDataSource(dataSource);
        WODisplayGroup dg = null;
        if (result instanceof D2WListPage) {
            dg = ((D2WListPage) result).displayGroup();
        } else if (result instanceof ERDListPageInterface) {
            dg = ((ERDListPageInterface) result).displayGroup();
        } else {
            dg = (WODisplayGroup) ((WOComponent) result).valueForKey("displayGroup");
        }
        if (dg != null) {
            dg.setSortOrderings(displayGroup.sortOrderings());
            dg.setNumberOfObjectsPerBatch(displayGroup.allObjects().count());
            dg.updateDisplayedObjects();
        }
        return (WOComponent) result;
    }
}
