package er.plot;

import java.util.Enumeration;

import org.jfree.chart.ChartFactory;
import org.jfree.chart.JFreeChart;
import org.jfree.data.general.Dataset;
import org.jfree.data.general.DefaultPieDataset;
import org.jfree.data.general.PieDataset;

import com.webobjects.appserver.WOContext;
import com.webobjects.foundation.NSArray;
import com.webobjects.foundation.NSKeyValueCoding;

/**
 * PieChart.
 *
 * @binding sample sample binding explanation
 *
 * @created ak on Thu Sep 25 2003
 * @project ERPlot
 */

public class ERPPieChart extends ERPChart {

    /** logging support */
    public static final NSArray SUPPORTED_TYPES = new NSArray(new Object[] {"PieChart", "PieChart3D"});
        
    /**
     * Public constructor
     * @param context the context
     */
    public ERPPieChart(WOContext context) {
        super(context);
    }
    
    public Dataset dataset() {
        if(_dataset == null) {
            _dataset = super.dataset();
            if(_dataset == null) {
                DefaultPieDataset dataset = new DefaultPieDataset();
                for(Enumeration items = items().objectEnumerator(); items.hasMoreElements(); ) {
                    Object item = items.nextElement();
                    String name = (String)NSKeyValueCoding.Utility.valueForKey(item, nameKey());
                    Number value = (Number)NSKeyValueCoding.Utility.valueForKey(item, valueKey());
                    dataset.setValue(name, value.intValue());
                }
                _dataset = dataset;
            }
        }
        return _dataset;
    }

    public JFreeChart chart() {
        if(_chart == null) {
            JFreeChart chart = null;
            PieDataset dataset = (PieDataset)dataset();
            String name = stringValueForBinding("name", null);

            if("PieChart3D".equals(chartType())) {
                chart = ChartFactory.createPieChart3D(name,dataset,true,false,false);
            } else {
                chart = ChartFactory.createPieChart(name,dataset,showLegends(),showToolTips(),showUrls());
            }
            _chart = chart;
        }
        return _chart;
    }

    protected NSArray supportedTypes() {
        return SUPPORTED_TYPES;
    }
}
