package er.plot;

import com.webobjects.foundation.*;
import com.webobjects.appserver.*;
import er.extensions.*;
import org.jfree.chart.*;
import org.jfree.chart.plot.*;
import org.jfree.data.*;
import java.util.*;

/**
 * Class for Chart Component ERPBarChart.
 *
 * @binding sample sample binding explanation
 *
 * @created ak on Thu Sep 25 2003
 * @project ERPlot
 */

public class ERPBarChart extends ERPChart {

    /** logging support */
    private static final ERXLogger log = ERXLogger.getERXLogger(ERPBarChart.class,"plot");

    protected String _categoryKey;
    protected String _yName;
    protected String _xName;

    /**
     * Public constructor
     * @param context the context
     */
    public ERPBarChart(WOContext context) {
        super(context);
    }

    public void reset() {
        super.reset();
        _xName = null;
        _yName = null;
        _categoryKey = null;
    }

    public String categoryKey() {
        if(_categoryKey == null) {
            _categoryKey = stringValueForBinding("categoryKey", null);
        }
        return _categoryKey;
    }

    public String xName() {
        if(_xName == null) {
            _xName = stringValueForBinding("xName", "xName");
        }
        return _xName;
    }

    public String yName() {
        if(_yName == null) {
            _yName = stringValueForBinding("yName", "yName");
        }
        return _yName;
    }

    public Dataset dataset() {
        if(_dataset == null) {
            DefaultCategoryDataset dataset = new DefaultCategoryDataset();

            for(Enumeration items = items().objectEnumerator(); items.hasMoreElements(); ) {
                Object item = items.nextElement();
                String name = (String)NSKeyValueCoding.Utility.valueForKey(item, nameKey());
                Number value = (Number)NSKeyValueCoding.Utility.valueForKey(item, valueKey());
                String category = null;
                if(categoryKey() != null) {
                    category = (String)NSKeyValueCoding.Utility.valueForKey(item, categoryKey());
                }
                dataset.setValue(value.intValue(), name, category);
            }
            _dataset = dataset;
        }
        return _dataset;
    }

    public JFreeChart chart() {
        if(_chart == null) {
            JFreeChart chart = null;
            CategoryDataset dataset = (CategoryDataset)dataset();
            String name = name();

            if("horizontal".equals(type())) {
                chart = ChartFactory.createBarChart(name, xName(), yName(), dataset, PlotOrientation.HORIZONTAL, true, true, false );
            } else {
                chart = ChartFactory.createBarChart(name, xName(), yName(), dataset, PlotOrientation.VERTICAL, true, true, false );
            }
            _chart = chart;
        }
        return _chart;
    }
}
