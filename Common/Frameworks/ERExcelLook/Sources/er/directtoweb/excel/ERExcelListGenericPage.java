//
// ERExcelListGenericPage.java: Class file for WO Component 'ERExcelListGenericPage'
// Project ERExcelLook
//
// Created by David Scheck on Mon Dec 6, 2006
//
package er.directtoweb.excel;

import com.webobjects.appserver.WOContext;
import com.webobjects.directtoweb.D2WContext;
import com.webobjects.eocontrol.EOArrayDataSource;
import com.webobjects.eocontrol.EODataSource;
import com.webobjects.foundation.NSArray;
import er.extensions.foundation.ERXValueUtilities;
import er.extensions.logging.ERXLogger;

public class ERExcelListGenericPage extends ERExcelListPage {
    public static ERXLogger log = ERXLogger.getERXLogger(ERExcelListGenericPage.class);

    public ERExcelListGenericPage(WOContext context) {
        super(context);
    }

    public boolean shouldShowProperty() {
        D2WContext d2wContext=d2wContext();

        if (log.isDebugEnabled()) {
            log.debug("pageConfiguration <" + d2wContext.valueForKeyPath("pageConfiguration") + ">\n "+
                      "task <"  + d2wContext.task() + ">\n "+
                      "forcedSubTask <"  + d2wContext.valueForKeyPath("forcedSubTask") + ">\n "+
                      "subTask <"  + d2wContext.valueForKeyPath("subTask") + ">\n "+
                      "propertyKey <" + d2wContext.propertyKey() + ">\n "+
                      "genericExport <"  + d2wContext.valueForKeyPath("genericExport") + ">\n "+
                      "skipPropertyDuringExcelExport <"  + d2wContext.valueForKeyPath("skipPropertyDuringExcelExport") + ">\n "+
                      "entityName <" + d2wContext.valueForKeyPath("entity.name") + ">\n "+
                      "displayPropertyKeys <" +d2wContext.valueForKeyPath("displayPropertyKeys")+ ">\n "+
                      "componentName <" + d2wContext().valueForKey("componentName") + ">\n "+
                      "customComponent <" +  d2wContext().valueForKey("customComponentName") + ">");
        }

        String skipPropertyDuringExcelExport=(String) d2wContext.valueForKey("skipPropertyDuringExcelExport");

        return !ERXValueUtilities.booleanValue(skipPropertyDuringExcelExport);
    }

    public String switchedComponentName() {
        return (String) d2wContext().valueForKey("componentName");
    }

    public void setDataSource(EODataSource dataSource) {
        if (dataSource instanceof EOArrayDataSource) {
           // If the datasource is an array datasource then just setup the array and try to get the sort orderings from the prefs
            NSArray sortOrderings=sortOrderings();
            displayGroup().setDataSource(dataSource);
            setSortOrderingsOnDisplayGroup(sortOrderings, displayGroup());
            displayGroup().fetch();
            objects = displayGroup().displayedObjects();
        } else {
            super.setDataSource(dataSource);
        }
    }
}
