/*
 * Copyright (C) NetStruxr, Inc. All rights reserved.
 *
 * This software is published under the terms of the NetStruxr
 * Public Software License version 0.5, a copy of which has been
 * included with this distribution in the LICENSE.NPL file.  */

package er.bugtracker;
import com.webobjects.appserver.*;
import com.webobjects.eocontrol.*;
import com.webobjects.eoaccess.*;
import com.webobjects.foundation.*;
import com.webobjects.directtoweb.*;
import java.lang.*;
import er.extensions.*;

public class MenuHeader extends WOComponent {

    public MenuHeader(WOContext aContext) {
        super(aContext);
    }

    public D2WContext _entityContext=new D2WContext(session());
    public String entityNameInList;
    protected EODatabaseDataSource _queryDataSource;
    protected NextPageDelegate _nextPageDelegate;
    private NSDictionary bindings;


    private String _manipulatedEntityName;
    EditPageInterface epi;
    
    public String manipulatedEntityName() {
        if (_manipulatedEntityName==null) {
            WOComponent currentPage=context().page();
            _manipulatedEntityName=D2W.factory().entityNameFromPage(currentPage);
        }
        return _manipulatedEntityName;
      }

    public void setManipulatedEntityName(String newValue) {
        _manipulatedEntityName=newValue;
    }
    

    public NSArray visibleEntityNames() {
        return D2WUtils.visibleEntityNames(_entityContext);
    }

    public boolean isEntityReadOnly(EOEntity e) {
        return ERXValueUtilities.booleanValue(_entityContext.valueForKey("readOnly")) || e.isReadOnly() || D2WUtils.readOnlyEntityNames(_entityContext).containsObject(e.name());
      }
    
    public WOComponent findEntityAction() {
        QueryPageInterface newQueryPage=D2W.factory().queryPageForEntityNamed(_manipulatedEntityName,session());
        return (WOComponent)newQueryPage;
    }

    public WOComponent newObjectAction() {
        WOComponent nextPage=null;
        EOEntity entity=EOModelGroup.defaultGroup().entityNamed(_manipulatedEntityName);
        EOClassDescription aClassDesc=entity.classDescriptionForInstances();
        if (isEntityReadOnly(entity)) {
            ErrorPageInterface api=D2W.factory().errorPage(session());
            api.setMessage("<b>You can not create new instances of "+_manipulatedEntityName+"</b><br><br>It is read-only.");
            api.setNextPage(context().page());
            nextPage=(WOComponent)api;
        } else {
            EOEditingContext peerContext=ERXEC.newEditingContext(session().defaultEditingContext().parentObjectStore());
            peerContext.lock();
            try {
                EOEnterpriseObject aNewEO=(EOEnterpriseObject)aClassDesc.createInstanceWithEditingContext(peerContext, null);
                peerContext.insertObject(aNewEO);
                if (_manipulatedEntityName.equals("TestItem")) {
                    epi=(EditPageInterface)D2W.factory().pageForConfigurationNamed("EditNewTestItem",session());

                    EOEnterpriseObject localUser=EOUtilities.localInstanceOfObject(aNewEO.editingContext(), ((Session)session()).getUser());
                    aNewEO.addObjectToBothSidesOfRelationshipWithKey(localUser,"owner");
                } else if (_manipulatedEntityName.equals("Bug")) {
                    epi=(EditPageInterface)D2W.factory().pageForConfigurationNamed("EditNewBug",session());
                    EOEnterpriseObject localUser=EOUtilities.localInstanceOfObject(aNewEO.editingContext(), ((Session)session()).getUser());
                    aNewEO.addObjectToBothSidesOfRelationshipWithKey(localUser,"originator");
                } else if (_manipulatedEntityName.equals("Requirement")) {
                    epi=(EditPageInterface)D2W.factory().pageForConfigurationNamed("EditNewRequirement",session());
                    EOEnterpriseObject localUser=EOUtilities.localInstanceOfObject(aNewEO.editingContext(), ((Session)session()).getUser());
                    aNewEO.addObjectToBothSidesOfRelationshipWithKey(localUser,"originator");
                } else {
                    epi=D2W.factory().editPageForEntityNamed(_manipulatedEntityName, session());
                }
                epi.setObject(aNewEO);
                epi.setNextPage(context().page());
                nextPage=(WOComponent)epi;
            } catch (IllegalArgumentException e) {
                ErrorPageInterface epf=D2W.factory().errorPage(session());
                epf.setMessage(e.toString());
                epf.setNextPage(context().page());
                nextPage=(WOComponent)epf;
            } finally {
                peerContext.unlock();
            }
        }
        return nextPage;
    }



