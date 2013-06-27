package er.reporting;

import java.util.Enumeration;

import org.apache.log4j.Logger;

import com.webobjects.appserver.WOComponent;
import com.webobjects.appserver.WOContext;
import com.webobjects.appserver.WORequest;
import com.webobjects.appserver.WOResponse;
import com.webobjects.foundation.NSArray;
import com.webobjects.foundation.NSDictionary;
import com.webobjects.foundation.NSMutableDictionary;
import com.webobjects.foundation.NSNotification;
import com.webobjects.foundation.NSNotificationCenter;
import com.webobjects.foundation.NSSelector;

import er.extensions.eof.ERXConstant;
import er.extensions.foundation.ERXStringUtilities;
import er.extensions.foundation.ERXValueUtilities;
import er.grouping.DRAttribute;
import er.grouping.DRCriteria;
import er.grouping.DRGroup;
import er.grouping.DRMasterCriteria;
import er.grouping.DRRecordGroup;
import er.grouping.DRReportModel;

public class WRReport extends WOComponent  {
    /** logging support */
    private static final Logger log = Logger.getLogger(WRReport.class);

    
    protected DRReportModel _model;

    // iterators...
    public DRGroup aGrp, hGroup, vGroup, zGroup;
    public String areportStyle;
    public int currentIndex, currentIndexV;
    public int currentLevelV, currentLevel;
    public int vheadingCount;
    public int depth;
    public String dispType;
    public DRAttribute attrib;

    protected DRCriteria _topCriteriaV, _topCriteria;
    protected int _vheadingIndex;


    protected NSMutableDictionary _indexDict;
    protected NSMutableDictionary _currentZCriteria;
    protected NSArray _recordGroupDisplayTypes;
    protected NSArray _reportStyles;
    protected String _selectedReportStyle;
    protected String _selectedRecordGroupDisplayType;
    protected String _recordGroupTotalToShow;
    protected String _recordGroupTotalFormat;
    protected Boolean _showRecordGroupAsTable;
    protected Boolean _showRecordGroupHeadings;
    protected Boolean _showPresentationControls;
    protected Boolean _showEditing;
    
    protected String _componentName;

    protected Boolean _showNavigation;
    protected NSArray _colorDict;
    //String _baseColor, _maxColor;
    //NSMutableDictionary _currCritDictCache;
    protected Boolean _showCustomReportStyle;
    protected boolean _initializedDimensionArrayFromBindings;
    protected Boolean _showTopCriteriaLabel;
    protected Boolean _shouldTotalCheck;

    public WRReport(WOContext c) {
        super(c);
        _currentZCriteria = new NSMutableDictionary();
        _indexDict = new NSMutableDictionary();
        //_currCritDictCache = new NSMutableDictionary();
        //_baseColor = "d0cfbd";
        //_maxColor = "ffec00";
        _colorDict = null;
        _initializedDimensionArrayFromBindings = false;
        _recordGroupDisplayTypes = new NSArray(new Object[]{"SINGLE_TOTAL" , "TABLE" , "TOTALS"});
        _reportStyles = new NSArray(new Object[]{"VERTICAL_ROWS" , "NESTED_CELLS"});

        NSSelector rebuildModelSelector = new NSSelector("rebuildModel", ERXConstant.NotificationClassArray);
        NSNotificationCenter.defaultCenter().addObserver(this, rebuildModelSelector, DRReportModel.DRReportModelRebuildNotification, null);
    }

    public Object recordGroupTest() {
      throw new IllegalStateException("There is a component bound to this variable and it doesn't exist.  If you need this component to work, please look at what this is supposed to do and submit a patch.");
    }
    
    @Override
    public void finalize() throws Throwable {
        NSNotificationCenter.defaultCenter().removeObserver(this);
        super.finalize();
    }

    
    public Boolean booleanValueForBinding(String name) {
        boolean flag = ERXValueUtilities.booleanValue(valueForBinding(name));
        return flag ? Boolean.TRUE : Boolean.FALSE;
    }

    public NSArray recordGroupDisplayTypes() {
        return _recordGroupDisplayTypes;
    }
    public NSArray reportStyles() {
        return _reportStyles;
    }

