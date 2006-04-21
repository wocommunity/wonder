package er.imadaptor;

public interface IInstantMessenger {
  public String getScreenName();

  public boolean isConnected();

  public void connect() throws IMConnectionException;

  public void disconnect();

  public void addBuddy(String _buddyName) throws InstantMessengerException;

  public void sendMessage(String _buddyName, String _message) throws MessageException;

  public void addMessageListener(IMessageListener _messageListener);

  public void removeMessageListener(IMessageListener _messageListener);
}
