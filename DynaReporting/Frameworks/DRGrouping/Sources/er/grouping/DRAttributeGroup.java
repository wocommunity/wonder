package er.grouping;

import java.lang.*;
import java.util.*;
import java.io.*;
import com.webobjects.foundation.*;
import com.webobjects.eocontrol.*;
import com.webobjects.eoaccess.*;
import com.webobjects.appserver.*;

/* DRAttributeGroup.h created by Administrator on Sun 01-Nov-1998 */
public class DRAttributeGroup extends DRAttribute  {

    protected NSMutableArray _attributes;
    protected NSMutableArray _flatAttributes;
    protected NSMutableArray _flatAttributesTotal;

    static public DRAttributeGroup withKeyPathFormatLabelTotalListUserInfo(String ap, String af, String al, boolean at, NSArray lst, NSDictionary ui) {
        DRAttributeGroup aAtt = new DRAttributeGroup();
        aAtt.setKeyPath(ap);
        aAtt.setFormat(af);
        aAtt.setLabel(al);
        aAtt.setShouldTotal(at);
        aAtt.setUserInfo(ui);
        if (lst != null) {
            aAtt.attributes().addObjectsFromArray(lst);
        }
        aAtt.flatListForAttributeList();
        return aAtt;
    }

    public DRAttributeGroup() {
        super();
        _attributes = new NSMutableArray();
        _flatAttributes = new NSMutableArray();
        _isGroup = true;
        _flatAttributesTotal = null;
        return;
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
        //OWDebug.println(1, "entered: att:"+att);
        //OWDebug.println(1, "attributeListDepth:"+attributeListDepth);
        NSMutableArray lst;
        Number dpthKey;

        if (!att.isGroup()) {
            //OWDebug.println(1, "att is NOT group");
            _flatAttributes.addObject(att);
        } else {
            //OWDebug.println(1, "att IS group");
            //OWDebug.println(1, "about to call flatAttributesWithDepthDictionary");
            NSArray subvls = att.flatAttributesWithDepthDictionary(attributeListDepth, flatAttributeDepthDict);
            _flatAttributes.addObjectsFromArray(subvls);
            if (att.shouldTotal()) {
                _flatAttributes.addObject(att);
            }
        }

        if (flatAttributeDepthDict != null) {
            dpthKey = new Integer(attributeListDepth);
            //OWDebug.println(1, "dpthKey:"+dpthKey);
            lst = (NSMutableArray)flatAttributeDepthDict.objectForKey(dpthKey);
            if (lst == null) {
                lst = new NSMutableArray();
                flatAttributeDepthDict.setObjectForKey(lst, dpthKey);
            }
            lst.addObject(att);
        }
        //OWDebug.println(1, "flatAttributeDepthDict:"+flatAttributeDepthDict);
    }


    public NSArray flatAttributesWithDepthDictionary(int attributeListDepth, NSMutableDictionary flatAttributeDepthDict) {
        //OWDebug.println(1, "entered: attributeListDepth:"+attributeListDepth);
        DRAttribute att;
        Enumeration anEnum = _attributes.objectEnumerator();
        _flatAttributes.removeAllObjects();
        attributeListDepth = attributeListDepth + 1;
        //OWDebug.println(1, "attributeListDepth after increment:"+attributeListDepth);
        while (anEnum.hasMoreElements()) {
            att = (DRAttribute)anEnum.nextElement();
            //OWDebug.println(1, "about to call flatListForAttributeDepthDictionary");
            this.flatListForAttributeDepthDictionary(att, attributeListDepth, flatAttributeDepthDict);
        }
        if (flatAttributeDepthDict != null && this.showTotal()) {
            Number dpthKey = new Integer(attributeListDepth);
            //OWDebug.println(1, "dpthKey:"+dpthKey);
            NSMutableArray lst = (NSMutableArray)flatAttributeDepthDict.objectForKey(dpthKey);
            if (lst == null) {
                lst = new NSMutableArray();
                flatAttributeDepthDict.setObjectForKey(lst, dpthKey);
            }
            lst.addObject(DRAttribute.withAttributeGroup(this));
        }
        //OWDebug.println(1, "_flatAttributes:"+_flatAttributes);
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