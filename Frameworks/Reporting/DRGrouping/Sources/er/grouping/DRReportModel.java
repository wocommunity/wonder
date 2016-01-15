package er.grouping;

import java.io.File;
import java.io.StringReader;
import java.util.Enumeration;

import org.apache.commons.codec.binary.Base64;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.xml.sax.InputSource;

import com.webobjects.appserver.xml.WOXMLCoder;
import com.webobjects.appserver.xml.WOXMLDecoder;
import com.webobjects.eocontrol.EOSortOrdering;
import com.webobjects.foundation.NSArray;
import com.webobjects.foundation.NSDictionary;
import com.webobjects.foundation.NSKeyValueCodingAdditions;
import com.webobjects.foundation.NSMutableArray;
import com.webobjects.foundation.NSMutableDictionary;
import com.webobjects.foundation.NSNotification;
import com.webobjects.foundation.NSNotificationCenter;
import com.webobjects.foundation.NSPropertyListSerialization;
import com.webobjects.foundation.NSSelector;
import com.webobjects.foundation._NSStringUtilities;

import er.extensions.eof.ERXConstant;
import er.extensions.foundation.ERXStringUtilities;
import er.extensions.foundation.ERXValueUtilities;

public class DRReportModel {
    private static final Logger log = LoggerFactory.getLogger(DRReportModel.class);

    public static final String DRReportModelUpdateNotification = "DRReportModelUpdate";
    public static final String DRReportModelRebuildNotification = "DRReportModelRebuild";

    public static final String GROUP_DEF_KEY = "GroupDef";
    public static final String ATTRIBUTE_DEF_KEY = "AttributeDef";
    
    protected NSMutableArray _vList;
    protected NSMutableArray _hList;
    protected NSMutableArray _zList;
    protected NSArray _records;
    protected NSArray _groups;
    protected NSDictionary _groupDict;
    protected NSArray _rawRecords;
    protected NSArray _attributeList;
    protected NSArray _criteriaList;

    //a list of all DRMasterCriterias
    protected NSMutableDictionary _registeredRecordGroups;

    protected NSMutableDictionary _flatAttributeDepthDict;
    protected NSMutableArray _flatAttributeList;
    protected NSMutableArray _flatAttributeListTotal;
    protected NSMutableDictionary _flatAttributeListTotalDict;
    protected int _attributeListDepth;
    protected NSArray _orderings;

    public void resetDefaults() {
    }

    public NSArray orderings() {
        return _orderings;
    }
    public void setOrderings(NSArray v) {
        _orderings = v;
    }

    public void buildOrderings() {
        NSMutableArray arr = new NSMutableArray();
        if(this.attributeList() != null){
        
            Enumeration en = this.attributeList().objectEnumerator();
            while (en.hasMoreElements()) {
                DRAttribute att = (DRAttribute)en.nextElement();
    
                //OWDebug.println(1, "att:"+ att);
                if (!att.isGroup()) {
                    //OWDebug.println(1, "att: is not Group");
                    if (att.shouldSort()) {
                        //OWDebug.println(1, "att: shouldSort");
                        String attName = att.keyPath();
                        EOSortOrdering ord = new EOSortOrdering(attName, EOSortOrdering.CompareAscending);
                        arr.addObject(ord);
                    }
    
                }
    
            }
            
        }

        setOrderings(arr);
        //OWDebug.println(1, "orderings:"+arr);
    }

    static public NSArray masterCriteriaForKey(String akey) {
        DRMasterCriteria mc;
        NSMutableArray mcrits = new NSMutableArray();
        NSMutableArray smcs = new NSMutableArray();
        DRSubMasterCriteria smc = DRSubMasterCriteria.withKeyUseMethodUseTimeFormatFormatPossibleValuesUseTypeGroupEdgesPossibleValues(akey, false, false, null, null, false, null);
        smcs.addObject(smc);
        mc = DRMasterCriteria.withSubMasterCriteriaUserInfo(smcs, null);
        mcrits.addObject(mc);
        return mcrits;
    }

    static public boolean writeStringToDiskPathAtomically(String string, String path, boolean flag) {
        _NSStringUtilities.writeToFile(new File(path), string);
        return true;
    }

