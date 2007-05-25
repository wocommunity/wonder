package er.bugtracker;

import java.util.Enumeration;

import com.webobjects.appserver.WOApplication;
import com.webobjects.appserver.WOComponent;
import com.webobjects.appserver.WODisplayGroup;
import com.webobjects.appserver.WOSession;
import com.webobjects.directtoweb.D2W;
import com.webobjects.directtoweb.EditPageInterface;
import com.webobjects.directtoweb.InspectPageInterface;
import com.webobjects.directtoweb.ListPageInterface;
import com.webobjects.directtoweb.NextPageDelegate;
import com.webobjects.directtoweb.QueryPageInterface;
import com.webobjects.eoaccess.EODatabaseDataSource;
import com.webobjects.eocontrol.EOAndQualifier;
import com.webobjects.eocontrol.EOArrayDataSource;
import com.webobjects.eocontrol.EODataSource;
import com.webobjects.eocontrol.EOEditingContext;
import com.webobjects.eocontrol.EOEnterpriseObject;
import com.webobjects.eocontrol.EOFetchSpecification;
import com.webobjects.eocontrol.EOKeyValueQualifier;
import com.webobjects.eocontrol.EOOrQualifier;
import com.webobjects.eocontrol.EOQualifier;
import com.webobjects.foundation.NSArray;
import com.webobjects.foundation.NSDictionary;
import com.webobjects.foundation.NSKeyValueCoding;
import com.webobjects.foundation.NSMutableArray;

import er.directtoweb.ERD2WFactory;
import er.directtoweb.ERD2WInspectPage;
import er.directtoweb.ERD2WQueryPage;
import er.extensions.ERXEC;
import er.extensions.ERXExtensions;
import er.extensions.ERXLocalizer;
import er.extensions.ERXStringUtilities;

/**
 * Central page creation class. All workflow things should go here or in the super classes. The session
 * and the current user will get retrieved via thread storage.
 * @author ak
 *
 */
public class Factory extends ERD2WFactory implements NSKeyValueCoding {

    public void takeValueForKey(Object value, String key) {
        throw new UnsupportedOperationException("Can't takeValueForKey");
    }

    public Object valueForKey(String key) {
        key = ERXStringUtilities.uncapitalize(key);
        return NSKeyValueCoding.DefaultImplementation.valueForKey(this, key);
    }

    /**
     * Bottleneck for most of the page creation.
     */
    public WOComponent pageForConfigurationNamed(String name, WOSession s) {
        WOComponent nextPage = super.pageForConfigurationNamed(name, s);
        return nextPage;
    }

    public EditPageInterface editPageNamed(String pageConfiguration, EOEnterpriseObject eo) {
        EditPageInterface epi = (EditPageInterface) inspectPageNamed(pageConfiguration, eo);
        epi.setObject(eo);
        return epi;
    }

    public InspectPageInterface inspectPageNamed(String pageConfiguration, EOEnterpriseObject eo) {
        InspectPageInterface epi = (InspectPageInterface) pageForConfigurationNamed(pageConfiguration, session());
        epi.setObject(eo);
        return epi;
    }

    protected InspectPageInterface createPageNamed(String name) {
        EditPageInterface epi = editPageForNewObjectWithConfigurationNamed(name, session());
        epi.setNextPage(homePage());
        return epi;
    }
    
    protected void applyCurrentUser(EOEnterpriseObject eo, String relationshipName) {
        EOEditingContext ec = eo.editingContext();
        People user = currentUser(ec);
        eo.addObjectToBothSidesOfRelationshipWithKey(user, relationshipName);
    }

    protected ListPageInterface listPageNamed(String name, EODataSource ds) {
        ListPageInterface lpi = (ListPageInterface) pageForConfigurationNamed(name);
        lpi.setDataSource(ds);
        return lpi;
    }

    protected WOComponent pageForConfigurationNamed(String name) {
        WOComponent page = D2W.factory().pageForConfigurationNamed(name, session());
        page.takeValueForKey(pageWithName("HomePage"), "nextPage");
        return page;
    }
    
    private Session session() {
        return (Session) ERXExtensions.session();
    }
    
    public WOComponent currentPage() {
        return session().context().page();
    }
    
    private People currentUser(EOEditingContext ec) {
        ec = (ec == null ? session().defaultEditingContext() : ec);
        return People.clazz.currentUser(ec);
    }
    
    protected WOComponent pageWithName(String name) {
        return WOApplication.application().pageWithName(name, session().context());
    }

    public WOComponent homePage() {
        return pageWithName("HomePage");
    }
    
    /**
     * Singleton of this class.
     * @return
     */
    public static Factory bugTracker() {
        return (Factory)D2W.factory();
    }
    
