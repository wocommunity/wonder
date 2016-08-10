package er.bugtracker.components;

import com.webobjects.appserver.WOComponent;
import com.webobjects.appserver.WOContext;
import com.webobjects.appserver.WOCookie;
import com.webobjects.directtoweb.D2W;
import com.webobjects.eocontrol.EOEditingContext;
import com.webobjects.foundation.NSTimestamp;

import er.bugtracker.Factory;
import er.bugtracker.People;
import er.bugtracker.Session;
import er.extensions.crypting.ERXCrypto;
import er.extensions.foundation.ERXUtilities;

public class LoginPanel extends WOComponent {

    public LoginPanel(WOContext context) {
        super(context);
    }

    public String username;
    public String password;
    public boolean validated;
    public boolean rememberPassword;
    public String errorMessage;

    private WOComponent _nextPage;
    public WOComponent nextPage() {
        if ((_nextPage == null) && (_nextPageCallback != null)) {
            _nextPage = (WOComponent)_nextPageCallback.invoke(session());
        }
        return _nextPage;
    }

    private ERXUtilities.Callback _nextPageCallback;
    public ERXUtilities.Callback nextPageCallback() { return _nextPageCallback; }
    public void setNextPageCallback(ERXUtilities.Callback value) {
        // delay the next page creation as long as possible because Main's constructor calls refresh which
        // will do nothing if the sesion's user is null, which it will be until deep in the defaultPage action
        // below.
        _nextPageCallback = value;
        _nextPage = null;
    }

    public WOComponent signUp() {
    	return Factory.bugTracker().signUp();
    }
    
    public WOComponent login() {
        EOEditingContext editingContext;
        
        Session session = (Session)session();
        editingContext = session.defaultEditingContext();

        if (!validated && false) {// captcha disabled for now
            errorMessage="The captcha is wrong!";
            return null;
        }

        if (username==null || password==null) {
            errorMessage="Please specify both fields!";
            return null;
        }
        
        People  userObject = People.clazz.userWithUsernamePassword(editingContext, username, password);
        if(userObject == null) {
            errorMessage="Sorry login incorrect!";
            return null;
        }

        if (!userObject.isActive()) {
            errorMessage="Sorry your account is inactive!";
            return null;
        }
        session.setUser(userObject);
        boolean isAdmin = userObject.isAdmin();
        D2W.factory().setWebAssistantEnabled(isAdmin);
        String encryptedIDPrimaryKey = ERXCrypto.crypterForAlgorithm(ERXCrypto.BLOWFISH).encrypt(userObject.primaryKey());
        WOCookie loginCookie=new WOCookie("BTL", rememberPassword ?  encryptedIDPrimaryKey : "-");
        loginCookie.setExpires(NSTimestamp.DistantFuture);
        loginCookie.setPath("/");
        context().response().addCookie(loginCookie);
        WOComponent nextPage = nextPage();
        return ((nextPage == null) ? pageWithName("HomePage") : nextPage);
    }
}