    static public String stringContentsOfFile(String path) {
        return ERXStringUtilities.stringWithContentsOfFile(path);
    }
    

    //
    // Each defrecord in array has a 
    // masterCriteriaList:  GROUP_DEF_KEY 
    // attributeList: ATTRIBUTE_DEF_KEY
    //
    static public boolean boolForString(String s) {
        return ERXValueUtilities.booleanValue(s);
    }

    static public String stringForBool(boolean b) {
        if (b) {
            return "true";
        }
        return "false";
    }

    static public NSArray possibleValues(NSDictionary smcdict) {
        //NSArray rawpossVals = (NSArray)smcdict.objectForKey("possibleValues");
        Object rawpossVals = smcdict.objectForKey("possibleValues");
        //String non = (String)smcdict.objectForKey("nonNumberOrDate");

        if (rawpossVals == null) {
            return null;
        }
        
        NSArray possVals = null;
        
        if (rawpossVals instanceof String) {
            WOXMLDecoder decoder = WOXMLDecoder.decoder();
            String xmlString = new String(Base64.decodeBase64((String) rawpossVals));
            log.info("xmlString: {}", xmlString);
            StringReader stringReader = new StringReader(xmlString);
            InputSource is = new InputSource(stringReader);
            // invoke setEncoding (on the input source) if the XML contains multibyte characters
            try {
                possVals = (NSArray)decoder.decodeRootObject(is);
            } catch(Exception e) {
                //OWDebug.println(1, "e:"+e);
            }
            //possVals = NSArchiver .unarchiveObjectWithData(rawpossVals);
        } else if(rawpossVals instanceof NSArray){
            possVals = (NSArray)rawpossVals;
        }
        return possVals;
    }

    static public NSArray subMasterCriteriaList(NSArray smcList) {
        NSMutableArray arr = new NSMutableArray();
        Enumeration en = smcList.objectEnumerator();

        while (en.hasMoreElements()) {
            NSDictionary smcdict = (NSDictionary)en.nextElement();
            NSArray possVals = DRReportModel.possibleValues(smcdict);
            DRSubMasterCriteria smc = new DRSubMasterCriteria(smcdict, possVals);
            arr.addObject(smc);
        }
        return arr;
    }

    static public NSArray masterCriteriaList(NSArray mcList) {
        NSMutableArray arr = new NSMutableArray();
        Enumeration en = mcList.objectEnumerator();

        while (en.hasMoreElements()) {
            NSDictionary mcdict = (NSDictionary)en.nextElement();
            NSArray smcList = DRReportModel.subMasterCriteriaList((NSArray)mcdict.objectForKey("subCriteriaList"));
            DRMasterCriteria mc = DRMasterCriteria.withSubMasterCriteriaUserInfo(smcList, (NSDictionary)mcdict.objectForKey("userInfo"));
            arr.addObject(mc);
        }
        return arr;
    }

    static public NSArray attributeList(NSArray attList) {
        NSMutableArray arr = new NSMutableArray();
        Enumeration en = attList.objectEnumerator();
        //OWDebug.println(1, "entered: attList:"+attList);
        while (en.hasMoreElements()) {
            NSDictionary attDict = (NSDictionary)en.nextElement();
            if(!"YES".equals(attDict.objectForKey("disabled"))) {
                DRAttribute att;
                NSArray subAttList = (NSArray)attDict.objectForKey("attributes");

                if (subAttList != null) {
                    NSArray subAttListObjects = DRReportModel.attributeList(subAttList);
                    att = DRAttributeGroup.withKeyPathFormatLabelTotalListUserInfo((String)attDict.objectForKey("keyPath"), (String)attDict.objectForKey("format"), (String)attDict.objectForKey("label"), ERXValueUtilities.booleanValue(attDict.objectForKey("total")), subAttListObjects, (NSDictionary)attDict.objectForKey("userInfo"));
                } else {
                    att = DRAttribute.withKeyPathFormatLabelTotalUserInfo((String)attDict.objectForKey("keyPath"), (String)attDict.objectForKey("format"), (String)attDict.objectForKey("label"), ERXValueUtilities.booleanValue(attDict.objectForKey("total")), (NSDictionary)attDict.objectForKey("userInfo"));
                }

                arr.addObject(att);
            }
        }
        //OWDebug.println(1, "entered: arr:"+arr);

        return arr;
    }

