package er.reporting;

import com.webobjects.appserver.WOComponent;
import com.webobjects.appserver.WOContext;
import com.webobjects.appserver.WOResponse;
import com.webobjects.foundation.NSArray;
import com.webobjects.foundation.NSDictionary;
import com.webobjects.foundation.NSNumberFormatter;

import er.extensions.formatters.ERXNumberFormatter;
import er.extensions.foundation.ERXValueUtilities;
import er.grouping.DRRecord;
import er.grouping.DRRecordGroup;
import er.grouping.DRReportModel;
import er.grouping.DRValue;

/**
 * Contains a cell in the table that {@link WRReport} has set up.
 * You should subclass this class to implement additional behaviour
 * or cell-level rendering, like colors that depend on the current group.
 * Additionally, you must specify the components name in the bindings
 * for the report component as the <code>reportComponentName</code>
 * binding.
 */
public class WRRecordGroup extends WOComponent  {
    String _totalToShow;
    DRRecordGroup _recordGroup;
    String _displayType;
    String _noTotalLabel;
    Boolean _showAsTable;
    Boolean _showHeadings;
    Boolean _showRecordTable;
    DRReportModel _model;
    NSArray _colors;
    String _reportStyle;
    NSDictionary _totalDict;
    NSDictionary _coordinates;
    int _totalCount = -1;

    public DRRecord record;
    public DRValue value;
    public DRValue totalValue;

    String bgcolor;

    public WRRecordGroup(WOContext c){
        super(c);
    }

    /** Resets cached values. */
    @Override
    public void reset() {
        _totalToShow = null;
        _recordGroup = null;
        _displayType = null;
        _noTotalLabel = null;
        _showAsTable = null;
        _showHeadings = null;
        _showRecordTable = null;
        _model = null;
        _colors = null;
        _reportStyle = null;
        _coordinates = null;
        _totalDict = null;
        _totalCount = -1;
        super.reset();
    }

    /** Component is stateless, and your subclasses should be so, too. */
    @Override
    public boolean isStateless() {
        return true;
    }

    @Override
    public void appendToResponse(WOResponse r, WOContext c) {
        super.appendToResponse(r, c);
    }


    /** Returns the {@link DRRecordGroup} from the bindings. */
    public DRRecordGroup recordGroup() {
        if (_recordGroup == null) {
            _recordGroup = (DRRecordGroup)valueForBinding("recordGroup");
        }
        return _recordGroup;
    }

    /**
     * Returns the display type from the bindings.
     * If not set, then <code>TOTALS</code> is used as the default,
     * meaning that ???
     */
    public String displayType() {
        if (_displayType == null) {
            _displayType = (String)valueForBinding("displayType");
            if (_displayType == null) {
                _displayType = "TOTALS";
            }
        }
        return _displayType;
    }

    public String noTotalLabel() {
        if (_noTotalLabel == null) {
            _noTotalLabel = (String)valueForBinding("noTotalLabel");
            if (_noTotalLabel == null) {
                _noTotalLabel = "-";
            }
        }
        return _noTotalLabel;
    }

    public String reportStyle() {
        if (_reportStyle == null) {
            _reportStyle = (String)valueForBinding("reportStyle");
            if (_reportStyle == null) {
                _reportStyle = "NESTED_CELLS";
            }
        }
        return _reportStyle;
    }


    public NSArray records() {
        return recordGroup().sortedRecordList();
    }


    public NSArray totals() {
        //log.debug( "entered");
        if(recordGroup() == null)
            return NSArray.EmptyArray;
        return recordGroup().totalList();
    }

    public double totalValueTotal() {
        if(totalValue != null) {
            return totalValue.total();
        }
        return 0;
    }

    public boolean nototals() {
        if (totals().count() > 0) {
            return false;
        }
        return true;
    }

    @Override
    public String toString() {
        return recordGroup().totals().toString();
    }

    public DRReportModel model() {
        if (_model == null) {
            _model = (DRReportModel)valueForBinding("model");
        }
        return _model;
    }

    public Boolean booleanValueForBinding(String name) {
        boolean flag = ERXValueUtilities.booleanValue(valueForBinding(name));
        return flag ? Boolean.TRUE : Boolean.FALSE;
    }

    public boolean showAsTable() {
        if (_showAsTable == null) {
            _showAsTable = booleanValueForBinding("showAsTable");
        }
        return _showAsTable.booleanValue();
    }