    ///  Component stuff;
    
    public WOComponent createComponent() {
        ERD2WInspectPage page = (ERD2WInspectPage) createPageNamed("CreateComponent");
        EOEnterpriseObject eo = (EOEnterpriseObject) page.object();
        applyCurrentUser(eo, "owner");
        return (WOComponent) page;
    }

    public WOComponent listComponents() {
        return (WOComponent) listPageNamed("ListComponent", new EODatabaseDataSource(ERXEC.newEditingContext(), "Component"));
    }

    ///People stuff 
    
    public WOComponent createPeople() {
        ERD2WInspectPage page = (ERD2WInspectPage) createPageNamed("CreatePeople");
        EOEnterpriseObject eo = (EOEnterpriseObject) page.object();
        //applyCurrentUser(eo, "owner");
        return (WOComponent) page;
    }

    public WOComponent listPeoples() {
        return (WOComponent) listPageNamed("ListPeople", new EODatabaseDataSource(ERXEC.newEditingContext(), "People"));
    }

    /// Framework stuff 
    
    public WOComponent createFramework() {
        ERD2WInspectPage page = (ERD2WInspectPage) createPageNamed("CreateFramework");
        EOEnterpriseObject eo = (EOEnterpriseObject) page.object();
        applyCurrentUser(eo, "owner");
        return (WOComponent) page;
    }

    public WOComponent listFrameworks() {
        return (WOComponent) listPageNamed("ListFramework", new EODatabaseDataSource(ERXEC.newEditingContext(), "Framework"));
    }

    /// Requirement stuff
    
    public WOComponent createRequirement() {
        ERD2WInspectPage page = (ERD2WInspectPage) createPageNamed("CreateRequirement");
        EOEnterpriseObject eo = (EOEnterpriseObject) page.object();
        applyCurrentUser(eo, "originator");
        applyCurrentUser(eo, "owner");
        return (WOComponent) page;
     }

    public WOComponent listMyRequirements() {
    	EOEditingContext ec = ERXEC.newEditingContext();
		ec.lock();
		try {
	        NSArray array = Requirement.clazz.myRequirementsWithUser(ec, People.clazz.currentUser(ec));
	        EOArrayDataSource ds = Requirement.clazz.newArrayDataSource(ec);
	        ds.setArray(array);
	        return (WOComponent) listPageNamed("ListMyRequirement", ds);
		} finally {
			ec.unlock();
		}
    }

    public WOComponent queryRequirements() {
        return (WOComponent) pageForConfigurationNamed("QueryRequirement");
    }

    /// Test item stuff

    public WOComponent createTestItem() {
        ERD2WInspectPage page = (ERD2WInspectPage) createPageNamed("CreateTestItem");
        EOEnterpriseObject eo = (EOEnterpriseObject) page.object();
        applyCurrentUser(eo, TestItem.Key.OWNER);
        return (WOComponent) page;
    }
    
    public WOComponent createBugFromTestItem(TestItem testItem) {
        EOEditingContext peer = ERXEC.newEditingContext(testItem.editingContext().parentObjectStore());
        EditPageInterface epi = null;
        peer.lock();
        try {
            testItem = (TestItem) testItem.localInstanceIn(peer);
            People user = People.clazz.currentUser(peer);
            Component component = testItem.component();

            Bug bug = (Bug) Bug.clazz.createAndInsertObject(peer);
            testItem.setState(TestItemState.BUG);

            bug.setTextDescription("[From Test #" + testItem.primaryKey()+"]");
            bug.addTestItem(testItem);
            bug.updateOriginator(user);
            bug.updateComponent(component);

            epi=(EditPageInterface)createPageNamed("CreateBug");
            epi.setObject(bug);
            epi.setNextPage(session().context().page());
        } finally {
            peer.unlock();
        }
         return (WOComponent)epi;        
    }

    public WOComponent createTestItemFromBug(Bug bug) {
        EOEditingContext peer = ERXEC.newEditingContext(bug.editingContext().parentObjectStore());
        peer.lock();
        try {
            bug = (Bug) bug.localInstanceIn(peer);
            TestItem testItem = (TestItem) TestItem.clazz.createAndInsertObject(peer);
            testItem.updateComponent(bug.component());
            String description = ERXLocalizer.currentLocalizer().localizedTemplateStringForKeyWithObject("CreateTestItemFromReq.templateString", this);
            testItem.setTextDescription(description);
            bug.addTestItem(testItem);
            EditPageInterface epi=(EditPageInterface)createPageNamed("CreateTestItemFrom" + bug.entityName() );
            epi.setObject(testItem);
            epi.setNextPage(session().context().page());
            return (WOComponent)epi;
        } finally {
            peer.unlock();
        }
    }

