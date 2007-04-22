package er.openid;

import java.util.List;

import org.openid4java.discovery.Identifier;
import org.openid4java.message.ax.FetchResponse;

import com.webobjects.foundation.NSKeyValueCoding;

/**
 * EROResponse encapsulates the response from an OpenID provider.
 * 
 * @author mschrag
 */
public class EROResponse implements NSKeyValueCoding.ErrorHandling {
  private Identifier _identifier;
  private FetchResponse _fetchResponse;

  /**
   * Construct a new EROResponse.
   * 
   * @param identifier the OpenID identifier (null if auth failed)
   * @param fetchResponse the FetchResponse (null if auth failed, or if there were no extended attributes)
   */
  public EROResponse(Identifier identifier, FetchResponse fetchResponse) {
    _identifier = identifier;
    _fetchResponse = fetchResponse;
  }

  /**
   * Returns whether or not this auth attempt succeeded.
   * 
   * @return whether or not this auth attempt succeeded
   */
  public boolean succeeded() {
    return _identifier != null;
  }

  /**
   * Returns the OpenID identifier
   * 
   * @return the OpenID identifier
   */
  public Identifier identifier() {
    return _identifier;
  }

  /**
   * Returns the OpenID FetchResponse that contains extended attributes from the request.
   * 
   * @return the OpenID FetchResponse
   */
  public FetchResponse fetchResponse() {
    return _fetchResponse;
  }

  public void handleTakeValueForUnboundKey(Object obj, String key) {
    // ignore
  }

  public void unableToSetNullForKey(String key) {
  }

  public Object handleQueryWithUnboundKey(String key) {
    Object value = null;
    if (_fetchResponse != null) {
      List values = _fetchResponse.getAttributeValues(key);
      if (values != null && values.size() > 0) {
        value = values.get(0);
      }
    }
    return value;
  }
}
