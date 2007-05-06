/*
 * Copyright (C) NetStruxr, Inc. All rights reserved.
 *
 * This software is published under the terms of the NetStruxr
 * Public Software License version 0.5, a copy of which has been
 * included with this distribution in the LICENSE.NPL file.  */

package er.bugtracker;

import com.webobjects.appserver.WOApplication;
import com.webobjects.appserver.WOComponent;
import com.webobjects.appserver.WODisplayGroup;
import com.webobjects.directtoweb.D2W;
import com.webobjects.directtoweb.InspectPageInterface;
import com.webobjects.directtoweb.ListPageInterface;
import com.webobjects.directtoweb.NextPageDelegate;
import com.webobjects.directtoweb.QueryPageInterface;
import com.webobjects.eoaccess.EODatabaseDataSource;
import com.webobjects.eoaccess.EOEntity;
import com.webobjects.eoaccess.EOUtilities;
import com.webobjects.eocontrol.EOAndQualifier;
import com.webobjects.eocontrol.EODataSource;
import com.webobjects.eocontrol.EOEditingContext;
import com.webobjects.eocontrol.EOEnterpriseObject;
import com.webobjects.eocontrol.EOFetchSpecification;
import com.webobjects.eocontrol.EOKeyValueQualifier;
import com.webobjects.eocontrol.EOQualifier;
import com.webobjects.foundation.NSArray;
import com.webobjects.foundation.NSDictionary;
import com.webobjects.foundation.NSKeyValueCoding;
import com.webobjects.foundation.NSMutableDictionary;
import com.webobjects.foundation.NSTimestamp;

import er.corebusinesslogic.ERCoreBusinessLogic;
import er.directtoweb.ERD2WFactory;
import er.directtoweb.ERD2WInspectPage;
import er.extensions.ERXEC;
import er.extensions.ERXEOControlUtilities;
import er.extensions.ERXSession;
import er.extensions.ERXStringUtilities;

public class Session extends ERXSession {

    public class Handler implements NSKeyValueCoding {

        protected InspectPageInterface createPageNamed(String name) {
            return ERD2WFactory.erFactory().editPageForNewObjectWithConfigurationNamed(name, Session.this);
        }
        
        protected WOComponent pageWithName(String name) {
            return WOApplication.application().pageWithName(name, context());
        }
        
        protected void applyCurrentUser(EOEnterpriseObject eo, String  relationshipName) {
            EOEditingContext ec = eo.editingContext();
            People user = (People)ERXEOControlUtilities.localInstanceOfObject(ec, getUser());
            eo.addObjectToBothSidesOfRelationshipWithKey(user, relationshipName);
        }

        protected ListPageInterface listPageNamed(String name, EODataSource ds) {
            ListPageInterface lpi = (ListPageInterface) pageForConfigurationNamed(name);
            lpi.setDataSource(ds);
            return lpi;
        }
        
        protected WOComponent pageForConfigurationNamed(String name) {
            return D2W.factory().pageForConfigurationNamed(name, Session.this);
        }

        public void takeValueForKey(Object value, String key) {
            throw new UnsupportedOperationException("Can't takeValueForKey");
        }

        public Object valueForKey(String key) {
            key = ERXStringUtilities.uncapitalize(key);
            return NSKeyValueCoding.DefaultImplementation.valueForKey(this, key);
        }

    }

    public class ComponentHandler extends Handler {
        
        public WOComponent createComponent() {
            ERD2WInspectPage page = (ERD2WInspectPage) createPageNamed("CreateComponent");
            EOEnterpriseObject eo = (EOEnterpriseObject) page.object();
            applyCurrentUser(eo, "owner");
            return (WOComponent) page;
        }

        public WOComponent listComponents() {
            return (WOComponent) listPageNamed("ListComponent", new EODatabaseDataSource(ERXEC.newEditingContext(), "Component"));
        }
    }

    public class PeopleHandler extends Handler {
        
