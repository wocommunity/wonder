/* VacationComponent.java created by MishrA on Wed 26-Sep-2001 */

import com.webobjects.foundation.*;
import com.webobjects.eocontrol.*;
import com.webobjects.appserver.*;


public class VacationComponent extends WOComponent {

    protected String message;
    protected Session session;
    protected Application application;

    public VacationComponent(WOContext aContext) {
        super(aContext);
        session = (Session) session();
        application = (Application) WOApplication.application();
    }

    public void setMessage(String newMessage) {
        message = newMessage;
    }

}
