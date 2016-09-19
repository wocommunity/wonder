package er.imadaptor;

import java.util.LinkedList;
import java.util.List;

public abstract class AbstractInstantMessenger implements IInstantMessenger {
	private String _screenName;
	private String _password;
	private List<IMessageListener> _listeners;

	public AbstractInstantMessenger(String screenName, String password) {
		_screenName = screenName;
		_password = password;
		_listeners = new LinkedList<>();
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
		for (IMessageListener listener : _listeners) {
			listener.messageReceived(this, buddyName, message);
		}
	}
}
