package er.reporting;

import com.webobjects.appserver.WOComponent;
import com.webobjects.appserver.WOContext;
import com.webobjects.foundation.NSArray;
import com.webobjects.foundation.NSMutableDictionary;

import er.extensions.components.ERXStatelessComponent;
import er.grouping.DRCriteria;
import er.grouping.DRGroup;
import er.grouping.DRMasterCriteria;
import er.grouping.DRReportModel;

public class WRNavigationControl extends ERXStatelessComponent {
    public DRReportModel model;
    public DRGroup group;
    public DRGroup zGroup;
    public String listLabel;
    public NSMutableDictionary zCriteria;

    public WRNavigationControl(WOContext context) {
        super(context);
    }

    @Override
    public void reset() {
        super.reset();
        group = null;
        listLabel = null;
        model = null;
        zCriteria = null;
    }

    public String currentZCriteriaLabel() {
        NSArray srtdList = zGroup.sortedCriteriaList();
        DRMasterCriteria mc = zGroup.masterCriteria();
        DRCriteria crt = (DRCriteria)zCriteria().objectForKey(mc.label());

        if (crt == null) {
            return "ERROR";
        }

        int index = srtdList.indexOfObject(crt);
        int srtdListCount = srtdList.count();


        if (index > srtdListCount-1) {
            crt = (DRCriteria)srtdList.objectAtIndex(srtdListCount-1);
        }
        //else {
        //    crt = (DRCriteria)srtdList.objectAtIndex(index);
        //}

        return crt.label();
    }

    public void updateZList(DRGroup group) {
        DRCriteria crit;
        DRMasterCriteria mc = group.masterCriteria();
        NSArray srtdList = group.sortedCriteriaList();
        String mcLabel = mc.label();
        crit = (DRCriteria)zCriteria().objectForKey(mcLabel);

        if (crit == null) {
            crit = (DRCriteria)srtdList.objectAtIndex(0);
            zCriteria().setObjectForKey(crit, mcLabel);
        }
    }
    
    public void moveGroupToList(String dest) {
        if("Z".equals(dest)) {
            model().addToZList(group);
            updateZList(group);
        } else if("H".equals(dest)) {
            model().addToHList(group);
        } else {
            model().addToVList(group);
        }
    }
    
    
    public WOComponent down() {
        model().moveUpDimension(group, false, listLabel());
        return null;
    }

    public WOComponent up() {
        model().moveUpDimension(group, true, listLabel());
        return null;
    }

    public WOComponent left() {
        moveGroupToList(leftLabel());
        return null;
    }

    public WOComponent right() {
        moveGroupToList(rightLabel());
        return null;
    }

    public String listLabel() {
        if(listLabel == null) {
            listLabel = ((String)valueForBinding("listLabel")).toUpperCase();
        }
        return listLabel;
    }

    public DRReportModel model() {
        if(model == null) {
            model = (DRReportModel)valueForBinding("model");
        }
        return model;
    }

    public NSMutableDictionary zCriteria() {
        if(zCriteria == null) {
            zCriteria = (NSMutableDictionary)valueForBinding("zCriteria");
        }
        return zCriteria;
    }

    public NSArray list() {
        return model.dimensionForName(listLabel());
    }
    
    public String leftLabel() {
        String listLabel = listLabel();
        if("V".equals(listLabel)) {
            return "Z";
        } else if("H".equals(listLabel)){
            return "V";
        } else {
            return "H";
        }
    }
    
    public String rightLabel() {
        String listLabel = listLabel();
        if("V".equals(listLabel)) {
            return "H";
        } else if("H".equals(listLabel)){
            return "Z";
        } else {
            return "V";
        }
    }

    public WOComponent nextZ() {
        DRCriteria crt;
        NSArray srtdList = zGroup.sortedCriteriaList();
        DRMasterCriteria mc = zGroup.masterCriteria();
        int index = srtdList.indexOfObject(zCriteria().objectForKey(mc.label()));
        int count = srtdList.count();
        int newIndex = index+1;

        if (newIndex == count) {
            newIndex = 0;
        }

        crt = (DRCriteria)srtdList.objectAtIndex(newIndex);
        zCriteria().setObjectForKey(crt, mc.label());
        return null;
    }


    public WOComponent prevZ() {
        DRCriteria crt;
        NSArray srtdList = zGroup.sortedCriteriaList();
        DRMasterCriteria mc = zGroup.masterCriteria();
        int index = srtdList.indexOfObject(zCriteria().objectForKey(mc.label()));
        int count = srtdList.count();
        int newIndex = index-1;

        if (newIndex < 0) {
            newIndex = count-1;
        }

        crt = (DRCriteria)srtdList.objectAtIndex(newIndex);
        zCriteria().setObjectForKey(crt, mc.label());
        return null;
    }


    public boolean showZDimensions() {
        if (model().zList().count() > 0 && "Z".equals(listLabel())) {
            return true;
        }
        return false;
    }

    public boolean showNavigation() {
        return booleanValueForBinding("showNavigation");
    }
}
