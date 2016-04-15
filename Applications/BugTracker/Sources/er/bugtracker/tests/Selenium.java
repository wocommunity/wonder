package er.bugtracker.tests;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.webobjects.appserver.WOActionResults;
import com.webobjects.appserver.WOCookie;
import com.webobjects.appserver.WORequest;
import com.webobjects.appserver.WOResponse;
import com.webobjects.eoaccess.EOUtilities;
import com.webobjects.eocontrol.EOEditingContext;
import com.webobjects.eocontrol.EOKeyValueQualifier;
import com.webobjects.eocontrol.EOQualifier;
import com.webobjects.foundation.NSArray;
import com.webobjects.foundation.NSTimestamp;

import er.bugtracker.Framework;
import er.bugtracker.People;
import er.bugtracker.Session;
import er.directtoweb.components.strings.ERDEditHTML;
import er.extensions.eof.ERXEC;
import er.extensions.eof.ERXEOControlUtilities;
import er.extensions.foundation.ERXProperties;
import er.extensions.foundation.ERXUtilities;
import er.selenium.SeleniumAction;

public class Selenium extends SeleniumAction  {
	private final static Logger log = LoggerFactory.getLogger(Selenium.class);
	
	public static final String USERNAME = "sel_name";
	public static final String PASSWORD = "sel_pass";
	private static final String NAME = "SeleniumName";
	
	private void deleteTestPeople() {
	    People people = People.clazz.userWithUsernamePassword(session().defaultEditingContext(), USERNAME, PASSWORD);
	    if (people != null) {
	        EOEditingContext ec = ERXEC.newEditingContext();
	        ec.lock();
	        try {
	            people = (People) EOUtilities.localInstanceOfObject(ec, people);
                ERXUtilities.deleteObjects(ec, people.allBugs());
                people.delete();
	            // ERXEOAccessUtilities.deleteRowsDescribedByQualifier(ec, Bug.ENTITY, qualifier)
	            /* TODO: this should be done in BTBusinessLogic */
	            EOKeyValueQualifier frameworkQualifier = new EOKeyValueQualifier(Framework.Key.OWNER, EOQualifier.QualifierOperatorEqual, people);
	            assert(frameworkQualifier != null);
	            NSArray frameworks = ERXEOControlUtilities.objectsWithQualifier(ec, 
	                    Framework.class.getSimpleName(), frameworkQualifier, null, false);
	            ERXUtilities.deleteObjects(ec, frameworks);

	            ec.saveChanges();
	        } finally {
	            ec.unlock();
	        }
	        log.debug("People {} deleted", USERNAME);
	    }
	}
	
	private People addTestPeople(boolean isAdmin) {
		EOEditingContext ec = ERXEC.newEditingContext();
		People people = People.clazz.createAndInsertObject(ec);
		people.setName(NAME);
		people.setLogin(USERNAME);
		people.setPassword(PASSWORD);
		people.setIsAdmin(isAdmin);
		ec.saveChanges();
		log.debug("People {} added", USERNAME);
		return people;
	}
	
	private WOActionResults ensureTestPeopleAreLoggedIn() {
		Session session = (Session)session();
        session.setLanguage("English");
		People people = People.clazz.userWithUsernamePassword(session.defaultEditingContext(), USERNAME, PASSWORD);
		session.setUser(people);
		return success();
	}

	public Selenium(WORequest request) {
		super(request);
	}
	
	public WOActionResults resetSessionAction() {
		WOCookie dummyCookie = new WOCookie("BTL", "dummy");
		dummyCookie.setPath("/");
		dummyCookie.setDomain(null);  // Let the browser set the domain
		dummyCookie.setExpires(new NSTimestamp().timestampByAddingGregorianUnits(0, -2, 0, 0, 0, 0));
		
		WOResponse response = success();
		response.addCookie(dummyCookie);
        session().terminate();
        return response;
	}

    public WOActionResults ensureTestAdminAction() {
        deleteTestPeople();
        addTestPeople(true);
        return ensureTestPeopleAreLoggedIn();
    }

    public WOActionResults ensurePeopleSetupAction() {
    	Session session = (Session)session();
        People people = People.clazz.userWithUsernamePassword(session.defaultEditingContext(), "user100", "user");
        assert people != null;
        people.setIsActive(false);
        
        people = People.clazz.userWithUsernamePassword(session.defaultEditingContext(), "user101", "user");
        assert people != null;
        people.setIsActive(true);
        people.setIsAdmin(false);
        people.setIsEngineering(false);
        people.setIsCustomerService(true);
        session.defaultEditingContext().saveChanges();
        
        return ensureTestAdminAction();
    }
    
    public WOActionResults deleteTestPeopleAction() {
    	deleteTestPeople();
    	return success();
    }
    
    public WOActionResults disableTinyMCEAction() {
    	ERXProperties.setStringForKey("nonexisting.js", ERDEditHTML.FILE_NAME_PROPERTY);
    	return success();
    }
    
    public WOActionResults enableTinyMCEAction() {
    	System.clearProperty(ERDEditHTML.FILE_NAME_PROPERTY);
    	return success();
    }
}