    @Override
    public boolean synchronizesVariablesWithBindings() {
        return false;
    }
    public void initializeDimensionArrayFromBindings() {
        if(!_initializedDimensionArrayFromBindings) {
            if(model() != null) {
                _initializedDimensionArrayFromBindings = true;
                initializeDimensionArrayFromBindings("H");
                initializeDimensionArrayFromBindings("V");
                initializeDimensionArrayFromBindings("Z");
                if(log.isDebugEnabled()) {
                    log.debug("V :" + model().vList());
                    log.debug("H :" + model().hList());
                    log.debug("Z :" + model().zList());
                }
            } else {
                log.error("Model is null!");
            }
        }
    }

    @Override
    public void awake() {
        _model = null;
        _vheadingIndex = 0;
        _indexDict.removeAllObjects();
        _colorDict = null;
        _showPresentationControls = null;
        _showRecordGroupHeadings = null;
        _showTopCriteriaLabel = null;
        _shouldTotalCheck = null;
        _recordGroupTotalToShow = null;
        _currentZCriteria.removeAllObjects();
        //_initializedDimensionArrayFromBindings = false;
        initializeDimensionArrayFromBindings();
    }

    
    @Override
    public void takeValuesFromRequest(WORequest r, WOContext c) {
        super.takeValuesFromRequest(r, c);
    }

    @Override
    public void appendToResponse(WOResponse r, WOContext c) {
        initializeDimensionArrayFromBindings();
        super.appendToResponse(r, c);
    }


    public void rebuildModel(NSNotification notification) {
        if(_model != null && _model == notification.object()) {
            log.debug("rebuildModel: " + notification.object().hashCode() + ": " + _model.hashCode());
            _currentZCriteria.removeAllObjects();
            _initializedDimensionArrayFromBindings = false;
            _model = null;
        }
    }
    

    public boolean showPresentationControls() {
        if (_showPresentationControls == null) {
            _showPresentationControls = booleanValueForBinding("showPresentationControls");
        }
        return _showPresentationControls.booleanValue();
    }
    
    public void setShowPresentationControls(boolean v) {
        _showPresentationControls = v ? Boolean.TRUE : Boolean.FALSE;
    }
    
    public void setShouldTotalCheck(boolean shouldTotalCheck) {
      _shouldTotalCheck = Boolean.valueOf(shouldTotalCheck);
    }
    
    public boolean shouldTotalCheck() {
      if (_shouldTotalCheck == null) {
        _shouldTotalCheck = booleanValueForBinding("shouldTotalCheck");
      }
      return _shouldTotalCheck.booleanValue();
    }

    public NSDictionary currentZCriteria() {
      return _currentZCriteria;
    }
    
    public String selectedRecordGroupDisplayType() {
        if (_selectedRecordGroupDisplayType == null) {
            _selectedRecordGroupDisplayType = (String)valueForBinding("selectedRecordGroupDisplayType");
            if (_selectedRecordGroupDisplayType == null) {
                _selectedRecordGroupDisplayType = "TOTALS";
            }
        }
        return _selectedRecordGroupDisplayType;
    }
    
    public void setSelectedRecordGroupDisplayType(String v) {
        _selectedRecordGroupDisplayType = v;
    }

    public String selectedReportStyle() {
        if (_selectedReportStyle == null) {
            _selectedReportStyle = (String)valueForBinding("selectedReportStyle");
            if (_selectedReportStyle == null) {
                _selectedReportStyle = "NESTED_CELLS";
            }
        }
        return _selectedReportStyle;
    }
    public void setSelectedReportStyle(String v) {
        _selectedReportStyle = v;
    }


    public boolean showVerticalRows() {
        if (selectedReportStyle().equals("VERTICAL_ROWS")
            && !showRecordGroupAsTable()
            && selectedRecordGroupDisplayType().equals("TABLE")) {
            return true;
        }
        return false;
    }


    public String recordGroupTotalToShow() {
        if (_recordGroupTotalToShow == null) {
            _recordGroupTotalToShow = (String)valueForBinding("recordGroupTotalToShow");
            if (_recordGroupTotalToShow == null) {
                _recordGroupTotalToShow = "description";
            }
        }
        return _recordGroupTotalToShow;
    }
    public void setRecordGroupTotalToShow(String v) {
        _recordGroupTotalToShow = v;
    }
    public String recordGroupTotalFormat() {
        if (_recordGroupTotalFormat == null) {
            _recordGroupTotalFormat = (String)valueForBinding("recordGroupTotalFormat");
            if (_recordGroupTotalFormat == null) {
                _recordGroupTotalFormat = "#,###0.00;;-#,###0.00";
            }
        }
        return _recordGroupTotalFormat;
    }
    public void setRecordGroupTotalFormat(String v) {
        _recordGroupTotalFormat = v;
    }

