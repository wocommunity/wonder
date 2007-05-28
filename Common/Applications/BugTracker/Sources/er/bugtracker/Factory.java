package er.bugtracker;

import com.webobjects.appserver.WOApplication;
import com.webobjects.appserver.WOComponent;
import com.webobjects.appserver.WOSession;
import com.webobjects.directtoweb.D2W;
import com.webobjects.directtoweb.D2WPage;
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
import com.webobjects.eocontrol.EOQualifier;
import com.webobjects.foundation.NSArray;
import com.webobjects.foundation.NSKeyValueCoding;

import er.directtoweb.ERD2WFactory;
import er.directtoweb.ERD2WInspectPage;
import er.directtoweb.ERD2WQueryPage;
import er.directtoweb.ERDQueryPageInterface;
import er.extensions.EOEnterpriseObjectClazz;
import er.extensions.ERXEC;
import er.extensions.ERXExtensions;
import er.extensions.ERXLocalizer;
import er.extensions.ERXPrimaryKeyListQualifier;
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
        if (nextPage instanceof D2WPage) {
            D2WPage page = (D2WPage) nextPage;
            page.setNextPage(currentPage());
        }
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

    protected ListPageInterface listPageNamed(String name, EOEnterpriseObjectClazz clazz) {
        EOEditingContext ec = ERXEC.newEditingContext();
        ec.lock();
        try {
            EODataSource ds = clazz.newDatabaseDataSource(ec);
            return listPageNamed(name, ds);
        } finally {
            ec.unlock();
        }
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
        Component eo = (Component) page.object();
        applyCurrentUser(eo, Component.Key.OWNER);
        return (WOComponent) page;
    }

    public WOComponent listComponents() {
        return (WOComponent) listPageNamed("ListComponent", Component.clazz);
    }

    ///People stuff 
    
    public WOComponent createPeople() {
        ERD2WInspectPage page = (ERD2WInspectPage) createPageNamed("CreatePeople");
        EOEnterpriseObject eo = (EOEnterpriseObject) page.object();
        //applyCurrentUser(eo, "owner");
        return (WOComponent) page;
    }

    public WOComponent listPeoples() {
        return (WOComponent) listPageNamed("ListPeople", People.clazz);
    }

    /// Framework stuff 
    
    public WOComponent createFramework() {
        ERD2WInspectPage page = (ERD2WInspectPage) createPageNamed("CreateFramework");
        EOEnterpriseObject eo = (EOEnterpriseObject) page.object();
        applyCurrentUser(eo, Framework.Key.OWNER);
        return (WOComponent) page;
    }

    public WOComponent listFrameworks() {
        return (WOComponent) listPageNamed("ListFramework", Framework.clazz);
    }

    /// Requirement stuff
    
    public WOComponent createRequirement() {
        ERD2WInspectPage page = (ERD2WInspectPage) createPageNamed("CreateRequirement");
        EOEnterpriseObject eo = (EOEnterpriseObject) page.object();
        applyCurrentUser(eo, Requirement.Key.ORIGINATOR);
        applyCurrentUser(eo, Requirement.Key.OWNER);
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
    
    public WOComponent listRecentRequirements() {
        EOEditingContext ec = ERXEC.newEditingContext();
        ec.lock();
        try {
            EODatabaseDataSource ds = Requirement.clazz.newDatabaseDataSource(ec);
            EOFetchSpecification fs = Requirement.clazz.fetchSpecificationForRecentBugs();
            
            ds.setFetchSpecification(fs);
            WOComponent page = (WOComponent) listPageNamed("ListRecentRequirement", ds);
            return page;

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
            bug.addToTestItems(testItem);
            bug.setOriginator(user);
            bug.setComponent(component);

            epi=(EditPageInterface)createPageNamed("CreateBugFromTestItem");
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
            testItem.setComponent(bug.component());
            String description = ERXLocalizer.currentLocalizer().localizedTemplateStringForKeyWithObject("CreateTestItemFromReq.templateString", this);
            testItem.setTextDescription(description);
            bug.addToTestItems(testItem);
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
        EOQualifier q1 = new EOKeyValueQualifier(Bug.Key.STATE, EOQualifier.QualifierOperatorEqual, State.ANALYZE);
        EOQualifier q2 = new EOKeyValueQualifier(Bug.Key.TARGET_RELEASE, EOQualifier.QualifierOperatorEqual, Release.clazz.defaultRelease(ec));
        EOQualifier q = new EOAndQualifier(new NSArray(new Object[] { q1, q2 }));
        EODatabaseDataSource ds = new EODatabaseDataSource(ec, "Bug");
        EOFetchSpecification fs = new EOFetchSpecification("Bug", q, null);
        ds.setFetchSpecification(fs);
        ListPageInterface lpi = (ListPageInterface) pageForConfigurationNamed("GroupedBugsByUser");
        lpi.setDataSource(ds);
        return (WOComponent) lpi;
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
        ERDQueryPageInterface qpi = (ERDQueryPageInterface) pageForConfigurationNamed("QueryBugForPush", session());
        qpi.setQueryMatchForKey(new NSArray(State.BUILD), ERXPrimaryKeyListQualifier.IsContainedInArraySelectorName, Bug.Key.STATE);
        Release release = Release.clazz.defaultRelease(ec);
        if(release != null) {
            qpi.setQueryMatchForKey(new NSArray(release), ERXPrimaryKeyListQualifier.IsContainedInArraySelectorName, Bug.Key.TARGET_RELEASE);
        }
        qpi.setNextPageDelegate(new NextPageDelegate() {
            public WOComponent nextPage(WOComponent sender2) {
                QueryPageInterface qpi2 = (QueryPageInterface) sender2;
                WOComponent bugList = sender2.pageWithName("GroupedBugsByRelease");
                //bugList.takeValueForKey(qpi2.queryDataSource().fetchObjects(), "bugsInBuild");
                return bugList;
            }
        });
        return (WOComponent) qpi;
    }

    /// Bug stuff

    public WOComponent createBug() {
        ERD2WInspectPage page = (ERD2WInspectPage) createPageNamed("CreateBug");
        Bug bug = (Bug) page.object();
        applyCurrentUser(bug, Bug.Key.ORIGINATOR);
        applyCurrentUser(bug, Bug.Key.OWNER);
        return (WOComponent) page;
    }


    public WOComponent editBug(Bug bug) {
        EditPageInterface epi = editPageNamed("Edit" + bug.entityName(), bug);
        epi.setNextPage(homePage());
        return (WOComponent)epi;
    }

    public WOComponent inspectBug(Bug bug) {
        InspectPageInterface epi = inspectPageNamed("Inspect" + bug.entityName(), bug);
        epi.setNextPage(homePage());
        return (WOComponent)epi;
    }

    public WOComponent listRecentBugs() {
        EOEditingContext ec = ERXEC.newEditingContext();
        ec.lock();
        try {
            EODatabaseDataSource ds = Bug.clazz.newDatabaseDataSource(ec);
            EOFetchSpecification fs = Bug.clazz.fetchSpecificationForRecentBugs();
            
            ds.setFetchSpecification(fs);
            WOComponent page = (WOComponent) listPageNamed("ListRecentBug", ds);
            return page;

        } finally {
            ec.unlock();
        }
    }

    public WOComponent listMyBugs() {
        EOEditingContext ec = ERXEC.newEditingContext();
        ec.lock();
        try {
            EODatabaseDataSource ds  = Bug.clazz.newDatabaseDataSource(ec);
            EOFetchSpecification fs = Bug.clazz.fetchSpecificationForOwnedBugs(currentUser(ec));
            
            ds.setFetchSpecification(fs);
           
            return (WOComponent) listPageNamed("ListMyBug", ds);

        } finally {
            ec.unlock();
        }
    }

    public WOComponent queryBugs() {
    	ERD2WQueryPage page = (ERD2WQueryPage) pageForConfigurationNamed("QueryBug");
    	page.setQueryMatchForKey(new NSArray(Priority.CRITICAL), EOQualifier.QualifierOperatorEqual.name(), Bug.Key.PRIORITY);
    	page.setQueryMatchForKey(new NSArray(People.clazz.currentUser(session().defaultEditingContext())), EOQualifier.QualifierOperatorEqual.name(), Bug.Key.ORIGINATOR);
    	page.setShowResults(true);
    	return page;
    }
    
    public WOComponent findBugs(String string) {
        NSArray bugs = Bug.clazz.findBugs(session().defaultEditingContext(), string);
        WOComponent result;
        if (bugs != null && bugs.count() == 1) {
            InspectPageInterface ipi = D2W.factory().inspectPageForEntityNamed("Bug", session());
            ipi.setObject((EOEnterpriseObject) bugs.objectAtIndex(0));
            ipi.setNextPage(currentPage());
            result = (WOComponent) ipi;
        } else {
            EOArrayDataSource ds = Bug.clazz.newArrayDataSource(session().defaultEditingContext());
            ds.setArray(bugs);
            ListPageInterface lpi = (ListPageInterface) D2W.factory().listPageForEntityNamed("Bug", session());
            lpi.setDataSource(ds);
            lpi.setNextPage(currentPage());
            result = (WOComponent) lpi;
        }
        return result;            
    }
    

    public WOComponent resolveBug(Bug bug) {
        EOEditingContext peer = ERXEC.newEditingContext(bug.editingContext().parentObjectStore());
        EditPageInterface epi = null;
        peer.lock();
        try {
            bug = (Bug) bug.localInstanceIn(peer);
            bug.close();
            epi=(EditPageInterface)editPageNamed("Edit" +bug.entityName()+ "ToClose", bug);
            epi.setObject(bug);
            epi.setNextPage(currentPage());
        } finally {
            peer.unlock();
        }

        return (WOComponent)epi;
    }

    public WOComponent commentBug(Bug bug) {
        EOEditingContext peer = ERXEC.newEditingContext(bug.editingContext().parentObjectStore());
        EditPageInterface epi = null;
        peer.lock();
        try {
            bug = (Bug) bug.localInstanceIn(peer);
            epi=(EditPageInterface)editPageNamed("Edit" +bug.entityName()+ "ToComment", bug);
            epi.setObject(bug);
            epi.setNextPage(currentPage());
        } finally {
            peer.unlock();
        }

        return (WOComponent)epi;
    }

    public WOComponent reopenBug(Bug bug) {
        EOEditingContext peer = ERXEC.newEditingContext(bug.editingContext().parentObjectStore());
        EditPageInterface epi = null;
        peer.lock();
        try {
            bug = (Bug) bug.localInstanceIn(peer);
            bug.reopen();
            epi=(EditPageInterface)editPageNamed("Edit" +bug.entityName()+ "ToReopen", bug);
            epi.setObject(bug);
            epi.setNextPage(currentPage());
        } finally {
            peer.unlock();
        }

        return (WOComponent)epi;
    }


    public WOComponent rejectBug(Bug bug) {
        EOEditingContext peer = ERXEC.newEditingContext(bug.editingContext().parentObjectStore());
        EditPageInterface epi = null;
        peer.lock();
        try {
            bug = (Bug) bug.localInstanceIn(peer);
            bug.rejectVerification();
            epi=(EditPageInterface)editPageNamed("Edit" +bug.entityName()+ "ToReject", bug);
            epi.setObject(bug);
            epi.setNextPage(currentPage());
        } finally {
            peer.unlock();
        }

        return (WOComponent)epi;
    }


}
