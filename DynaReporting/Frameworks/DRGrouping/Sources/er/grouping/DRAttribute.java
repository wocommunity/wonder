package er.grouping;

import com.webobjects.foundation.NSArray;
import com.webobjects.foundation.NSDictionary;
import com.webobjects.foundation.NSMutableArray;
import com.webobjects.foundation.NSMutableDictionary;

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
    protected NSArray _emptyArray = NSArray.EmptyArray;
    private NSMutableArray __attributes = new NSMutableArray();

    static public DRAttribute withAttributeGroup(DRAttributeGroup attributeGroup) {
        DRAttribute attribute = new DRAttribute(attributeGroup);
        return attribute;
    }

    static public DRAttribute withKeyPathFormatLabelTotalUserInfo(String keyPath, String format, String label, boolean shouldTotal, NSDictionary userInfo) {
        DRAttribute attribute = new DRAttribute(keyPath, format, label, shouldTotal, userInfo);
        return attribute;
    }

    public DRAttribute(String keyPath, String format, String label, boolean shouldTotal, NSDictionary userInfo) {
        this();
        setKeyPath(keyPath);
        setFormat(format);
        setLabel(label);
        setShouldTotal(shouldTotal);
        setUserInfo(userInfo);
    }

    public DRAttribute(DRAttributeGroup attributeGroup) {
        this();
        setAttributeGroup(attributeGroup);
        setIsPlaceHolderTotal(true);
        setLabel("Total");
    }
    
    public DRAttribute() {
        super();
        _userInfo = new NSMutableDictionary();
        //_emptyArray = new NSMutableArray();
        this.resetDefaults();
    }

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

    public String keyPath() {
        return _keyPath;
    }
    public void setKeyPath(String value) {
        _keyPath = value;
    }

    public String format() {
        return _format;
    }
    public void setFormat(String value) {
        _format = value;
    }

    public boolean showTotal() {
        return false;
    }
    
    public boolean shouldTotal() {
        return _shouldTotal;
    }
    public void setShouldTotal(boolean value) {
        _shouldTotal = value;
    }
    
    public boolean shouldSort() {
        return _shouldSort;
    }
    public void setShouldSort(boolean value) {
        _shouldSort = value;
    }

    public boolean shouldRestrictToFirstRow() {
        return _shouldRestrictToFirstRow;
    }
    public void setShouldRestrictToFirstRow(boolean value) {
        _shouldRestrictToFirstRow = value;
    }

    public String label() {
        return _label;
    }
    public void setLabel(String value) {
        _label = value;
    }

    public NSDictionary userInfo() {
        return _userInfo;
    }
    public void setUserInfo(NSDictionary value) {
        if(value == null) {
            value = new NSMutableDictionary();
        }
        _userInfo = value;
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
    protected NSArray flatAttributesWithDepthDictionary(int attributeListDepth, NSMutableDictionary flatAttributeDepthDict) {
        return _emptyArray;
    }

    public void setAttributeGroup(DRAttributeGroup attributeGroup) {
        _attributeGroup = attributeGroup;
        if(attributeGroup != null) {
            setKeyPath(attributeGroup.keyPath());
            setFormat(attributeGroup.format());
            setShouldTotal(attributeGroup.shouldTotal());
            setUserInfo(attributeGroup.userInfo());
        }
    }
    public DRAttributeGroup attributeGroup() {
        return _attributeGroup;
    }

    public boolean isPlaceHolderTotal() {
        return _isPlaceHolderTotal;
    }
    public void setIsPlaceHolderTotal(boolean value) {
        _isPlaceHolderTotal = value;
    }

    public String toString() {
        return "<DRAttribute label:\"" + label() + "\"; keyPath:\"" + keyPath() + "\"; format:\"" + format() + "\"; >";
    }
}