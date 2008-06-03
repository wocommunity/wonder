package er.grouping;

import java.util.*;

import com.webobjects.foundation.*;

/* DRValueGroup.h created by Administrator on Sun 01-Nov-1998 */
//#import <WebObjects/WebObjects.h>
public class DRValueGroup extends DRValue  {

    protected boolean _hasTotaled;
    protected double _total;
    protected NSMutableArray _flatValues;
    protected NSMutableArray _values;
    protected boolean _showTotal;
    
    public DRValueGroup(){
        super();
    }

    static public DRValue withRecordAttribute(DRRecord rec, DRAttribute att) {
        DRValueGroup aVal = new DRValueGroup(rec, att);
        return aVal;
    }

    public boolean showTotal() {
        return _showTotal;
    }

    public DRValueGroup(DRRecord rec, DRAttribute att) {
        _isGroup = true;
        _hasTotaled = false;
        _total = 0.0;

        if (att.showTotal()) {
            _showTotal = true;
        }

        _values = new NSMutableArray();
        _flatValues = new NSMutableArray();
        _record = rec;
        _attribute = att;
        this.buildSubValues();
    }

    private void buildSubValues() {
        NSArray attrs = this.attribute().attributes();
        Enumeration anEnum = attrs.objectEnumerator();

        while (anEnum.hasMoreElements()) {
            DRAttribute att = (DRAttribute)anEnum.nextElement();
            DRValue val;

            if (att.isGroup()) {
                val = DRValueGroup.withRecordAttribute(this.record(), att);
                NSArray vals = val.flatValues();
                _flatValues.addObjectsFromArray(vals);
            } else {
                val = DRValue.withRecordAttribute(this.record(), att);
                _flatValues.addObject(val);
            }

            double subtot = val.total();
            _total = _total+subtot;
            _values.addObject(val);
        }

        if (this.attribute().shouldTotal()) {
            _flatValues.addObject(DRValue.withTotalAttribute(_total, this.attribute()));
        }

    }

    public NSArray flatValues() {
        return _flatValues;
    }

    public boolean isGroup() {
        return _isGroup;
    }

    public double total() {
        return _total;
    }

}