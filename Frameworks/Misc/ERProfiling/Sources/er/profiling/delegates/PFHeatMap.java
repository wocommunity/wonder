package er.profiling.delegates;

import java.util.List;

import com.webobjects.appserver.WOContext;
import com.webobjects.appserver.WOElement;
import com.webobjects.appserver.WORequest;
import com.webobjects.appserver.WOResponse;

import er.extensions.foundation.ERXProperties;
import er.profiling.PFProfiler;
import er.profiling.PFStatsNode;

public class PFHeatMap implements PFProfiler.Delegate {
    private static Boolean _heatOverride;

    private boolean _heatEnabled;

    public static void setHeatEnabled(boolean heatEnabled) {
        _heatOverride = Boolean.valueOf(heatEnabled);
    }

    public static boolean isHeatEnabled() {
        return _heatOverride != null ? _heatOverride.booleanValue() : ERXProperties.booleanForKey("PFProfiler.heatMapEnabled");
    }

    public void requestStarted(WORequest request) {
        _heatEnabled = isHeatEnabled();
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
        if (_heatEnabled) {
            PFStatsNode rootStats = PFProfiler.currentStats();
            if (rootStats != null) {
                response.appendContentString("<style>");
                appendHeatStyles(rootStats, response, context);
                response.appendContentString("</style>");
            }
        }
    }

    protected void appendHeatStyles(PFStatsNode stats, WOResponse response, WOContext context) {
        List<PFStatsNode> children = stats.children();
        if (children != null) {
            for (PFStatsNode child : children) {
                appendHeatStyles(child, response, context);
            }
        }
        if ("appendToResponse".equals(stats.name())) {
            double fValue = stats.percentage();
            int r = 255;
            int g = (int) (255 * (1.0 - fValue * fValue));
            int b = 0;
            int w = 1;
            if (fValue > 0.75) {
                w = 4;
            } else if (fValue > 0.4) {
                w = 3;
            } else if (fValue > 0.1) {
                w = 2;
            }
            response.appendContentString("." + stats.cssID() + " { outline: " + w + "px solid rgb(" + r
                    + "," + g + "," + b + ") !important; outline-offset: -" + w + "px !important }\n");
        }

    }
}
