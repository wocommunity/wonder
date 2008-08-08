package er.ajax.example2.components;

import com.webobjects.appserver.WOActionResults;
import com.webobjects.appserver.WOContext;
import com.webobjects.foundation.NSTimestamp;

public class AjaxNotifications extends AjaxWOWODCPage {
  public AjaxNotifications(WOContext context) {
    super(context);
  }

  @Override
  protected boolean useDefaultComponentCSS() {
    return true;
  }

  public WOActionResults generateNotification() {
    session().addNotification("This is a notification from an Ajax request on " + new NSTimestamp());
    return null;
  }

  public WOActionResults generateException() {
    if (true) {
      throw new IllegalArgumentException("This is an exception that was thrown during an Ajax request.");
    }
    return null;
  }

  public WOActionResults generateSessionTimeout() {
    session().terminate();
    return null;
  }
}