    public WOComponent listMyTestItems() {
    	EOEditingContext ec = ERXEC.newEditingContext();
		ec.lock();
		try {
	        NSArray array = TestItem.clazz.unclosedTestItemsWithUser(ec, People.clazz.currentUser(ec));
	        EOArrayDataSource ds = TestItem.clazz.newArrayDataSource(ec);
	        ds.setArray(array);
	        return (WOComponent) listPageNamed("ListMyTestItem", ds);
		} finally {
			ec.unlock();
		}
    }

    public WOComponent queryTestItems() {
        return (WOComponent) pageForConfigurationNamed("QueryTestItem");
    }

    /// Release stuff 

    public WOComponent trackDefaultRelease() {
        EOEditingContext ec = session().defaultEditingContext();
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
        EOEditingContext ec = session().defaultEditingContext();
        EOEnterpriseObject user = currentUser(ec);
        QueryPageInterface qpi = (QueryPageInterface) D2W.factory().pageForConfigurationNamed("TrackRelease", session());
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

    /// Bug stuff

    public WOComponent createBug() {
        ERD2WInspectPage page = (ERD2WInspectPage) createPageNamed("CreateBug");
        Bug bug = (Bug) page.object();
        applyCurrentUser(bug, "originator");
        applyCurrentUser(bug, "owner");
        return (WOComponent) page;
    }


    public WOComponent editBug(Bug bug) {
        EditPageInterface epi = editPageNamed("EditBug", bug);
        epi.setNextPage(homePage());
        return (WOComponent)epi;
    }

    public WOComponent listRecentBugs() {
        EODatabaseDataSource ds = new EODatabaseDataSource(ERXEC.newEditingContext(), "Bug");
        ds.fetchSpecification().setIsDeep(false);
        WOComponent page = (WOComponent) listPageNamed("ListRecentBug", ds);
        return page;
    }

    public WOComponent listMyBugs() {
        EOEditingContext ec = ERXEC.newEditingContext();
        ec.lock();
        try {
        	EODatabaseDataSource ds  = Bug.clazz.newDatabaseDataSource(ec);
        	NSDictionary bindings = new NSDictionary(new Object[] { currentUser(ec) }, new Object[] { "user" });
        	EOFetchSpecification fs = Bug.clazz.fetchSpecificationNamed(ec, "bugsOwned").fetchSpecificationWithQualifierBindings(bindings);
            ds.setFetchSpecification(fs);
       		ds.fetchSpecification().setIsDeep(false);

        	return (WOComponent) listPageNamed("ListMyBug", ds);

        } finally {
        	ec.unlock();
        }
    }

    public WOComponent queryBugs() {
    	ERD2WQueryPage page = (ERD2WQueryPage) pageForConfigurationNamed("QueryBug");
    	page.setQueryMatchForKey(new NSArray(Priority.CRITICAL), EOQualifier.QualifierOperatorEqual.name(), "priority");
    	page.setQueryMatchForKey(new NSArray(People.clazz.currentUser(session().defaultEditingContext())), EOQualifier.QualifierOperatorEqual.name(), "originator");
    	page.setShowResults(true);
    	return page;
    }
    
    public WOComponent findBugs(String string) {
        NSArray a=NSArray.componentsSeparatedByString(string," ");
        NSMutableArray quals=new NSMutableArray();
        for (Enumeration e=a.objectEnumerator(); e.hasMoreElements();) {
            String s=(String)e.nextElement();
            try {
                Integer i=new Integer(s);
                quals.addObject(new EOKeyValueQualifier("id", EOQualifier.QualifierOperatorEqual, i));

            } catch (NumberFormatException ex) {}
        }
        EOOrQualifier or=new EOOrQualifier(quals);
        EODatabaseDataSource ds=new EODatabaseDataSource(session().defaultEditingContext(), "Bug");
        EOFetchSpecification fs=new EOFetchSpecification("Bug",or,null);
        ds.setFetchSpecification(fs);
        NSArray bugs = ds.fetchObjects();
        WOComponent result;
        if (bugs != null && bugs.count() == 1) {
            InspectPageInterface ipi = D2W.factory().inspectPageForEntityNamed("Bug", session());
            ipi.setObject((EOEnterpriseObject) bugs.objectAtIndex(0));
            ipi.setNextPage(currentPage());
            result = (WOComponent) ipi;
        } else {
            ds.setFetchSpecification(fs);
            ListPageInterface lpi = (ListPageInterface) D2W.factory().listPageForEntityNamed("Bug", session());
            lpi.setDataSource(ds);
            lpi.setNextPage(currentPage());
            result = (WOComponent) lpi;
        }
        return result;            
    }
}
