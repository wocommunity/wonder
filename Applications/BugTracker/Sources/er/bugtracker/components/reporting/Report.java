package er.bugtracker.components.reporting;

import org.apache.log4j.Logger;

import com.webobjects.appserver.WOContext;
import com.webobjects.foundation.NSDictionary;
import com.webobjects.foundation.NSTimestamp;

import er.grouping.DRRecordGroup;
import er.reporting.WRReport;

public class Report extends WRReport {
    private static final Logger log = Logger.getLogger(Report.class);

    public Report(WOContext context) {
        super(context);
    }

    /** component does not synchronize it's variables */
    @Override
    public boolean synchronizesVariablesWithBindings() {
        return false;
    }

    @Override
    public Object handleQueryWithUnboundKey(String key) {
        log.error("handleQueryWithUnboundKey: " + key, new RuntimeException("Stacktrace"));
        return null;
    }

    public void handleTakeValueWithUnboundKey(Object o, String key) {
        log.error(key);
    }

    @Override
    public String classAttributeTd() {
        return "WRAttribute" + depth + "Total" + totalCount();
    }

    public NSTimestamp startDate() {
        return (NSTimestamp) valueForBinding("startDate");
    }

    public NSTimestamp endDate() {
        return (NSTimestamp) valueForBinding("endDate");
    }

    @Override
    public DRRecordGroup recordGroup() {
        NSDictionary crds = currentCoordinates();
        DRRecordGroup drg = model().recordGroupForCoordinates(crds);
        return drg;
    }
}
