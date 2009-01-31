/*
* David Scheck,  Dec 6, 2006
*/
package er.directtoweb.excel;

import com.webobjects.foundation.*;
import com.webobjects.appserver.*;
import com.webobjects.eocontrol.*;
import com.webobjects.eoaccess.*;
import com.webobjects.directtoweb.*;
import er.extensions.ERXLogger;
import er.directtoweb.*;

import com.webobjects.appserver.WOComponent;
import com.webobjects.appserver.WOContext;
import com.webobjects.appserver.WODisplayGroup;
import com.webobjects.directtoweb.D2W;
import com.webobjects.directtoweb.D2WContext;
import com.webobjects.directtoweb.D2WPage;
import com.webobjects.directtoweb.ListPageInterface;
import com.webobjects.eocontrol.EOArrayDataSource;
import com.webobjects.eocontrol.EOClassDescription;
import er.extensions.ERXValueUtilities;
/**
 * Component used in the List page to provide a generic excel export.<br />
 * 
 * @binding d2wContext
 * @binding displayGroup
 */

public class ERExcelExportButton extends ERDCustomQueryComponent {

    public ERExcelExportButton(WOContext context) { super(context); }

    private final static ERXLogger log = ERXLogger.getERXLogger(ERExcelExportButton.class);

    public boolean isStateless() { return true; }
    public boolean synchronizesVariablesWithBindings() { return false; }

    public boolean showExportDisplayed() {
        return displayGroup().hasMultipleBatches() || (displayGroup().qualifier() != null);
    }

    public WOComponent excelExportDisplayed() {
        EOArrayDataSource ds=new EOArrayDataSource(EOClassDescription.classDescriptionForEntityName(d2wContext().entity().name()), displayGroup().dataSource().editingContext());
        ds.setArray(displayGroup().displayedObjects());
        return excelExport(ds);
    }

    public WOComponent excelExportAll() {
        EOArrayDataSource ds=new EOArrayDataSource(EOClassDescription.classDescriptionForEntityName(d2wContext().entity().name()), displayGroup().dataSource().editingContext());
        ds.setArray(displayGroup().allObjects());
        return excelExport(ds);
    }

    public WOComponent excelExport(EODataSource ds){
        D2WContext context=d2wContext();

        D2WContext newContext=new D2WContext(context);
        newContext.takeValueForKey(context.task(),"task");

        // not using subTask directly here because the cache mechanism relies on being able to compute wether this key
        // is 'computable' (subTask is since a rule can fire to give a default) or an external output
        newContext.takeValueForKey("excel","forcedSubTask");
        newContext.takeValueForKey(context.valueForKey("pageName"),"existingPageName");
        newContext.takeValueForKey(context.valueForKey("subTask"),"existingSubTask");
        newContext.takeValueForKey(context.valueForKey("pageConfiguration"),"pageConfiguration");
        newContext.takeValueForKey(context.entity(),"entity");
        newContext.takeValueForKey("true","genericExport");
        newContext.takeValueForKey(context.entity().name(),"genericExcelSheetName");

        String pageName = (String)newContext.valueForKey("pageName");

        WOComponent result=WOApplication.application().pageWithName(pageName,session().context());
        ((D2WPage)result).setLocalContext(newContext);

        ((D2WPage)result).setDataSource(ds);

        return result;
    }
}
