package er.plot;

import com.webobjects.foundation.*;
import com.webobjects.appserver.*;
import er.extensions.*;
import org.jfree.chart.*;
import org.jfree.data.*;
import java.util.*;

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
    private static final ERXLogger log = ERXLogger.getERXLogger(ERPPieChart.class,"plot");
        
    /**
     * Public constructor
     * @param context the context
     */
    public ERPPieChart(WOContext context) {
        super(context);
    }

    public Dataset dataset() {
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
        return _dataset;
    }

    public JFreeChart chart() {
        if(_chart == null) {
            JFreeChart chart = null;
            PieDataset dataset = (PieDataset)dataset();
            String name = name();

            if("3D".equals(type())) {
                chart = ChartFactory.createPie3DChart(name,dataset,true,false,false);
            } else {
                chart = ChartFactory.createPieChart(name,dataset,true,false,false);
            }
            _chart = chart;
        }
        return _chart;
    }
}
