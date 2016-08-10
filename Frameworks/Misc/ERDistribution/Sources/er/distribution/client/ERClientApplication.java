package er.distribution.client;

import java.io.IOException;
import java.util.prefs.Preferences;

import org.apache.log4j.Logger;

import com.webobjects.eocontrol.EOClassDescription;
import com.webobjects.eodistribution.client.EODistributionChannel;
import com.webobjects.foundation.NSArray;
import com.webobjects.foundation.NSBundle;

import er.distribution.client.exceptions.LostServerConnectionException;
import er.distribution.client.exceptions.MissingSessionException;
import er.distribution.client.exceptions.NoInstanceAvailableException;
import er.distribution.client.exceptions.RequestedApplicationNotFoundException;
import er.distribution.client.exceptions.ServerConnectionException;
import er.distribution.client.exceptions.ServerException;
import er.extensions.eof.ERXEC;
import er.extensions.logging.ERXLogger;

public abstract class ERClientApplication {

	private static final Logger log = Logger.getLogger(ERClientApplication.class);

	private Preferences userDefaults;
	private ERDistributedObjectStore remoteObjectStore;

	public ERClientApplication() {
		NSBundle mainBundle = NSBundle.mainBundle(); // causes the bundle to be loaded and evaluated (will load Properties)
		if (mainBundle == null) {
			throw new IllegalStateException("Main bundle not found");
		}
		
		userDefaults = Preferences.userNodeForPackage( getClass() );

		ERXLogger.configureLoggingWithSystemProperties();
	}
	
	protected void connectToServer() {
		try {
			remoteObjectStore = ERDistributedObjectStore.connectToServer();
			remoteObjectStore.distributionChannel().setDelegate(this);
		} catch (ServerConnectionException e) {
			handleNoInstanceAvailable(e);
		}
	
		ERXEC.setDefaultParentObjectStore(remoteObjectStore);
		
		// Fetch the class descriptions right away to avoid bugs and performance issues from doing it on demand
		NSArray<EOClassDescription> classDescriptions = (NSArray<EOClassDescription>) remoteObjectStore.invokeStatelessRemoteMethod("clientSideRequestGetClassDescriptions");
		registerClassDescriptions(classDescriptions);
	}

	/**
	 * Should return the name of the package where your EOEnterpriseObject classes are located
	 * @return the name of the package
	 */
	protected abstract String modelPackageName();
	
	/**
	 * Note: assumes your entity names and your classes' simple-names are the same.
	 * 
	 * @param classDescriptions
	 */
	protected void registerClassDescriptions(NSArray<EOClassDescription> classDescriptions) {
		for (EOClassDescription classDescription : classDescriptions) {
			String className = modelPackageName() + "." + classDescription.entityName();
			Class<?> clazz;
			try {
				clazz = Class.forName(className);
			} catch (ClassNotFoundException e) {
				throw new RuntimeException(e);
			}
			EOClassDescription.registerClassDescription(classDescription, clazz);
		}
	}
	
	public ERDistributedObjectStore distributedObjectStore() {
		return remoteObjectStore;
	}
	
	public Preferences userDefaults() {
		return userDefaults;
	}
	
	/**
	 * EODistributionChannel.Delegate
	 * Gives the delegate an opportunity to handle an I/O exception which occurred while communicating with the server. 
	 * The delegate can try to handle the exception and return a new one or null if it is able 
	 * to deal with the exception completely. If the delegate does not want to handle the exception, 
	 * it should return the exception passed as the ioException argument 
	 * (which is the exception the client throws if the delegate does not implement this method or the method is not set).
	 */
	public IOException distributionChannelShouldThrowIOException(final EODistributionChannel channel, final IOException ioException) {
		return analyzeIOException(ioException);
	}

	private IOException analyzeIOException(IOException ioException) {
		if (ioException.getMessage().indexOf("Missing Session Error") != -1) { 
			return new MissingSessionException(ioException);
		} else if (ioException.getMessage().indexOf("No instance available") != -1) {
			return new NoInstanceAvailableException(ioException);
		} else if (ioException.getMessage().indexOf("The requested application was not found on this server") != -1) {
			return new RequestedApplicationNotFoundException(ioException);
		} else {
			return ioException;
		}
	}

	public void handleLostServerConnectionException(LostServerConnectionException e) {
		if (e instanceof MissingSessionException) { 
			handleMissingSession(e);
		} else if (e instanceof NoInstanceAvailableException) {
			handleNoInstanceAvailable(e);
		} else if (e instanceof RequestedApplicationNotFoundException) {
			handleNoInstanceAvailable(e);
		}
	}

	/**
	 * Should show a message and exit the app.
	 * @param e
	 */
	protected abstract void handleNoInstanceAvailable(IOException e);

	/**
	 * Session timed out or server restarted.<br>
	 * You could show a message like: "Your session has timed out.  The application will restart." And then restart the app.
	 * @param e
	 */
	protected abstract void handleMissingSession(IOException e);

	/**
	 * EODistributionChannel.Delegate
	 * Gives the delegate an opportunity to handle an exception that occurred on the server side. 
	 * The delegate can try to handle the exception and return a new one or null if it is able 
	 * to deal with the exception completely. If the delegate does not want to handle the exception, 
	 * it should return the exception passed as the clientExceptionForServerException argument 
	 * (which is the exception the client throws if the delegate does not implement this method or the method is not set).
	 */
	public Throwable distributionChannelShouldThrowServerException(EODistributionChannel channel, 
			Throwable clientExceptionForServerException, String originalServerExceptionClassName, String originalServerExceptionMessage) {
		
		log.error(originalServerExceptionClassName + ": " + originalServerExceptionMessage, clientExceptionForServerException);
		return new ServerException(clientExceptionForServerException);
	}
	
}
