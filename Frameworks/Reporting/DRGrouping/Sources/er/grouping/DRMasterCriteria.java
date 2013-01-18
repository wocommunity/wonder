package er.grouping;

import java.util.Enumeration;

import com.webobjects.foundation.NSArray;
import com.webobjects.foundation.NSDictionary;
import com.webobjects.foundation.NSMutableArray;
import com.webobjects.foundation.NSMutableDictionary;

import er.extensions.foundation.ERXValueUtilities;

public class DRMasterCriteria {

    //
    // userInfo used for convenience by higher code to asscociate
    // presentation specifics with a given criteria group.
    // Possible keys from experience using ReportingKit...
    //    showTitles, showHeading, showTotals,
    //    titleLabel, headingLabel, totalsLabel,
    //
    protected NSMutableDictionary _userInfo;
    protected NSArray _subCriteriaList;
    protected boolean _isPreset;
    protected boolean _useStringMatchForLookup;
    protected NSMutableDictionary _criteriaLookupDict;

    // For string lookup
    protected String _label;

    static public DRMasterCriteria withSubMasterCriteriaUserInfo(NSArray smcList, NSDictionary info) {
        DRMasterCriteria aVal = new DRMasterCriteria();
        aVal.initWithSubMasterCriteria(smcList, info);
        return aVal;
    }

    public boolean shouldTotal() {
        if (_userInfo == null) {
            return true;
        }
        if (ERXValueUtilities.booleanValue(_userInfo.objectForKey("SHOW_TOTAL"))) {
            return true;
        }
        return false;
    }

    public boolean shouldShowOther() {
        if (_userInfo == null) {
            return true;
        }
        if (ERXValueUtilities.booleanValue(_userInfo.objectForKey("SHOW_OTHER"))) {
            return true;
        }
        return false;
    }

    public boolean isString() {
        if (_userInfo == null) {
            return true;
        }
        if (ERXValueUtilities.booleanValue(_userInfo.objectForKey("IS_STRING"))) {
            return true;
        }
        return false;
    }

    public DRMasterCriteria initWithSubMasterCriteria(NSArray smcList, NSDictionary info) {
        _subCriteriaList = new NSArray(smcList);
        _userInfo.setObjectForKey("true", "SHOW_TOTAL");
        _userInfo.setObjectForKey("true", "SHOW_OTHER");
        _userInfo.setObjectForKey("false", "IS_STRING");
        _userInfo.setObjectForKey("Other", "OTHER_LABEL");
        _userInfo.setObjectForKey("Total", "TOTAL_LABEL");
        _userInfo.setObjectForKey("%m/%d/%Y", "calendarFormat");
        _userInfo.setObjectForKey(" to ", "rangeSeparator");
        _userInfo.setObjectForKey("|", "compoundSeparator");
        _userInfo.addEntriesFromDictionary(info);
        _useStringMatchForLookup = decideLookupMethod(_subCriteriaList);
        _isPreset = decideIsPreset(_subCriteriaList);

        if (isPreset()) {
            criteriaWithPossibleValues();
        }

        return this;
    }

    public DRMasterCriteria() {
        super();
        _label = null;
        _userInfo = new NSMutableDictionary();
        _criteriaLookupDict = new NSMutableDictionary();
    }

    public NSMutableDictionary userInfo() {
        return _userInfo;
    }

    public NSArray subCriteriaList() {
        return _subCriteriaList;
    }

    public boolean useStringMatchForLookup() {
        return _useStringMatchForLookup;
    }

    public boolean decideLookupMethod(NSArray scList) {
        Enumeration anEnum = scList.objectEnumerator();
        while (anEnum.hasMoreElements()) {
            DRSubMasterCriteria scSub = (DRSubMasterCriteria)anEnum.nextElement();
            if (scSub.mustSearchForLookup()) {
                return false;
            }
        }
        return true;
    }

    public boolean isPreset() {
        return _isPreset;
    }

