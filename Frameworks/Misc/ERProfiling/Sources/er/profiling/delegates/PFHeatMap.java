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
            int r = 0;
            int g = 0;
            int b = 0;
            int a = 1;
            if (fValue <= 0.01) {
                // r = 255;
                // g = 255;
                // b = 255;
                // response.appendContentString(".wo_p_" +
                // duration.getKey() + " { background-color: rgba(" + r
                // + "," + g + "," + b + "," + a + ") !important; }\n");
            } else {
                int value = (int) (255 * (1.0 - fValue * fValue));
                r = 255;
                g = value;
                b = 0;
                response.appendContentString("." + stats.cssID() + " { !important; border: 3px solid rgba(" + r + "," + g + "," + b + "," + a + ") !important; }\n");
            }
        }

    }
}
