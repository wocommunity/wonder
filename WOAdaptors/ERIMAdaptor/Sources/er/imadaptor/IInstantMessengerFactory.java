package er.imadaptor;

/**
 * InstantMessengerFactory is the factory interface that InstantMessengerAdaptor uses to create instances of an
 * InstantMessenger.
 * 
 * @author mschrag
 */
public interface IInstantMessengerFactory {
	/**
	 * Returns an IInstantMessenger instance.
	 * 
	 * @param screenName
	 *            the screen name to login with
	 * @param password
	 *            the password to login with
	 * @return an IInstantMessenger instance
	 */
	public IInstantMessenger createInstantMessenger(String screenName, String password);
}