    public boolean decideIsPreset(NSArray scList) {
        Enumeration anEnum = scList.objectEnumerator();
        while (anEnum.hasMoreElements()) {
            DRSubMasterCriteria scSub = (DRSubMasterCriteria)anEnum.nextElement();
            if (!scSub.isPreset()) {
                return false;
            }
        }
        return true;
    }

    public NSDictionary valueDictFromValuesRecord(NSDictionary vlDict, DRRecord rec) {
        NSMutableDictionary dict = new NSMutableDictionary();
        String ky = "|";
        Enumeration anEnum = _subCriteriaList.objectEnumerator();
        //OWDebug.println(1, "vlDict:"+vlDict);
        //OWDebug.println(1, "rec:"+rec);
        while (anEnum.hasMoreElements()) {
            DRSubMasterCriteria smc = (DRSubMasterCriteria)anEnum.nextElement();
            Object val = null;

            if (vlDict != null) {
                //OWDebug.println(1, "smc.keyDesc():"+smc.keyDesc());
                val = vlDict.objectForKey(smc.keyDesc());
            } else {
                val = smc.lookUpValueForRecord(rec);
            }
            //OWDebug.println(1, "val:"+val);

            if (val == null) {
                return null;
            }

            String pky = smc.lookUpKeyForValue(val);
            dict.setObjectForKey(val, smc.keyDesc());
            ky = ky + pky + "|";
        }

        dict.setObjectForKey(ky, "lookupKey");
        return dict;
    }

    public NSDictionary valueDictRecord(DRRecord rec) {
        return valueDictFromValuesRecord(null, rec);
    }

    public NSDictionary valueDictFromValues(NSDictionary vlDict) {
        return valueDictFromValuesRecord(vlDict, null);
    }

    public void criteriaWithPossibleValueList(NSDictionary vlDict) {
        NSDictionary valueDict = valueDictFromValues(vlDict);
        String lookupKey = (String)valueDict.objectForKey("lookupKey");
        DRCriteria crit = DRCriteria.withMasterCriteriaValueDict(this, valueDict);
        _criteriaLookupDict.setObjectForKey(crit, lookupKey);
    }

    public NSDictionary criteriaLookupDict() {
        return _criteriaLookupDict;
    }

    public void walkPresetsPossibleValues(NSArray presets, NSDictionary vlDict) {
        DRSubMasterCriteria scSub = (DRSubMasterCriteria)presets.objectAtIndex(0);
        NSMutableArray newPresets = new NSMutableArray(presets);
        Enumeration anEnum = scSub.possibleValues().objectEnumerator();
        newPresets.removeObjectAtIndex(0);
        int count = newPresets.count();

        while (anEnum.hasMoreElements()) {
            Object possibleValue = anEnum.nextElement();
            NSMutableDictionary moreVlDict = new NSMutableDictionary(vlDict);
            moreVlDict.setObjectForKey(possibleValue, scSub.keyDesc());

            if (count == 0) {
                //////////NSLog(@"DRMasterCriteria: walkPresets:possibleValues: count == 0");
                criteriaWithPossibleValueList(moreVlDict);
            } else {
                //////////NSLog(@"DRMasterCriteria: walkPresets:possibleValues: count _NOT== 0");
                walkPresetsPossibleValues(newPresets, moreVlDict);
            }

        }

    }

    private void criteriaWithPossibleValues() {
        walkPresetsPossibleValues(_subCriteriaList, new NSDictionary());

        if (isPreset()) {
            DRSubMasterCriteria smc = (DRSubMasterCriteria)_subCriteriaList.objectAtIndex(0);

            if (!smc.groupEdges()) {
                NSArray arr = _criteriaLookupDict.allValues();

                if (arr.count() > 0) {
                    DRCriteria c = (DRCriteria)arr.objectAtIndex(0);
                    Object val = c.valueDict().objectForKey(smc.keyDesc());

                    if (val instanceof String) {
                        _userInfo.setObjectForKey("true", "IS_STRING");
                    }

                }
            }
            
            if(shouldShowOther()){
                String lk = "|Other|";
                DRCriteria crit = DRCriteria.asOtherWithMasterCriteria(this);
                _criteriaLookupDict.setObjectForKey(crit, lk);
            }

            //OWDebug.println(1, "criteriaWithPossibleValues: criteriaLookupDict: "+ _criteriaLookupDict);
        }

    }

