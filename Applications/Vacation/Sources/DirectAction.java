
import com.webobjects.foundation.*;
import com.webobjects.appserver.*;
import com.webobjects.eocontrol.*;
import com.webobjects.eoaccess.*;


/** Handles direct action requests for the WOApp<BR>
* Added a custom direct action for quick login and user editting.
*/
public class DirectAction extends WODirectAction {

    /** Constructor */
    public DirectAction(WORequest aRequest) {
        super(aRequest);
    }

    // automatic login if the user has cookies set with their password, otherwise it returns the main page with the login panel
    public WOActionResults defaultAction() {

        Session session = (Session) session();
        WOComponent pageToReturn = (Main) pageWithName("Main");

        if (session.loginViaCookies()) {
            if (session.user().type().equals("Admin")) {
                pageToReturn = (PersonSummary) pageWithName("PersonSummary");
                
                String editPersonString = (String)request().formValueForKey("edit");
                if (editPersonString!=null) {
                    Person editPerson = session.findPerson(editPersonString);
                    if (editPerson!=null) {
                        pageToReturn = (PersonEditor) pageWithName("PersonEditor");
                        ((PersonEditor) pageToReturn).setPerson(editPerson);
                    }
                }
            }
            else pageToReturn = (UserInfo) pageWithName("UserInfo");
        }

        return pageToReturn;
    }

}
