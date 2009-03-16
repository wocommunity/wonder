package er.reporting.examples;

import java.util.Date;

import com.webobjects.appserver.WOComponent;
import com.webobjects.appserver.WOContext;
import com.webobjects.appserver.WOResponse;
import com.webobjects.foundation.NSArray;
import com.webobjects.woextensions.WOLongResponsePage;

import er.grouping.DRReportModel;

public class RefreshPage0 extends WOLongResponsePage  {

    public double count;
    public Object start;
    public Object stop;
    public double barValue;

    public NSArray critArray;
    public NSArray attribArray;
    public NSArray records;
    
    public DRReportModel model;

    public RefreshPage0(WOContext c) {
        super(c);
        count = 0;
        barValue = 10;
    }

    public void appendToResponse(WOResponse aResponse, WOContext aContext) {
        count = count+1;
        this.setRefreshInterval(1);
        super.appendToResponse(aResponse, aContext);
    }

    public Object performAction() {
        this.rebuildReport();
        return "DONE";
    }

    public DRReportModel rebuildReport() {
        
        Date ts1 = new Date();
        long ts1L = ts1.getTime();
        //System.out.println("ts1L: "+ts1L);
        model = DRReportModel.withRawRecordsCriteriaListAttributeList(records, critArray, attribArray);
        Date ts2 = new Date();
        long ts2L = ts2.getTime();
        //System.out.println("ts2L: "+ts1L);
        long delta = ts2L - ts1L;
        System.out.println("Report building delta: "+(double)delta/(double)1000.0);
        
        
        return model;
    }

    public WOComponent refreshPageForStatus(Object status) {
        if (count < 2) {
            barValue += 10;
        } else {
            barValue += 30/count;
        }

        return this;
    }

    public WOComponent pageForResult(Object result) {
        WOComponent resultPage = (WOComponent)this.pageWithName("Report");
        Session s = (Session)session();
        s.setModel(model);
        return resultPage;
    }

}