    public void groupRecordRecordGroupsDictGroupParent(DRRecord rec, NSMutableDictionary recGrpDict, DRGroup grp, DRRecordGroup parent) {
        DRRecordGroup recGrp = findRecordGroupForRecordGroupsDictGroupParent(rec, recGrpDict, grp, parent);

        if (recGrp != null) {
            recGrp.recordList().addObject(rec);
        }

    }

    public DRRecordGroup findRecordGroupForRecordGroupsDictGroupParent(DRRecord rec, NSMutableDictionary recGrpDict, DRGroup grp, DRRecordGroup parent) {
        //OWDebug.println(1, "entered");
        DRRecordGroup recGrp = null;
        NSDictionary valueDict = valueDictRecord(rec);

        //OWDebug.println(1, "valueDict:"+valueDict);
        //OWDebug.println(1, "this.isPreset():"+this.isPreset());
        if (valueDict==null && !isPreset()) {
            return null;
        }
        //if(valueDict==null){
        //    return null;
        //}

        DRCriteria crit = null;
        String lookupKey = null;
        if(valueDict != null){
            lookupKey = (String)valueDict.objectForKey("lookupKey");
            //OWDebug.println(1, "lookupKey:"+lookupKey);
            crit = (DRCriteria)_criteriaLookupDict.objectForKey(lookupKey);
        }

        //OWDebug.println(1, "crit:"+crit);
        if (crit==null && isPreset()) {
            //OWDebug.println(1, "is preset AND no crit found");
            crit = (DRCriteria)_criteriaLookupDict.objectForKey("|Other|");
            //OWDebug.println(1, "should be OTHER crit:"+crit);

            if (crit==null) {
                return null;
            } else {
                recGrp = (DRRecordGroup)recGrpDict.objectForKey(crit.keyDesc());
                if(recGrp == null) {
                    recGrp = DRRecordGroup.withCriteriaGroupParent(crit, grp, parent);
                    recGrpDict.setObjectForKey(recGrp, crit.keyDesc());
                    //_criteriaLookupDict.setObjectForKey(crit, lookupKey);
                }
            }

        } else if (crit==null) {
            crit = DRCriteria.withMasterCriteriaValueDict(this, valueDict);
            recGrp = DRRecordGroup.withCriteriaGroupParent(crit, grp, parent);
            recGrpDict.setObjectForKey(recGrp, crit.keyDesc());
            _criteriaLookupDict.setObjectForKey(crit, lookupKey);
            
        } else {
        
            recGrp = (DRRecordGroup)recGrpDict.objectForKey(crit.keyDesc());
        }
        //OWDebug.println(1, "recGrpDict.allKeys():"+recGrpDict.allKeys());

        //OWDebug.println(1, " - DONE -");
        return recGrp;
    }

    public String label() {
        if (_label==null) {
            _label = (String)_userInfo.objectForKey("label");

            if (_label==null) {
                int i = 0;
                int count = _subCriteriaList.count();
                Enumeration en = _subCriteriaList.objectEnumerator();
                String lbl = "";

                while (en.hasMoreElements()) {
                    DRSubMasterCriteria smc = (DRSubMasterCriteria)en.nextElement();
                    lbl = lbl.concat(smc.label());
                    i++;

                    if (!(i == count)) {
                        lbl = lbl.concat("|");
                    }

                }
                _label = lbl;

            }

        }

        return _label;
    }

    private String _keyDesc = null;
    public String keyDesc() {
        if(_keyDesc == null) {
            _keyDesc = super.toString();
        }
        return _keyDesc;
    }

    @Override
    public String toString() {
        return "<DRMasterCriteria label: \"" + label() + "\"; shouldTotal: " + shouldTotal() + "; subCriterias: " + subCriteriaList() + " >";
    }
}