package er.extensions.appserver;

import java.lang.reflect.Field;
import java.net.ServerSocket;
import java.net.URL;

import com.webobjects.appserver.WOApplication;
import com.webobjects.appserver._private.WOProperties;
import com.webobjects.foundation.NSBundle;
import com.webobjects.foundation.NSDictionary;

/**
 * ERXSecureDefaultAdaptor is a subclass of ERXDefaultAdaptor that
 * enables SSL support in the WODefaultAdaptor so that it can be
 * assigned as an application additionalAdaptor and used in
 * DirectConnect mode.
 *  
 * @author mschrag
 */
public class ERXSecureDefaultAdaptor extends ERXDefaultAdaptor {
	/**
	 * Constructs an ERXSecureAdaptor.
	 * 
	 * @param name the name of the adaptor
	 * @param parameters the adaptor parameters (see WODefaultAdaptor's)
	 */
	public ERXSecureDefaultAdaptor(String name, NSDictionary parameters) {
		super(name, parameters, true);
		
		// This is completely lame, but there's no API to get back to the secure socket to determine what
		// port number was selected!
		if (parameters != null && Integer.valueOf(0).equals(parameters.objectForKey(WOProperties._PortKey))) {
			try {
				Field listenSocketField = Class.forName("com.webobjects.appserver._private.WOClassicAdaptor").getDeclaredField("_listenSocket");
				listenSocketField.setAccessible(true);
				ServerSocket socket = (ServerSocket) listenSocketField.get(this);
				int localPort = socket.getLocalPort();
				ERXApplication.erxApplication()._setSslPort(localPort);
			}
			catch (Throwable e) {
				throw new RuntimeException("Failed to properly initialize the SSL socket.", e);
			}
		}
	}

	/**
	 * Verifies that your SSL configuration is correct.
	 */
	public static void checkSSLConfig() {
		String s = NSBundle.mainBundle().resourcePathForLocalizedResourceNamed("adaptorsslpassphrase", null);
		if (s == null) {
			throw new IllegalStateException("You must create an executable named 'adaptorsslpassphrase' in your Resources folder that can return the passphrase for your KeyStore. As an example, you can create an executable shell script that simply contains:\necho yourpassword");
		}
		URL keyStorePath = WOApplication.application().createResourceManager().pathURLForResourceNamed("adaptorssl.key", null, null);
		if (keyStorePath == null) {
			throw new IllegalStateException("You must create a KeyStore named 'adaptorssl.key' in your Resources folder. Run 'keytool -genkey -alias WebObjects -keyalg RSA -keystore /path/to/Resources/adaptorssl.key' and make sure the password matches what 'adaptorsslpassphrase' returns.");
		}
	}
}
