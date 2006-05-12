package er.imadaptor;

/**
 * InstantMessengerFactory is the factory interface that InstantMessengerAdaptor uses 
 * to create instances of an InstantMessenger.
 * 
 * @author mschrag
 */
public interface IInstantMessengerFactory {
  /**
   * Returns an IInstantMessenger instance.
   * 
   * @param _screenName the screen name to login with
   * @param _password the password to login with
   * @return an IInstantMessenger instance
   */
  public IInstantMessenger createInstantMessenger(String _screenName, String _password);
}
