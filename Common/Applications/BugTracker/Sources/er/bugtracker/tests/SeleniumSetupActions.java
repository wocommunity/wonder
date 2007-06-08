package er.bugtracker.tests;

import org.apache.log4j.Logger;

import com.webobjects.appserver.WOContext;
import com.webobjects.appserver.WOCookie;
import com.webobjects.appserver.WOResponse;
import com.webobjects.directtoweb.D2W;
import com.webobjects.foundation.NSArray;
import com.webobjects.eoaccess.EOUtilities;
import com.webobjects.eocontrol.EOEditingContext;
import com.webobjects.eocontrol.EOKeyValueQualifier;
import com.webobjects.eocontrol.EOQualifier;
import com.webobjects.foundation.NSBundle;
import com.webobjects.foundation.NSTimestamp;

import er.bugtracker.Bug;
import er.bugtracker.Framework;
import er.bugtracker.People;
import er.bugtracker.Session;
import er.extensions.ERXCrypto;
import er.extensions.ERXEC;
import er.extensions.ERXEOControlUtilities;
import er.extensions.ERXUtilities;
import er.selenium.SeleniumDefaultSetupActions;

public class SeleniumSetupActions extends SeleniumDefaultSetupActions {
	public final static Logger log = Logger.getLogger(SeleniumSetupActions.class);
	
	public static final String USERNAME = "sel_name";
	public static final String PASSWORD = "sel_pass";
	private static final String NAME = "SeleniumName";

	public static void deleteTestPeople(WOResponse response, WOContext context) {
		People people = People.clazz.userWithUsernamePassword(context.session().defaultEditingContext(), USERNAME, PASSWORD);
		if (people != null) {
			EOEditingContext ec = ERXEC.newEditingContext();
			people = (People) EOUtilities.localInstanceOfObject(ec, people);
			people.delete();
			ERXUtilities.deleteObjects(ec, Bug.clazz.bugsOwnedWithUser(ec, people));
			
			/* TODO: this should be done in BTBusinessLogic */
			EOKeyValueQualifier frameworkQualifier = new EOKeyValueQualifier(Framework.Key.OWNER, EOQualifier.QualifierOperatorEqual, people);
			assert(frameworkQualifier != null);
			NSArray frameworks = ERXEOControlUtilities.objectsWithQualifier(ec, 
					Framework.class.getSimpleName(), frameworkQualifier, null, false);
			ERXUtilities.deleteObjects(ec, frameworks);
			
			ec.saveChanges();
			log.debug("People " + USERNAME + " deleted");
		}
	}
	
	private static People addTestPeople(WOContext context, boolean isAdmin) {
		EOEditingContext ec = ERXEC.newEditingContext();
		People people = (People) People.clazz.createAndInsertObject(ec);
		people.setName(NAME);
		people.setLogin(USERNAME);
		people.setPassword(PASSWORD);
		people.setIsAdmin(isAdmin);
		ec.saveChanges();
		log.debug("People " + USERNAME + " added");
		return people;
	}
	
	public static void resetSession(WOResponse response, WOContext context) {
		WOCookie dummyCookie = new WOCookie("BTL", "dummy");
		dummyCookie.setPath("/");
		dummyCookie.setDomain(null);  // Let the browser set the domain
		dummyCookie.setExpires(new NSTimestamp().timestampByAddingGregorianUnits(0, -2, 0, 0, 0, 0));
		response.addCookie(dummyCookie);
		
		SeleniumDefaultSetupActions.resetSession(response, context);
	}

	public static void ensureTestPeopleAreLoggedIn(WOResponse response, WOContext context) {
		Session session = (Session) context.session();
		People people = People.clazz.userWithUsernamePassword(context.session().defaultEditingContext(), USERNAME, PASSWORD);
		session.setUser(people);
	}

	public static void ensureTestAdmin(WOResponse response, WOContext context) {
		deleteTestPeople(response, context);
		addTestPeople(context, true);
		ensureTestPeopleAreLoggedIn(response, context);
	}
	
}
