import com.webobjects.foundation.*;
import com.webobjects.appserver.*;
import com.webobjects.eocontrol.*;
import com.webobjects.eoaccess.*;

/** First page the user sees.  Used to login in the user. */
public class Main extends VacationComponent {

    protected NSArray searchResults;

    /** Constructor */
    public Main(WOContext context) {
        super(context);
        message = (String) application.settings.objectForKey("welcomeMessage");
    }

    /** Handles user login attempts */

    public WOComponent checkLogin() {

        WOComponent nextPage = null;

        if (searchResults.count()==0) {
            message = "Sorry, invalid login or password.";
        }
        else {
            Person loginPerson = (Person) searchResults.objectAtIndex(0);
            session.setUser(loginPerson);

            if (loginPerson != null) {
                if (loginPerson.type().equals("Admin")) nextPage = this.pageWithName("PersonSummary");
                else nextPage = this.pageWithName("UserInfo");
            }
        
        }

        return nextPage;
    }
    
    public String mailto()
    {
        return "mailto:" + application.settings.objectForKey("adminEmail");
    }
}