    static public NSDictionary modelDictWithPListString(String plistString) {
        NSMutableDictionary dict = new NSMutableDictionary();
        
        //WOXMLDecoder decoder = WOXMLDecoder.decoder();
        //StringReader stringReader = new StringReader(plistString);
        //InputSource is = new InputSource(stringReader);
        // invoke setEncoding (on the input source) if the XML contains multibyte characters
        //NSDictionary rawdict = (NSDictionary)decoder.decodeRootObject(is);
        
        NSDictionary rawdict = (NSDictionary)NSPropertyListSerialization.propertyListFromString(plistString);

        NSArray mcList = DRReportModel.masterCriteriaList((NSArray)rawdict.objectForKey(GROUP_DEF_KEY));
        NSArray attList = DRReportModel.attributeList((NSArray)rawdict.objectForKey(ATTRIBUTE_DEF_KEY));
        dict.setObjectForKey(mcList, GROUP_DEF_KEY);
        dict.setObjectForKey(attList, ATTRIBUTE_DEF_KEY);
        return dict;
    }

    static public NSDictionary modelFromPlistString(String plistString) {
        return DRReportModel.modelDictWithPListString(plistString);
    }

    static public NSArray masterSubCriteriaListString(NSArray smcList) {
        NSMutableArray arr = new NSMutableArray();
        Enumeration en = smcList.objectEnumerator();

        while (en.hasMoreElements()) {
            DRSubMasterCriteria smc = (DRSubMasterCriteria)en.nextElement();
            NSMutableDictionary smcDict = new NSMutableDictionary();
            smcDict.setObjectForKey(smc.key(), "key");
            smcDict.setObjectForKey(DRReportModel.stringForBool(smc.useMethod()), "useMethod");
            smcDict.setObjectForKey(DRReportModel.stringForBool(smc.useTimeFormat()), "useTimeFormat");

            if (smc.format() != null) {
                smcDict.setObjectForKey(smc.format(), "format");
            }

            if (smc.possibleValuesUseType() != null) {
                smcDict.setObjectForKey(smc.possibleValuesUseType(), "possibleValuesUseType");
            }

            smcDict.setObjectForKey(DRReportModel.stringForBool(smc.groupEdges()), "groupEdges");

            if (smc.rawPossibleValues() != null) {
                if (smc.nonNumberOrDate()) {
                    smcDict.setObjectForKey("true", "nonNumberOrDate");
                    String passValsXMLString = WOXMLCoder.coder().encodeRootObjectForKey(smc.rawPossibleValues(), "XML");
                    String base64XML = Base64.encodeBase64String(passValsXMLString.getBytes());
                    smcDict.setObjectForKey(base64XML, "possibleValues");
                }else{
                    smcDict.setObjectForKey(smc.rawPossibleValues(), "possibleValues");
                }
            }

            arr.addObject(smcDict);
        }

        return arr;
    }

    static public NSArray masterCriteriaListString(NSArray mcList) {
        NSMutableArray arr = new NSMutableArray();
        Enumeration en = mcList.objectEnumerator();

        while (en.hasMoreElements()) {
            DRMasterCriteria mc = (DRMasterCriteria)en.nextElement();
            NSMutableDictionary mcDict = new NSMutableDictionary();

            if (mc.userInfo() != null) {
                mcDict.setObjectForKey(mc.userInfo(), "userInfo");
            }

            mcDict.setObjectForKey(DRReportModel.masterSubCriteriaListString(mc.subCriteriaList()), "subCriteriaList");
            arr.addObject(mcDict);
        }

        return arr;
    }

