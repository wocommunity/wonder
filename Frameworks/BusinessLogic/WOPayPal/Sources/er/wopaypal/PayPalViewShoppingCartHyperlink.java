//
// PayPalViewShoppingCartHyperlink.java: Class file for WO Component 'PayPalViewShoppingCartHyperlink'
// Project WOPayPal
//
// Created by travis on Mon May 06 2002
//
package er.wopaypal;

import com.webobjects.appserver.WOContext;

/** PayPalViewShoppingCartHyperlink is a WOComponent that allows you to embed text, an image, etc. inside the hyperlink, or assign the string through through its bindings.
 *
 * It returns the appropriate url to view the user's PayPal shopping cart in a new window.
 */
public class PayPalViewShoppingCartHyperlink extends PayPalSingleItemHyperlink {

    /** The cgi command we're calling on PayPal's server.
     */
    protected static String PAYPAL_CGI_COMMAND = "?cmd=_cart&display=1";

    /** Constructor
     *
     * @param context WOContext
     */
    public PayPalViewShoppingCartHyperlink(WOContext context) {
        super(context);
    }

    /** viewShoppingCartHref assembles the URL for the item, based on the values of the bindings it reads from the WOComponent.  It gets the values, and most of it's code from the superclass.
     *
     * @return String containing the url to view the user's PayPal shopping cart.
     * 
     */
    public String viewShoppingCartHref() {
        StringBuffer sb = WOPayPal.baseUrl();

        sb.append(PayPalSingleItemLinkBase.PAYPAL_CGI_NAME);
        sb.append(PAYPAL_CGI_COMMAND);
        sb.append(payPalUrlParams());

        return sb.toString();
    }

    /** onClickString basically takes the viewShoppingCartHref and embeds it in a JavaScript window.open() method, as per PayPal's instructions.  The JavaScript called tells the browser to open a new window that loads the viewShoppingCartHref url and has the characteristics given...
     *
     * @return String that contains the JavaScript code for the viewShoppingCart link
     */
    public String onClickString() {
        StringBuilder sb = new StringBuilder();

        sb.append("window.open('");
        sb.append(viewShoppingCartHref());
        sb.append("','cartwin','width=600,height=400,scrollbars,location,resizable,status');");

        return sb.toString();
    }
}
