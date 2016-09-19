package er.plot;

import java.lang.reflect.Method;
import java.util.Enumeration;

import org.jfree.chart.ChartFactory;
import org.jfree.chart.JFreeChart;
import org.jfree.chart.plot.PlotOrientation;
import org.jfree.data.category.CategoryDataset;
import org.jfree.data.category.DefaultCategoryDataset;
import org.jfree.data.general.Dataset;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.webobjects.appserver.WOContext;
import com.webobjects.foundation.NSArray;
import com.webobjects.foundation.NSForwardException;
import com.webobjects.foundation.NSKeyValueCodingAdditions;

/**
 * Display a category chart - a chart where you have 2 or 3 dimensions. The most important binding is <code>items</code> which
 * should contain an array of objects from which the values <code>xNameKey</code>, <code>yNameKey</code>, <code>categoryKey</code> and <code>
 * valueKey</code> are retrieved. For example, you might have an array of line items, 
 * with a valueKey <code>amount</code>, an <code>xNameKey</code> with <code>invoice.datePurchased</code>, <code>yNameKey</code>, <code>categoryKey</code>and a nameKey <code>product.name</code>. 
 * 
 * @binding name the name of the chart
 * @binding chartType the type of the chart (possible values depend on the concrete subclass)
 * @binding imageType the type of the image to show: <code>png</code> (default) or <code>jpeg</code>
 * @binding width the width of the chart (400 pixel if not specified)
 * @binding height the height of the chart (400 pixel if not specified)
 * @binding dataset Dataset to use. If this is given, then items, nameKey, valueKey and categoryKey are not considered.
 * @binding items array of values to display the chart for
 * @binding nameKey the key for the name (must return Comparable)
 * @binding valueKey the key for the value (must return Number)
 * @binding categoryKey the key for the categories (optional, must return Comparable)
 * @binding xName the name for the x axis (String)
 * @binding yName the name for the y axis (String)
 * @binding showLegends true, if legends should be shown
 * @binding showToolTips true, if tool tips should be shown
 * @binding showUrls true, if urls should be shown
 * @binding orientation either "horizontal" (default) or "vertical"
 * @binding chart Chart to use instead of the created one. If this binding is set-able, then it will be set to the actually used chart
 * @binding configuration NSDictionary that will be applied to the chart via key-value-coding prior to rendering. Contains 
 *      entries like <code>antiAlias=true</code> or <code>categoryPlot.dataAreaRatio = 0.8</code>.
 * @author ak
 */

public class ERPCategoryChart extends ERPChart {
	/**
	 * Do I need to update serialVersionUID?
	 * See section 5.6 <cite>Type Changes Affecting Serialization</cite> on page 51 of the 
	 * <a href="http://java.sun.com/j2se/1.4/pdf/serial-spec.pdf">Java Object Serialization Spec</a>
	 */
	private static final long serialVersionUID = 1L;

	private static final Logger log = LoggerFactory.getLogger(ERPCategoryChart.class);

    public static final NSArray<String> SUPPORTED_TYPES = new NSArray<>(new String[]{
            "BarChart", "StackedBarChart", "BarChart3D", "StackedBarChart3D", "AreaChart", 
            "StackedAreaChart", "LineChart", "WaterfallChart"
    });
    
    protected String _categoryKey;
    protected String _yName;
    protected String _xName;
    protected PlotOrientation _orientation;
    
    public ERPCategoryChart(WOContext context) {
        super(context);
    }
    
    @Override
	public void reset() {
        super.reset();
        _xName = null;
        _yName = null;
        _categoryKey = null;
        _orientation = null;
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
    
    public PlotOrientation orientation() {
        if(_orientation == null) {
            _orientation = ("horizontal".equals(stringValueForBinding("orientation", "vertical")) ? 
                    PlotOrientation.HORIZONTAL : PlotOrientation.VERTICAL);
        }
        return _orientation;
    }
    
    @Override
	protected NSArray<String> supportedTypes() {
        return SUPPORTED_TYPES;
    }
    
    @Override
	protected JFreeChart createChart() {
        JFreeChart chart = null;
        String name = stringValueForBinding("name", "");
        Class<ChartFactory> clazz = ChartFactory.class;
        try {
            Method method = clazz.getDeclaredMethod("create" + chartType(), new Class[] {
                String.class, String.class, String.class, CategoryDataset.class, PlotOrientation.class, 
                boolean.class, boolean.class, boolean.class
            });
            chart = (JFreeChart) method.invoke(clazz, new Object[] {name, xName(), yName(), dataset(), orientation(), 
                    (showLegends() ? Boolean.TRUE : Boolean.FALSE),
                    (showToolTips() ? Boolean.TRUE : Boolean.FALSE),
                    (showUrls() ? Boolean.TRUE : Boolean.FALSE )
            });
        } catch(Throwable t) {
            log.error("Could not create chart.", t);
            throw  NSForwardException._runtimeExceptionForThrowable(t);
        }
        return chart;
    }
    
    @Override
	protected Dataset createDataset() {
        DefaultCategoryDataset dataset = new DefaultCategoryDataset();
        
        for(Enumeration<?> items = items().objectEnumerator(); items.hasMoreElements(); ) {
            Object item = items.nextElement();
            Comparable<?> name = (Comparable<?>)NSKeyValueCodingAdditions.Utility.valueForKeyPath(item, nameKey());
            Number value = (Number)NSKeyValueCodingAdditions.Utility.valueForKeyPath(item, valueKey());
            Comparable<?> category = null;
            if(categoryKey() != null) {
                category = (Comparable<?>)NSKeyValueCodingAdditions.Utility.valueForKeyPath(item, categoryKey());
            }
            dataset.setValue(value, name, category);
        }
        return dataset;
    }
}