    public boolean showDefaultReportStyle() {
        return showCustomReportStyle();
    }

    public boolean showCustomReportStyle() {
        if (_showCustomReportStyle == null) {
            _showCustomReportStyle = booleanValueForBinding("showCustomReportStyle");
        }
        return _showCustomReportStyle.booleanValue();
    }
    
    public void setShowCustomReportStyle(boolean v) {
        _showCustomReportStyle = v ? Boolean.TRUE : Boolean.FALSE;
    }


    public boolean showRecordGroupHeadings() {
        if (_showRecordGroupHeadings == null) {
            if (!hasBinding("showRecordGroupHeadings")) {
                _showRecordGroupHeadings = Boolean.TRUE;
            } else {
                _showRecordGroupHeadings = booleanValueForBinding("showRecordGroupHeadings");
            }
        }
        return _showRecordGroupHeadings.booleanValue();
    }
    
    public void setShowRecordGroupHeadings(boolean v) {
        _showRecordGroupHeadings = v ? Boolean.TRUE : Boolean.FALSE;
    }


    public boolean showRecordGroupAsTable() {
        if (_showRecordGroupAsTable == null) {
            if (!hasBinding("showRecordGroupAsTable")) {
                _showRecordGroupAsTable = Boolean.FALSE;
            } else {
                _showRecordGroupAsTable = booleanValueForBinding("showRecordGroupAsTable");
            }
        }
        return _showRecordGroupAsTable.booleanValue();
    }
    
    public void setShowRecordGroupAsTable(boolean v) {
        _showRecordGroupAsTable = v ? Boolean.TRUE : Boolean.FALSE;
    }


    public boolean showTotalsOnlyAsCells() {
        if (selectedRecordGroupDisplayType().equals("TOTALS")
            && !showRecordGroupAsTable()) {
            return true;
        }
        return false;
    }

    public String componentName() {
        if(_componentName == null) {
            _componentName = (String)valueForBinding("recordGroupComponentName");
            if(_componentName == null) {
                _componentName = "WRRecordGroup";
            }
        }
        return _componentName;
    }

    public boolean showAsCells() {
        if (!showRecordGroupAsTable()
            && !selectedRecordGroupDisplayType().equals("SINGLE_TOTAL")
            && !selectedRecordGroupDisplayType().equals("TABLE")) {
            return true;
        }
        return false;
    }

    public boolean showEditing() {
        if(_showEditing == null){
            _showEditing = booleanValueForBinding("showEditing");
        }
        return _showEditing.booleanValue();
    }

    public boolean showNavigation() {
        if (_showNavigation ==null) {
            _showNavigation = booleanValueForBinding("showNavigation");
        }
        return _showNavigation.booleanValue();
    }
    
    public void setShowNavigation(boolean v) {
        _showNavigation = v ? Boolean.TRUE : Boolean.FALSE;
    }


    public DRReportModel model() {
        if (_model == null) {
            _model = (DRReportModel)valueForBinding("model");
        }
        return _model;
    }


    public boolean showSingleRow() {
        if (model().vList().count() > 0) {
            return false;
        }
        return true;
    }


    public boolean showSingleCol() {
        if (model().hList().count() > 0) {
            return false;
        }
        return true;
    }


    public boolean showNoColNoRow() {
        if (showSingleRow() && showSingleCol()) {
            return true;
        }
        return false;
    }


    public NSArray zDimensions() {
        return model().zList();
    }


    public NSArray horzDimensions() {
        return model().hList();
    }


    public NSArray vertDimensions() {
        return model().vList();
    }


    public NSArray topHorzGroupCriteriaList() {
        NSArray hList = model().hList();
        if (hList.count() > 0) {
            DRGroup group = (DRGroup)hList.objectAtIndex(0);
            NSArray sortedCriteriaList = group.sortedCriteriaList();
            return sortedCriteriaList;
        }
        return NSArray.EmptyArray;
    }


