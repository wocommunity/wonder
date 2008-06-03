package er.imadaptor;

public interface IMessageListener {
	public void messageReceived(IInstantMessenger instantMessenger, String buddyName, String message);
}
