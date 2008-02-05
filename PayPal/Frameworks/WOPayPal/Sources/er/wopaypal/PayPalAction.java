//
// PayPalAction.java
// Project WOPayPal
//
// Created by travis on Sat Feb 09 2002
//
package er.wopaypal;

import com.webobjects.foundation.*;
import com.webobjects.appserver.*;
import com.webobjects.eocontrol.*;
import com.webobjects.eoaccess.*;

/** PayPalAction is a DirectAction subclass that holds all the functionality for processing information that PayPal would send back to the application, including processing Instant Payment Notification communications, and responding to the successful and cancelled transaction pages to which PayPal will return users.  Its action handler is PayPalAction, so your url will take the form: http://www.yoursite.com/cgi-bin/WebObjects/yourAppName.woa/wa/PayPalAction/actionName...
 */
public class PayPalAction extends WODirectAction {

    /** Simply PayPal's url, sans the protocol
     */
    public static final String paypalSite = "www.paypal.com";
    /** The cgi portion of PayPal's url for doing Instant Payment Notification verifications
     */
    public static final String paypalCgi="/cgi-bin/webscr";
    
    /** Constructor
     * 
     * @param aRequest WORequest
     */
    public PayPalAction(WORequest aRequest) {
        super(aRequest);
    }


    /** Processor for Instant Payment Notifications
     * 
     * The class takes the request and constructs a response that it then echoes back to PayPal, with the additional value "&cmd=_notify-validate.
     * 
     * PayPal will then send a one word code for the status of the transaction.  This method then parses for that word and sends the appropriate notification for the result, with the original WORequest object from PayPal attached to it (as the notification's object).
     * 
     * The notification gets picked up by the PayPalNotificationListenerClass, which then hands it to the delegate class you assigned to handle the notification.  Pretty simple.
     */
    public void ipnAction() { // processor for Instant Payment Notifications

        WORequest ppIPNRequest = request(); // the incoming PayPal IPN (Instant Payment Notification)
        if (NSLog.debugLoggingAllowedForLevel(NSLog.DebugLevelInformational)) {
            NSLog.debug.appendln("PayPal's request looks like: " + ppIPNRequest + "\n\n");
            NSLog.debug.appendln("PayPal's request content looks like: " + ppIPNRequest.contentString() + "\n\n");
        }


        String returnString = ppIPNRequest.contentString() + "&cmd=_notify-validate";

        WOHTTPConnection ppEchoConnection = new WOHTTPConnection(paypalSite, 80); // our echo to PayPal
        WOResponse ppValidationResponse = new WOResponse(); // PayPal's validation of our echoed data

        // assemble User-Agent header
        StringBuffer ua = new StringBuffer();
        ua.append("WebObjects/5.1 [en] (");
        ua.append(System.getProperty("os.arch"));
        ua.append("; ");
        ua.append(System.getProperty("os.name"));
        ua.append(" ");
        ua.append(System.getProperty("os.version"));
        ua.append(")");

        NSMutableDictionary headers = new NSMutableDictionary();
        headers.setObjectForKey("en", "Accept-Language");
        headers.setObjectForKey("iso-8859-1,*,utf-8", "Accept-Charset");
        headers.setObjectForKey("image/gif, image/x-xbitmap, image/jpeg, image/pjpeg, image/png, */*", "Accept");
        headers.setObjectForKey(ua.toString(),"User-Agent");

        // the response back to PayPal
        WORequest paypalEchoRequest = new WORequest("POST", paypalCgi, "HTTP/1.1", headers, null, null);

        paypalEchoRequest.setContent(returnString);
        ppEchoConnection.setReceiveTimeout(90 * 1000); // 90 second timeout -- this might be too long!?!
        boolean success = ppEchoConnection.sendRequest(paypalEchoRequest);
        if (success) {
            ppValidationResponse = ppEchoConnection.readResponse(); // read PayPal's validation
            // PayPal's response *content* will either be "VERIFIED" or "INVALID"
            if (NSLog.debugLoggingAllowedForLevel(NSLog.DebugLevelInformational)) {
                NSLog.debug.appendln("the response looks like: " + ppValidationResponse + "\n\n");
                NSLog.debug.appendln("the response content looks like: " + ppValidationResponse.contentString() + "\n\n");
            }

            if (ppValidationResponse.contentString().equalsIgnoreCase("VERIFIED")) {
                if (((String)ppIPNRequest.formValueForKey("payment_status")).equalsIgnoreCase("completed")) {
                //should check previous txn_id's to be sure this isn't a duplicate notification

                NSNotificationCenter.defaultCenter().postNotification(PayPalNotificationListener.ValidPayPalPaymentReceivedNotification, ppIPNRequest);

            } else if (((String)ppIPNRequest.formValueForKey("payment_status")).equalsIgnoreCase("pending")) {
                // status pending
                NSNotificationCenter.defaultCenter().postNotification(PayPalNotificationListener.PendingPayPalPaymentReceivedNotification, ppIPNRequest);

            } else if (((String)ppIPNRequest.formValueForKey("payment_status")).equalsIgnoreCase("failed")) {
                // bank account payment failed -- customer probably didn't have the funds
                NSNotificationCenter.defaultCenter().postNotification(PayPalNotificationListener.FailedPayPalPaymentReceivedNotification, ppIPNRequest);

            } else if (((String)ppIPNRequest.formValueForKey("payment_status")).equalsIgnoreCase("denied")) {
                // you (the merchant) denied the payment
                NSNotificationCenter.defaultCenter().postNotification(PayPalNotificationListener.DeniedPayPalPaymentReceivedNotification, ppIPNRequest);

            } else {
                // the payment_status value is not any of the accepted values
            }
            } else if (ppValidationResponse.contentString().equalsIgnoreCase("INVALID")) {
            // possible fraud!!!
            NSNotificationCenter.defaultCenter().postNotification(PayPalNotificationListener.InvalidPayPalPaymentReceivedNotification, ppIPNRequest);

            } else {
                // received unaccepted response content string value -- log error and incoming i.p. address
                NSLog.err.appendln("PayPalAction->ipnAction: PayPal transaction validation returned unaccepted validation status from i.p: " + (((String)ppIPNRequest.headerForKey("REMOTE_ADDR") != null) ? (String)ppIPNRequest.headerForKey("REMOTE_ADDR") : "- unknown -"));
            }
        } else {
            NSLog.err.appendln("PayPalAction->ipnAction: PayPal transaction validation connection failed.");
        }

    }

