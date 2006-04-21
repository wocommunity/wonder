package er.imadaptor;

public interface IMessageListener {
  public void messageReceived(IInstantMessenger _instantMessenger, String _buddyName, String _message);
}
