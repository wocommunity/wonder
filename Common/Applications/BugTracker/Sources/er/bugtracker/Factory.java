package er.bugtracker;

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

import er.directtoweb.ERD2WFactory;
import er.directtoweb.ERD2WInspectPage;
import er.extensions.ERXEC;
import er.extensions.ERXExtensions;
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
        EODatabaseDataSource ds = new EODatabaseDataSource(ERXEC.newEditingContext(), "Requirement");
        return (WOComponent) listPageNamed("ListRequirement", ds);
    }

    public WOComponent queryRequirements() {
        return (WOComponent) pageForConfigurationNamed("QueryRequirement");
    }

    /// Test item stuff

    public WOComponent createTestItem() {
        ERD2WInspectPage page = (ERD2WInspectPage) createPageNamed("CreateTestItem");
        EOEnterpriseObject eo = (EOEnterpriseObject) page.object();
        applyCurrentUser(eo, "owner");
        return (WOComponent) page;
    }

    public WOComponent listMyTestItems() {
        EODatabaseDataSource ds = new EODatabaseDataSource(ERXEC.newEditingContext(), "TestItem");
        return (WOComponent) listPageNamed("ListMyTestItem", ds);
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
        EditPageInterface epi = (EditPageInterface) editPageForEntityNamed("Bug", session());
        epi.setObject(bug);
        epi.setNextPage(homePage());
        return (WOComponent)epi;
    }

    public WOComponent listRecentBugs() {
        EODatabaseDataSource ds = new EODatabaseDataSource(ERXEC.newEditingContext(), "Bug");
        WOComponent page = (WOComponent) listPageNamed("ListRecentBug", ds);
        return page;
    }

    public WOComponent listMyBugs() {
        EOEditingContext ec = ERXEC.newEditingContext();
        ec.lock();
        try {
            EODatabaseDataSource ds  = new EODatabaseDataSource(ec, "Bug");
            NSDictionary bindings = new NSDictionary(new Object[] { currentUser(ec) }, new Object[] { "user" });
            EOEntity bugEntity = EOUtilities.entityNamed(ec, "Bug");
            EOFetchSpecification fs = bugEntity.fetchSpecificationNamed("bugsOwned").fetchSpecificationWithQualifierBindings(bindings);

            return (WOComponent) listPageNamed("ListMyBug", ds);

        } finally {
            ec.unlock();
        }
    }

    public WOComponent queryBugs() {
        return (WOComponent) pageForConfigurationNamed("QueryBug");
    }

}
