package er.grouping;

import com.webobjects.foundation.*;

public class DRAttribute extends Object  {
    protected String _keyPath;
    protected String _format;
    protected boolean _shouldTotal;
    protected boolean _shouldSort;
    protected boolean _shouldRestrictToFirstRow;
    protected boolean _isGroup;
    protected String _label;
    protected NSDictionary _userInfo;
    protected DRAttributeGroup _attributeGroup;
    protected boolean _isPlaceHolderTotal;
    protected boolean _isTotal;
    protected NSArray _emptyArray;
    private NSMutableArray __attributes = new NSMutableArray();

    public void resetDefaults() {
        _shouldTotal = false;
        _shouldSort = false;
        _shouldRestrictToFirstRow = false;
        _isGroup = false;
        _isPlaceHolderTotal = false;
        _isTotal = false;
        this.setFormat("");
        this.setKeyPath(null);
        this.setLabel(null);
        this.setAttributeGroup(null);
    }

    static public DRAttribute withAttributeGroup(DRAttributeGroup attg) {
        DRAttribute aAtt = new DRAttribute();
        aAtt.setAttributeGroup(attg);
        aAtt.setIsPlaceHolderTotal(true);
        aAtt.setKeyPath(attg.keyPath());
        aAtt.setFormat(attg.format());
        aAtt.setLabel("Total");
        aAtt.setShouldTotal(attg.shouldTotal());
        aAtt.setUserInfo(attg.userInfo());
        return aAtt;
    }

    static public DRAttribute withKeyPathFormatLabelTotalUserInfo(String ap, String af, String al, boolean at, NSDictionary ui) {
        DRAttribute aAtt = new DRAttribute();
        aAtt.setKeyPath(ap);
        aAtt.setFormat(af);
        aAtt.setLabel(al);
        aAtt.setShouldTotal(at);
        aAtt.setUserInfo(ui);
        return aAtt;
    }

    public DRAttribute() {
        super();
        _userInfo = new NSMutableDictionary();
        _emptyArray = new NSMutableArray();
        this.resetDefaults();
        return;
    }

    public String keyPath() {
        return _keyPath;
    }
    public void setKeyPath(String aval) {
        _keyPath = aval;
    }

    public String format() {
        return _format;
    }
    public void setFormat(String aval) {
        _format = aval;
    }

    public boolean showTotal() {
        return false;
    }
    
    public boolean shouldTotal() {
        return _shouldTotal;
    }
    public void setShouldTotal(boolean aval) {
        _shouldTotal = aval;
    }
    
    public boolean shouldSort() {
        return _shouldSort;
    }
    public void setShouldSort(boolean aval) {
        _shouldSort = aval;
    }

    public boolean shouldRestrictToFirstRow() {
        return _shouldRestrictToFirstRow;
    }
    public void setShouldRestrictToFirstRow(boolean aval) {
        _shouldRestrictToFirstRow = aval;
    }

    public String label() {
        return _label;
    }
    public void setLabel(String aval) {
        _label = aval;
    }

    public NSDictionary userInfo() {
        return _userInfo;
    }
    public void setUserInfo(NSDictionary aval) {
        if(aval == null) {
            aval = new NSMutableDictionary();
        }
        _userInfo = aval;
    }

    public boolean isGroup() {
        return _isGroup;
    }

    public NSMutableArray attributes() {
        return __attributes;
    }
    public NSArray flatAttributes() {
        return _emptyArray;
    }
    public NSArray flatAttributesTotal() {
        return _emptyArray;
    }
    public NSArray flatAttributesWithDepthDictionary(int attributeListDepth, NSMutableDictionary flatAttributeDepthDict) {
        return _emptyArray;
    }

    public String toString() {
        return ""+(super.toString())+"-"+(_keyPath);
    }

    public void setAttributeGroup(DRAttributeGroup atg) {
        _attributeGroup = atg;
    }
    public DRAttributeGroup attributeGroup() {
        return _attributeGroup;
    }

    public boolean isPlaceHolderTotal() {
        return _isPlaceHolderTotal;
    }
    public void setIsPlaceHolderTotal(boolean aval) {
        _isPlaceHolderTotal = aval;
    }
}