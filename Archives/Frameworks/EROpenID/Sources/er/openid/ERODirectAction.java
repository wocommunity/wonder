package er.openid;

import org.openid4java.association.AssociationException;
import org.openid4java.consumer.ConsumerException;
import org.openid4java.discovery.DiscoveryException;
import org.openid4java.message.MessageException;

import com.webobjects.appserver.WOActionResults;
import com.webobjects.appserver.WOComponent;
import com.webobjects.appserver.WORequest;

import er.extensions.appserver.ERXDirectAction;
import er.extensions.foundation.ERXProperties;

/**
 * ERODirectAction contains OpenID direct actions.
 * 
 * @property er.openid.successPageName the name of the openID success page (should implement IEROResponsePage)
 * @property er.openid.failurePageName the name of the openID failure page (should implement IEROResponsePage)
 * 
 * @author mschrag
 */
public class ERODirectAction extends ERXDirectAction {
  public ERODirectAction(WORequest request) {
    super(request);
  }
  
  private WOComponent pageForKey(String key)
  {
      String successPageName = ERXProperties.stringForKey(key);
      if (successPageName == null) {
        throw new IllegalArgumentException("You must set the property '" + key + "'.");
      }
      return pageWithName(successPageName);
  }

  /**
   * openIDRequest initiates the Open ID request.  The request must
   * contain an form value named "identity" that contains the user's
   * OpenID identity url.  You can provide your own request URL if 
   * you'd like.  This just calls EROpenIDManager.manager().authRequest.
   * 
   * @return the redirection or form redirection page
   * @throws MessageException
   * @throws DiscoveryException
   * @throws ConsumerException
   */
  public WOActionResults openIDRequestAction() {
	String identity = request().stringFormValueForKey("identity");
	String realm = request().stringFormValueForKey("realm");
    WOActionResults results = null;
    try
    {
      results = EROpenIDManager.manager().authRequest(identity, realm, request(), context());
      if (results == null)
        results = this.pageForKey("er.openid.failurePageName");
    }
    catch (Exception exception)
    {
      EROpenIDManager.log.info(exception);
      results = this.pageForKey("er.openid.failurePageName");
    }
    return results;
  }

  /**
   * openIDResponse is the direct action that is redirected back to
   * from the OpenID provider.  See EROpenIDManager's documentation for
   * information on overriding this.
   * 
   * @return the results
   * @throws MessageException
   * @throws DiscoveryException
   * @throws AssociationException
   */
  public WOActionResults openIDResponseAction() {
    EROResponse response = null;
    try
    {
      response = EROpenIDManager.manager().verifyResponse(request(), context());
    }
    catch (Exception exception)
    {
      EROpenIDManager.log.info(exception);
    }
    WOActionResults results;
    if (response != null && response.succeeded()) {
      WOComponent successPage = this.pageForKey("er.openid.successPageName");
      if (successPage instanceof IEROResponsePage) {
        ((IEROResponsePage) successPage).setOpenIDResponse(response);
      }
      results = successPage;
    }
    else {
      WOComponent failurePage = this.pageForKey("er.openid.failurePageName");
      if (failurePage instanceof IEROResponsePage) {
        ((IEROResponsePage) failurePage).setOpenIDResponse(response);
      }
      results = failurePage;
    }
    return results;
  }
}
