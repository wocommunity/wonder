package er.plot;

import java.io.ByteArrayOutputStream;
import java.util.Enumeration;

import org.jfree.chart.ChartRenderingInfo;
import org.jfree.chart.ChartUtilities;
import org.jfree.chart.JFreeChart;
import org.jfree.chart.entity.StandardEntityCollection;
import org.jfree.chart.imagemap.ImageMapUtilities;
import org.jfree.chart.imagemap.ToolTipTagFragmentGenerator;
import org.jfree.chart.imagemap.URLTagFragmentGenerator;
import org.jfree.data.general.Dataset;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.webobjects.appserver.WOContext;
import com.webobjects.foundation.NSArray;
import com.webobjects.foundation.NSData;
import com.webobjects.foundation.NSDictionary;
import com.webobjects.foundation.NSForwardException;
import com.webobjects.foundation.NSKeyValueCodingAdditions;

import er.extensions.components.ERXStatelessComponent;
import er.extensions.foundation.ERXAssert;

/**
 * Abstract superclass of the charts. The most important binding is <code>items</code> which
 * should contain an array of objects from which the values <code>nameKey</code> and <code>
 * valueKey</code> are retrieved. For example, you might have an array of line items, 
 * with a valueKey <code>amount</code> and a nameKey <code>product.name</code>.
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
 * @binding showLegends true, if legends should be shown
 * @binding showToolTips true, if tool tips should be shown
 * @binding showUrls true, if urls should be shown
 * @binding chart Chart to use instead of the created one. If this binding is setable, then it will be set to the actually used chart
 * @binding configuration NSDictionary that will be applied to the chart via key-value-coding prior to rendering. Contains 
 *      entries like <code>antiAlias=true</code> or <code>categoryPlot.dataAreaRatio = 0.8</code>.
 *
 * @author ak
 */
public abstract class ERPChart extends ERXStatelessComponent {
	/**
	 * Do I need to update serialVersionUID?
	 * See section 5.6 <cite>Type Changes Affecting Serialization</cite> on page 51 of the 
	 * <a href="http://java.sun.com/j2se/1.4/pdf/serial-spec.pdf">Java Object Serialization Spec</a>
	 */
	private static final long serialVersionUID = 1L;

    private static final Logger log = LoggerFactory.getLogger(ERPChart.class);
    protected static final int DEFAULT_SIZE = 400;

    protected NSData _imageData;
    public String _imageKey;
    public String _imageMap;
    public String _imageMapName;

    protected NSArray<?> _items;
    protected String _name;
    protected String _chartType;
    protected String _imageType;
    protected String _nameKey;
    protected String _valueKey;
    protected int _width = 0;
    protected int _height = 0;
    protected Dataset _dataset;
    protected JFreeChart _chart;
    protected NSDictionary<String,?> _configuration;

    /**
     * Public constructor
     * @param context the context
     */
    public ERPChart(WOContext context) {
        super(context);
    }