    public WOComponent homeAction() {
        return pageWithName("HomePage");
    }

    public WOComponent showWebAssistant() {
        return D2W.factory().webAssistantInContext(context());
    }

    public boolean isWebAssistantEnabled () {
        return D2W.factory().isWebAssistantEnabled();
    }
        
    public WOComponent myBugs() {
        _queryDataSource =new EODatabaseDataSource(session().defaultEditingContext(), "Bug");
        Session mysession = (Session)session();
        bindings = new NSDictionary(new Object[] {mysession.getUser()}, new Object[] {"user"});
        EOEntity bugEntity=EOUtilities.entityNamed(session().defaultEditingContext(),"Bug");
        EOFetchSpecification fs=bugEntity.fetchSpecificationNamed("MyBugs").fetchSpecificationWithQualifierBindings(bindings);
        
        
        //_queryDataSource.setQualifierBindings(bindings);

        //_queryDataSource.setAuxiliaryQualifier(qualifier());
        _queryDataSource.setFetchSpecification(fs);
       // _queryDataSource.fetchSpecification().setIsDeep(false);
       //_queryDataSource.fetchSpecification().setUsesDistinct(false);
       // _queryDataSource.fetchSpecification().setRefreshesRefetchedObjects(false);

            if (_nextPageDelegate==null) {
                ListPageInterface listPage=(ListPageInterface)D2W.factory().pageForConfigurationNamed("ListMyBugs",session());
                listPage.setDataSource(_queryDataSource);
               // listPage.setNextPage(this);
                return (WOComponent)listPage;
            } else
                return _nextPageDelegate.nextPage(this);
    }

    public void setNextPageDelegate(NextPageDelegate delegate) {
        _nextPageDelegate=delegate;
    }
    

    public WOComponent bugsFiledRecently() {
        EOEnterpriseObject user=((Session)session()).getUser();
        QueryPageInterface qpi=(QueryPageInterface)D2W.factory().pageForConfigurationNamed("BugsFiledRecently",session());
        //((WOComponent)qpi).takeValueForKeyPath(user,"displayGroup.queryMatch.originator");
        NSTimestamp now=new NSTimestamp();
        ((WOComponent)qpi).takeValueForKeyPath(now.timestampByAddingGregorianUnits(0,0,-1,0,0,0),
                                               "displayGroup.queryMin.dateSubmitted");
        return (WOComponent)qpi;
        
    }

    public WOComponent editMyInfo() {
        EOEnterpriseObject user=((Session)session()).getUser();
        EditPageInterface epi=(EditPageInterface)D2W.factory().pageForConfigurationNamed("EditMyInformation",session());
        epi.setObject(user);
        epi.setNextPage(context().page());
        return (WOComponent)epi;
    }

    public WOComponent trackRelease() {
        return trackDefaultRelease(context().page());
    }

    public WOComponent trackMyRelease() {
        return trackMyDefaultRelease(((Session)session()).getUser(), context().page());
    }
    
    public static WOComponent trackAnyRelease(WOComponent sender) {
        EOEnterpriseObject user=((Session)sender.session()).getUser();
        QueryPageInterface qpi=(QueryPageInterface)D2W.factory().pageForConfigurationNamed("TrackRelease",sender.session());
        WODisplayGroup dg=(WODisplayGroup)((WOComponent)qpi).valueForKey("displayGroup");
        Release defaultRelease = Release.clazz.defaultRelease(sender.session().defaultEditingContext());
        if (defaultRelease!=null) dg.queryMatch().setObjectForKey(defaultRelease,"targetRelease");
        dg.setQualifier(new EOKeyValueQualifier("state", EOQualifier.QualifierOperatorEqual, State.ANALYZE)); // picked up in ERQueryPage

        qpi.setNextPageDelegate(new NextPageDelegate() {
            public WOComponent nextPage(WOComponent senders) {
                QueryPageInterface qpi2=(QueryPageInterface)senders;
                WOComponent bugList=senders.pageWithName("BugsPerUser");
                bugList.takeValueForKey(qpi2.queryDataSource(),"bugsDataSource");
                return bugList;
            }
        });
        return (WOComponent)qpi;        
    }