    static public NSDictionary stringForAttribute(DRAttribute att) {
        NSMutableDictionary dict = new NSMutableDictionary();

        if (att.keyPath() != null) {
            dict.setObjectForKey(att.keyPath(), "keyPath");
        }

        if (att.format() != null) {
            dict.setObjectForKey(att.format(), "format");
        }

        if (att.label() != null) {
            dict.setObjectForKey(att.label(), "label");
        }

        if (att.shouldTotal()) {
            dict.setObjectForKey(DRReportModel.stringForBool(att.shouldTotal()), "total");
        }

        if (att.userInfo() != null) {
            dict.setObjectForKey(att.userInfo(), "userInfo");
        }

        if (att.isGroup()) {
            dict.setObjectForKey(DRReportModel.attributeListString(att.attributes()), "attributes");
        }

        return dict;
    }

    static public NSArray attributeListString(NSArray attList) {
        NSMutableArray arr = new NSMutableArray();
        Enumeration en = attList.objectEnumerator();

        while (en.hasMoreElements()) {
            DRAttribute att = (DRAttribute)en.nextElement();
            NSDictionary attString = DRReportModel.stringForAttribute(att);
            arr.addObject(attString);
        }

        return arr;
    }

    static public String pListStringAttributeListMasterCriteriaList(NSArray attList, NSArray mcList) {
        NSMutableDictionary dict = new NSMutableDictionary();
        NSArray mcListString = DRReportModel.masterCriteriaListString(mcList);
        NSArray attListString = DRReportModel.attributeListString(attList);
        dict.setObjectForKey(mcListString, GROUP_DEF_KEY);
        dict.setObjectForKey(attListString, ATTRIBUTE_DEF_KEY);
        return dict.toString();
    }

    static public DRReportModel withRawRecordsCriteriaListAttributeList(NSArray rawr, NSArray aCritArray, NSArray aAttribList) {
        DRReportModel rmod = new DRReportModel();
        rmod.initWithRawRecords(rawr, aCritArray, aAttribList);
        return rmod;
    }

    public void buildGrandTotal() {
        DRRecordGroup rg = DRRecordGroup.withCriteriaGroupParent(null, null, null);
        registerRecordGroupWithCoordinates(rg, new NSDictionary());
        rg.recordList().addObjectsFromArray(_records);
        //OWDebug.println(1, "buildGrandTotal: rg:"+ rg);
    }

    public DRReportModel initWithRawRecords(NSArray rawr, NSArray aCritArray, NSArray aAttribList) {
        NSDictionary dict;
        _attributeListDepth = 0;
        _rawRecords = rawr;
        _attributeList = aAttribList;
        _criteriaList = aCritArray;
        _registeredRecordGroups = new NSMutableDictionary();
        _records = recordsForRawRecords(_rawRecords);
        _vList = new NSMutableArray();
        _hList = new NSMutableArray();
        _zList = new NSMutableArray();
        _flatAttributeList = new NSMutableArray();
        _flatAttributeListTotal = new NSMutableArray();
        _flatAttributeDepthDict = new NSMutableDictionary();
        _flatAttributeListTotalDict = new NSMutableDictionary();
        dict = groupsWithCriteriaArray(aCritArray);
        _groups = (NSArray)dict.objectForKey("groups");
        _groupDict = (NSDictionary)dict.objectForKey("lookup");
        _vList.addObjectsFromArray(_groups);
        groupAllRecordGroups();
        log.debug("ABOUT TO GET flatListForAttributeList");
        flatListForAttributeList();
        log.debug("ABOUT TO GET flatListForAttributeListTotals");
        flatListForAttributeListTotals();
        log.debug("flatAttributeList: {}", _flatAttributeList);
        log.debug("flatAttributeListTotal: {}", _flatAttributeListTotal);
        buildGrandTotal();
        buildOrderings();
        
        NSSelector synchModelSelector = new NSSelector("synchModel", ERXConstant.NotificationClassArray);
        
        NSNotificationCenter.defaultCenter().addObserver(this, synchModelSelector, DRReportModel.DRReportModelUpdateNotification, this);
        NSNotificationCenter.defaultCenter().addObserver(this, synchModelSelector, DRReportModel.DRReportModelRebuildNotification, this);
        return this;
    }

    public void synchModel() {
        log.info("synchModel()");
        computeRecordValuesForRecords(records());
        makeRecordGroupsStaleTotal();
        flatListForAttributeList();
        flatListForAttributeListTotals();
        buildGrandTotal();
        buildOrderings();
    }

