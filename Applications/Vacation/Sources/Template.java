
import com.webobjects.foundation.*;
import com.webobjects.appserver.*;
import com.webobjects.eocontrol.*;

/** Template is the navigation wrapper.  It uses a WOComponentContent component to embed other pages within it */
public class Template extends WOComponent {

    public Template(WOContext aContext) {
        super(aContext);
    }


    /** Logs the current user out, and terminates the user's session */
    public LogoutPage logout() {

        LogoutPage nextPage = (LogoutPage)pageWithName("LogoutPage"); 
        // Initialize your component here
        Session session = (Session) session();
        
        session.setUser(null);
        session.terminate();
        
        return nextPage;
    }

}
