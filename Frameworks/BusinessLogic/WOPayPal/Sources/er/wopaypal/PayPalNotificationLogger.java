package er.wopaypal;

//
// PayPalNotificationLogger.java
// Project WOPayPal
//
// Created by travis on Tue Feb 12 2002
//

import com.webobjects.appserver.WORequest;
import com.webobjects.foundation.NSLog;

/**
 *  The PayPalNotificationLogger class is simply an example of a delegate for the PayPalNotificationListener class.  In reality, a delegate can implement any or all of these methods from the PayPalNotificationListener.Delegate interface, and use whatever custom logic fits the need.  Examples would be logging the transactions to a database, a file, etc.
 */

public class PayPalNotificationLogger {

    /** Constructor.
     */
    public PayPalNotificationLogger() {
        super();
    }


    public void processDeniedPaypalTransaction(WORequest aRequest) {
        NSLog.out.appendln("PaypalNotificationLogger: Denied Paypal transaction: " + aRequest.formValueForKey("txn_id"));
    }

    public void processFailedPaypalTransaction(WORequest aRequest) {
        NSLog.out.appendln("PaypalNotificationLogger: Failed Paypal transaction: " + aRequest.formValueForKey("txn_id"));
    }

    public void processInvalidPaypalTransaction(WORequest aRequest) {
        NSLog.out.appendln("PaypalNotificationLogger: Invalid Paypal transaction: " + aRequest.formValueForKey("txn_id") + " from i.p.: " + ((aRequest.headerForKey("REMOTE_ADDR") != null) ? (String)aRequest.headerForKey("REMOTE_ADDR") : "- unknown -"));
    }

    public void processPendingPaypalTransaction(WORequest aRequest) {
        NSLog.out.appendln("PaypalNotificationLogger: Pending Paypal transaction: " + aRequest.formValueForKey("txn_id"));
    }

    public void processValidPaypalTransaction(WORequest aRequest) {
        NSLog.out.appendln("PaypalNotificationLogger: Valid Paypal transaction: " + aRequest.formValueForKey("txn_id"));
    }


}
