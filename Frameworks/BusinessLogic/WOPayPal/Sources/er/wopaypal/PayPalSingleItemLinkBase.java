//
// PayPalSingleItemLink.java
// Project WOPayPal
//
// Created by travis on Wed Feb 13 2002
//
package er.wopaypal;

import com.webobjects.appserver.WOApplication;
import com.webobjects.appserver.WOComponent;
import com.webobjects.appserver.WOContext;
import com.webobjects.foundation.NSArray;
import com.webobjects.foundation.NSLog;

/** PayPalSingleItemLinkBase is the abstract superclass for most of the PayPal components.  It contains the bulk of the values that PayPal is interested in, and the list of bindings that those values correlate to.
 */
public abstract class PayPalSingleItemLinkBase extends WOComponent {

    /** The cgi script name we're calling on PayPal's server.
     */
    public static String PAYPAL_CGI_NAME = "cgi-bin/webscr";
    
    /** Required by PayPal, this is a valid PayPal account name; hopefully your own if you want to make any money...
     */
    public String payPalBusinessName;

    /** optional, customer editable if omitted
     */
    /** whether or not to allow the customer to add comment in a note
     */
    public Boolean allowCustomerNote;
    /** cost of the item
     */
    public String amount;
    /** currency of transaction; Currently supported are: USD (US Dollars), CAD (Canadian Dollars),
     *  GBP (British Pounds Sterling), EUR (Euros), JPY (Japanese Yen).  Defaults to USD.
     */
    public String currencyCode;
    /** a string to represent the item's plain language name (up to 127 char), e.g. "Cool Widget";
     */
    public String itemName;
    /** whether or not to add shipping address to the purchase
     */
    public Boolean collectShippingAddress;
    /** flag to tell PayPal whether or not to show a user-editable quantity field for the item
     */
    public Boolean userDefinableQuantity;

    /** optional, never presented to customer
     */
    /** a custom string that will be "passed through" Paypal's service and back to you; never shown to the customer
     */
    public String custom;
    /** a string to represent the item # (up to 127 char); like a UPC code, or a stocking code, etc.
     */
    public String itemNumber;
    /** URL to a 150 x 50 pixel image that can be used to customize the PayPal transaction page.  This is VERY discouraged unless your image is on an https server.
     */
    public String logoURL;
    /** the URL to which the customer will be taken if he cancels the purchase; defaults to PayPal
     */
    public String cancelURL;
    /** the URL to which the customer will be taken upon completion of the purchase; defaults to PayPal
     */
    public String returnURL;
    /** the URL to which PayPal will send the Instant Payment Notifications, if you've set up that option
     */
    public String notifyURL;

    /** are we using Instant Payment Notification?
     */
    public Boolean useIPN;
    /** use the default IPN Notification URL from the PayPalAction class?
     */
    public Boolean useDefaultIPNURL;
    
    /** Transaction-based tax override variable.
     */
    public String tax_rate;

    /** Constructor.
     * 
     * @param context WOContext
     */
    public PayPalSingleItemLinkBase(WOContext context) {
        super(context);
    }

    /** the base list of bindings to pull from WOComponents
     * 
     * @return NSArray
     */
    protected NSArray baseBindingList() {
        return new NSArray( new Object[]
            {   "payPalBusinessName",
                "userDefinableQuantity",
                "itemName",
                "itemNumber",
                "amount",
                "currencyCode",
                "collectShippingAddress",
                "allowCustomerNote",
                "logoURL",
                "returnURL",
                "cancelURL",
                "notifyURL",
                "useIPN",
                "useDefaultIPNURL",
                "tax_rate",
                "custom"});
    } 

    /** for subclasses to add additional bindings
     * 
     * @return NSArray
     */
    protected abstract NSArray additionalBindingList();
    

    /** try to intelligently construct the path back to the ipnAction in the PayPalAction class
     * 
     * @return String
     */
    protected String defaultNotificationURL() {
        StringBuilder notURL = new StringBuilder();
        WOApplication app = application();

        //check if we're in directConnect mode
        if (app.isDirectConnectEnabled()) {
            // we're running in development mode; try to get the i.p. address; if it's not 127.0.0.1, we'll include the notify_url parameter; .local. means we're running locally under rendezvous
            if (app.host().equals("localhost") || app.host().indexOf(".local.") != -1) {
                if (app.hostAddress().getHostAddress().equals("127.0.0.1")) {
                    // probably not connected to internet
                    return "testing_mode"; //just so we can see this method working
                } else {
                    //get protocol
                    String protocol = app.cgiAdaptorURL().substring(0, app.cgiAdaptorURL().indexOf(":"));
                    notURL.append(protocol).append("://"); // http:// or https://
                    notURL.append(app.hostAddress().getHostAddress()); // host i.p.
                    if (app.port().intValue() != 80) { // 80 is standard web port
                        notURL.append(':').append(app.port()); // :portNum
                    }
                    notURL.append(context().request().adaptorPrefix()).append('/'); // cgi-bin/WebObjects/
                    notURL.append(context().request().applicationName()).append(".woa/wa/PayPalAction/ipn"); // our processing action
                }

            }
        } else {
            // we're running the app in a deployment or testing mode
            notURL.append(app.cgiAdaptorURL()).append("://"); // http://host/cgi-bin/WebObjects
            notURL.append('/').append(context().request().applicationName()).append(".woa/"); // /applicationName.woa/
            notURL.append(context().request().applicationNumber()); // app instance number (for routing with multiple instances running)
            notURL.append("/wa/PayPalAction/ipn"); // our processing action
        }
        NSLog.debug.appendln("defaultNotificationURL: " + notURL.toString());
        return notURL.toString();
    }

}