    public void synchModel(NSNotification notification) {
        synchModel();
    }

    public void groupAllRecordGroups() {
        Enumeration anEnum = _groups.objectEnumerator();

        while (anEnum.hasMoreElements()) {
            DRGroup grp = (DRGroup)anEnum.nextElement();
            grp.groupSubRecordGroupsWithMasterCriteriaLookupDict(_groupDict);
        }

    }

    public NSArray recordsForRawRecords(NSArray rawr) {
        NSMutableArray recs = new NSMutableArray();
        Enumeration anEnum = rawr.objectEnumerator();

        while (anEnum.hasMoreElements()) {
            NSKeyValueCodingAdditions rawrec = (NSKeyValueCodingAdditions)anEnum.nextElement();
            recs.addObject(DRRecord.withRawRecordModel(rawrec, this));
        }

        return new NSArray(recs);
    }

    //
    // Used to recache derived values in Record objects
    //
    public int attributeListDepth() {
        return _attributeListDepth;
    }

    public NSArray flatAttributeList() {
        return _flatAttributeList;
    }

    public void flatListForAttribute(DRAttribute att) {
        //OWDebug.println(1, "att:"+att);
        if (!att.isGroup()) {
            //OWDebug.println(1, "att is NOT group");
            _flatAttributeList.addObject(att);
        } else {
            //OWDebug.println(1, "att IS group");
            NSArray subvls = att.flatAttributesWithDepthDictionary(_attributeListDepth, _flatAttributeDepthDict);
            _flatAttributeList.addObjectsFromArray(subvls);

            if (att.shouldTotal()) {
                _flatAttributeList.addObject(att);
            }

        }

        Number dpthKey = Integer.valueOf(_attributeListDepth);
        //OWDebug.println(1, "dpthKey:"+dpthKey);
        NSMutableArray lst = (NSMutableArray)_flatAttributeDepthDict.objectForKey(dpthKey);

        if (lst == null) {
            lst = new NSMutableArray();
            _flatAttributeDepthDict.setObjectForKey(lst, dpthKey);
        }

        lst.addObject(att);
        //OWDebug.println(1, "lst:"+lst);
    }

    public void flatListForAttributeList() {
        //OWDebug.println(1, "entered");
        _flatAttributeList.removeAllObjects();
        _flatAttributeDepthDict.removeAllObjects();
        _attributeListDepth = 0;
        //OWDebug.println(1, "_attributeList: "+ _attributeList);

        if(_attributeList != null){
            Enumeration anEnum = _attributeList.objectEnumerator();
            while (anEnum.hasMoreElements()) {
                DRAttribute att = (DRAttribute)anEnum.nextElement();
                //OWDebug.println(1, "att:"+att);
                flatListForAttribute(att);
            }
        }

        //OWDebug.println(1, "_flatAttributeDepthDict: "+ _flatAttributeDepthDict);
    }

    private void getFlatAttributeDepthDictTotals() {
        int i;
        //OWDebug.println(1, "_flatAttributeDepthDict.allKeys(): "+ _flatAttributeDepthDict.allKeys());
        NSArray depthKeys = _flatAttributeDepthDict.allKeys();
        int cnt = depthKeys.count();

        for (i = 0; i < cnt; i++) {
            Number ky = Integer.valueOf(i);
            //OWDebug.println(1, "ky:"+ ky);
            NSArray attsForDepth = (NSArray)_flatAttributeDepthDict.objectForKey(ky);
            Enumeration anEnum = attsForDepth.objectEnumerator();

            while (anEnum.hasMoreElements()) {
                DRAttribute att = (DRAttribute)anEnum.nextElement();

                //OWDebug.println(1, "att: "+ att);
                if (att.shouldTotal()) {
                    //OWDebug.println(1, "SHOULD TOTAL. att: "+ att);
                    NSMutableArray lst = (NSMutableArray)_flatAttributeListTotalDict.objectForKey(ky);

                    if (lst == null) {
                        lst = new NSMutableArray();
                        _flatAttributeListTotalDict.setObjectForKey(lst, ky);
                    }

                    lst.addObject(att);
                }

            }

        }

    }

