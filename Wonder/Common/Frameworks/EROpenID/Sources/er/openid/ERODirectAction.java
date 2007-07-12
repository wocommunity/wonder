package er.openid;

import org.openid4java.association.AssociationException;
import org.openid4java.consumer.ConsumerException;
import org.openid4java.discovery.DiscoveryException;
import org.openid4java.message.MessageException;

import com.webobjects.appserver.WOActionResults;
import com.webobjects.appserver.WOComponent;
import com.webobjects.appserver.WORequest;

import er.extensions.ERXDirectAction;
import er.extensions.ERXProperties;

/**
 * ERODirectAction contains OpenID direct actions.
 * 
 * @property er.openid.successPageName the name of the openID success page (should implement IEROResponsePage)
 * @property er.openid.failurePageName the name of the openID failuer page (should implement IEROResponsePage)
 * 
 * @author mschrag
 */
public class ERODirectAction extends ERXDirectAction {
  public ERODirectAction(WORequest request) {
    super(request);
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
  public WOActionResults openIDRequestAction() throws MessageException, DiscoveryException, ConsumerException {
    String identity = request().stringFormValueForKey("identity");
    return EROpenIDManager.manager().authRequest(identity, request(), context());
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
  public WOActionResults openIDResponseAction() throws MessageException, DiscoveryException, AssociationException {
    EROResponse response = EROpenIDManager.manager().verifyResponse(request(), context());
    WOActionResults results;
    if (response.succeeded()) {
      String successPageName = ERXProperties.stringForKey("er.openid.successPageName");
      if (successPageName == null) {
        throw new IllegalArgumentException("You must set the property 'er.openid.successPageName'.");
      }
      WOComponent successPage = pageWithName(successPageName);
      if (successPage instanceof IEROResponsePage) {
        ((IEROResponsePage) successPage).setOpenIDResponse(response);
      }
      results = successPage;
    }
    else {
      String failurePageName = ERXProperties.stringForKey("er.openid.failurePageName");
      if (failurePageName == null) {
        throw new IllegalArgumentException("You must set the property 'er.openid.failurePageName'.");
      }
      WOComponent failurePage = pageWithName(failurePageName);
      if (failurePage instanceof IEROResponsePage) {
        ((IEROResponsePage) failurePage).setOpenIDResponse(response);
      }
      results = failurePage;
    }
    return results;
  }
}
