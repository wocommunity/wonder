package er.distribution.client;

import java.io.IOException;
import java.net.Socket;
import java.security.KeyManagementException;
import java.security.NoSuchAlgorithmException;
import java.security.cert.X509Certificate;

import javax.net.SocketFactory;
import javax.net.ssl.SSLContext;
import javax.net.ssl.SSLSocketFactory;
import javax.net.ssl.TrustManager;
import javax.net.ssl.X509TrustManager;
import javax.swing.SwingUtilities;

import org.apache.log4j.Logger;

import com.webobjects.eodistribution.client.EOHTTPChannel;
import com.webobjects.foundation.NSCoder;
import com.webobjects.foundation.NSDictionary;
import com.webobjects.foundation.NSPropertyListSerialization;

import er.extensions.foundation.ERXProperties;

/**
 * 
 * @property er.distribution.disallowHttpRequestsOnEventThread
 * 		throw an exception if an HTTP request is attempted on the Event Dispatch Thread (EDT); prevents creating a terrible UI.
 * 
 * @property er.distribution.trustAllSslCertificates
 * 		disable SSL certificate checking; useful for development mode, but not really safe for production
 * 
 * @property er.distribution.applicationUrl
 * 		the URL for connecting to the server application.
 * 
 * @author john
 *
 */
public class ERHTTPChannel extends EOHTTPChannel {

	private static final Logger log = Logger.getLogger(ERHTTPChannel.class);

	private String url;
	
	public ERHTTPChannel() {
		this(ERXProperties.stringForKey("er.distribution.applicationUrl"));
	}
	
	public ERHTTPChannel(String url) {
		this.url = url;
		if (url != null) {
			setConnectionDictionary(new NSDictionary<String, String>(url, EOHTTPChannel.ApplicationURLKey));
		}
	}
	
	public String url() {
		return url;
	}
	
	@Override
	public Object responseToMessage(Object message, NSCoder coder) {
		if (log.isDebugEnabled()) {
			log.debug("request: " + message.toString().replace('\n', ' '));
		}
		
		if (NSPropertyListSerialization.booleanForString(System.getProperty("er.distribution.disallowHttpRequestsOnEventThread")) &&
				SwingUtilities.isEventDispatchThread() && 
				message.toString().indexOf("clientSideRequestHandleExit") == -1) {
			throw new IllegalStateException("HTTP requests are not allowed on the UI thread.");
		}
		
		Object response = super.responseToMessage(message, coder);
		
//		if (log.isDebugEnabled()) {
//			log.debug("response: " + response.toString().replace('\n', ' '));
//		}
		
		return response;
	}
	
	@Override
	public Socket createSocket(String protocol, String hostName, int portNumber) throws IOException {
		if ("https".equals(protocol)) {
			return createSslSocket(protocol, hostName, portNumber);
		} else {
			return super.createSocket(protocol, hostName, portNumber);
		}
	}

	protected Socket createSslSocket(String protocol, String hostName, int portNumber) throws IOException {
		try {
			SocketFactory socketFactory;
			if (NSPropertyListSerialization.booleanForString(System.getProperty("er.distribution.trustAllSslCertificates"))) {
				socketFactory = trustAllSocketFactory();
			} else {
				socketFactory = SSLSocketFactory.getDefault();
			}
			Socket socket = socketFactory.createSocket(hostName, portNumber == 80 ? 443 : portNumber);
			return socket;
		} catch (NoSuchAlgorithmException e) {
			throw new RuntimeException(e);
		} catch (KeyManagementException e) {
			throw new RuntimeException(e);
		}
	}
	
	protected SocketFactory trustAllSocketFactory() throws NoSuchAlgorithmException, KeyManagementException {
		// Create a trust manager that does not validate certificate chains
	    TrustManager[] trustAllCerts = new TrustManager[] {
	        new X509TrustManager() {
	            public X509Certificate[] getAcceptedIssuers() {
	                return null;
	            }
	 
	            public void checkClientTrusted(X509Certificate[] certs, String authType) {
	                // Trust always
	            }
	 
	            public void checkServerTrusted(X509Certificate[] certs, String authType) {
	                // Trust always
	            }
	        }
	    };
	 
	    // Install the all-trusting trust manager
	    SSLContext sc = SSLContext.getInstance("SSL");
	    sc.init(null, trustAllCerts, new java.security.SecureRandom());
	    return sc.getSocketFactory();
	}
		
}
