package er.plot;

import java.io.ByteArrayOutputStream;
import java.lang.reflect.Method;
import java.util.Enumeration;

import org.jfree.chart.ChartFactory;
import org.jfree.chart.ChartRenderingInfo;
import org.jfree.chart.ChartUtilities;
import org.jfree.chart.JFreeChart;
import org.jfree.chart.entity.StandardEntityCollection;
import org.jfree.chart.imagemap.ImageMapUtil;
import org.jfree.chart.imagemap.ToolTipTagFragmentGenerator;
import org.jfree.chart.imagemap.URLTagFragmentGenerator;
import org.jfree.chart.plot.PlotOrientation;
import org.jfree.data.category.CategoryDataset;
import org.jfree.data.category.DefaultCategoryDataset;
import org.jfree.data.general.Dataset;

import com.webobjects.appserver.WOContext;
import com.webobjects.foundation.NSArray;
import com.webobjects.foundation.NSData;
import com.webobjects.foundation.NSForwardException;
import com.webobjects.foundation.NSKeyValueCodingAdditions;

/**
 * Class for Chart Component ERPCategoryChart.
 *
 * @binding sample sample binding explanation
 *
 * @created ak on 17.12.04
 * @project ERPlot
 */

public class ERPCategoryChart extends ERPChart {

    public static final NSArray SUPPORTED_TYPES = new NSArray(new Object[]{
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
    
    public PlotOrientation orientation() {
        if(_orientation == null) {
            _orientation = ("horizontal".equals(stringValueForBinding("orientation", "vertical")) ? 
                    PlotOrientation.HORIZONTAL : PlotOrientation.VERTICAL);
        }
        return _orientation;
    }
    
    public Dataset dataset() {
        if(_dataset == null) {
            _dataset = super.dataset();
            if(_dataset == null) {
                DefaultCategoryDataset dataset = new DefaultCategoryDataset();
                
                for(Enumeration items = items().objectEnumerator(); items.hasMoreElements(); ) {
                    Object item = items.nextElement();
                    Comparable name = (Comparable)NSKeyValueCodingAdditions.Utility.valueForKeyPath(item, nameKey());
                    Number value = (Number)NSKeyValueCodingAdditions.Utility.valueForKeyPath(item, valueKey());
                    Comparable category = null;
                    if(categoryKey() != null) {
                        category = (Comparable)NSKeyValueCodingAdditions.Utility.valueForKeyPath(item, categoryKey());
                    }
                    dataset.setValue(value, name, category);
                }
                _dataset = dataset;
            }
        }
        return _dataset;
    }
    
    protected NSArray supportedTypes() {
        return SUPPORTED_TYPES;
    }
    
    public JFreeChart chart() {
        if(_chart == null) {
            JFreeChart chart = null;
            CategoryDataset dataset = (CategoryDataset)dataset();
            String name = stringValueForBinding("name", "");
            Class clazz = ChartFactory.class;
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
                throw  NSForwardException._runtimeExceptionForThrowable(t);
            }
            _chart = chart;
        }
        return _chart;
    }
}
