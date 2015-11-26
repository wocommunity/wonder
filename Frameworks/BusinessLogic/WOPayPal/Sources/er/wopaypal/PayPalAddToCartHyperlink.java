//
// PayPalAddToCartHyperlink.java: Class file for WO Component 'PayPalAddToCartHyperlink'
// Project WOPayPal
//
// Created by travis on Sat Feb 09 2002
//
package er.wopaypal;

import com.webobjects.appserver.WOContext;

/** PayPalAddToCartHyperlink is a WOComponent that allows you to embed text, an image, etc. inside the hyperlink, or assign the string through through its bindings.
 * 
 * It returns the appropriate url to add an item to the user's PayPal shopping cart.
 */
public class PayPalAddToCartHyperlink extends PayPalSingleItemHyperlink {
    
    /** The cgi command we're calling on PayPal's server.
     */
    protected static String PAYPAL_CGI_COMMAND = "?cmd=_cart&add=1";

    
    /** Constructor
     * 
     * @param context WOContext
     */
    public PayPalAddToCartHyperlink(WOContext context) {
        super(context);
    }

    
    /** addToCartHref assembles the URL for the item, based on the values of the bindings it reads from the WOComponent.  It gets the values, and most of it's code from the superclass.
     * 
     * @return String containing the url to add the current item to the user's
     *      PayPal shopping cart.
     */
    public String addToCartHref() { 
        StringBuffer sb = WOPayPal.baseUrl();

        sb.append(PayPalSingleItemLinkBase.PAYPAL_CGI_NAME);
        sb.append(PAYPAL_CGI_COMMAND);
        sb.append(payPalUrlParams());

        return sb.toString();
    }

    /** onClickString basically takes the addToCartHref and embeds it in a JavaScript window.open() method, as per PayPal's instructions.  The JavaScript called tells the browser to open a new window that loads the addToCartHref url and has the characteristics given...
     * 
     * @return String that contains the JavaScript code for the addToCart link
     */
    public String onClickString() {
        StringBuilder sb = new StringBuilder();

        sb.append("window.open('");
        sb.append(addToCartHref());
        sb.append("','cartwin','width=600,height=400,scrollbars,location,resizable,status');");

        return sb.toString();
    }

}