    public void flatListForAttributeListTotals() {
        _flatAttributeListTotal.removeAllObjects();
        _flatAttributeListTotalDict.removeAllObjects();

        Enumeration anEnum = _flatAttributeList.objectEnumerator();
        while (anEnum.hasMoreElements()) {
            DRAttribute att = (DRAttribute)anEnum.nextElement();

            if (att.shouldTotal()) {
                _flatAttributeListTotal.addObject(att);
            }

        }

        getFlatAttributeDepthDictTotals();
        //OWDebug.println(1, "_flatAttributeListTotalDict:"+ _flatAttributeListTotalDict);
    }

    public NSDictionary flatAttributeDepthDict() {
        return _flatAttributeDepthDict;
    }

    public NSDictionary flatAttributeListTotalDict() {
        return _flatAttributeListTotalDict;
    }

    public NSArray flatAttributeListTotal() {
        return _flatAttributeListTotal;
    }

    private void computeRecordValuesForRecords(NSArray recs) {
        Enumeration anEnum = recs.objectEnumerator();

        while (anEnum.hasMoreElements()) {
            DRRecord rec = (DRRecord)anEnum.nextElement();
            rec.populateValueList();
        }

    }

    //
    // keys: 'groups', 'lookup'
    //
    public NSDictionary groupsWithCriteriaArray(NSArray aCritArray) {
        NSMutableDictionary grpDict = new NSMutableDictionary();
        NSMutableArray grps = new NSMutableArray();
        Enumeration anEnum = aCritArray.objectEnumerator();

        while (anEnum.hasMoreElements()) {
            DRMasterCriteria amc = (DRMasterCriteria)anEnum.nextElement();
            DRGroup grp = DRGroup.withReportModelMasterCriteria(this, amc);
            grps.addObject(grp);
            grpDict.setObjectForKey(grp, amc.keyDesc());
        }

        NSDictionary dict = new NSDictionary(new Object[]{grps, grpDict}, new Object[]{"groups", "lookup"});
        return dict;
    }

    public int spanForVListIndexAsCellsShowHeadingShowTotals(boolean forVlist, int indx, boolean asCells, boolean showHeading, boolean showTotals) {
        NSArray lst;

        if (!forVlist) {
            lst = _hList;
        } else {
            lst = _vList;
        }

        int cnt = lst.count();
        int span = 1;
        int startIndex = indx+1;

        if (!(startIndex >= cnt)) {
            for (int i = startIndex; i < cnt; i++) {
                DRGroup grp = (DRGroup)lst.objectAtIndex(i);
                span = span*grp.sortedCriteriaList().count();
            }

        }

        if (!forVlist) {
            ////////NSLog(@"not forVlist");
            if (asCells) {
                //////NSLog(@"asCells");
                if (showTotals && (_flatAttributeListTotal.count() > 0)) {
                    ////////NSLog(@"showTotals: before span:%d", span);
                    ////////NSLog(@"showTotals: [flatAttributeListTotal count]: %d", [flatAttributeListTotal count]);
                    span = span*_flatAttributeListTotal.count();
                    ////////NSLog(@"showTotals: span:%d", span);
                } else {
                    if (_flatAttributeList.count() > 0) {
                        span = span*_flatAttributeListTotal.count();
                    }

                }

            }

        } else {
            if (asCells) {
                //if(showHeading)span = span*2;
            }

        }

        return span;
    }

    public NSArray dimensionForName(String dim) {
        if (dim == null) {
            return vList();
        }

        if (dim.equals("H")) {
            return hList();
        }

        if (dim.equals("Z")) {
            return zList();
        }

        if (dim.equals("V")) {
            return vList();
        }

        return vList();
    }

    public void moveUpDimension(DRGroup vGroup, boolean up, String dim) {
        int cnt;
        NSMutableArray dims = (NSMutableArray)dimensionForName(dim);
        int cur = dims.indexOfObject(vGroup);
        dims.removeObject(vGroup);
        cnt = dims.count();

        if (up) {
            int newdex = cur-1;

            if (newdex < 0) {
                dims.addObject(vGroup);
            } else {
                dims.insertObjectAtIndex(vGroup, newdex);
            }

        } else {
            int newdex = cur+1;

            if (newdex > cnt) {
                dims.insertObjectAtIndex(vGroup, 0);
            } else {
                dims.insertObjectAtIndex(vGroup, newdex);
            }

        }

    }

