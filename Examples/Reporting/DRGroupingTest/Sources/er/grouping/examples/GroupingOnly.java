//
// GroupingOnly.java: Class file for WO Component 'GroupingOnly'
// Project DRGroupingTestJava
//
// Created by dneumann on Tue Oct 02 2001
//
package er.grouping.examples;

import webobjectsexamples.businesslogic.movies.common.Movie;

import com.webobjects.appserver.WOComponent;
import com.webobjects.appserver.WOContext;
import com.webobjects.foundation.NSArray;

import er.grouping.DRGroup;
import er.grouping.DRRecordGroup;
import er.grouping.DRReportModel;

public class GroupingOnly extends WOComponent {

    public GroupingOnly(WOContext context) {
        super(context);
        Session s = (Session)session();
        NSArray mcrits = DRReportModel.masterCriteriaForKey(s.selectedGroupingCriteriaString());

        DRReportModel mod = DRReportModel.withRawRecordsCriteriaListAttributeList(s.objects(), mcrits, null);
        //DRReportModel mod = new DRReportModel(s.objects() , mcrits, null);
        s.setReportModel(mod);
    }

    public Movie aMovie;
    public DRRecordGroup aDRRecordGroup;

    public String criteriaForRow() {
        return aDRRecordGroup.criteria().label();
    }

    public WOComponent regroup() {
        Session s = (Session)session();
        NSArray mcrits = DRReportModel.masterCriteriaForKey(s.selectedGroupingCriteriaString());
        DRReportModel mod = DRReportModel.withRawRecordsCriteriaListAttributeList(s.objects(), mcrits, null);
        //DRReportModel mod = new DRReportModel(s.objects(), mcrits, null);
        s.setReportModel(mod);
        return null;
    }

    public WOComponent regroupWithReportEditor() {
        Session s = (Session)session();
        
        DRReportModel mod = DRReportModel.withRawRecordsCriteriaListAttributeList(s.objects(), s.critArray(), null);

        //DRReportModel mod = new DRReportModel(s.objects(), s.critArray(), null);
        s.setReportModel(mod);
        return null;
    }
    
    public NSArray recordGroups() {
        Session s = (Session)session();
        NSArray recGrps = new NSArray();
        NSArray grps = s.reportModel().groups();
        if(grps.count() > 0){
            DRGroup grp = (DRGroup)grps.objectAtIndex(0);
            recGrps = grp.recordGroupList();
        }
        return recGrps;
    }
}