    public boolean showHeadings() {
        if (showSingleValue()) {
            return false;
        }
        if (_showHeadings == null) {
            _showHeadings = booleanValueForBinding("showHeadings");
        }
        return _showHeadings.booleanValue();
    }

    public boolean showHeadingsForTotalsOrTable() {
        return showHeadings();
    }


    public boolean showRecordTable() {
        if(_showRecordTable == null) {
            _showRecordTable = "TABLE".equals(displayType()) ? Boolean.TRUE: Boolean.FALSE;
        }
        return _showRecordTable.booleanValue();
    }


    public boolean showSingleValue() {
        if ("SINGLE_TOTAL".equals(displayType())) {
            return true;
        }
        return false;
    }


    public boolean showTotalsOnly() {
        if ("TOTALS".equals(displayType())) {
            return true;
        }
        return false;
    }


    public boolean showTotalsOnlyAsTable() {
        if (showTotalsOnly() && showAsTable()) {
            return true;
        }
        return false;
    }


    public boolean showTotalsOnlyAsCells() {
        if (showTotalsOnly() && !showAsTable()) {
            return true;
        }
        return false;
    }


    public boolean showRecordTableAsCells() {
        if (showRecordTable() && !showAsTable()) {
            return true;
        }
        return false;
    }


    public boolean showRecordTableAsTable() {
        if (showRecordTable() && showAsTable()) {
            return true;
        }
        return false;
    }


    public boolean totalsOnly() {
        return showTotalsOnly();
    }


    public String totalToShow() {
        if (_totalToShow == null) {
            _totalToShow = (String)valueForBinding("totalToShow");
        }
        return _totalToShow;
    }


    public String singleTotal() {
        if(recordGroup() == null)
            return noTotalLabel();
        String totalKey = totalToShow();

        double doubleValue = 0.0;
        
        if(totalKey != null) {
            NSArray tots = recordGroup().totalList();
            
            if (tots != null && tots.count() > 0) {
                DRValue v = recordGroup().totalForKey(totalToShow());
                if(v != null) {
                    doubleValue = v.total();
                } else {
                    return noTotalLabel();
                }
            }
        }
        Number nm = Double.valueOf(doubleValue);
        String formatString = (String)valueForBinding("formatForSingleTotal");
        NSNumberFormatter formatter = ERXNumberFormatter.numberFormatterForPattern(formatString);
        return formatter.format(nm);
    }


    public int nototalsrowspan() {
        return model().flatAttributeListTotal().count();
    }


    public boolean hDimsUsed() {
        if (model().hList().count() > 0) {
            return true;
        }
        return false;
    }


    public boolean useVerticalReportStyle() {
        String style = reportStyle();
        if ("VERTICAL_ROWS".equals(style)) {
            return true;
        }
        return false;
    }

    public NSDictionary totalDict() {
        if (_totalDict == null) {
            _totalDict = (NSDictionary)valueForBinding("totalDict");
        }
        return _totalDict;
    }

    public NSDictionary coordinates() {
        if (_coordinates == null) {
            _coordinates = (NSDictionary)valueForBinding("coordinates");
        }
        return _coordinates;
    }

    public NSArray colors() {
        if (_colors == null) {
            _colors = (NSArray)valueForBinding("colors");
            if(_colors == null) {
                _colors = NSArray.EmptyArray;
            }
        }
        return _colors;
    }

    public int totalCount() {
        if(_totalCount == -1) {
            NSDictionary d = totalDict();
            if (d == null) {
                _totalCount = 0;
            } else {
                _totalCount = d.allKeys().count();
            }
        }
        return _totalCount;
    }

    public boolean isNotTotalGroup() {
        if (totalCount() > 0) {
            return false;
        }
        return true;
    }

    public NSDictionary attributeListDict() {
        if (totalsOnly()) {
            return model().flatAttributeListTotalDict();
        }

        return model().flatAttributeDepthDict();
    }


    public int depthCount() {
        return attributeListDict().allKeys().count();
    }


    public int colspanForAllAttribs() {
        return model().flatAttributeList().count();
    }

    /*
     public void takeValuesFromRequest(WORequest r, WOContext c) {
         //Abort call to super to save all this processing time
     }
     */

    public NSArray recordFlatValueList() {
        return record.flatValueList();
    }
}
