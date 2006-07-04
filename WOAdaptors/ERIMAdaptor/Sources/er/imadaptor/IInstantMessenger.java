package er.imadaptor;

/**
 * IInstantMessenger defines the interface for an instant messenger
 * implementation to hook into the ERIMAdaptor framework.
 * 
 * @author mschrag
 */
public interface IInstantMessenger {
  /**
   * Returns the screen name of the current user.
   */
  public String getScreenName();

  /**
   * Returns whether or not this messenger is connected.
   */
  public boolean isConnected();

  /**
   * Connects to the instant messenger service.
   * 
   * @throws IMConnectionException if the connect request fails
   */
  public void connect() throws IMConnectionException;

  /**
   * Disconnects from the instant messenger service.
   */
  public void disconnect();

  /**
   * Returns whether or not the specified buddy name is online.
   * 
   * @param buddyName the name of the buddy to check for
   * @throws InstantMessengerException
   */
  public boolean isBuddyOnline(String buddyName) throws InstantMessengerException;

  /**
   * Adds a buddy to the buddy list.
   * 
   * @param buddyName the name of the buddy to add
   * @throws InstantMessengerException
   */
  public void addBuddy(String buddyName) throws InstantMessengerException;

  /**
   * Sends a message to the specified buddy.
   * 
   * @param buddyName the name of the buddy to message
   * @param message the message to send
   * @throws MessageException
   */
  public void sendMessage(String buddyName, String message) throws MessageException;

  /**
   * Adds a listener to this InstantMessenger.
   * 
   * @param messageListener the message listener to add
   */
  public void addMessageListener(IMessageListener messageListener);

  /**
   * Removes a listener from this InstantMessenger.
   * 
   * @param messageListener the message listener to remove
   */
  public void removeMessageListener(IMessageListener messageListener);
}
