package er.reporting.examples;

import webobjectsexamples.businesslogic.movies.common.Movie;

import com.webobjects.appserver.WOComponent;
import com.webobjects.appserver.WOContext;
import com.webobjects.foundation.NSArray;

import er.grouping.DRGroup;
import er.grouping.DRRecordGroup;
import er.grouping.DRReportModel;

public class GroupingOnly extends WOComponent {
    protected Movie aMovie;
    protected DRRecordGroup aDRRecordGroup;

    public GroupingOnly (WOContext c){
        super(c);
        Session s = (Session)session();
        NSArray mcrits = DRReportModel.masterCriteriaForKey(s.selectedGroupingCriteriaString);
        DRReportModel mod = DRReportModel.withRawRecordsCriteriaListAttributeList(s.objects , mcrits, null);
        s.reportModel = mod;
    }

    public String criteriaForRow() {
        return aDRRecordGroup.criteria().label();
    }

    public WOComponent regroup() {
        Session s = (Session)session();
        NSArray mcrits = DRReportModel.masterCriteriaForKey(s.selectedGroupingCriteriaString);
        DRReportModel mod = DRReportModel.withRawRecordsCriteriaListAttributeList(s.objects, mcrits, null);
        s.reportModel = mod;
        return null;
    }

    public NSArray recordGroups() {
        Session s = (Session)session();
        NSArray recGrps;
    	NSArray grps = s.reportModel.groups();
        if(grps.count() > 0){
            DRGroup grp = (DRGroup)grps.objectAtIndex(0);
            recGrps = grp.recordGroupList();
        }else{
            recGrps = new NSArray();
        }
        return recGrps;
    }


    public WOComponent regroupWithReportEditor() {
	Session s = (Session)session();
	DRReportModel mod = DRReportModel.withRawRecordsCriteriaListAttributeList(s.objects, s.critArray, null);
	s.reportModel = mod;
        return null;
    }
}
