package er.ajax.example2.components;

import com.webobjects.appserver.WOContext;
import com.webobjects.appserver.WOResponse;

public class NotificationsList extends AjaxWOWODCComponent {
  public String _repetitionNotification;

  public NotificationsList(WOContext context) {
    super(context);
  }

  @Override
  public void appendToResponse(WOResponse response, WOContext context) {
    super.appendToResponse(response, context);
    session().clearNotifications();
  }
}