package er.directtoweb;

import com.webobjects.appserver.WOContext;

/** 
 * Unless you need the special shouldProvideConfirmMessage, one should use ERD2WMessagePage instead 
 */
public class ERD2WConfirmPage extends ERD2WMessagePage {

    public ERD2WConfirmPage(WOContext context) { 
    	super(context); 
    }
}
