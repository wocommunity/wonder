package er.extensions.components;

import java.io.IOException;
import java.io.InputStream;
import java.net.URL;
import java.net.URLConnection;
import java.util.Enumeration;

import org.apache.commons.lang3.CharEncoding;

import com.webobjects.appserver.WOContext;
import com.webobjects.appserver.WOResponse;
import com.webobjects.foundation.NSForwardException;

import er.extensions.foundation.ERXFileUtilities;

/**
 * Wraps an external content which is retrieved from an URL. Any binding starting with "?" will 
 * be added to the request headers. Supports file:// and http:// URLs. The reason it's stateless
 * - which will make is pretty costly - is that it's supposed to get wrapped by an ERXCachingWrapper.
 *
 * @binding url url to get content from.
 * @binding encoding content encoding to use.
 *
 * @author ak on 31.10.05
 */
public class ERXExternalContent extends ERXStatelessComponent {
	/**
	 * Do I need to update serialVersionUID?
	 * See section 5.6 <cite>Type Changes Affecting Serialization</cite> on page 51 of the 
	 * <a href="http://java.sun.com/j2se/1.4/pdf/serial-spec.pdf">Java Object Serialization Spec</a>
	 */
	private static final long serialVersionUID = 1L;

    /**
     * Public constructor
     * @param context the context
     */
    public ERXExternalContent(WOContext context) {
        super(context);
    }
    
    private URL url() {
    	URL result = null;
    	Object o =valueForBinding("url");
    	if (o instanceof String) {
    		try {
    			result = new URL((String)o);
    		} catch(Exception ex) {
    			throw new IllegalArgumentException("Wrong URL: " + o + ", " + ex);
    		}
    	} else if(o instanceof URL) {
    		result = (URL)o;
    	}
    	return result;
    }

    protected String contentString() {
    	String result = null;
    	URL url;
    	String encoding = null;
    	try {
    		url =  url();
    		URLConnection connection = url.openConnection();
    		connection.setDoInput(true);
    		connection.setDoOutput(false);
    		connection.setUseCaches(false);
    		for(Enumeration e = bindingKeys().objectEnumerator(); e.hasMoreElements();) {
    			String key = (String)e.nextElement();
    			if(key.startsWith("?")) {
    				connection.setRequestProperty(key.substring(1), valueForBinding(key).toString());
    			}
    		}
    		
    		if(connection.getContentEncoding() != null) {
    			encoding = connection.getContentEncoding();
    		}
    		if(encoding == null) {
    			encoding = (String)valueForBinding("encoding");
    		}
    		if(encoding == null) {
    			encoding = CharEncoding.UTF_8;
    		}
    		try (InputStream stream = connection.getInputStream()) {
    			byte bytes[] = ERXFileUtilities.bytesFromInputStream(stream);
    			result = new String(bytes, encoding);
    		}
    	} catch (IOException ex) {
    		throw NSForwardException._runtimeExceptionForThrowable(ex);
    	}
    	return result;
    }
    
    @Override
    public void appendToResponse(WOResponse response, WOContext arg1) {
    	String content = contentString();
    	response.appendContentString(content);
    }
 }
