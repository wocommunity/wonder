package er.plot;

import java.util.*;

import org.jfree.data.*;

import com.webobjects.appserver.*;
import com.webobjects.foundation.*;

import er.extensions.*;

/**
 * Class for Chart Component ERPCategoryChart.
 *
 * @binding sample sample binding explanation
 *
 * @created ak on 17.12.04
 * @project ERPlot
 */

public abstract class ERPCategoryChart extends ERPChart {

    /** logging support */
    private static final ERXLogger log = ERXLogger.getERXLogger(ERPCategoryChart.class,"plot");
	
    protected String _categoryKey;
    protected String _yName;
    protected String _xName;
    
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
    
    public Dataset dataset() {
        if(_dataset == null) {
            _dataset = super.dataset();
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
        }
        return _dataset;
    }
}
