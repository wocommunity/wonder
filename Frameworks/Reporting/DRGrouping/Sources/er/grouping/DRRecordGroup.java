package er.grouping;

import java.util.Enumeration;

import com.webobjects.eocontrol.EOSortOrdering;
import com.webobjects.foundation.NSArray;
import com.webobjects.foundation.NSDictionary;
import com.webobjects.foundation.NSMutableArray;
import com.webobjects.foundation.NSMutableDictionary;

/* DRRecordGroup.h created by Administrator on Sun 01-Nov-1998 */
//#import <WebObjects/WebObjects.h>
public class DRRecordGroup {
    protected DRCriteria _criteria;
    protected DRGroup _group;
    protected NSMutableDictionary _totals;
    protected NSMutableDictionary _totalsByKey;
    protected NSMutableArray _recordList;
    protected NSMutableDictionary _recordGroupDict;
    protected DRRecordGroup _parent;
    protected boolean _staleTotal;
    protected boolean _pregroupedListFound;
    
    // not used
    protected NSMutableDictionary _lookUpCoordinates;
    
    protected NSArray _totalList;
    protected NSArray _sortedRecordList;
    protected NSArray _rawRecordList;

    static public DRRecordGroup withCriteriaGroupParent(DRCriteria c, DRGroup grp, DRRecordGroup recGrp) {
        DRRecordGroup aVal = new DRRecordGroup();
        aVal.initWithCriteria(c, grp, recGrp);
        return aVal;
    }

    private void coordsFromRecGroupDictionary(DRRecordGroup prnt, NSMutableDictionary dict) {
        DRRecordGroup nextParent = prnt.parent();
        DRCriteria crit = prnt.criteria();
        dict.setObjectForKey(crit, crit.masterCriteria().keyDesc());
        if (nextParent != null) {
            coordsFromRecGroupDictionary(nextParent, dict);
        }
    }

    private NSMutableDictionary buildLookUpCoordinates() {
        NSMutableDictionary dict = new NSMutableDictionary();
        if (_criteria == null) {
            return dict;
        }
        if (_parent != null) {
            coordsFromRecGroupDictionary(_parent, dict);
        }
        dict.setObjectForKey(_criteria, _criteria.masterCriteria().keyDesc());
        return dict;
    }

    public DRRecordGroup initWithCriteria(DRCriteria c, DRGroup grp, DRRecordGroup recGrp) {
        _criteria = c;
        _group = grp;
        _parent = recGrp;
        _lookUpCoordinates = buildLookUpCoordinates();
        DRRecordGroup preexistrg = null;
        if(_group != null){
            preexistrg = _group.reportModel().recordGroupForCoordinates(_lookUpCoordinates);
    
            if (preexistrg != null) {
                _recordList = preexistrg.recordList();
                _pregroupedListFound = true;
            } else {
                _group.reportModel().registerRecordGroupWithCoordinates(this, _lookUpCoordinates);
            }
        }
        return this;
    }

    public DRRecordGroup() {
        super();
        _totals = new NSMutableDictionary();
        _recordList = new NSMutableArray();
        _recordGroupDict = new NSMutableDictionary();
        _totalsByKey = new NSMutableDictionary();
        _staleTotal = true;
        _pregroupedListFound = false;
        _sortedRecordList = null;
    }

    public NSMutableDictionary recordGroupDict() {
        return _recordGroupDict;
    }

    public DRCriteria criteria() {
        return _criteria;
    }

    public DRGroup group() {
        return _group;
    }

    public DRValue totalForKey(String totalKey) {
        DRValue value = (DRValue)_totalsByKey.objectForKey(totalKey);
        return value;
    }

    public NSDictionary totals() {
        // Loop over all DRRecords and ask each 'total-able' key for its value
        // and sum up into a dictionary of totals. keys in dict are keys into records
        // values are NSNumbers. Once computed, cache.
 
        //AK: there are two types of totals, the one is the simple sum we had earlier,
        // it gets stored in the _totals under (index number of the current record*index number of 
        if (_staleTotal) {
            for(Enumeration en = recordList().objectEnumerator(); en.hasMoreElements(); ) {
                int i = 0;
                DRRecord rec = (DRRecord)en.nextElement();
                NSArray flatlist = rec.flatValueList();
                for(Enumeration en2 = flatlist.objectEnumerator(); en2.hasMoreElements(); ) {
                    DRValue val = (DRValue)en2.nextElement();
                    boolean isComputed = val.attribute().isComputed();
                    double subTot, lastTot, newTot;
                    Number indexNum = Integer.valueOf(i);
                    DRValue totalValue = (DRValue)_totals.objectForKey(indexNum);
                    if (totalValue == null) {
                        if (val.shouldTotal()) {
                            if(!isComputed) {
                                totalValue = DRValue.withTotalAttribute(0, val.attribute());
                            } else {
                                final NSArray rawRecords = rawRecordList();
                                totalValue = new DRValue(0, val.attribute()) {
                                    private Double total;
                                    @Override
                                    public double total() {
                                        if(total == null) { 
                                            total = Double.valueOf(attribute().computeFromRawRecords(rawRecords));
                                        }
                                        return total.doubleValue();
                                    }
                                };
                            }
                            _totalsByKey.setObjectForKey(totalValue, totalValue.key());
                        } else {
                            totalValue = DRValue.nullTotal();
                        }
                        _totals.setObjectForKey(totalValue, indexNum);
                    }
                    if(!isComputed) {
                        lastTot = totalValue.total();
                        subTot = val.total();
                        newTot = lastTot+subTot;
                        totalValue.setTotal(newTot);
                    }
                    i++;
                }
            }
             _staleTotal = false;
         }

        return _totals;
    }

