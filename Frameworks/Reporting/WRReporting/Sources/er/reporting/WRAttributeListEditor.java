package er.reporting;

import java.util.Enumeration;

import com.webobjects.appserver.WOComponent;
import com.webobjects.appserver.WOContext;
import com.webobjects.foundation.NSArray;
import com.webobjects.foundation.NSMutableArray;
import com.webobjects.foundation.NSNotificationCenter;

import er.grouping.DRAttribute;
import er.grouping.DRAttributeGroup;
import er.grouping.DRReportModel;

public class WRAttributeListEditor extends WOComponent implements DRAttributeEditing  {

    protected NSArray _attributeList;
    protected String attributeUpdateAction;

    public DRAttribute attribute;
    protected int currentIndexAtt;
    protected int currentLevelAtt;

    public WRAttributeListEditor(WOContext c){
        super(c);
    }

    // CHECKME ak This can not be working!! We will mess seriously when we have several users and one edits this list
    public void resetAttributes() {
        NSNotificationCenter.defaultCenter().postNotification(DRReportModel.DRReportModelUpdateNotification, null, null);
    }


    @Override
    public void reset() {
        _attributeList = null;
    }

    public NSArray attributeList() {
        return _attributeList;
    }
    public void setAttributeList(NSArray v) {
        _attributeList = v;
    }


    public NSArray attributeSubList() {
        if (attribute.isGroup()) {
            return attribute.attributes();
        }

        return null;
    }


    public void toggleGroupInList(DRAttribute att) {
        int curDex;
        DRAttribute newAtt;
        NSMutableArray arr = new NSMutableArray(attributeList());
        curDex = arr.indexOfObject(att);

        if (att.isGroup()) {
            newAtt = DRAttribute.withKeyPathFormatLabelTotalUserInfo(att.keyPath(), att.format(), att.label(), att.shouldTotal(), att.userInfo());
        } else {
            newAtt = DRAttributeGroup.withKeyPathFormatLabelTotalListUserInfo(att.keyPath(), att.format(), att.label(), att.shouldTotal(), new NSMutableArray(), att.userInfo());
        }

        arr.insertObjectAtIndex(newAtt, curDex);
        arr.removeObjectAtIndex(curDex+1);
        setAttributeList(arr);
    }


    public void addObjectToList() {
        NSMutableArray arr = new NSMutableArray(attributeList());
        arr.addObject(DRAttribute.withKeyPathFormatLabelTotalUserInfo("keypath", null, "Label", false, null));
        setAttributeList(arr);
    }


    public Object add() {
        addObjectToList();
        return null;
    }


    public String attribLabel() {
        return attribute.label();
    }


    public NSMutableArray newArraySans(NSArray arr1, Object member) {
        NSMutableArray arr = new NSMutableArray();
        Enumeration en = arr1.objectEnumerator();

        while (en.hasMoreElements()) {
            DRAttribute att = (DRAttribute)en.nextElement();

            if (!att.equals(member)) {
                arr.addObject(att);
            }

        }

        return arr;
    }


    public NSArray moveUpArray(DRAttribute member, boolean up, NSArray arr1) {
        int cnt;
        int cur = arr1.indexOfObject(member);
        NSMutableArray arr = newArraySans(arr1, member);
        cnt = arr.count();

        if (up) {
            int newdex = cur-1;

            if (newdex < 0) {
                arr.addObject(member);
            } else {
                arr.insertObjectAtIndex(member, newdex);
            }

        } else {
            int newdex = cur+1;

            if (newdex > cnt) {
                arr.insertObjectAtIndex(member, 0);
            } else {
                arr.insertObjectAtIndex(member, newdex);
            }

        }

        return arr;
    }


    public void deleteSubAttribute(DRAttribute subAttribute) {
        setAttributeList(newArraySans(attributeList(), subAttribute));
    }


    public void moveSubAttributeUp(DRAttribute subAtt, boolean up) {
        setAttributeList(moveUpArray(subAtt, up, attributeList()));
    }


    public Object regenReport() {
        resetAttributes();

        if (attributeUpdateAction != null) {
            return performParentAction(attributeUpdateAction);
        }

        return null;
    }
}
