package er.reporting;

import com.webobjects.appserver.WOComponent;
import com.webobjects.appserver.WOContext;
import com.webobjects.foundation.NSMutableArray;
import com.webobjects.foundation.NSNotificationCenter;

import er.grouping.DRAttribute;
import er.grouping.DRAttributeGroup;
import er.grouping.DRReportModel;

public class WRAttributeEditor extends WOComponent implements DRAttributeEditing  {

    protected DRAttribute _attribute;
    protected DRAttribute _subAttribute;

    public WRAttributeEditor(WOContext c){
        super(c);
    }

    public DRAttribute attribute() {
    	return _attribute;
    }

    public void moveUp(DRAttribute member, boolean up) {
        int cnt;
        NSMutableArray arr = _attribute.attributes();
        int cur = arr.indexOfObject(member);
        arr.removeObject(member);
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

    }


    // CHECKME ak This can not be working!! We will mess seriously when we have several users and one edits this item
    public void resetAttributes() {
        NSNotificationCenter.defaultCenter().postNotification(DRReportModel.DRReportModelUpdateNotification, null, null);
    }

    public void deleteSubAttribute(DRAttribute subAtt) {
        _attribute.attributes().removeObject(subAtt);
    }


    public WOComponent deleteSubAttribute() {
        //log.debug( "attribute:"+ _attribute);
        //log.debug( "about to call parent");
        DRAttributeEditing prt = (DRAttributeEditing)parent();
        prt.deleteSubAttribute(_attribute);
        return null;
    }


    public void moveSubAttributeUp(DRAttribute subAtt, boolean up) {
        moveUp(subAtt, up);
    }


    public WOComponent down() {
        DRAttributeEditing prt = (DRAttributeEditing)parent();
        prt.moveSubAttributeUp(_attribute, false);
        return null;
    }


    public WOComponent up() {
        DRAttributeEditing prt = (DRAttributeEditing)parent();
        prt.moveSubAttributeUp(_attribute, true);
        return null;
    }


    public void toggleGroupInList(DRAttribute att) {
        DRAttribute newAtt;
        NSMutableArray arr = _attribute.attributes();
        int curDex = arr.indexOfObject(att);

        if (att.isGroup()) {
            newAtt = DRAttribute.withKeyPathFormatLabelTotalUserInfo(att.keyPath(), att.format(), att.label(), att.shouldTotal(), att.userInfo());
        } else {
            newAtt = DRAttributeGroup.withKeyPathFormatLabelTotalListUserInfo(att.keyPath(), att.format(), att.label(), att.shouldTotal(), new NSMutableArray(), att.userInfo());
        }

        arr.insertObjectAtIndex(newAtt, curDex);
        arr.removeObjectAtIndex(curDex+1);
    }


    public WOComponent toggleGroup() {
        DRAttributeEditing prt = (DRAttributeEditing)parent();
        prt.toggleGroupInList(_attribute);
        return null;
    }


    public void addObjectToList() {
        NSMutableArray arr = _attribute.attributes();
        arr.addObject(DRAttribute.withKeyPathFormatLabelTotalUserInfo("keypath", null, "Label", false, null));
    }


    public WOComponent add() {
        addObjectToList();
        return null;
    }


    public String toggleGroupLabel() {
        if (_attribute.isGroup()) {
            return "Make Attribute";
        } else {
            return "Make Group";
        }

    }


    public String toggleGroupImg() {
        if (_attribute.isGroup()) {
            return "folders.gif";
        } else {
            return "folder.gif";
        }

    }


    public boolean isGroup() {
        return _attribute.isGroup();
    }


    public WOComponent showUserInfo() {
        return null;
    }


    public int rowspan() {
        return _attribute.attributes().count()+1;
    }


    public DRAttribute subAttribute() {
        return _subAttribute;
    }
    public void setSubAttribute(DRAttribute v) {
        _subAttribute = v;
    }
}
