package er.reporting;

import com.webobjects.appserver.WOComponent;
import com.webobjects.appserver.WOContext;
import com.webobjects.foundation.NSArray;
import com.webobjects.foundation.NSMutableArray;
import com.webobjects.foundation.NSPropertyListSerialization;

import er.grouping.DRMasterCriteria;
import er.grouping.DRSubMasterCriteria;

public class WRMasterCriteriaEditor extends WOComponent  {

	public DRMasterCriteria masterCriteria;
    public DRSubMasterCriteria aSubMasterCriteria;
    public String aPossibleUseType;
    public String masterCriteriaUpdateAction;

    public WRMasterCriteriaEditor(WOContext c){
        super(c);
    }

    public boolean showTotal() {
        if (masterCriteria.userInfo().objectForKey("SHOW_TOTAL").equals("true")) {
            return true;
        }

        return false;
    }


    public void setShowTotal(boolean v) {
        if (v) {
            masterCriteria.userInfo().setObjectForKey("true", "SHOW_TOTAL");
        } else {
            masterCriteria.userInfo().setObjectForKey("false", "SHOW_TOTAL");
        }

    }


    public boolean showOther() {
        if(masterCriteria.shouldShowOther()){
        //if (masterCriteria.userInfo().objectForKey("SHOW_OTHER").equals("true")) {
            return true;
        }

        return false;
    }


    public void setShowOther(boolean v) {
        if (v) {
            masterCriteria.userInfo().setObjectForKey("true", "SHOW_OTHER");
        } else {
            masterCriteria.userInfo().setObjectForKey("false", "SHOW_OTHER");
        }

    }


    public boolean isAString() {
        //log.debug( "isAString: "+(masterCriteria.userInfo().objectForKey("IS_STRING")));

        if (masterCriteria.userInfo().objectForKey("IS_STRING").equals("true")) {
            return true;
        }

        return false;
    }


    public void setIsAString(boolean v) {
        if (v) {
            //log.debug( "setIsAString: to _YES");
            masterCriteria.userInfo().setObjectForKey("true", "IS_STRING");
        } else {
            //log.debug( "setIsAString: to _NO");
            masterCriteria.userInfo().setObjectForKey("false", "IS_STRING");
        }

        //log.debug( "setIsAString: "+(masterCriteria.userInfo().objectForKey("IS_STRING")));
    }


    public Object addSubMasterCrit() {
        NSMutableArray smcList = new NSMutableArray(masterCriteria.subCriteriaList());
        DRMasterCriteriaEditing prnt = (DRMasterCriteriaEditing)parent();
        smcList.addObject(DRSubMasterCriteria.withKeyUseMethodUseTimeFormatFormatPossibleValuesUseTypeGroupEdgesPossibleValues("category", false, false, null, null, false, null));
        prnt.replaceMCWith(masterCriteria, smcList);
        return null;
    }


    public Object deleteSubMasterCrit() {
        NSMutableArray smcList = new NSMutableArray(masterCriteria.subCriteriaList());
        DRMasterCriteriaEditing prnt = (DRMasterCriteriaEditing)parent();
        smcList.removeObject(aSubMasterCriteria);
        prnt.replaceMCWith(masterCriteria, smcList);
        return null;
    }


    public String possibleValuesString() {
        String pls = aSubMasterCriteria.rawPossibleValues().toString();
        return pls;
    }


    public void setPossibleValuesString(String pls) {
        if (!aSubMasterCriteria.nonNumberOrDate()) {
            NSArray vals = (NSArray)NSPropertyListSerialization.propertyListFromString(pls);
            aSubMasterCriteria.setRawPossibleValues(vals);
        }

    }


    public boolean showAddSub() {
        return true;
    }


    public boolean showDeleteSub() {
        if (masterCriteria.subCriteriaList().count() == 1) {
            return false;
        }

        return true;
    }
}
