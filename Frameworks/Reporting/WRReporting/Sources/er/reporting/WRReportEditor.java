package er.reporting;

import java.util.Enumeration;

import com.webobjects.appserver.WOComponent;
import com.webobjects.appserver.WOContext;
import com.webobjects.appserver.WOResponse;
import com.webobjects.foundation.NSArray;
import com.webobjects.foundation.NSMutableArray;
import com.webobjects.foundation.NSNotificationCenter;

import er.grouping.DRMasterCriteria;
import er.grouping.DRReportModel;
import er.grouping.DRSubMasterCriteria;

public class WRReportEditor extends WOComponent implements DRMasterCriteriaEditing  {

    protected NSArray _masterCriteriaList;
    protected NSArray _attributeList;
    protected DRMasterCriteria _masterCriteria;
    
    protected boolean _condition;
    protected String masterCriteriaUpdateAction;
    protected String attributeUpdateAction;
    protected String showAttributeEditor;

    public WRReportEditor(WOContext c){
        super(c);
    }

    public boolean showAttributeEditorBoolean() {
        if (showAttributeEditor == null) {
            return true;
        }
        if (showAttributeEditor.equals("false")) {
            return false;
        }
        return true;
    }


    public void resetGroups() {
        NSNotificationCenter.defaultCenter().postNotification(DRReportModel.DRReportModelRebuildNotification, null, null);
    }


    public boolean condition() {
        return _condition;
    }


    public void setCondition(boolean val) {
        _condition = val;
    }


    @Override
    public void reset() {
        _masterCriteria = null;
        _masterCriteriaList = null;
        _attributeList = null;
    }


    @Override
    public void awake() {
        //[self reset];
        super.awake();
    }


    @Override
    public void appendToResponse(WOResponse r, WOContext c) {
        //[self reset];
        super.appendToResponse(r, c);
    }

    public DRMasterCriteria masterCriteria() {
        return _masterCriteria;
    }
    public void setMasterCriteria(DRMasterCriteria v) {
        _masterCriteria = v;
    }


    public NSArray attributeList() {
        return _attributeList;
    }
    public void setAttributeList(NSArray v) {
        _attributeList = v;
    }


    public NSArray masterCriteriaList() {
        return _masterCriteriaList;
    }
    public void setMasterCriteriaList(NSArray v) {
        _masterCriteriaList = v;
    }

    public NSArray newSubCriteriaListFromMC(DRMasterCriteria amc) {
        NSMutableArray newSMCs = new NSMutableArray();
        NSArray oldSMCs = amc.subCriteriaList();
        Enumeration en = oldSMCs.objectEnumerator();

        while (en.hasMoreElements()) {
            DRSubMasterCriteria smc = (DRSubMasterCriteria)en.nextElement();
            DRSubMasterCriteria newsmc = DRSubMasterCriteria.withKeyUseMethodUseTimeFormatFormatPossibleValuesUseTypeGroupEdgesPossibleValues(smc.key(), smc.useMethod(), smc.useTimeFormat(), smc.format(), smc.possibleValuesUseType(), smc.groupEdges(), smc.rawPossibleValues());
            newSMCs.addObject(newsmc);
        }

        return newSMCs;
    }


    public Object regenReportGroup() {
        NSArray oldMCs = new NSArray(masterCriteriaList());
        Enumeration en = oldMCs.objectEnumerator();

        while (en.hasMoreElements()) {
            DRMasterCriteria amc = (DRMasterCriteria)en.nextElement();
            NSArray smcList = newSubCriteriaListFromMC(amc);
            replaceMCWith(amc, smcList);
        }

        if (masterCriteriaUpdateAction != null) {
            return performParentAction(masterCriteriaUpdateAction);
        }

        return null;
    }


    public Object addMC() {
        NSMutableArray arr = new NSMutableArray(masterCriteriaList());
        NSMutableArray smcList = new NSMutableArray();
        smcList.addObject(DRSubMasterCriteria.withKeyUseMethodUseTimeFormatFormatPossibleValuesUseTypeGroupEdgesPossibleValues("category", false, false, null, null, false, null));
        arr.addObject(DRMasterCriteria.withSubMasterCriteriaUserInfo(smcList, null));
        setMasterCriteriaList(arr);
        //[self resetGroups]
        return null;
    }


    public NSMutableArray newArraySans(NSArray arr1, Object member) {
        NSMutableArray arr = new NSMutableArray();
        Enumeration en = arr1.objectEnumerator();

        while (en.hasMoreElements()) {
            DRMasterCriteria att = (DRMasterCriteria)en.nextElement();

            if (!att.equals(member)) {
                arr.addObject(att);
            }

        }

        return arr;
    }


    public Object deleteMC() {
        NSMutableArray arr = newArraySans(masterCriteriaList(), masterCriteria());
        setMasterCriteriaList(arr);
        //[self resetGroups]
        return null;
    }


    public void replaceMCWith(DRMasterCriteria oldMC, NSArray smcList) {
        NSMutableArray arr = new NSMutableArray(masterCriteriaList());
        int indx = arr.indexOfObject(oldMC);
        arr.insertObjectAtIndex(DRMasterCriteria.withSubMasterCriteriaUserInfo(smcList, oldMC.userInfo()), indx);
        arr.removeObject(oldMC);
        setMasterCriteriaList(arr);
    }
}
