package er.profiling;

import java.text.DecimalFormat;
import java.util.Enumeration;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import com.webobjects.appserver.WOApplication;
import com.webobjects.appserver.WOContext;
import com.webobjects.appserver.WOElement;
import com.webobjects.appserver.WORequest;
import com.webobjects.appserver.WORequestHandler;
import com.webobjects.appserver.WOResponse;
import com.webobjects.eoaccess.EOSQLExpression;
import com.webobjects.eocontrol.EOFetchSpecification;
import com.webobjects.foundation.NSArray;

import er.profiling.delegates.PFHeatMap;

public class PFProfilerRequestHandler extends WORequestHandler {
    public PFProfilerRequestHandler() {
    }

    protected void appendSingleNodeStatsToResponse(PFStatsNode stats, WOResponse response, WOContext context, float minimumPercentage, Set<String> filters,
            Set<PFStatsNode> renderedStats, boolean tree) {
        if (tree && filters != null && stats.parentStats() != null && !renderedStats.contains(stats.parentStats())) {
            appendSingleNodeStatsToResponse(stats.parentStats(), response, context, minimumPercentage, filters, renderedStats, tree);
        }

        if (tree) {
            int depth = stats.depth();
            for (int i = 0; i < depth; i++) {
                if (i == depth - 1) {
                    response.appendContentString("+-");
                } else {
                    response.appendContentString("| ");
                }
            }
        }

	if (stats.hasErrors()) {
	    response.appendContentString("<font color=\"red\">");
	}
        else if (stats.isImportant()) {
            response.appendContentString("<font color=\"black\">");
        }
        
        response.appendContentString("[" + String.format("%5.2f", stats.durationMillis()) + "ms / " + DecimalFormat.getPercentInstance().format(stats.percentage()) + "] ");
        response.appendContentString(stats.name());
        if (stats.type() != null) {
            response.appendContentString(" (" + stats.type() + ")");
        }
        if (stats.hasErrors()) {
            for (String error : stats.errors()) {
                response.appendContentString(" <b>" + error + "</b> ");
            }
        }
        
        Object target = stats.target();
        if (target instanceof WORequest) {
            response.appendContentString(": " + ((WORequest) target).uri());
        } else if (target instanceof WOElement) {
            response.appendContentString(": " + ((WOElement) target).getClass().getSimpleName());
        } else if (target instanceof EOSQLExpression) {
            response.appendContentString(": " + ((EOSQLExpression) target).statement());
        } else if (target instanceof EOFetchSpecification) {
            response.appendContentString(": entity=" + ((EOFetchSpecification) target).entityName() + ", qualifier=" + ((EOFetchSpecification) target).qualifier());
        } else {
            response.appendContentString(": " + target);
        }
        if (stats.counters() != null) {
            response.appendContentString(", ");
            response.appendContentString(stats.counters().toString());
        }
        if (stats.hasErrors()) {
            response.appendContentString("</font>");
        }
        else if (stats.isImportant()) {
            response.appendContentString("</font>");
        }
        response.appendContentString("\n");
        renderedStats.add(stats);
    }

    protected void appendNodesStatsToResponse(PFStatsNode stats, WOResponse response, WOContext context, float minimumPercentage, Set<String> filters,
            Set<PFStatsNode> renderedStats, boolean tree) {
        if (stats != null) {
            if ((filters == null || filters.contains(stats.name())) && stats.isAtLeastPercentage(minimumPercentage)) {
                appendSingleNodeStatsToResponse(stats, response, context, minimumPercentage, filters, renderedStats, tree);
            }

            List<PFStatsNode> children = stats.children();
            if (children != null) {
                for (PFStatsNode child : children) {
                    appendNodesStatsToResponse(child, response, context, minimumPercentage, filters, renderedStats, tree);
                }
            }
        }
    }

    @Override
    public WOResponse handleRequest(WORequest request) {
        WOContext context = WOApplication.application().createContextForRequest(request);
        WOResponse response = WOApplication.application().createResponseInContext(context);

        String requestPath = request.requestHandlerPath();
        if ("heat".equals(requestPath)) {
            PFHeatMap.setHeatEnabled(!PFHeatMap.isHeatEnabled());
            response.appendContentString("<script>window.close();</script>");
        } else {
            String id = request.stringFormValueForKey("id");
            PFStatsNode stats = PFProfiler.statsWithID(id);
            response.appendContentString("<html><body>");
            response.appendContentString("<pre style=\"color: grey\">");
            if (stats == null) {
                response.appendContentString("Unknown stats id #" + stats);
            } else {
                String filter = request.stringFormValueForKey("filter");
                Set<String> filters = null;
                if (filter != null) {
                    filters = new HashSet<>();
                    NSArray filterNamesArray = NSArray.componentsSeparatedByString(filter, ",");
                    Enumeration filterNamesEnum = filterNamesArray.objectEnumerator();
                    while (filterNamesEnum.hasMoreElements()) {
                        Object filterName = filterNamesEnum.nextElement();
                        filters.add((String) filterName);
                    }
                }
                float minimumPercentage = 0.0f;
                String minimumPercentageStr = request.stringFormValueForKey("min");
                if (minimumPercentageStr != null) {
                    minimumPercentage = Float.parseFloat(minimumPercentageStr);
                }
                boolean tree = "tree".equals(requestPath);
                PFStatsChecker.checkForErrors(stats);
                appendNodesStatsToResponse(stats, response, context, minimumPercentage, filters, new HashSet<>(), tree);
            }
            response.appendContentString("</pre>");
            response.appendContentString("</body></html>");
        }
        return response;
    }
}