    public NSArray topVertGroupCriteriaList() {
        NSArray arr = model().vList();
        if (arr.count() > 0) {
            DRGroup grp = (DRGroup)arr.objectAtIndex(0);
            return grp.sortedCriteriaList();
        }
        return NSArray.EmptyArray;
    }


    public int colSpanForHorzList() {
        return model().spanForVListIndexAsCellsShowHeadingShowTotals(false, currentLevel-1, showAsCells(), showRecordGroupHeadings(), selectedRecordGroupDisplayType().equals("TOTALS"));
    }


    public int horzColSpan() {
        return colSpanForHorzList();
    }

    public boolean hasTitle() {
        return title() != null;
    }

    public String title() {
        return (String)valueForBinding("title");
    }
    

    public NSArray vertSubList() {
        DRMasterCriteria masterCritForCrit = topCriteriaV().masterCriteria();
        DRGroup drg = (DRGroup)model().vList().lastObject();
        
        if(drg != null){
            DRMasterCriteria bottomMasterCrit = drg.masterCriteria();
            if (masterCritForCrit.equals(bottomMasterCrit)) {
                return null;
            }
        }
        DRGroup innerGroup = (DRGroup)model().vList().objectAtIndex(currentLevelV - 1);
        return innerGroup.sortedCriteriaList();
    }


    public DRCriteria topCriteria() {
        return _topCriteria;
    }
    public void setTopCriteria(DRCriteria c) {
        //log.debug( "c:"+c);
        if(c != null){
            String ky = c.masterCriteria().label();
            //log.debug( "ky:"+ky);
            if (ky != null) {
                _currentZCriteria.setObjectForKey(c, ky);
            }
        }
        _topCriteria = c;
    }


    public NSArray horzSubList() {
        // current DRGroup for row is: hGroup
        // current DRCriteria for nesting is: topCriteria
        // need list of sub criteria if any give the above
        DRGroup innerGroup;
        DRMasterCriteria masterCritForCrit = topCriteria().masterCriteria();
        if (masterCritForCrit.equals(hGroup.masterCriteria())) {
            return null;
        }
        innerGroup = (DRGroup)model().hList().objectAtIndex(currentLevel-1);
        return innerGroup.sortedCriteriaList();
    }


    public NSArray horzSubList2() {
        DRMasterCriteria masterCritForCrit = topCriteria().masterCriteria();
        DRGroup grp = (DRGroup)model().hList().lastObject();
        DRMasterCriteria bottomMasterCrit = grp.masterCriteria();

        if (masterCritForCrit.equals(bottomMasterCrit)) {
            return null;
        }

        DRGroup innerGroup = (DRGroup)model().hList().objectAtIndex(currentLevel-1);
        return innerGroup.sortedCriteriaList();
    }

    public void initializeDimensionArrayFromBindings(String dimension) {
        NSArray keypaths = ERXValueUtilities.arrayValue(valueForBinding("keysIn" + dimension));
        if(keypaths != null && keypaths.count() > 0) {
            for (Enumeration en = keypaths.objectEnumerator(); en.hasMoreElements(); ) {
                String keypath = (String)en.nextElement();
                DRMasterCriteria crit = model().masterCriteriaForKeyPath(keypath);
                if(crit != null) {
                    DRGroup group = model().groupForMasterCriteria(crit);
                    if("Z".equals(dimension))
                        model().addToZList(group);
                    else if("H".equals(dimension))
                        model().addToHList(group);
                    else if("V".equals(dimension))
                        model().addToVList(group);
                } else {
                    log.warn("Criteria not found: " + keypath);
                }
            }
        }
    }

    public NSDictionary addCoordsFrom(NSMutableDictionary currCritDict) {
        NSMutableDictionary dict = new NSMutableDictionary();
        Enumeration en = model().groups().objectEnumerator();
        //log.debug( "currCritDict:"+currCritDict);

        while (en.hasMoreElements()) {
            DRGroup grp = (DRGroup)en.nextElement();
            DRMasterCriteria dmc = grp.masterCriteria();
            DRCriteria crt = (DRCriteria)currCritDict.objectForKey(dmc.label());
            if(crt != null) {
                dict.setObjectForKey(crt, dmc.keyDesc());

                if (crt.isTotal()) {
                    NSMutableDictionary d = (NSMutableDictionary)dict.objectForKey("isTotal");

                    if (d == null) {
                        d = new NSMutableDictionary();
                        dict.setObjectForKey(d, "isTotal");
                    }

                    d.setObjectForKey("true", dmc.keyDesc());
                }
            }

        }
        return dict;
    }

