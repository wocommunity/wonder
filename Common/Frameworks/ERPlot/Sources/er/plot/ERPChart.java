package er.plot;
import com.webobjects.foundation.*;
import com.webobjects.appserver.*;
import er.extensions.*;
import java.io.*;
import org.jfree.chart.*;
import org.jfree.data.*;

/**
 * Abstract superclass of the charts. The most important binding is <code>items</code> which
 * should contain an array of objects from which the values <code>nameKey</code> and <code>
 * valueKey</code> are retrieved. For example, you might have an array of
 * @binding name the name of the chart
 * @binding chartType the type of the chart (possible values depend on the concrete subclass)
 * @binding imageType the type of the image to show: <code>png</code> (default) or <code>jpeg</code>
 * @binding width the width of the chart (400 pixel if not specified)
 * @binding height the height of the chart (400 pixel if not specified)
 * @binding dataset Dataset to use. If this is given, then items, nameKey, valueKey and categoryKey are not considered.
 * @binding items array of values to display the chart for
 * @binding nameKey the key for the name (must return String)
 * @binding valueKey the key for the value (must return Number)
 * 
 * @author ak
 */
public abstract class ERPChart extends ERXStatelessComponent {
    
    /** logging support */
    private static final ERXLogger log = ERXLogger.getERXLogger(ERPChart.class, "plot");
    protected static final int DEFAULT_SIZE = 400;

    protected NSData _imageData;
    public String _imageKey;

    protected NSArray _items;
    protected String _name;
    protected String _chartType;
    protected String _imageType;
    protected String _nameKey;
    protected String _valueKey;
    protected int _width = 0;
    protected int _height = 0;
    protected Dataset _dataset;
    protected JFreeChart _chart;

    /**
     * Public constructor
     * @param context the context
     */
    public ERPChart(WOContext context) {
        super(context);
    }

    public NSArray items() {
        if(_items == null) {
            _items = (NSArray)valueForBinding("items");
            ERXAssert.DURING.notNull("items", _items);
        }
        return _items;
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

    public String nameKey() {
        if(_nameKey == null) {
            _nameKey = (String)valueForBinding("nameKey");
            ERXAssert.DURING.notNull("nameKey", _nameKey);
        }
        return _nameKey;
    }

    public String chartType() {
        if(_chartType == null) {
            _chartType = (String)valueForBinding("chartType");
        }
        return _chartType;
    }

    public String imageType() {
        if(_imageType == null) {
            _imageType = (String)valueForBinding("imageType");
            if(_imageType == null) {
                _imageType = "image/png";
            }
        }
        return _imageType;
    }

    public String valueKey() {
        if(_valueKey == null) {
            _valueKey = (String)valueForBinding("valueKey");
            ERXAssert.DURING.notNull("valueKey", _valueKey);
        }
        return _valueKey;
    }

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
    }

    public abstract JFreeChart chart();
    
    public Dataset dataset() {
        if(_dataset == null) {
            _dataset = (Dataset)valueForBinding("dataset");
        }
        return _dataset;
    }

    public NSData imageData() {
        if(_imageData == null) {
            try {
                JFreeChart chart = chart();
                if(chart != null) {
                    ByteArrayOutputStream baos = new ByteArrayOutputStream();
                    if("image/jpeg".equals(imageType())) {
                        ChartUtilities.writeChartAsJPEG(baos, chart, width(), height());
                    } else {
                        ChartUtilities.writeChartAsPNG(baos, chart, width(), height());
                    }
                    _imageData = new NSData(baos.toByteArray());
                }
            } catch (Exception ex) {
                log.warn(ex,ex);
            }
        }
        return _imageData;
    }
}    