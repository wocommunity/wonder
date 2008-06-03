package er.bugtracker.components.reporting;

import org.apache.log4j.Logger;

import com.webobjects.appserver.WOContext;



public class ExcelReport extends Report {

    /** logging support */
    private static final Logger log = Logger.getLogger(ExcelReport.class);
	
    /**
     * Public constructor
     * @param context the context
     */
    public ExcelReport(WOContext context) {
        super(context);
    }
}
