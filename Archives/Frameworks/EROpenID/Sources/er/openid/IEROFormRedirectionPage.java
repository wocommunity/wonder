package er.openid;

/**
 * IEROFormRedirectionPage should be implemented by any form redirection page.
 * 
 * @author mschrag
 */
public interface IEROFormRedirectionPage {
  /**
   * Sets the OpenID redirection URL to submit the form to.
   *  
   * @param redirectionUrl the OpenID redirection URL
   */
  public void setRedirectionUrl(String redirectionUrl);
}
