package er.plot;

import org.jfree.chart.*;
import org.jfree.chart.plot.*;
import org.jfree.data.*;
import com.webobjects.appserver.*;
import er.extensions.*;

/**
 * Class for Chart Component ERPLineChart.
 *
 * @binding sample sample binding explanation
 *
 * @created ak on 17.12.04
 * @project ERPlot
 */

public class ERPLineChart extends ERPCategoryChart {

    /** logging support */
    private static final ERXLogger log = ERXLogger.getERXLogger(ERPLineChart.class,"plot");
	
    /**
     * Public constructor
     * @param context the context
     */
    public ERPLineChart(WOContext context) {
        super(context);
    }
    
    public JFreeChart chart() {
        if(_chart == null) {
            JFreeChart chart = null;
            CategoryDataset dataset = (CategoryDataset)dataset();
            String name = (String) valueForBinding("name");
            
            if("stackedArea".equals(chartType())) {
                chart = ChartFactory.createStackedAreaChart(name, xName(), yName(), dataset, PlotOrientation.VERTICAL, true, true, false );
            } else {
                chart = ChartFactory.createLineChart(name, xName(), yName(), dataset, PlotOrientation.VERTICAL, true, true, false );
            }
            _chart = chart;
        }
        return _chart;
    }
}
