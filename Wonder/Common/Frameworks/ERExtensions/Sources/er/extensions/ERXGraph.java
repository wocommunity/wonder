/*
 * Copyright (C) NetStruxr, Inc. All rights reserved.
 *
 * This software is published under the terms of the NetStruxr
 * Public Software License version 0.5, a copy of which has been
 * included with this distribution in the LICENSE.NPL file.  */
package er.extensions;

import java.util.*;

import com.webobjects.appserver.*;
import com.webobjects.foundation.*;
import com.webobjects.plot.*;

/**
 * Nice component wrapper around GifPlot, just bind arrays and strings<br />
 * 
 * @binding plotsForGraph
 * @binding xAxisLabel
 * @binding yAxisLabel
 * @binding type
 * @binding title
 * @binding xAxisDataFormat
 * @binding extraBindings
 */

public class ERXGraph extends WOComponent {

    public ERXGraph(WOContext aContext) {
        super(aContext);
    }

    public static final ERXLogger log = ERXLogger.getERXLogger(ERXGraph.class);
    
    public boolean synchronizesVariablesWithBindings() { return false; }

    private NSArray _plotsForGraph;
    public NSArray plotsForGraph() { return (NSArray)valueForBinding("plotsForGraph"); }        //plotsForGraph is an array of dictionaries
    public void setPlotsForGraph(NSArray plots) {setValueForBinding(plots, "plotsForGraph");}
    
    public NSData newImage() {
        GIFPlot plot = new GIFPlot();
        String path = ERXFileUtilities.pathForResourceNamed("erDefault.graph", "ERExtensions", null);
        plot.addContentsOfFile(path);
        if(hasBinding("extraBindings"))
            plot.addContentsOfDictionary((NSDictionary)valueForBinding("extraBindings"));
        plot.addContentsOfDictionary(plotsDictionary());
        initPlotSettings(plot);
        if(log.isDebugEnabled())
            log.debug("Plot settings: " +plot.settings());
        plot.generateGraph();
        return plot.imageData();
    }

    public NSDictionary plotsDictionary() {
        NSMutableArray plots = new NSMutableArray();
        for(Enumeration e = plotsForGraph().objectEnumerator(); e.hasMoreElements();) {
            NSDictionary aPlot = (NSDictionary)e.nextElement();
            plots.addObject(aPlot);
        }
        return new NSDictionary(plots, "Plots");
    }

    protected void initPlotSettings(GIFPlot plot) {
        if (hasBinding("xAxisLabel"))((NSDictionary)plot.settings().objectForKey("XAxis")).takeValueForKey(valueForBinding("xAxisLabel"), "Unit");
        if (hasBinding("xAxisDataFormat"))
            ((NSDictionary)plot.settings().objectForKey("XAxis")).takeValueForKey(valueForBinding("xAxisDataFormat"), "DataFormat");
        if (hasBinding("yAxisLabel"))((NSDictionary)plot.settings().objectForKey("YAxis")).takeValueForKey(valueForBinding("yAxisLabel"), "Unit");
        if (hasBinding("title"))plot.settings().takeValueForKey(valueForBinding("title"), "Title");
        if (hasBinding("type"))plot.settings().takeValueForKey(valueForBinding("type"), "Type");
    }
}