        public WOComponent createPeople() {
            ERD2WInspectPage page = (ERD2WInspectPage) createPageNamed("CreatePeople");
            EOEnterpriseObject eo = (EOEnterpriseObject) page.object();
            //applyCurrentUser(eo, "owner");
            return (WOComponent) page;
        }

        public WOComponent listPeoples() {
            return (WOComponent) listPageNamed("ListPeople", new EODatabaseDataSource(ERXEC.newEditingContext(), "People"));
        }
    }

    public class FrameworkHandler extends Handler {
        
        public WOComponent createFramework() {
            ERD2WInspectPage page = (ERD2WInspectPage) createPageNamed("CreateFramework");
            EOEnterpriseObject eo = (EOEnterpriseObject) page.object();
            applyCurrentUser(eo, "owner");
            return (WOComponent) page;
        }

        public WOComponent listFrameworks() {
            return (WOComponent) listPageNamed("ListFramework", new EODatabaseDataSource(ERXEC.newEditingContext(), "Framework"));
        }
    }

    public class RequirementHandler extends Handler {
        
        public WOComponent createRequirement() {
            ERD2WInspectPage page = (ERD2WInspectPage) createPageNamed("CreateRequirement");
            EOEnterpriseObject eo = (EOEnterpriseObject) page.object();
            applyCurrentUser(eo, "originator");
            applyCurrentUser(eo, "owner");
            return (WOComponent) page;
         }

        public WOComponent listMyRequirements() {
            EODatabaseDataSource ds = new EODatabaseDataSource(ERXEC.newEditingContext(), "Requirement");
            return (WOComponent) listPageNamed("ListRequirement", ds);
        }

        public WOComponent queryRequirements() {
            return (WOComponent) pageForConfigurationNamed("QueryRequirement");
        }
    }

    public class TestItemHandler extends Handler {

        public WOComponent createTestItem() {
            ERD2WInspectPage page = (ERD2WInspectPage) createPageNamed("CreateTestItem");
            EOEnterpriseObject eo = (EOEnterpriseObject) page.object();
            applyCurrentUser(eo, "owner");
            return (WOComponent) page;
        }

        public WOComponent listMyTestItems() {
            EODatabaseDataSource ds = new EODatabaseDataSource(ERXEC.newEditingContext(), "TestItem");
            return (WOComponent) listPageNamed("ListTestItem", ds);
        }

        public WOComponent queryTestItems() {
            return (WOComponent) pageForConfigurationNamed("QueryTestItem");
        }
    }

    public class ReleaseHandler extends Handler {

        public WOComponent trackDefaultRelease() {
            EOEditingContext ec = defaultEditingContext();
            EOQualifier q1 = new EOKeyValueQualifier("state", EOQualifier.QualifierOperatorEqual, State.ANALYZE);
            EOQualifier q2 = new EOKeyValueQualifier("targetRelease", EOQualifier.QualifierOperatorEqual, Release.clazz.defaultRelease(ec));
            EOQualifier q = new EOAndQualifier(new NSArray(new Object[] { q1, q2 }));
            EODatabaseDataSource ds = new EODatabaseDataSource(ec, "Bug");
            EOFetchSpecification fs = new EOFetchSpecification("Bug", q, null);
            ds.setFetchSpecification(fs);
            WOComponent bugList = pageWithName("BugsPerUser");
            bugList.takeValueForKey(ds, "bugsDataSource");
            return bugList;
        }

        public WOComponent createRelease() {
            ERD2WInspectPage page = (ERD2WInspectPage) createPageNamed("CreateRelease");
            EOEnterpriseObject eo = (EOEnterpriseObject) page.object();
            return (WOComponent) page;
        }
        
        public WOComponent trackRelease() {
            return trackDefaultRelease();
        }

        public WOComponent trackMyRelease() {
            return trackDefaultRelease();
        }

