package er.imadaptor;

import java.util.ArrayList;
import java.util.List;

public abstract class AbstractInstantMessenger implements IInstantMessenger {
  private String _screenName;
  private String _password;
  private List _listeners;

  public AbstractInstantMessenger(String screenName, String password) {
    _screenName = screenName;
    _password = password;
    _listeners = new ArrayList();
  }

  public String getScreenName() {
    return _screenName;
  }

  public String getPassword() {
    return _password;
  }

  public void addMessageListener(IMessageListener messageListener) {
    _listeners.add(messageListener);
  }

  public void removeMessageListener(IMessageListener messageListener) {
    _listeners.remove(messageListener);
  }

  protected void fireMessageReceived(String buddyName, String message) {
    int listenerCount = _listeners.size();
    for (int i = 0; i < listenerCount; i++) {
      IMessageListener listener = (IMessageListener) _listeners.get(i);
      listener.messageReceived(this, buddyName, message);
    }
  }
}
