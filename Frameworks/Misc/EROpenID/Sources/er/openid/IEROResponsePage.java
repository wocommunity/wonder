package er.openid;

/**
 * Implemented by the success and failure page if you are using
 * the default OpenID direct actions.
 *  
 * @author mschrag
 */
public interface IEROResponsePage {
  /**
   * Sets the OpenID response from the provider.
   * 
   * @param response the OpenID provider's response
   */
  public void setOpenIDResponse(EROResponse response);
}