    public NSArray<?> items() {
        if(_items == null) {
            _items = (NSArray<?>)valueForBinding("items");
            ERXAssert.DURING.notNull("items", _items);
        }
        return _items;
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

    public int width() {
        if(_width == 0) {
            _width = intValueForBinding("width", DEFAULT_SIZE);
        }
        return _width;
    }

    public int height() {
        if(_height == 0) {
            _height = intValueForBinding("height", DEFAULT_SIZE);
        }
        return _height;
    }

    public String chartType() {
        if(_chartType == null) {
            _chartType = (String)valueForBinding("chartType");
            ERXAssert.DURING.notNull("chartType", _chartType);
            ERXAssert.DURING.isTrue("chartType "+_chartType +" is not in supported types: " + supportedTypes(), 
                    supportedTypes().containsObject(_chartType));
        }
        return _chartType;
    }

    public String imageType() {
        if(_imageType == null) {
            _imageType = stringValueForBinding("imageType", "image/png");
        }
        return _imageType;
    }

    public boolean showLegends() {
        return booleanValueForBinding("showLegends", true);
    }
    
    public boolean showUrls() {
        return booleanValueForBinding("showUrls", false);
    }
    
    public boolean showToolTips() {
        return booleanValueForBinding("showToolTips", true);
    }

    @SuppressWarnings("unchecked")
	public NSDictionary<String,?> configuration() {
        if(_configuration == null) {
            _configuration = (NSDictionary<String,Object>)valueForBinding("configuration");
            if(_configuration == null) {
                _configuration = NSDictionary.EmptyDictionary;
            }
        }
        return _configuration;
    }

    @Override
	public void reset() {
        super.reset();
        _imageData = null;
        _imageKey = null;
        _imageType = null;
        _items = null;
        _name = null;
        _chartType = null;
        _nameKey = null;
        _valueKey = null;
        _width = 0;
        _height = 0;
        _dataset = null;
        _chart = null;
        _configuration = null;
    }

    protected abstract JFreeChart createChart();
    protected abstract Dataset createDataset();
    protected abstract NSArray<String> supportedTypes();

    
    public Dataset dataset() {
        if(_dataset == null) {
            if(hasBinding("dataset") && canGetValueForBinding("dataset")) {
                _dataset = (Dataset)valueForBinding("dataset");
            }
            if(_dataset == null) {
                _dataset = createDataset();
                if(canSetValueForBinding("dataset")) {
                    setValueForBinding(_dataset, "dataset");
                }
            }
        }
        return _dataset;
    }
    
    public JFreeChart chart() {
        if(_chart == null) {
            if(hasBinding("chart") && canGetValueForBinding("chart")) {
                _chart = (JFreeChart)valueForBinding("chart");
            }
            if(_chart == null) {
                _chart = createChart();
                if(hasBinding("chart") && canSetValueForBinding("chart")) {
                    setValueForBinding(_chart, "chart");
                }
                for (Enumeration<String> keys = configuration().keyEnumerator(); keys.hasMoreElements();) {
                    String keypath = keys.nextElement();
                    Object value = configuration().objectForKey(keypath);
                    NSKeyValueCodingAdditions.Utility.takeValueForKeyPath(_chart, value, keypath);
                }
            }
        }
        return _chart;
    }
    
    public NSData imageData() {
        if(_imageData == null) {
            try {
                JFreeChart chart = chart();
                if(chart != null) {
                    ByteArrayOutputStream imageStream = new ByteArrayOutputStream();
                    ChartRenderingInfo info = null;
                    if(showToolTips() || showUrls()) {
                        info = new ChartRenderingInfo(new StandardEntityCollection());
                    }
                    if("image/jpeg".equals(imageType())) {
                        ChartUtilities.writeChartAsJPEG(imageStream, chart, width(), height(), info);
                    } else {
                        ChartUtilities.writeChartAsPNG(imageStream, chart, width(), height(), info, true, 0);
                    }
                    if(showToolTips() || showUrls()) {
                        _imageMapName = "ERP" + System.identityHashCode(chart);
                        ToolTipTagFragmentGenerator toolTipGenerator = null;
                        URLTagFragmentGenerator urlTagFragmentGenerator = null;
                        if(showToolTips()) {
                            toolTipGenerator = new ToolTipTagFragmentGenerator() {
                                public String generateToolTipFragment(String toolTip) {
                                    return " title=\"" +toolTip + "\"";
                                }
                            };
                        }
                        if(showUrls()) {
                            urlTagFragmentGenerator = new URLTagFragmentGenerator() {
                                public String generateURLFragment(String url) {
                                    return " href=\""+url+"\"";
                                }
                            };
                        }
                        // for jfreechart-1.0.x use this line
						 _imageMap = ImageMapUtilities.getImageMap(_imageMapName, info, toolTipGenerator, urlTagFragmentGenerator);
						// for jfreechart-0.9.x use this line
                        // _imageMap = ImageMapUtil.getImageMap(_imageMapName, info, toolTipGenerator, urlTagFragmentGenerator);
                    }
                    _imageData = new NSData(imageStream.toByteArray());
                }
            } catch (Exception ex) {
                log.error("Could not convert chart to NSData.", ex);
                NSForwardException._runtimeExceptionForThrowable(ex);
            }
        }
        return _imageData;
    }
    
    public String imageMap() {
    	imageData();
        return _imageMap;
    }
    
    public String otherTagString() {
        String map = imageMap();
        return map == null ? null : "usemap=\"#" + _imageMapName  + "\"";
    }
}    