package er.grouping;

import java.util.*;
import com.webobjects.foundation.*;
import er.extensions.*;

public class DRAttributeGroup extends DRAttribute  {

    protected NSMutableArray _attributes;
    protected NSMutableArray _flatAttributes;
    protected NSMutableArray _flatAttributesTotal;

    static public DRAttributeGroup withKeyPathFormatLabelTotalListUserInfo(String ap, String af, String al, boolean at, NSArray lst, NSDictionary ui) {
        DRAttributeGroup aAtt = new DRAttributeGroup(ap, af, al, at, lst, ui);
        return aAtt;
    }

    public DRAttributeGroup() {
        _attributes = new NSMutableArray();
        _flatAttributes = new NSMutableArray();
        _isGroup = true;
        _flatAttributesTotal = null;
    }

    public DRAttributeGroup(String ap, String af, String al, boolean at, NSArray lst, NSDictionary ui) {
        this();
        setKeyPath(ap);
        setFormat(af);
        setLabel(al);
        setShouldTotal(at);
        setUserInfo(ui);
        if (lst != null) {
            attributes().addObjectsFromArray(lst);
        }
        flatListForAttributeList();
    }

    public DRAttributeGroup(NSDictionary attDict, NSArray subAttListObjects) {
        this((String)attDict.objectForKey("keyPath"),
             (String)attDict.objectForKey("format"),
             (String)attDict.objectForKey("label"),
             ERXValueUtilities.booleanValue(attDict.objectForKey("total")),
             subAttListObjects,
             (NSDictionary)attDict.objectForKey("userInfo"));
    }

    public boolean showTotal() {
        return _shouldTotal;
    }

    public boolean isGroup() {
        return _isGroup;
    }

    public NSMutableArray attributes() {
        return _attributes;
    }

    public void flatListForAttributeDepthDictionary(DRAttribute att, int attributeListDepth, NSMutableDictionary flatAttributeDepthDict) {
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
            dpthKey = new Integer(attributeListDepth);
            lst = (NSMutableArray)flatAttributeDepthDict.objectForKey(dpthKey);
            if (lst == null) {
                lst = new NSMutableArray();
                flatAttributeDepthDict.setObjectForKey(lst, dpthKey);
            }
            lst.addObject(att);
        }
    }

    public NSArray flatAttributesWithDepthDictionary(int attributeListDepth, NSMutableDictionary flatAttributeDepthDict) {
        DRAttribute att;
        Enumeration anEnum = _attributes.objectEnumerator();
        _flatAttributes.removeAllObjects();
        attributeListDepth = attributeListDepth + 1;
        while (anEnum.hasMoreElements()) {
            att = (DRAttribute)anEnum.nextElement();
            this.flatListForAttributeDepthDictionary(att, attributeListDepth, flatAttributeDepthDict);
        }
        if (flatAttributeDepthDict != null && this.showTotal()) {
            Number dpthKey = new Integer(attributeListDepth);
            NSMutableArray lst = (NSMutableArray)flatAttributeDepthDict.objectForKey(dpthKey);
            if (lst == null) {
                lst = new NSMutableArray();
                flatAttributeDepthDict.setObjectForKey(lst, dpthKey);
            }
            lst.addObject(DRAttribute.withAttributeGroup(this));
        }
        return _flatAttributes;
    }

    public void flatListForAttributeList() {
        this.flatAttributesWithDepthDictionary(0, null);
    }

    public NSArray flatAttributes() {
        return _flatAttributes;
    }

    public NSArray flatAttributesTotal() {
        if (_flatAttributesTotal == null) {
            DRAttribute att;
            Enumeration anEnum = this.flatAttributes().objectEnumerator();
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
}