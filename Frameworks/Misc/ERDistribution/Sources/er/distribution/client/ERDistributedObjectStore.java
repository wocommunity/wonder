package er.distribution.client;

import java.lang.reflect.Field;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.webobjects.eocontrol.EOGlobalID;
import com.webobjects.eodistribution.client.EODistributedObjectStore;
import com.webobjects.eodistribution.client.EODistributionChannel;
import com.webobjects.eodistribution.client.EOHTTPChannel;
import com.webobjects.eodistribution.common._EOReferenceRecordingCoder;

import er.distribution.client.exceptions.ServerConnectionException;
import er.distribution.common.ERReferenceRecordingCoder;

/**
 * Adds some new functionality
 *
 */
public class ERDistributedObjectStore extends EODistributedObjectStore {

	private static final Logger log = LoggerFactory.getLogger(ERDistributedObjectStore.class);

	public ERDistributedObjectStore(EODistributionChannel channel) {
		super(channel);
		
		try {
			_EOReferenceRecordingCoder newCoder = new ERReferenceRecordingCoder(false);
			newCoder.setDelegate(this);
			Field coderField = EODistributedObjectStore.class.getDeclaredField("_coder");
			coderField.setAccessible(true);
			coderField.set(this, newCoder);
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	public static ERDistributedObjectStore connectToServer() throws ServerConnectionException {
		return connectToServer(new ERHTTPChannel());
	}
	
	public static ERDistributedObjectStore connectToServer(EODistributionChannel channel) throws ServerConnectionException {
		log.info("Will try to establish a connection to application server at: " + channel.connectionDictionary().get(EOHTTPChannel.ApplicationURLKey));
		try {
			channel.establishConnection(); // may fail with exception
					
			ERDistributedObjectStore remoteObjectStore = new ERDistributedObjectStore(channel);
					
			log.info("Succesfully established a connection with the application server. SessionID=" + remoteObjectStore.sessionID());

			return remoteObjectStore;
		} catch (Exception e) {
			log.error(e.getMessage(), e);
			if (e.getMessage().indexOf("No instance available") != -1) {
				throw new ServerConnectionException("The server is not available.\nPlease wait 30 seconds and try again.");
			} else if (e.getMessage().indexOf("The requested application was not found on this server") != -1) {
				throw new ServerConnectionException("The server is not available.\nPlease wait 30 seconds and try again.");
			} else {
				throw new ServerConnectionException("Unable to connect to server: " + e.getMessage(), e);
			}
		}
	}

	/**
	 * 
	 * @param username
	 * @param password
	 * @return the globalID for the User object which can be used to retrieve the User after authenticating
	 */
	public EOGlobalID login(String username, String password) {
		return (EOGlobalID) invokeStatelessRemoteMethod("clientSideRequestLogin", username, password);
	}
	
	public void terminateSessionOnServer() {
		try {
			distributionChannel().setDelegate(null); // clear delegate so if an error occurs with the following call it will be caught here
			invokeStatelessRemoteMethodWithKeyPath(null, "clientSideRequestHandleExit", null, null);
		} catch (Exception e) {
			log.error("Unable to terminate session: " + e.getMessage(), e);
		}
	}
	
	public String sessionID() {
		EOHTTPChannel channel = (EOHTTPChannel) distributionChannel();
		Field sessionIdField;
		try {
			sessionIdField = EOHTTPChannel.class.getDeclaredField("_sessionID");
			sessionIdField.setAccessible(true);
			return (String) sessionIdField.get(channel);
		} catch (Exception e) {
			return null;
		}
	}
	
	// CONVENIENCE METHODS
	public Object invokeStatelessRemoteMethod(final String methodName) {
		return invokeStatelessRemoteMethod(methodName, new Object[0]);
	}
	
	public Object invokeStatelessRemoteMethod(final String methodName, Object... arguments) {
		Class<?>[] argClasses = classesForObjects(arguments);
		return invokeStatelessRemoteMethodWithKeyPath("session", methodName, argClasses, arguments);
	}
	
	public static Class<?>[] classesForObjects(final Object[] arguments) {
		final Class<?>[] argumentTypes = new Class[arguments.length];
		for(int i = 0 ; i < arguments.length ; i++)
			argumentTypes[i] = arguments[i].getClass();
		return argumentTypes;
	}
	
}
