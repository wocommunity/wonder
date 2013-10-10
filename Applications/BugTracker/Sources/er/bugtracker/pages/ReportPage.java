package er.bugtracker.pages;

import com.webobjects.appserver.WOComponent;
import com.webobjects.appserver.WOContext;
import com.webobjects.appserver.WOResponse;
import com.webobjects.eoaccess.EOUtilities;
import com.webobjects.foundation.NSArray;
import com.webobjects.foundation.NSData;
import com.webobjects.foundation.NSDictionary;
import com.webobjects.foundation.NSNotificationCenter;

import er.directtoweb.pages.ERD2WPage;
import er.extensions.foundation.ERXFileUtilities;
import er.grouping.DRReportModel;

// this doesn't need to be a D2W page, but it's more practical this way.
public class ReportPage extends ERD2WPage {

    public DRReportModel model;
    public NSArray objects;
    public NSData reportData;
    public boolean enableExcel = false;
    public boolean enableChart = false;
    public NSDictionary _selectedReport;
    public String currentName;
    
    public ReportPage(WOContext context) {
        super(context);
    }
    
    public String reportComponentName() {
        if(enableExcel) {
            return "ExcelReport";
        } else if(enableChart) {
            return "ChartReport";
        }
        return "WRReport";
    }

    public void postModelChangedNotification(DRReportModel model) {
        if (model != null) {
            log.info("postModel: " + model.hashCode());
            NSNotificationCenter.defaultCenter().postNotification(DRReportModel.DRReportModelRebuildNotification, model, null);
        }
    }
 
    public WOComponent exportExcelAction() {
        enableExcel = true;
        return context().page();
    }
    
    public NSDictionary selectedReport() {
        if(_selectedReport == null) {
            setSelectedReportName("ReportBugsPerUser");
        }
        return _selectedReport;
    }
    
    public void setSelectedReportName(String reportName) {
        _selectedReport = (NSDictionary)reports().objectForKey(reportName);
        postModelChangedNotification(model);
    }

    public void selectReport() {
        setSelectedReportName(currentName);
    }
    
    public NSDictionary reports() {
        return (NSDictionary)ERXFileUtilities.readPropertyListFromFileInFramework("Reports.plist", null);
    }

    public WOComponent showAsChartAction() {
        enableChart = true;
        return context().page();
    }
    
    public WOComponent showAsTableAction() {
        enableChart = false;
        return context().page();
    }
    
    @Override
    public void appendToResponse(WOResponse response, WOContext context) {
        super.appendToResponse(response, context);
        if(reportData != null) {
            response.setContent(reportData);
            reportData = null;
            String fileName = "results.xls";
            response.setHeader("inline; filename=\"" + fileName + "\"", "content-disposition");
            response.setHeader("application/vnd.ms-excel", "content-type");
            enableExcel = false;
        }
    }
    
    public NSArray objects() {
        if(objects == null) {
            objects = EOUtilities.objectsForEntityNamed(session().defaultEditingContext(), entityName());
        }
        return objects;
    }
}