    public NSArray zList() {
        return _zList;
    }

    public NSArray hList() {
        return _hList;
    }

    public NSArray vList() {
        return _vList;
    }

    public void addToVList(DRGroup drg) {
        if (!_vList.containsObject(drg)) {
            _vList.addObject(drg);
        }

        if (_hList.containsObject(drg)) {
            _hList.removeObject(drg);
        }

        if (_zList.containsObject(drg)) {
            _zList.removeObject(drg);
        }

    }

    public void addToHList(DRGroup drg) {
        if (!_hList.containsObject(drg)) {
            _hList.addObject(drg);
        }

        if (_vList.containsObject(drg)) {
            _vList.removeObject(drg);
        }

        if (_zList.containsObject(drg)) {
            _zList.removeObject(drg);
        }

    }

    public void addToZList(DRGroup drg) {
        if (!_zList.containsObject(drg)) {
            _zList.addObject(drg);
        }

        if (_vList.containsObject(drg)) {
            _vList.removeObject(drg);
        }

        if (_hList.containsObject(drg)) {
            _hList.removeObject(drg);
        }

    }

    public NSArray records() {
        return _records;
    }

    public NSArray groups() {
        return _groups;
    }

    public DRGroup groupForMasterCriteria(DRMasterCriteria mc) {
        return (DRGroup)_groupDict.objectForKey(mc.keyDesc());
    }

    public DRMasterCriteria masterCriteriaForKeyPath(String keypath) {
        Enumeration anEnum = criteriaList().objectEnumerator();

        while (anEnum.hasMoreElements()) {
            DRMasterCriteria amc = (DRMasterCriteria)anEnum.nextElement();
            if(amc.label().startsWith(keypath))
                return amc;
        }
        return null;
    }
    

    public NSArray rawRecords() {
        return _rawRecords;
    }

    public NSArray criteriaList() {
        return _criteriaList;
    }
    public void setCriteriaList(NSArray arr) {
        _criteriaList = arr;
    }

    public NSArray attributeList() {
        return _attributeList;
    }

    public void setAttributeList(NSArray arr) {
        //OWDebug.println(1, "entered");
        _attributeList = new NSArray(arr);
        this.synchModel();
        //[self computeRecordValuesForRecords:[self records]];
        //[self flatListForAttributeListTotals];
        //[self flatListForAttributeList];
    }

    @Override
    public String toString() {
        return _groups.toString();
    }

    public String coordinateKey(NSDictionary coordDict) {
        String lookupCoordKey = "/";
        for (Enumeration criterias = _criteriaList.objectEnumerator(); criterias.hasMoreElements(); ) {
            DRMasterCriteria mc = (DRMasterCriteria)criterias.nextElement();
            String lookupkey;
            DRCriteria c = (DRCriteria)coordDict.objectForKey(mc.keyDesc());

            if (c==null || c.isTotal()) {
                lookupkey = "*";
            } else {
                lookupkey = (String)c.valueDict().objectForKey("lookupKey");
            }
            lookupCoordKey = lookupCoordKey + lookupkey + "/";
        }
        return lookupCoordKey;
    }

    public void registerRecordGroupWithCoordinates(DRRecordGroup recGrp, NSDictionary coordDict) {
        String coordKey = coordinateKey(coordDict);
        _registeredRecordGroups.setObjectForKey(recGrp, coordKey);
    }

    public void makeRecordGroupsStaleTotal() {
        for(Enumeration recordGroups = _registeredRecordGroups.allValues().objectEnumerator(); recordGroups.hasMoreElements(); ) {
            DRRecordGroup recordGroup = (DRRecordGroup)recordGroups.nextElement();
            recordGroup.makeStale();
        }
    }

    public DRRecordGroup recordGroupForCoordinates(NSDictionary coordDict) {
        String coordKey = coordinateKey(coordDict);
        DRRecordGroup recordGroup = (DRRecordGroup)_registeredRecordGroups.objectForKey(coordKey);
        return recordGroup;
    }
}