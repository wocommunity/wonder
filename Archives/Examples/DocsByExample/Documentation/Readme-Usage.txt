DynaReporter: December 22, 1998.



Grouping Usage



Using DynaReporter's Grouping framework, DRGrouping.framework, independently of WRReporting.framework (DynaReporter's HTML presentation framework)



If you have an array of EOs, GenericRecords, or dictionaries, you can group them by attribute using DRGrouping.framework. This line creates a new DRReportModel from your list of objects (objs) and a array of master criteria (mcrits) to group by: 



   DRReportModel mod = new DRReportModel(objs, mcrits, null);



You might have got your objects from a dataSource, a displayGroup, a editingContext, thin air.  The master criteria list, mcrits will typically just have one member since usually one wishes to group a result set by one paramter. The following code creates an array, mcrits filled with a DRMasterCriteria:



public NSArray masterCriteriaForKey(String key){

   NSMutableArray mcrits = new NSMutableArray();

   NSMutableArray smcs = new NSMutableArray();

   DRSubMasterCriteria smc = new DRSubMasterCriteria(key, false, false, null, null, false, null);

   smcs.addObject(smc);	

   DRMasterCriteria mc = new DRMasterCriteria(smcs, null);

   mcrits.addObject(mc);

   return mcrits;

}



As a convenience there is a class method on DRReportModel by this very name: public NSArray masterCriteriaForKey(String key)



Note that DRSubMasterCriteria has a LOT of other options:



   DRSubMasterCriteria smc = new DRSubMasterCriteria(key, false, false, null, null, false, null);



But for the most common kind of grouping, all you need do is pass in the 'key' or attribute name you want to group by. All the other arguments are either false or null.  You might use the WRReportEditor.wo on your pages as a means to edit all criteria parameters. See the GroupingOnly WOApp example to see how you might use this Component.



To get the groups of records created by the DRReportModel's grouping engine, you can ask your DRReportModel a few questions:



   NSArray grps = mod.groups();

   DRGroup grp = grps.objectAtIndex(0);

   NSArray recGrpList = grp.recordGroupList();

   

At this point you have an array of DRRecordGroup objects, 'recGrpList'.  A DRRecordGroup is composed of two interesting items: the criteria being grouped for (e.g. Action, Horror, or Drama if we were grouping be category), and the list of records matching that criteria. You can get the DRCriteria for a DRRecordGroup by sending it a 'criteria()' message. You can get the list of EnterprisObjects associated with that criteria by sending the message 'rawRecordList()' to the DRRecordGroup. If instead you want the list of DRRecords, you can send 'recordList()'.

 

If you want the name of the DRCriteria for each record group, you can simply send it a 'label()' message.



Note, a DRRecord wraps exactly one 'raw record'.  A raw record is one element of whatever array of objects you originally grouped in the first place, such as an EnterpriseObject. DRRecords are useful if you are using the DRReportModel's totalling and attribute clustering features. Use of those features is beyond the scope of this readme though. You might want to look at the WRReporting.framework's WRReport class for an example of the usage of all the features available for reporting in the DRGrouping.framework.



The 'GroupingOnly' example app was provided to illustrate all of these basic methods in action so you can create your own completely custom presentations on top of your own DRReportModel.



d





