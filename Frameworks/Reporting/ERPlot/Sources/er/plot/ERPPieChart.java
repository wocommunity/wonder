package er.plot;

import java.util.Enumeration;

import org.jfree.chart.ChartFactory;
import org.jfree.chart.JFreeChart;
import org.jfree.data.general.Dataset;
import org.jfree.data.general.DefaultPieDataset;
import org.jfree.data.general.PieDataset;

import com.webobjects.appserver.WOContext;
import com.webobjects.foundation.NSArray;
import com.webobjects.foundation.NSKeyValueCodingAdditions;

import er.extensions.foundation.ERXAssert;

/**
 * Displays a Pie chart with an optional map. The most important binding is <code>items</code> which
 * should contain an array of objects from which the values <code>nameKey</code> and <code>
 * valueKey</code> are retrieved. For example, you might have an array of line items, 
 * with a valueKey <code>amount</code> and a nameKey <code>product.name</code>. If you supply a 
 * PieDataset instead, this will get used instead of the other bindings.
 * @binding name the name of the chart
 * @binding chartType the type of the chart (possible values depend on the concrete subclass)
 * @binding imageType the type of the image to show: <code>png</code> (default) or <code>jpeg</code>
 * @binding width the width of the chart (400 pixel if not specified)
 * @binding height the height of the chart (400 pixel if not specified)
 * @binding dataset Dataset to use. If this is given, then items, nameKey, valueKey and categoryKey are not considered.
 * @binding items array of values to display the chart for
 * @binding nameKey the key for the name (must return String)
 * @binding valueKey the key for the value (must return Number)
 * @binding showLegends true, if legends should be shown
 * @binding showToolTips true, if tool tips should be shown
 * @binding showLegends true, if legends should be shown
 * @binding chart Chart to use instead of the created one. If this binding is setable, then it will be set to the actually used chart
 * @binding configuration NSDictionary that will be applied to the chart via key-value-coding prior to rendering. Contains 
 *      entries like <code>antiAlias=true</code> or <code>categoryPlot.dataAreaRatio = 0.8</code>.
 * @author ak
 */

public class ERPPieChart extends ERPChart {

    /** logging support */
    public static final NSArray SUPPORTED_TYPES = new NSArray(new Object[] {"PieChart", "PieChart3D"});
        
    /**
     * Utility class to accomodate for accumulating data (the superclass can only replace values, 
     * so it will always yield the latest one.)
     * @author ak
     */
    public static class AccumulatingPieDataset extends DefaultPieDataset {
        
        /**
         * Overridden so it adds the value to the current value for the key instead of replacing it.
         */
        public void setValue(Comparable key, Number value) {
            Number oldValue = getValue(key);
            if(oldValue != null) {
                value = new Double(value.doubleValue() + oldValue.doubleValue());
            }
            super.setValue(key, value);
        }
    }
    
    /**
     * Public constructor
     * @param context the context
     */
    public ERPPieChart(WOContext context) {
        super(context);
    }
    
    public String nameKey() {
        if(_nameKey == null) {
            _nameKey = (String)valueForBinding("nameKey");
            ERXAssert.DURING.notNull("nameKey", _nameKey);
        }
        return _nameKey;
    }

    public String valueKey() {
        if(_valueKey == null) {
            _valueKey = (String)valueForBinding("valueKey");
            ERXAssert.DURING.notNull("valueKey", _valueKey);
        }
        return _valueKey;
    }

    protected Dataset createDataset() {
        AccumulatingPieDataset dataset = new AccumulatingPieDataset();
         for(Enumeration items = items().objectEnumerator(); items.hasMoreElements(); ) {
            Object item = items.nextElement();
            Comparable name = (Comparable)NSKeyValueCodingAdditions.Utility.valueForKeyPath(item, nameKey());
            Number value = (Number)NSKeyValueCodingAdditions.Utility.valueForKeyPath(item, valueKey());
            dataset.setValue(name, value);
        }
        return dataset;
    }
    
    public JFreeChart createChart() {
        JFreeChart chart = null;
        PieDataset dataset = (PieDataset)dataset();
        String name = stringValueForBinding("name", null);
        
        if("PieChart3D".equals(chartType())) {
            chart = ChartFactory.createPieChart3D(name,dataset,showLegends(),showToolTips(),showUrls());
        } else {
            chart = ChartFactory.createPieChart(name,dataset,showLegends(),showToolTips(),showUrls());
        }
        return chart;
    }

    protected NSArray supportedTypes() {
        return SUPPORTED_TYPES;
    }
}
