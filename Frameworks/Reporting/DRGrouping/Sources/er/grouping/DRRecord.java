package er.grouping;

import java.util.*;

import com.webobjects.foundation.*;

public class DRRecord extends Object  {

    protected NSKeyValueCodingAdditions _rawRecord;
    protected DRReportModel _model;
    protected NSMutableArray _valueList;
    protected NSMutableArray _flatValueList;

    static public DRRecord withRawRecordModel(NSKeyValueCodingAdditions rawr, DRReportModel aMod) {
        DRRecord drr = new DRRecord();
        drr.initWithRawRecord(rawr, aMod);
        return drr;
    }

    // compute subtotals for clusters of attributes
    public void populateValueList() {
        _valueList.removeAllObjects();
        _flatValueList.removeAllObjects();
        //OWDebug.println(1, "entered");
        if(this.attributeList() != null){
            Enumeration anEnum = this.attributeList().objectEnumerator();
            while (anEnum.hasMoreElements()) {
                DRAttribute att = (DRAttribute)anEnum.nextElement();
                //OWDebug.println(1, "att:"+att);
                DRValue val = this.valueForAttributeRecord(att, this);
                //OWDebug.println(1, "val:"+val);
                _valueList.addObject(val);
            }
        }

    }

    public DRValue valueForAttributeRecord(DRAttribute att, DRRecord rec) {
        DRValue vl;

        if (!att.isGroup()) {
            vl = DRValue.withRecordAttribute(rec, att);
            _flatValueList.addObject(vl);
        } else {
            vl = DRValueGroup.withRecordAttribute(rec, att);
            NSArray subvls = vl.flatValues();
            _flatValueList.addObjectsFromArray(subvls);
        }

        return vl;
    }

    public DRRecord initWithRawRecord(NSKeyValueCodingAdditions rawr, DRReportModel aMod) {
        _rawRecord = rawr;
        _model = aMod;
        _valueList = new NSMutableArray();
        _flatValueList = new NSMutableArray();
        this.populateValueList();
        return this;
    }

    public NSKeyValueCodingAdditions rawRecord() {
        return _rawRecord;
    }

    public Object valueForKey(String key) {
        return NSKeyValueCoding.DefaultImplementation.valueForKey(_rawRecord, key);
    }

    //
    // This is key for reporting. You loop over the valueList to display values
    // You can also ask any value if it isGroup() and if so get the total
    //
    public NSArray valueList() {
        return _valueList;
    }

    public NSArray flatValueList() {
        return _flatValueList;
    }

    public DRReportModel model() {
        return _model;
    }

    public NSArray attributeList() {
        return this.model().attributeList();
    }

}