package er.ajax.example2;

import com.webobjects.appserver.WOContext;
import com.webobjects.appserver.WORequest;
import com.webobjects.appserver.WOResponse;

import er.ajax.AjaxResponseAppender;
import er.ajax.AjaxUtils;

public class NotificationResponseAppender extends AjaxResponseAppender {

  @Override
  public void appendToResponse(WOResponse response, WOContext context) {
    Session session = (Session) context.session();
    if (session.hasNotifications()) {
      WORequest request = context.request();
      AjaxUtils.appendScriptHeaderIfNecessary(request, response);
      response.appendContentString("notificationsUpdate();");
      AjaxUtils.appendScriptFooterIfNecessary(request, response);
    }
  }

}
