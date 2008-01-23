package er.grouping;

import ognl.webobjects.WOOgnl;

import com.webobjects.foundation.*;

/**
 * DRAttributes are to display what EOAttributes are to the back-end.  
 * Each DRAttribute can be defined with:
 * <li>'keyPath' (what you want to ask the dictionary or EO)
 * <li>'label' (what you want to call the attribute for display)
 * <li>a boolean for 'shouldTotal'
 * <li>a boolean for 'shouldSort'
 * <li>'format' for formatting dates
 * <li> a toggle turning a attribute into a group. 
 * 
 * Each group can have 0 or more DRAttributes within it.
 * @author david neumann
 *
 */
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
    protected boolean _isComputed;
    protected NSArray _emptyArray = NSArray.EmptyArray;
    private NSMutableArray __attributes = new NSMutableArray();

    /**
     * Creates a new DRAttributes from a DRAttributeGroup.
     * @param attributeGroup
     */
    public static DRAttribute withAttributeGroup(DRAttributeGroup attributeGroup) {
        DRAttribute attribute = new DRAttribute(attributeGroup);
        return attribute;
    }
    
    /**
     * Creates a new DRAttribute from the supplied parmeters.
     * @param keyPath
     * @param format
     * @param label
     * @param shouldTotal
     * @param userInfo
     */
    public static DRAttribute withKeyPathFormatLabelTotalUserInfo(String keyPath, String format, String label, boolean shouldTotal, NSDictionary userInfo) {
        DRAttribute attribute = new DRAttribute(keyPath, format, label, shouldTotal, userInfo);
        return attribute;
    }

    /**
     * Constructor with parameters.
     * @param keyPath
     * @param format
     * @param label
     * @param shouldTotal
     * @param userInfo
     */
    public DRAttribute(String keyPath, String format, String label, boolean shouldTotal, NSDictionary userInfo) {
        this();
        setKeyPath(keyPath);
        setFormat(format);
        setLabel(label);
        setShouldTotal(shouldTotal);
        setUserInfo(userInfo);
    }

    /**
     * Constructor with DRAttributeGroup.
     * @param attributeGroup
     */
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
        if(value != null) {
            _isComputed = value.indexOf("~") == 0 || value.indexOf("@") == 0;
        }
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

    public boolean isComputed() {
        return _isComputed;
    }
    
     public String toString() {
        return "<DRAttribute label:\"" + label() + "\"; keyPath:\"" + keyPath() + "\"; format:\"" + format() + "\"; >";
    }

    public double computeFromRawRecords(NSArray rawRecords) {
        String totalKey = keyPath();
        double doubleValue = 0.0;
        if(totalKey.indexOf("~") == 0) {
            Object result = WOOgnl.factory().getValue(totalKey.substring(1), rawRecords);
            doubleValue = DRValueConverter.converter().doubleForValue(result);
        } else if(totalKey.indexOf("@") == 0) {
            Object result = rawRecords.valueForKeyPath(totalKey);
            doubleValue = DRValueConverter.converter().doubleForValue(result);
            if(doubleValue == 0.0 && totalKey.indexOf("@count") == 0) {
                // FIXME: ak, we should return "-" on not found... probably possible via a formatter 
                return 0.0;
            }
        }
        return doubleValue;
    }
}