        public WOComponent pushRelease() {
            EOEditingContext ec = defaultEditingContext();
            EOEnterpriseObject user = getUser();
            QueryPageInterface qpi = (QueryPageInterface) D2W.factory().pageForConfigurationNamed("TrackRelease", Session.this);
            WODisplayGroup dg = (WODisplayGroup) ((WOComponent) qpi).valueForKey("displayGroup");
            /* dg.queryMatch().setObjectForKey(Bug.DEFAULT_RELEASE,"targetRelease"); */
            dg.setQualifier(new EOKeyValueQualifier("bugs.state", EOQualifier.QualifierOperatorEqual, State.BUILD)); // picked
            // up in ERQueryPage
            qpi.setNextPageDelegate(new NextPageDelegate() {
                public WOComponent nextPage(WOComponent sender2) {
                    QueryPageInterface qpi2 = (QueryPageInterface) sender2;
                    WOComponent bugList = sender2.pageWithName("PushRelease");
                    bugList.takeValueForKey(qpi2.queryDataSource().fetchObjects(), "bugsInBuild");
                    return bugList;
                }
            });
            return (WOComponent) qpi;
        }
    }

    public class BugHandler extends Handler {

        public WOComponent createBug() {
            ERD2WInspectPage page = (ERD2WInspectPage) createPageNamed("CreateBug");
            Bug bug = (Bug) page.object();
            applyCurrentUser(bug, "originator");
            applyCurrentUser(bug, "owner");
            return (WOComponent) page;
        }

        public WOComponent listRecentBugs() {
            EODatabaseDataSource ds = new EODatabaseDataSource(ERXEC.newEditingContext(), "Bug");
            EOQualifier q = new EOKeyValueQualifier("owner", EOQualifier.QualifierOperatorEqual, getUser());
            return (WOComponent) listPageNamed("ListBug", ds);
        }
 
        public WOComponent listMyBugs() {
            EOEditingContext ec = ERXEC.newEditingContext();
            ec.lock();
            try {
                EODatabaseDataSource ds  = new EODatabaseDataSource(ec, "Bug");
                NSDictionary bindings = new NSDictionary(new Object[] { getUser() }, new Object[] { "user" });
                EOEntity bugEntity = EOUtilities.entityNamed(ec, "Bug");
                EOFetchSpecification fs = bugEntity.fetchSpecificationNamed("bugsOwned").fetchSpecificationWithQualifierBindings(bindings);

                return (WOComponent) listPageNamed("ListBug", ds);

            } finally {
                ec.unlock();
            }
        }

        public WOComponent queryBugs() {
            return (WOComponent) pageForConfigurationNamed("QueryBug");
        }
    }

    public NSMutableDictionary handlers = new NSMutableDictionary();

    public Session() {
        super();
        handlers.setObjectForKey(new ReleaseHandler(), "releases");
        handlers.setObjectForKey(new BugHandler(), "bugs");
        handlers.setObjectForKey(new ComponentHandler(), "components");
        handlers.setObjectForKey(new PeopleHandler(), "peoples");
        handlers.setObjectForKey(new FrameworkHandler(), "frameworks");
        handlers.setObjectForKey(new TestItemHandler(), "testItems");
        handlers.setObjectForKey(new RequirementHandler(), "requirements");
    }

    public void setDefaultEditingContext(EOEditingContext newEc) {
        super.setDefaultEditingContext(newEc);
    }

    protected String _lastname;

    protected String _firstname;

    private NSArray _activeUsers;

    public NSArray activeUsers() {
        if (_activeUsers == null) {
            _activeUsers = People.clazz.activeUsers(defaultEditingContext());
        }
        return _activeUsers;
    }

    protected EOEnterpriseObject _user;

    public EOEnterpriseObject getUser() {
        return _user;
    }

    public void setUser(EOEnterpriseObject user) {
        _user = user;
        ERCoreBusinessLogic.setActor(getUser());
    }

    public void awake() {
        super.awake();
        if (getUser() != null)
            ERCoreBusinessLogic.setActor(getUser());
    }

    public void sleep() {
        ERCoreBusinessLogic.setActor(null);
        super.sleep();
    }

}