    public NSDictionary currentCoordinates() {
        NSDictionary dict = addCoordsFrom(_currentZCriteria);
        return dict;
    }


    public DRRecordGroup recordGroup() {
        NSDictionary crds = currentCoordinates();
        DRRecordGroup drg =  model().recordGroupForCoordinates(crds);
        return drg;
    }


    public DRCriteria topCriteriaV() {
        return _topCriteriaV;
    }
    public void setTopCriteriaV(DRCriteria c) {
        if(c != null) {
            String ky = c.masterCriteria().label();
            if (ky != null) {
                _currentZCriteria.setObjectForKey(c, ky);
            }
        }
        _topCriteriaV = c;
    }


    public String topCriteriaVLabel() {
        return topCriteriaV().label();
    }


    public int vheadingCount() {
        return model().vList().count()-1;
    }


    public int vheadingIndex() {
        return _vheadingIndex;
    }


    public void setVheadingIndex(int indx) {
        _vheadingIndex = indx;
    }


    public int vertRowSpan() {
        return model().spanForVListIndexAsCellsShowHeadingShowTotals(true, vheadingIndex(), !showRecordGroupAsTable(), showRecordGroupHeadings(), selectedRecordGroupDisplayType().equals("TOTALS"));
    }


    public boolean showIndentCell() {
        int oldCnt;
        Number oldCount = (Number)_indexDict.objectForKey(Integer.valueOf(vheadingIndex()));

        if (oldCount == null) {
            oldCnt = 0;
        } else {
            int span = model().spanForVListIndexAsCellsShowHeadingShowTotals(true, vheadingIndex(), !showRecordGroupAsTable(), showRecordGroupHeadings(), selectedRecordGroupDisplayType().equals("TOTALS"));
            oldCnt = oldCount.intValue();
            oldCnt++;

            if (oldCnt > (span-1)) {
                oldCnt = 0;
            }

        }

        _indexDict.setObjectForKey(Integer.valueOf(oldCnt), Integer.valueOf(vheadingIndex()));

        if (oldCnt == 0) {
            return true;
        }

        return false;
    }


    public String vIndentCriteriaLabel() {
        DRGroup grp = (DRGroup)model().vList().objectAtIndex(vheadingIndex());
        String ky = grp.masterCriteria().label();
        DRCriteria crt = (DRCriteria)_currentZCriteria.objectForKey(ky);
        return crt.label();
    }

    public WOComponent regenReport() {
        //_initializedDimensionArrayFromBindings = false;
        return null;
    }


    public int numberOfCrits() {
        int nm = 1;
        Enumeration en = model().hList().objectEnumerator();
        //log.debug( "this.model().hList():"+this.model().hList());

        while (en.hasMoreElements()) {
            DRGroup grp = (DRGroup)en.nextElement();
            nm = nm*grp.sortedCriteriaList().count();
        }

        //log.debug( "nm:"+nm);
        return nm;
    }


    public boolean showHeadersForAsCells() {
        if (showRecordGroupHeadings() && showTotalsOnlyAsCells()) {
            return true;
        }

        return false;
    }


    public NSDictionary attributeListDict() {
        return model().flatAttributeListTotalDict();
    }


    public int depthCount() {
        //log.debug( "this.attributeListDict().allKeys().count():"+this.attributeListDict().allKeys().count());
        //log.debug( "this.attributeListDict().allKeys().count():"+this.attributeListDict());
        return attributeListDict().allKeys().count();
    }


    public int colspanAddition() {
        int count = model().vList().count();
        return count - currentLevelV;
    }


    public int depthCountAllAttribs() {
        return model().flatAttributeList().count()+model().vList().count() - vheadingIndex();
    }


    public int indentCellCount() {
        return currentLevelV;
    }


    public boolean showVHeadings() {
        if (vheadingIndex() == 0) {
            return true;
        }

        return false;
    }


