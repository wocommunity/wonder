import com.webobjects.foundation.*;
import com.webobjects.appserver.*;
import com.webobjects.eocontrol.*;
import com.webobjects.eoaccess.*;

import java.util.*;

/** The session object created for each user .
* Used to login or check for a user.
* Keeps a reference to the user logged in, for use in WO Components
*/
public class Session extends WOSession {

    /** This variable is a pointer to the current user logged in */
    protected Person user;
    protected Integer yearIterator;
    protected Integer selectedYear;


    public NSArray groups;
    
    public NSArray groups() {
        if (groups==null) {
            groups = EOUtilities.objectsForEntityNamed(defaultEditingContext(), "Group");
        }
        return groups;
    }

    public Session() {
        super();
        selectedYear = new Integer((new GregorianCalendar()).get(GregorianCalendar.YEAR));
    }
    
    public Person user() {
        return user;
    }
    
    public void setUser(Person newUser) {
        user = newUser;
        if (user!=null) user.selectedYear = new Integer(selectedYear.intValue());
    }

    public boolean loginViaCookies() {

        NSArray users = new NSArray();

        String login = context().request().cookieValueForKey("login_" + context().request().applicationName());
        String password = context().request().cookieValueForKey("password_" + context().request().applicationName());
        

        try {
            if (login!=null && password!=null) {
            NSDictionary loginBindings = new NSDictionary(new Object[] {login,password},new Object[] {"userID","password"});
            users = EOUtilities.objectsMatchingValues(defaultEditingContext(), "Person", loginBindings);
            }
        }
        catch (Exception e) {
            System.out.println(e);
        }
        
        if (users.count()>0) {
            setUser((Person) users.objectAtIndex(0));
            return true;
        }

        return false;
        
    }

    /** Use to find a user by a userID.  If the user is found, they are returned, else the method returns null.
        @param userID user's userID (login name)
        */
    public Person findPerson(String userID) {
        Person personFound = null;
        NSDictionary bindings = new NSDictionary(new Object[] {userID}, new Object[] {"userID"});

        try
        {
            personFound = (Person) EOUtilities.objectMatchingValues(defaultEditingContext(),"Person",bindings);
        }
        catch (Exception e)
        {
            System.out.println(e);
        }

        return personFound;
    }

}
