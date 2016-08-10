import com.gammastream.validity.GSVEOModel;
import com.gammastream.validity.GSVModel;
import com.webobjects.appserver.WOApplication;
import com.webobjects.appserver.WOContext;
import com.webobjects.appserver.WORequest;
import com.webobjects.appserver.WOResponse;
import com.webobjects.appserver.WOSession;

public class Session extends WOSession {
    public GSVEOModel gsveomodel = null;
    public GSVModel model = null;
    protected boolean isLoggedIn = false;


    public GSVEOModel gsveomodel(){
        return gsveomodel;
    }
    
    public void setGsveomodel(GSVEOModel s){
        gsveomodel = s;
    }

    public GSVModel model(){
        return model;
    }
    
    public void setModel(GSVModel s){
        model = s;
    }
    
    public boolean isLoggedIn() {
        if(((Application)WOApplication.application()).config().password()==null || ((Application)WOApplication.application()).config().password().equals(""))
            isLoggedIn=true;
        return isLoggedIn;
    }
    public void setIsLoggedIn(boolean newIsLoggedIn) {
        isLoggedIn = newIsLoggedIn;
    }

    public void takeValuesFromRequest(WORequest request, WOContext context) {
        request.setDefaultFormValueEncoding("UTF8");
        super.takeValuesFromRequest(request, context);
    }

    public void appendToResponse(WOResponse response, WOContext context) {
        response.setContentEncoding("UTF8");
        super.appendToResponse(response, context);
        response.setHeader("text/html; charset=UTF-8", "Content-Type");
    }

}
