package er.ajax.example2;

import com.webobjects.foundation.NSArray;
import com.webobjects.foundation.NSMutableArray;

import er.extensions.appserver.ERXSession;

public class Session extends ERXSession {
  private static final long serialVersionUID = 1L;

  private NSMutableArray<String> _notifications;
  
  public Session() {
    setStoresIDsInCookies(true);
    setStoresIDsInURLs(false);
    _notifications = new NSMutableArray<>();
  }
  
  public void addNotification(String notification) {
    _notifications.addObject(notification);
  }
  
  public boolean hasNotifications() {
    return _notifications.count() > 0;
  }
  
  public NSArray<String> notifications() {
    return _notifications;
  }
  
  public void clearNotifications() {
    _notifications.removeAllObjects();
  }
  
  @Override
  public void sleep() {
    super.sleep();
  }
}
