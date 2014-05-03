package er.wolips;

import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;

import org.apache.commons.lang.CharEncoding;

import com.webobjects.appserver.WOContext;
import com.webobjects.appserver.WOResponse;
import com.webobjects.foundation.NSDictionary;

import er.extensions.appserver.ERXResponseRewriter;
import er.extensions.foundation.ERXProperties;

/**
 * WOLipsUtilities provide handy-dandy WOLips communication methods.
 * 
 * @author mschrag
 */
public class WOLipsUtilities {
  public static void includePrototype(WOResponse response, WOContext context) {
    String prototypeFrameworkName = ERXProperties.stringForKeyWithDefault("wolips.prototype.framework", "Ajax");
    String prototypeFileName = ERXProperties.stringForKeyWithDefault("wolips.prototype.fileName", "prototype.js");
    ERXResponseRewriter.addScriptResourceInHead(response, context, prototypeFrameworkName, prototypeFileName);
  }
  
  public static boolean isWOLipsPasswordDefinde() {
    String password = System.getProperty("wolips.password");
    return password != null;
  }
  
  public static String wolipsUrl(String action, String key, String value) {
    return WOLipsUtilities.wolipsUrl(action, new NSDictionary(value, key));
  }

  public static String wolipsUrl(String action, NSDictionary params) {
    try {
      String host = System.getProperty("wolips.host", "localhost");
      int port = Integer.parseInt(System.getProperty("wolips.port", "9485"));
      String password = System.getProperty("wolips.password");
      if (password == null) {
        throw new NullPointerException("You must set 'wolips.password' in your Properties file.");
      }
      StringBuilder urlBuffer = new StringBuilder();
      urlBuffer.append("http://");
      urlBuffer.append(host);
      urlBuffer.append(':');
      urlBuffer.append(port);
      urlBuffer.append('/');
      urlBuffer.append(action);
      urlBuffer.append("?pw=");
      urlBuffer.append(URLEncoder.encode(password, CharEncoding.UTF_8));
      if (params != null && !params.isEmpty()) {
        for (Object key : params.allKeys()) {
          urlBuffer.append("&amp;");
          urlBuffer.append(URLEncoder.encode(key.toString(), CharEncoding.UTF_8));
          urlBuffer.append('=');
          Object value = params.objectForKey(key);
          urlBuffer.append(URLEncoder.encode(value.toString(), CharEncoding.UTF_8));
        }
      }
      return urlBuffer.toString();
    }
    catch (UnsupportedEncodingException e) {
      throw new RuntimeException("Unsupported character encoding.", e);
    }
  }
}
