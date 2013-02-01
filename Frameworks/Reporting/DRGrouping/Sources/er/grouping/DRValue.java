package er.grouping;

import com.webobjects.foundation.NSArray;

/* DRValue.h created by Administrator on Sun 01-Nov-1998 */
//#import <WebObjects/WebObjects.h>
public class DRValue {

    protected DRRecord _record;
    protected DRAttribute _attribute;
    protected boolean _isGroup = false;
    protected boolean _isTotal = false;
    protected boolean _isNull = false;
    protected Number _totalValue;
    protected double _rawTotal;
    protected String _key;

    static public DRValue nullTotal() {
        DRValue aVal = new DRValue();
        aVal.initAsNull();
        return aVal;
    }

    static public DRValue withTotalAttribute(double tot, DRAttribute att) {
        DRValue aVal = new DRValue(tot, att);
        return aVal;
    }

    static public DRValue withRecordAttribute(DRRecord rec, DRAttribute att) {
        DRValue aVal = new DRValue(rec, att);
        return aVal;
    }

    public DRValue(DRRecord rec, DRAttribute att) {
        super();
        _record = rec;
        _attribute = att;
    }

    public DRValue(double tot, DRAttribute att) {
        super();
        _isTotal = true;
        _totalValue = Double.valueOf(tot);
        _attribute = att;
        _rawTotal = tot;
    }

    public DRValue initAsNull() {
        _isNull = true;
        _totalValue = Double.valueOf(0);
        _rawTotal = 0.0;
        return this;
    }

    public DRValue() {
        super();
    }

    public boolean isGroup() {
        return _isGroup;
    }

    public boolean isTotal() {
        return _isTotal;
    }

    public boolean isNull() {
        return _isNull;
    }

    public boolean shouldTotal() {
        if (_isNull) {
            return false;
        }

        if (_isTotal) {
            return true;
        }

        if (_attribute.shouldTotal()) {
            return true;
        }

        return false;
    }

    public String key() {
        if (_key == null) {
            if (_isTotal) {
                String s = _attribute.keyPath();
                _key = s;
            } else {
                _key = _attribute.keyPath();
            }

        }

        return _key;
    }

    public DRAttribute attribute() {
        return _attribute;
    }
    
    public DRRecord record() {
        return _record;
    }

    public Object val() {
        if (_isTotal) {
            return _totalValue;
        }
        if(_record == null || _record.rawRecord() == null)
            return null;
        if(_attribute.isComputed()) {
            // String code = _attribute.keyPath().substring(1);
            // we ignore WOOgnl because it is handled in the reporting framework
            return null;
        }
        return _record.rawRecord().valueForKeyPath(_attribute.keyPath());
    }

    public double total() {
        if (_isTotal) {
            return _rawTotal;
        }
        if (_isNull) {
            return 0.0;
        }
        if (!_attribute.shouldTotal()) {
            return 0.0;
        }

        Object val = val();

        if (val != null) {
            return DRValueConverter.converter().doubleForValue(val);
        }

        return 0.0;
    }

    public void setTotal(double tot) {
        // only mutable id 'isTotal'

        if (_isTotal) {
            _totalValue = Double.valueOf(tot);
            _rawTotal = tot;
        }

    }

    public NSArray flatValues() {
        return null;
    }

}
