package er.imadaptor;

import java.util.ArrayList;
import java.util.List;

public abstract class AbstractInstantMessenger implements IInstantMessenger {
  private String myScreenName;
  private String myPassword;
  private List myListeners;

  public AbstractInstantMessenger(String _screenName, String _password) {
    myScreenName = _screenName;
    myPassword = _password;
    myListeners = new ArrayList();
  }

  public String getScreenName() {
    return myScreenName;
  }

  public String getPassword() {
    return myPassword;
  }

  public void addMessageListener(IMessageListener _messageListener) {
    myListeners.add(_messageListener);
  }

  public void removeMessageListener(IMessageListener _messageListener) {
    myListeners.remove(_messageListener);
  }

  protected void fireMessageReceived(String _buddyName, String _message) {
    int listenerCount = myListeners.size();
    for (int i = 0; i < listenerCount; i++) {
      IMessageListener listener = (IMessageListener) myListeners.get(i);
      listener.messageReceived(this, _buddyName, _message);
    }
  }
}