    public NSArray attributeListAtDepth() {
        Number ky = Integer.valueOf(depth);
        //log.debug( "ky:"+ky);
        NSArray a = (NSArray)attributeListDict().objectForKey(ky);
        //log.debug( "a:"+a);
        return a;
    }


    public int colSpan() {
        int cls = attrib.flatAttributesTotal().count();
        if (attrib.showTotal()) {
            cls = cls+1;
        }
        //log.debug( "cls:"+cls);
        return cls == 0 ? 1 : cls;
    }


    public int rowSpan() {
        //flatAttributes
        int rs = 1;
        if (!attrib.isGroup()) {
            rs = depthCount()-depth;
        }
        //log.debug( "rs:"+rs);
        return rs;
    }


    public String attribLabel() {
        return attrib.label();
    }

    public String idAttributeTd() {
    	String id = attrib.keyPath();
    	id = ERXStringUtilities.escapeNonXMLChars(id);
    	return id;
    }
    
    public DRAttribute attrib() {
        return attrib;
    }


    public void setAttrib(DRAttribute at) {
        attrib = at;
    }


    public DRGroup aGrp() {
        return aGrp;
    }


    public void setAGrp(DRGroup gp) {
        aGrp = gp;
    }

    public NSDictionary totalDict() {
        NSDictionary dict = currentCoordinates();
        NSDictionary totalDict = (NSDictionary)dict.objectForKey("isTotal");
        return totalDict;
    }


    public int totalCount() {
        NSDictionary totalDict = totalDict();
        
        if (totalDict == null) {
            return 0;
        }

        int totalCount = totalDict.allKeys().count();
        return totalCount;
    }


    public String colorForCoords() {
        int totalCount = totalCount();
        int maxColorsConfigured = colorDict().count();

        if (totalCount == maxColorsConfigured) {
            return "#eeeeee";
        }

        if (totalCount > maxColorsConfigured) {
            return "#ffffff";
        }
        return (String)colorDict().objectAtIndex(totalCount);
    }

    public String bgcolorRowSpanTd() {
        return colorForCoords();
    }

    public String bgcolorColSpanTd() {
        return colorForCoords();
    }

    public String classAttributeTd() {
        return "WRAttribute" + depth + "Total" + totalCount();
    }

    public String classColSpanTd() {
        return "WRHTotal" + totalCount();
    }
    
    public String classRowSpanTd() {
        return "WRVTotal" + totalCount();
    }

    /*
    public int hexstringToInt(String str) {
        String base = "0x";
        String hexs = base.concat(str);
        long n = this.strtoul(hexs.toCharArray(), (char)null, 16);
        int i = (int)n;
        return i;
    }
    */


    public NSArray colorDict() {
        if (_colorDict == null) {
            if (hasBinding("colors")) {
                _colorDict = (NSArray)valueForBinding("colors");
            }
            if (_colorDict == null) {
                _colorDict = new NSArray(new Object[]{"#c6c3af" , "#b7af4b" , "#d5ba27" , "#ffec00"});
            }

            /*
            int i;
            int numOfGroups = [[[self model] groups] count];
            int maxColorAsNumber = [self hexstringToInt:_maxColor];
            int minColorAsNumber = [self hexstringToInt:_baseColor];
            int colorDelta = maxColorAsNumber - minColorAsNumber;
            double colorInterval = (double)colorDelta/(double)numOfGroups;
            NSMutableArray *arr = [NSMutableArray array];
            for(i=0;i< numOfGroups; i++){
                    int colorAsDecimal = i* colorInterval + minColorAsNumber;
                    NSString * colorHexString = [NSString stringWithFormat:@"%x", colorAsDecimal];
                    [arr addObject:colorHexString];
            }
            //NSLog(@"buildColorDict: %@", arr);
            colorDict = [[NSArray arrayWithArray:arr] retain];
	*/
        }

        return _colorDict;
    }


    public int vheadingCount2() {
        return model().vList().count();
    }


    public boolean showTopCriteriaLabel() {
        if(_showTopCriteriaLabel == null) {
            if(!hasBinding("showTopCriteriaLabel")) {
                _showTopCriteriaLabel = Boolean.TRUE;
            } else {
                _showTopCriteriaLabel = booleanValueForBinding("showTopCriteriaLabel");
            }
        }
        return _showTopCriteriaLabel.booleanValue();
    }
}
