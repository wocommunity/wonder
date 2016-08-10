package er.grouping;

import java.util.Enumeration;

import com.webobjects.eocontrol.EOSortOrdering;
import com.webobjects.foundation.NSArray;
import com.webobjects.foundation.NSDictionary;
import com.webobjects.foundation.NSMutableArray;
import com.webobjects.foundation.NSMutableDictionary;

public class DRGroup {

    protected DRReportModel _reportModel;
    protected DRMasterCriteria _masterCriteria;
    protected NSArray _masterCriteriaDrillDownList;
    protected NSMutableDictionary _recordGroupDict;
    protected NSArray _ordering;
    protected NSArray _sortedArray;
    protected NSArray _sortedArrayBase;
    protected boolean _useGroupTotal;

    public void resetDefaults() {
    }

    // This is the only the top-level grouping algorithm
    // It returns a dict with two values
    // an array of DRRecordGroups and a list of all the DRCriteria found
    // keys: 'groups', 'criteriaList':
    // 
    public NSMutableDictionary childrenFromGroupCriteriaList() {
        DRCriteria crit;
        NSArray crits = _masterCriteria.criteriaLookupDict().allValues();
        Enumeration anEnum = crits.objectEnumerator();
        NSMutableDictionary recGrpDict = new NSMutableDictionary();

        while (anEnum.hasMoreElements()) {
            crit = (DRCriteria)anEnum.nextElement();
            DRRecordGroup recGrp = DRRecordGroup.withCriteriaGroupParent(crit, this, null);
            recGrpDict.setObjectForKey(recGrp, crit.keyDesc());
        }

        return recGrpDict;
    }

    public NSMutableDictionary groupBy(NSArray recs, DRMasterCriteria amc) {
        DRRecord rec;
        NSMutableDictionary recGrpDict;
        Enumeration anEnum = recs.objectEnumerator();

        if (amc.isPreset()) {
            recGrpDict = childrenFromGroupCriteriaList();
        } else {
            recGrpDict = new NSMutableDictionary();
        }

        while (anEnum.hasMoreElements()) {
            rec = (DRRecord)anEnum.nextElement();
            amc.groupRecordRecordGroupsDictGroupParent(rec, recGrpDict, this, null);
        }

        return recGrpDict;
    }

    static public NSArray drillDownListForMasterCriteriaList(DRMasterCriteria mc, NSArray mcList) {
        int i;
        NSMutableArray arr = new NSMutableArray();
        int mcCount = mcList.count();
        int startIndex = mcList.indexOfObject(mc);

        for (i = 0; i < mcCount; i++) {
            int atIndex = i+startIndex;
            if (atIndex == mcCount) {
                atIndex = 0;
                startIndex = -1;
            }

            DRMasterCriteria aMc = (DRMasterCriteria)mcList.objectAtIndex(atIndex);
            arr.addObject(aMc);
        }

        return arr;
    }

    static public DRGroup withReportModelMasterCriteria(DRReportModel aMod, DRMasterCriteria amc) {
        DRGroup grp = new DRGroup(aMod, amc);
        return grp;
    }

    public DRGroup(DRReportModel aMod, DRMasterCriteria amc) {
        super();
        _reportModel = aMod;
        _masterCriteria = amc;
        _useGroupTotal = _masterCriteria.shouldTotal();
        resetDefaults();
        _recordGroupDict = groupBy(_reportModel.records(), _masterCriteria);
        _masterCriteriaDrillDownList = DRGroup.drillDownListForMasterCriteriaList(_masterCriteria, _reportModel.criteriaList());
        _ordering = new NSArray(new EOSortOrdering("score", EOSortOrdering.CompareAscending));
    }

    public NSArray recordGroupList() {
        return _recordGroupDict.allValues();
    }

    public NSDictionary recordGroupDict() {
        return _recordGroupDict;
    }

    public DRReportModel reportModel() {
        return _reportModel;
    }

    public NSArray criteriaList() {
        //OWDebug.println(1, "_recordGroupDict.allKeys(): "+_recordGroupDict.allKeys());
        //return _recordGroupDict.allKeys();
        return _masterCriteria.criteriaLookupDict().allValues();
    }

    public DRMasterCriteria masterCriteria() {
        return _masterCriteria;
    }

    public NSArray masterCriteriaDrillDownList() {
        return _masterCriteriaDrillDownList;
    }

    /** Loops over each RecordGroup and group it. Init each new sub RecordGroup with empty record groups for each record group in the parent */
    
    public void groupSubRecordGroupsWithMasterCriteriaLookupDict(NSDictionary groupLookUpDict) {
        Enumeration anEnum = recordGroupList().objectEnumerator();

        while (anEnum.hasMoreElements()) {
            DRRecordGroup recGrp = (DRRecordGroup)anEnum.nextElement();
            NSMutableArray arr = new NSMutableArray(masterCriteriaDrillDownList());
            arr.removeObjectAtIndex(0);
            recGrp.groupSubRecordGroupGroupLookUpDict(arr, groupLookUpDict);
        }

    }

    public NSArray sortedCriteriaList() {
        if (_sortedArray == null) {
            _sortedArrayBase = EOSortOrdering.sortedArrayUsingKeyOrderArray(criteriaList(), _ordering);
            NSMutableArray sortedArray2 = new NSMutableArray(_sortedArrayBase);
            sortedArray2.addObject(DRCriteria.asTotalWithMasterCriteria(_masterCriteria));
            _sortedArray = new NSArray(sortedArray2);
        }

        if (_useGroupTotal) {
            return _sortedArray;
        }

        return _sortedArrayBase;
    }

    public NSArray sortedCriteriaListBase() {
        sortedCriteriaList();
        return _sortedArrayBase;
    }

    public boolean useGroupTotal() {
        return _useGroupTotal;
    }
    public void setUseGroupTotal(boolean v) {
        _useGroupTotal = v;
    }

    private String _keyDesc = null;
    public String keyDesc(){
        if(_keyDesc == null){
            _keyDesc = super.toString();
        }
        return _keyDesc;
    }
    @Override
    public String toString() {
        return "<DRGroup masterCriteria: " + masterCriteria() + "; >";
    }
}