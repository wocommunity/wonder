package er.reporting;

import com.webobjects.appserver.WOComponent;
import com.webobjects.appserver.WOContext;
import com.webobjects.foundation.NSArray;

import er.grouping.DRGroup;

public class WRLayoutEditor extends WOComponent  {
    public DRGroup aGrp;
    public String dispType;
    public String areportStyle;

    protected NSArray _recordGroupDisplayTypes;
    protected NSArray _reportStyles;

    public WRLayoutEditor(WOContext c) {
        super(c);
        _recordGroupDisplayTypes = new NSArray(new Object[]{"SINGLE_TOTAL" , "TABLE" , "TOTALS"});
        _reportStyles = new NSArray(new Object[]{"VERTICAL_ROWS" , "NESTED_CELLS"});
    }

    public NSArray recordGroupDisplayTypes(){
        return _recordGroupDisplayTypes;
    }

    public NSArray reportStyles(){
        return _reportStyles;
    }

    @Override
    public boolean synchronizesVariablesWithBindings() {
        return false;
    }

    public boolean shouldTotalCheck() {
        return aGrp.useGroupTotal();
    }

    public WOComponent regenReport() {
        //_initializedDimensionArrayFromBindings = false;
        return null;
    }

    public void setShouldTotalCheck(boolean v) {
        if (v) {
            aGrp.setUseGroupTotal(true);
        } else {
            NSArray srtdList = aGrp.sortedCriteriaList();
            aGrp.setUseGroupTotal(false);
            //ak@prnet.de: commented out because of refactoring
            // _currentZCriteria.setObjectForKey(srtdList.objectAtIndex(0), aGrp.masterCriteria().label());
        }

    }
}