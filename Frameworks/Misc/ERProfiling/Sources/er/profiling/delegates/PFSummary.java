package er.profiling.delegates;

import java.text.DecimalFormat;
import java.util.HashSet;
import java.util.Set;
import java.util.UUID;

import com.webobjects.appserver.WOContext;
import com.webobjects.appserver.WOElement;
import com.webobjects.appserver.WORequest;
import com.webobjects.appserver.WOResponse;

import er.extensions.foundation.ERXProperties;
import er.profiling.PFProfiler;
import er.profiling.PFStatsChecker;
import er.profiling.PFStatsNode;

public class PFSummary implements PFProfiler.Delegate {
    private boolean _summaryEnabled;

    public void requestStarted(WORequest request) {
        _summaryEnabled = ERXProperties.booleanForKeyWithDefault("PFProfiler.summaryEnabled", true);
    }

    public void requestEnded(WORequest request) {
        // DO NOTHING
    }

    public void willAppendToResponse(WOElement element, WOResponse response, WOContext context) {
        // DO NOTHING
    }

    public void didAppendToResponse(WOElement element, WOResponse response, WOContext context) {
        // DO NOTHING
    }

    public void responseEnded(WOResponse response, WOContext context) {
        if (_summaryEnabled) {
            PFStatsNode rootStats = PFProfiler.currentStats().rootStats();
            double total = rootStats.durationMillis();
            int sqlCount = rootStats.countOf("SQL", false);
            double sqlDuration = rootStats.durationOfMillis("SQL", false);
            double sqlDurationPercent = sqlDuration / total;
            int d2wCount = rootStats.countOf("D2W", false);
            double d2wDuration = rootStats.durationOfMillis("D2W", false);
            double d2wDurationPercent = d2wDuration / total;

            double takeValuesFromRequest = rootStats.durationOfMillis("takeValuesFromRequest", false) / total;
            double invokeAction = rootStats.durationOfMillis("invokeAction", false) / total;
            double appendToResponse = rootStats.durationOfMillis("appendToResponse", false) / total;

            String uuid = UUID.randomUUID().toString();
            PFProfiler.setStatsWithID(rootStats, uuid);

            StringBuilder profileString = new StringBuilder();
            profileString.append("Profiler: " + String.format("%.2f", total) + "ms; ");
            profileString.append("SQL: " + DecimalFormat.getPercentInstance().format(sqlDurationPercent) + " (" + sqlCount + "); ");
            profileString.append("D2W: " + DecimalFormat.getPercentInstance().format(d2wDurationPercent) + " (" + d2wCount + "); ");
            profileString.append("T/I/A: " + DecimalFormat.getPercentInstance().format(takeValuesFromRequest) + " / " + DecimalFormat.getPercentInstance().format(invokeAction)
                    + " / " + DecimalFormat.getPercentInstance().format(appendToResponse));
            System.out.println(profileString);

            response
                    .appendContentString("<div id=\"_profiler\" style=\"position: fixed; right: 0px; bottom: 0px; font-family: Helvetica; font-size: 9pt; font-weight:bold; white-space: no-wrap; clear: both; padding: 0.5em; padding-left: 10px; padding-right: 10px; background-color:rgba(240, 240, 255, 0.8); border: 1px solid rgb(200, 200, 215); border-bottom: none; border-right: none; border-top-left-radius: 10px\">");
            response.appendContentString("<span style=\"color:rgb(150,150,150)\">profiler:</span> " + String.format("%.2f", total) + "ms");

            Set<PFStatsNode> errorNodes = PFStatsChecker.checkForErrors(rootStats);
            if (!errorNodes.isEmpty()) {
                Set<String> errorTypes = new HashSet<String>();
                for (PFStatsNode errorNode : errorNodes) {
                    errorTypes.add(errorNode.name());
                }
                response.appendContentString("&nbsp;&nbsp;|&nbsp;&nbsp;");
                response.appendContentString("<font color=\"red\">profiler errors " + errorTypes + "</font>");
            }

            response.appendContentString("&nbsp;&nbsp;<span style=\"color:rgb(150,150,150)\">|</span>&nbsp;&nbsp;");
            PFStatsNode.DurationCount oneMS = rootStats.countBetweenDurations(0, 1000000);
            PFStatsNode.DurationCount tenMS = rootStats.countBetweenDurations(1000000, 10 * 1000000);
            PFStatsNode.DurationCount hundredMS = rootStats.countBetweenDurations(10 * 1000000, 100 * 1000000);
            PFStatsNode.DurationCount moreMS = rootStats.countBetweenDurations(100 * 1000000, Long.MAX_VALUE);

            float maxHeight = 20;
            response.appendContentString("<style>#pf_histogram .pf_histogram_details { display: none; } #pf_histogram:hover .pf_histogram_details { display: block; }</style>");
            response.appendContentString("<span id=\"pf_histogram\">");
            response.appendContentString("<span style=\"background-color:rgb(200,200,200);margin:0px;padding:0px;width:10px;display:inline-table;font-size:0pt;height:" + (maxHeight * oneMS.millis() / total) + "px;\">&nbsp;</span>"); 
            response.appendContentString("<span style=\"background-color:rgb(150,150,150);margin:0px;padding:0px;width:10px;display:inline-table;font-size:0pt;height:" + (maxHeight * tenMS.millis() / total) + "px;\">&nbsp;</span>"); 
            response.appendContentString("<span style=\"background-color:rgb(100,100,100);margin:0px;padding:0px;width:10px;display:inline-table;font-size:0pt;height:" + (maxHeight * hundredMS.millis() / total) + "px;\">&nbsp;</span>"); 
            response.appendContentString("<span style=\"background-color:rgb( 50, 50, 50);margin:0px;padding:0px;width:10px;display:inline-table;font-size:0pt;height:" + (maxHeight * moreMS.millis() / total) + "px;\">&nbsp;</span>");
            
            response.appendContentString("<span class=\"pf_histogram_details\" style=\"position:absolute; top:-20px;background-color:rgba(240, 240, 255, 1.0);border: 1px solid rgb(200, 200, 215);padding:5px;-webkit-box-shadow: 0px 3px 10px rgb(100, 100, 100);\">");
            response.appendContentString("<span style=\"color:rgb(150,150,150)\"><1ms:</span>");
            response.appendContentString(DecimalFormat.getPercentInstance().format(oneMS.millis() / total));
            response.appendContentString(" (");
            response.appendContentString(String.valueOf(oneMS.count()));
            response.appendContentString(")");
            
            response.appendContentString("<span style=\"color:rgb(150,150,150)\">, <10ms:</span>");
            response.appendContentString(DecimalFormat.getPercentInstance().format(tenMS.millis() / total));
            response.appendContentString(" (");
            response.appendContentString(String.valueOf(tenMS.count()));
            response.appendContentString(")");
            
            response.appendContentString("<span style=\"color:rgb(150,150,150)\">, <100ms:</span>");
            response.appendContentString(DecimalFormat.getPercentInstance().format(hundredMS.millis() / total));
            response.appendContentString(" (");
            response.appendContentString(String.valueOf(hundredMS.count()));
            response.appendContentString(")");
            
            response.appendContentString("<span style=\"color:rgb(150,150,150)\">, >=100ms:</span>");
            response.appendContentString(DecimalFormat.getPercentInstance().format(moreMS.millis() / total));
            response.appendContentString(" (");
            response.appendContentString(String.valueOf(moreMS.count()));
            response.appendContentString(")");
            response.appendContentString("</span>");

            response.appendContentString("</span>");

            response.appendContentString("&nbsp;&nbsp;<span style=\"color:rgb(150,150,150)\">|</span>&nbsp;&nbsp;");
            response.appendContentString("<a href=\"" + context.completeURLWithRequestHandlerKey("profiler", "tree", "id=" + uuid + "&filter=SQL", false, 0)
                    + "\" target=\"_blank\">SQL</a>: " + DecimalFormat.getPercentInstance().format(sqlDurationPercent) + " (" + sqlCount + ")");

            response.appendContentString("&nbsp;&nbsp;<span style=\"color:rgb(150,150,150)\">|</span>&nbsp;&nbsp;");
            response.appendContentString("<a href=\"" + context.completeURLWithRequestHandlerKey("profiler", "tree", "id=" + uuid + "&filter=D2W", false, 0)
                    + "\" target=\"_blank\">D2W</a>: " + DecimalFormat.getPercentInstance().format(d2wDurationPercent) + " (" + d2wCount + ")");

            response.appendContentString("&nbsp;&nbsp;<span style=\"color:rgb(150,150,150)\">|</span>&nbsp;&nbsp;");
            response.appendContentString("<a href=\""
                    + context.completeURLWithRequestHandlerKey("profiler", "tree", "id=" + uuid + "&filter=takeValuesFromRequest&min=0.01", false, 0)
                    + "\" target=\"_blank\">take</a>:");
            response.appendContentString(DecimalFormat.getPercentInstance().format(takeValuesFromRequest));
            response.appendContentString("<span style=\"color:rgb(150,150,150)\">, </span>");

            response.appendContentString("<a href=\"" + context.completeURLWithRequestHandlerKey("profiler", "tree", "id=" + uuid + "&filter=invokeAction&min=0.01", false, 0)
                    + "\" target=\"_blank\">invoke</a>:");
            response.appendContentString(DecimalFormat.getPercentInstance().format(invokeAction));
            response.appendContentString("<span style=\"color:rgb(150,150,150)\">, </span>");

            response.appendContentString("<a href=\"" + context.completeURLWithRequestHandlerKey("profiler", "tree", "id=" + uuid + "&filter=appendToResponse&min=0.01", false, 0)
                    + "\" target=\"_blank\">append</a>:");
            response.appendContentString(DecimalFormat.getPercentInstance().format(appendToResponse));

            response.appendContentString(" (<a href=\""
                    + context.completeURLWithRequestHandlerKey("profiler", "tree", "id=" + uuid + "&filter=takeValuesFromRequest,invokeAction,appendToResponse&min=0.01", false, 0)
                    + "\" target=\"_blank\">all three</a>)");

            response.appendContentString("&nbsp;&nbsp;<span style=\"color:rgb(150,150,150)\">|</span>&nbsp;&nbsp;");
            response.appendContentString("<a href=\"" + context.completeURLWithRequestHandlerKey("profiler", "tree", "id=" + uuid + "&min=0.01", false, 0)
                    + "\" target=\"_blank\">all</a>");

            response.appendContentString("&nbsp;&nbsp;<span style=\"color:rgb(150,150,150)\">|</span>&nbsp;&nbsp;");
            response.appendContentString("<a href=\"javascript:void(0);\" onClick=\"window.open('" + context.completeURLWithRequestHandlerKey("profiler", "heat", "", false, 0)
                    + "','heat','width=1,height=1')\">");
            if (PFHeatMap.isHeatEnabled()) {
                response.appendContentString("heat is on");
            } else {
                response.appendContentString("heat is off");
            }
            response.appendContentString("</a>");

            response.appendContentString("</div>");
        }
    }
}
