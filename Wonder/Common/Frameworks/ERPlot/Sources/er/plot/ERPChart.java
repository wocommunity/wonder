package er.plot;
import com.webobjects.foundation.*;
import com.webobjects.appserver.*;
import er.extensions.*;
import java.io.*;
import org.jfree.chart.*;
import org.jfree.data.*;

public class ERPChart extends ERXStatelessComponent {
    
    /** logging support */
    private static final ERXLogger log = ERXLogger.getERXLogger(ERPChart.class, "plot");
    protected static final int DEFAULT_SIZE = 400;

    protected NSData _imageData;
    public String _imageKey;

    protected NSArray _items;
    protected String _name;
    protected String _type;
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
            _width = intValueForBinding("_width", DEFAULT_SIZE);
        }
        return _width;
    }

    public int height() {
        if(_height == 0) {
            _height = intValueForBinding("_height", DEFAULT_SIZE);
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

    public String type() {
        if(_type == null) {
            _type = (String)valueForBinding("type");
        }
        return _type;
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

        _items = null;
        _name = null;
        _type = null;
        _nameKey = null;
        _valueKey = null;
        _width = 0;
        _height = 0;
        _dataset = null;
        _chart = null;
    }

    public JFreeChart chart() {
        return null;
    }
    
    public Dataset dataset() {
        return null;
    }

    public NSData imageData() {
        if(_imageData == null) {
            try {
                JFreeChart chart = chart();
                if(chart != null) {
                    ByteArrayOutputStream baos = new ByteArrayOutputStream();
                    ChartUtilities.writeChartAsJPEG(baos, chart, height(), width());
                    _imageData = new NSData(baos.toByteArray());
                }
            } catch (Exception ex) {
                log.warn(ex,ex);
            }
        }
        return _imageData;
    }
}    