    /** Provides a default method to return the page to which PayPal will send users after a successful transaction.
     * 
     * First it checks to see if there's a System property assigned to tell it what page to return.  If it finds one, it returns that page by calling pageWithName(property value).
     * 
     * If it doesn't find one, it returns the very simple default component, SuccessfulPayPalTransaction.
     * 
     * @return WOComponent for successful PayPal transactions
     */
    public WOActionResults returnAction() {
        String componentName = (String)System.getProperty("SuccessfulPayPalTransactionComponent");
        if (componentName == null || componentName.equals("")) {
            componentName = "SuccessfulPayPalTransaction";
        }
        return pageWithName(componentName);
    }

    /** Provides a default method to return the page to which PayPal will send users after a cancelled transaction.
     * 
     * First it checks to see if there's a System property assigned to tell it what page to return.  If it finds one it returns that page by calling pageWithName(property value).
     * 
     * If it doesn't find one, it returns the very simple default component, CancelledPayPalTransaction.
     * 
     * @return WOComponent for cancelled PayPal transactions
     */
    public WOActionResults cancelAction() {
        String componentName = (String)System.getProperty("CancelledPayPalTransactionComponent");
        if (componentName == null || componentName.equals("")) {
            componentName = "CancelledPayPalTransaction";
        }
        return pageWithName(componentName);
    }


}
