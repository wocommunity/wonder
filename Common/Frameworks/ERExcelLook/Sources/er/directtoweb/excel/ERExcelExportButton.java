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

import java.io.*;
import java.lang.reflect.*;

/**
 * Component used in the List page to provide a generic excel export.<br />
 * 
 * @binding d2wContext
 * @binding displayGroup
 */

public class ERExcelExportButton extends ERDCustomQueryComponent {

    public static NSSelector DisplayedObjectsForExcelSelector = new NSSelector("displayedObjectsForExcelExport");
    public static NSSelector AllObjectsForExcelSelector = new NSSelector("allObjectsForExcelExport");

    public ERExcelExportButton(WOContext context) { super(context); }

    private final static ERXLogger log = ERXLogger.getERXLogger(ERExcelExportButton.class);

    public boolean isStateless() { return true; }
    public boolean synchronizesVariablesWithBindings() { return false; }

    public boolean showExportDisplayed() {
        return displayGroup().hasMultipleBatches() || (displayGroup().qualifier() != null);
    }

    public WOComponent excelExportDisplayed() {
        NSArray array = null;

        EOArrayDataSource ds=new EOArrayDataSource(EOClassDescription.classDescriptionForEntityName(d2wContext().entity().name()), displayGroup().dataSource().editingContext());

        Object delegate = d2wContext().valueForKey("excelExportDelegate");

        if (DisplayedObjectsForExcelSelector.implementedByObject(delegate)) {
            // Needed this for custom template pages that display more item than whats in the standard displayGroup
            try {
                array = (NSArray) DisplayedObjectsForExcelSelector.invoke(delegate);
            }
            catch (IllegalAccessException e) {

            }
            catch (IllegalArgumentException e) {

            }
            catch (InvocationTargetException e) {

            }
            catch (NoSuchMethodException e) {

            }
        }
        else {
            array = displayGroup().displayedObjects();
        }

        if (log.isDebugEnabled()) log.debug("displayedObjectsForExcel = "+array);

        ds.setArray(array);

        return excelExport(ds);
    }

    public WOComponent excelExportAll() {
        NSArray array = null;

        EOArrayDataSource ds=new EOArrayDataSource(EOClassDescription.classDescriptionForEntityName(d2wContext().entity().name()), displayGroup().dataSource().editingContext());

        Object delegate = d2wContext().valueForKey("excelExportDelegate");

        if (AllObjectsForExcelSelector.implementedByObject(delegate)) {
            // Needed this for custom template pages that display more item than whats in the standard displayGroup
            try {
                array = (NSArray) AllObjectsForExcelSelector.invoke(delegate);
            }
            catch (IllegalAccessException e) {

            }
            catch (IllegalArgumentException e) {

            }
            catch (InvocationTargetException e) {

            }
            catch (NoSuchMethodException e) {

            }
        }
        else {
            array = displayGroup().allObjects();
        }

        if (log.isDebugEnabled()) log.debug("allObjectsForExcel = "+array);

        ds.setArray(array);

        return excelExport(ds);
    }

    public WOComponent excelExport(EODataSource ds){
        D2WContext context=d2wContext();

        if (log.isDebugEnabled()) log.debug("ds all objects = "+ds.fetchObjects());

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
