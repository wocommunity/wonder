import com.webobjects.foundation.*;
import com.webobjects.appserver.*;
import com.webobjects.eocontrol.*;
import com.webobjects.eoaccess.*;

/** The Person Summary page is used by the Admin users to manage users.<BR>
The summary page by default displays only users which belong to the same Group as the Admin user.  If the Admin user has no Group, then they by default they see all users.<BR>
*/
public class PersonSummary extends VacationComponent {

    protected Person person;  // iterator for the WORepetition
    /** @TypeInfo Group */
    protected EOEnterpriseObject selectedGroup;
    
    protected NSArray groups;
    protected Group group;

    public PersonSummary(WOContext context) {
        super(context);
        groups = EOUtilities.objectsForEntityNamed(session.defaultEditingContext(),"Group");
        selectedGroup = session.user.group();
    }


    /** Allows the admin user to create a new person.  This method initializes a new person object and returns the Person Editor page */
    public PersonEditor CreateNewPerson() {
        PersonEditor nextPage = (PersonEditor)pageWithName("PersonEditor"); 

        // pass person to editor page
        nextPage.setPerson(null);

        return nextPage;
    }

    /** Allows the admin user to edit an existing user.  Method sets the person object and loads the Person Editor page */
    public WOComponent editPerson() {
        WOComponent nextPage=null;
        
        if (person == session.user && person.group()!=null && person.group().parentGroup()!=null) {
            nextPage = (UserInfo) pageWithName("UserInfo");
        }
        else {
            nextPage = (PersonEditor)pageWithName("PersonEditor");
            ((PersonEditor) nextPage).setPerson(person);
        }
        return nextPage;
    }

    /** This is the default action binded to WOPopuplists, which simply causes a refetch of persons from the WODisplayGroup */
    public WOComponent refreshUsers() {
        return null;
    }

    // required because component synchronization expects a variables
    public void setPersonsList(NSArray dummyArray) {
    }

    /** @TypeInfo Person */
    public NSArray personsList() {

        NSMutableDictionary bindings = new NSMutableDictionary();

        if (selectedGroup!=null) bindings.setObjectForKey(selectedGroup,"GROUP");
        if (session.selectedYear!=null) bindings.setObjectForKey(session.selectedYear, "YEAR");

        NSArray filteredPersons = EOUtilities.objectsWithFetchSpecificationAndBindings(session.defaultEditingContext(),"Person", "fetchUserByTypeAndGroup", bindings);

        java.util.Enumeration enumerator = filteredPersons.objectEnumerator();

        while (enumerator.hasMoreElements()) {
            Person ePerson = (Person) enumerator.nextElement();
            ePerson.selectedYear = new Integer(session.selectedYear.intValue());
        }

        return filteredPersons;
    }
    
}
