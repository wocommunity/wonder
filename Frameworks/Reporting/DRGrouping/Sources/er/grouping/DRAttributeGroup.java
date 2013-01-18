package er.grouping;

import java.util.Enumeration;

import com.webobjects.foundation.NSArray;
import com.webobjects.foundation.NSDictionary;
import com.webobjects.foundation.NSMutableArray;
import com.webobjects.foundation.NSMutableDictionary;

import er.extensions.foundation.ERXValueUtilities;

/**
 * Group of attributes which is also an attribute by itself.
 */
public class DRAttributeGroup extends DRAttribute  {

    protected NSMutableArray _attributes;
    protected NSMutableArray _flatAttributes;
    protected NSMutableArray _flatAttributesTotal;

    public static DRAttributeGroup withKeyPathFormatLabelTotalListUserInfo(String keyPath, String format, String label, boolean shouldTotal, NSArray attributes, NSDictionary userInfo) {
        DRAttributeGroup attributeGroup = new DRAttributeGroup(keyPath, format, label, shouldTotal, attributes, userInfo);
        return attributeGroup;
    }

    @Override
    public void resetDefaults() {
        super.resetDefaults();
        _attributes = new NSMutableArray();
        _flatAttributes = new NSMutableArray();
        _isGroup = true;
        _flatAttributesTotal = null;
    }

    public DRAttributeGroup(String keyPath, String format, String label, boolean shouldTotal, NSArray attributes, NSDictionary userInfo) {
        super(keyPath, format, label, shouldTotal, userInfo);
        if (attributes != null) {
            _attributes.addObjectsFromArray(attributes);
        }
        flatListForAttributeList();
    }
    
    public DRAttributeGroup(NSDictionary dictionary, NSArray subAttributes) {
        this((String)dictionary.objectForKey("keyPath"),
             (String)dictionary.objectForKey("format"),
             (String)dictionary.objectForKey("label"),
             ERXValueUtilities.booleanValue(dictionary.objectForKey("total")),
             subAttributes,
             (NSDictionary)dictionary.objectForKey("userInfo"));
    }

    @Override
    public boolean showTotal() {
        return _shouldTotal;
    }

    @Override
    public boolean isGroup() {
        return _isGroup;
    }

    @Override
    public NSMutableArray attributes() {
        return _attributes;
    }

    protected void flatListForAttributeDepthDictionary(DRAttribute att, int attributeListDepth, NSMutableDictionary flatAttributeDepthDict) {
        NSMutableArray lst;
        Number dpthKey;

        if (!att.isGroup()) {
            _flatAttributes.addObject(att);
        } else {
            NSArray subvls = att.flatAttributesWithDepthDictionary(attributeListDepth, flatAttributeDepthDict);
            _flatAttributes.addObjectsFromArray(subvls);
            if (att.shouldTotal()) {
                _flatAttributes.addObject(att);
            }
        }

        if (flatAttributeDepthDict != null) {
            dpthKey = Integer.valueOf(attributeListDepth);
            lst = (NSMutableArray)flatAttributeDepthDict.objectForKey(dpthKey);
            if (lst == null) {
                lst = new NSMutableArray();
                flatAttributeDepthDict.setObjectForKey(lst, dpthKey);
            }
            lst.addObject(att);
        }
    }

    @Override
    protected NSArray flatAttributesWithDepthDictionary(int attributeListDepth, NSMutableDictionary flatAttributeDepthDict) {
        DRAttribute att;
        Enumeration anEnum = _attributes.objectEnumerator();
        _flatAttributes.removeAllObjects();
        attributeListDepth = attributeListDepth + 1;
        while (anEnum.hasMoreElements()) {
            att = (DRAttribute)anEnum.nextElement();
            flatListForAttributeDepthDictionary(att, attributeListDepth, flatAttributeDepthDict);
        }
        if (flatAttributeDepthDict != null && showTotal()) {
            Number dpthKey = Integer.valueOf(attributeListDepth);
            NSMutableArray lst = (NSMutableArray)flatAttributeDepthDict.objectForKey(dpthKey);
            if (lst == null) {
                lst = new NSMutableArray();
                flatAttributeDepthDict.setObjectForKey(lst, dpthKey);
            }
            lst.addObject(DRAttribute.withAttributeGroup(this));
        }
        return _flatAttributes;
    }

    protected void flatListForAttributeList() {
        flatAttributesWithDepthDictionary(0, null);
    }

    @Override
    public NSArray flatAttributes() {
        return _flatAttributes;
    }

    @Override
    public NSArray flatAttributesTotal() {
        if (_flatAttributesTotal == null) {
            DRAttribute att;
            Enumeration anEnum = flatAttributes().objectEnumerator();
            _flatAttributesTotal = new NSMutableArray();
            while (anEnum.hasMoreElements()) {
                att = (DRAttribute)anEnum.nextElement();
                if (att.shouldTotal()) {
                    _flatAttributesTotal.addObject(att);
                }
            }
        }
        return _flatAttributesTotal;
    }

    @Override
    public String toString() {
        return "<DRAttributeGroup keyPath:\"" + keyPath() + "\"; label:\"" + label() + "\"; attributes: " + attributes() + "; >";
    }
}