    public static WOComponent trackDefaultRelease(WOComponent sender) {
        EOQualifier q1=new EOKeyValueQualifier("state",
                                               EOQualifier.QualifierOperatorEqual,
                                               State.ANALYZE);
        EOQualifier q2=new EOKeyValueQualifier("targetRelease",
                                               EOQualifier.QualifierOperatorEqual,
                                               Release.clazz.defaultRelease(sender.session().defaultEditingContext()));
        EOQualifier q=new EOAndQualifier(new NSArray(new Object[] {q1,q2}));
        EODatabaseDataSource ds=new EODatabaseDataSource(sender.session().defaultEditingContext(), "Bug");
        EOFetchSpecification fs=new EOFetchSpecification("Bug",q,null);
        ds.setFetchSpecification(fs);
        WOComponent bugList=sender.pageWithName("BugsPerUser");
        bugList.takeValueForKey(ds,"bugsDataSource");
        return bugList;
    }

    public static WOComponent trackMyDefaultRelease(EOEnterpriseObject me, WOComponent sender) {
        EOQualifier q1=new EOKeyValueQualifier("state",
                                               EOQualifier.QualifierOperatorEqual,
                                               State.ANALYZE);
        EOQualifier q2=new EOKeyValueQualifier("targetRelease",
                                               EOQualifier.QualifierOperatorEqual,
                                               Release.clazz.defaultRelease(sender.session().defaultEditingContext()));
        EOQualifier q3=new EOKeyValueQualifier("owner",
                                               EOQualifier.QualifierOperatorEqual,
                                               me);        
        EOQualifier q=new EOAndQualifier(new NSArray(new Object[] {q1,q2,q3}));
        EODatabaseDataSource ds=new EODatabaseDataSource(sender.session().defaultEditingContext(), "Bug");
        EOFetchSpecification fs=new EOFetchSpecification("Bug",q,null);
        ds.setFetchSpecification(fs);
        
        ListPageInterface lpi=(ListPageInterface)D2W.factory().listPageForEntityNamed("Bug", sender.session());
        //EOArrayDataSource ads=new EOArrayDataSource(EOClassDescription.classDescriptionForEntityName("Bug"),
        //                                            session().defaultEditingContext());
        //ads.setArray(bugsPerOwner);
        lpi.setDataSource(ds);
        lpi.setNextPage(sender);
        return (WOComponent)lpi;
    }
    
    public WOComponent pushRelease() {
        return pushRelease(this);
    }
    
    public static WOComponent pushRelease (WOComponent sender) {
        EOEnterpriseObject user=((Session)sender.session()).getUser();
        QueryPageInterface qpi=(QueryPageInterface)D2W.factory().pageForConfigurationNamed("TrackRelease",sender.session());
        WODisplayGroup dg=(WODisplayGroup)((WOComponent)qpi).valueForKey("displayGroup");
        /* dg.queryMatch().setObjectForKey(Bug.DEFAULT_RELEASE,"targetRelease"); */
        dg.setQualifier(new EOKeyValueQualifier("state",
                                                EOQualifier.QualifierOperatorEqual,
                                                State.BUILD)); // picked up in ERQueryPage
        qpi.setNextPageDelegate(new NextPageDelegate() {
            public WOComponent nextPage(WOComponent sender2) {
                QueryPageInterface qpi2=(QueryPageInterface)sender2;
                WOComponent bugList=sender2.pageWithName("PushRelease");
                bugList.takeValueForKey(qpi2.queryDataSource().fetchObjects(),"bugsInBuild");
                return bugList;
            }
        });
        return (WOComponent)qpi;        
    }   
 
    public WOComponent freeQuery() {
        return pageWithName("FreeQuery");
    }

    protected Integer bugNumber;
    public WOComponent findBugByNumber () {
        EOQualifier q=new EOKeyValueQualifier("bugid",
                                              EOQualifier.QualifierOperatorEqual,
                                              bugNumber);
        WOComponent result=null;
        EODatabaseDataSource ds=new EODatabaseDataSource(session().defaultEditingContext(), "Bug");
        EOFetchSpecification fs=new EOFetchSpecification("Bug",q,null);
        NSArray bugs=session().defaultEditingContext(). objectsWithFetchSpecification(fs);
        if (bugs!=null && bugs.count()==1) {
            InspectPageInterface ipi=D2W.factory().inspectPageForEntityNamed("Bug",session());
            ipi.setObject((EOEnterpriseObject)bugs.objectAtIndex(0));
            ipi.setNextPage(context().page());
            result=(WOComponent)ipi;
        } else {
            ds.setFetchSpecification(fs);
            ListPageInterface lpi=(ListPageInterface)D2W.factory().listPageForEntityNamed("Bug",session());
            lpi.setDataSource(ds);
            lpi.setNextPage(context().page());
            result=(WOComponent)lpi;
        }
        return result;            
    }
}
