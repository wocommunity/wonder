package er.ajax.example2;

import com.webobjects.appserver.WOContext;
import com.webobjects.appserver.WOResponse;

import er.ajax.AjaxResponse;
import er.ajax.AjaxUtils;
import er.ajax.example2.util.StringUtils;
import er.extensions.appserver.ERXApplication;

public class Application extends ERXApplication {
  public static void main(String[] argv) {
    ERXApplication.main(argv, Application.class);
  }

  public Application() {
    AjaxResponse.addAjaxResponseAppender(new NotificationResponseAppender());
  }

  @Override
  public WOResponse handleException(Exception exception, WOContext context) {
    WOResponse response;
    if (context != null && AjaxUtils.isAjaxRequest(context.request())) {
      response = createResponseInContext(context);
      response.appendContentString(StringUtils.toErrorString(exception));
    }
    else {
      response = super.handleException(exception, context);
    }
    return response;
  }

  @Override
  public WOResponse handleSessionRestorationErrorInContext(WOContext context) {
    WOResponse response;
    if (context != null && AjaxUtils.isAjaxRequest(context.request())) {
      response = createResponseInContext(context);
      String sessionExpiredUrl = context.directActionURLForActionNamed("sessionExpired", null); 
      response.appendContentString("<script>document.location.href='" + sessionExpiredUrl + "';</script>");
    }
    else {
      response = super.handleSessionRestorationErrorInContext(context);
    }
    return response;
  }
}