    public NSArray totalList() {
        if (_totalList == null) {
            int cnt = totals().allKeys().count();
            int i;
            NSMutableArray totList = new NSMutableArray();

            for (i = 0; i < cnt; i++) {
                totList.addObject(totals().objectForKey(Integer.valueOf(i)));
            }
            _totalList = new NSArray(totList);
        }

        return _totalList;
    }

    public NSArray sortedRecordList() {
        if (_sortedRecordList == null) {
            NSArray ords = null;
            if(_group != null){
                ords = _group.reportModel().orderings();
            }
            _sortedRecordList = EOSortOrdering.sortedArrayUsingKeyOrderArray(_recordList, ords);
        }

        return _sortedRecordList;
    }

    public NSArray rawRecordList() {
        if (_rawRecordList == null) {
            NSMutableArray rawRecs = new NSMutableArray();
            NSArray recs = sortedRecordList();
            Enumeration en = recs.objectEnumerator();

            while (en.hasMoreElements()) {
                DRRecord rec = (DRRecord)en.nextElement();
                Object rawRec = rec.rawRecord();
                rawRecs.addObject(rawRec);
            }

            _rawRecordList = new NSArray(rawRecs);
        }

        return _rawRecordList;
    }

    public NSMutableArray recordList() {
        // might sort this based on settings in DRAttributes
        return _recordList;
    }

    public boolean pregroupedListFound() {
        return _pregroupedListFound;
    }

    public NSDictionary lookUpCoordinates() {
        return _lookUpCoordinates;
    }

    public NSArray children() {
        return _recordGroupDict.allValues();
    }

    public DRRecordGroup parent() {
        return _parent;
    }

    public boolean childrenFromGroupCriteriaList(DRGroup grp) {
        //was sorted
        boolean listFound = false;
        NSArray crits = grp.criteriaList();
        Enumeration anEnum = crits.objectEnumerator();

        while (anEnum.hasMoreElements()) {
            DRCriteria crit = (DRCriteria)anEnum.nextElement();
            DRRecordGroup recGrp = DRRecordGroup.withCriteriaGroupParent(crit, grp, this);
            listFound = recGrp.pregroupedListFound();
            _recordGroupDict.setObjectForKey(recGrp, crit.keyDesc());
        }

        return listFound;
    }

    public void groupSubRecordGroupGroupLookUpDict(NSArray groupList, NSDictionary groupLookUpDict) {
        int cnt = groupList.count();

        if (cnt > 0) {
            DRMasterCriteria mc = (DRMasterCriteria)groupList.objectAtIndex(0);
            DRGroup grp = (DRGroup)groupLookUpDict.objectForKey(mc.keyDesc());

            if (!childrenFromGroupCriteriaList(grp)) {
                groupByInto(recordList(), grp.masterCriteria(), recordGroupDict());
            }

            // loop over each RecordGroup and send groupSubRecordGroup:(NSArray *)groupList
            // but only count is > 1
            Enumeration anEnum = children().objectEnumerator();

            while (anEnum.hasMoreElements()) {
                DRRecordGroup rg = (DRRecordGroup)anEnum.nextElement();
                NSMutableArray arr = new NSMutableArray(groupList);
                arr.removeObjectAtIndex(0);
                rg.groupSubRecordGroupGroupLookUpDict(arr, groupLookUpDict);
            }

        }

    }

    public void groupByInto(NSMutableArray recs, DRMasterCriteria amc, NSMutableDictionary recGrpDict) {
        Enumeration anEnum = recs.objectEnumerator();
        while (anEnum.hasMoreElements()) {
            DRRecord rec = (DRRecord)anEnum.nextElement();
            amc.groupRecordRecordGroupsDictGroupParent(rec, recGrpDict, group(), this);
        }

    }

    @Override
    public String toString() {
        return ""+(super.toString())+"-lc:"+(_lookUpCoordinates)+"-"+(recordList().count())+"-"+(_recordGroupDict.toString());
    }

    public boolean staleTotal() {
        return _staleTotal;
    }

    public void makeStale() {
        _staleTotal = true;
        _totals.removeAllObjects();
        _totalList = null;
    }

}
