//
// WOPayPal.java
// Project WOPayPal
//
// Created by travis on Tue Feb 12 2002
//

package er.wopaypal;

import com.webobjects.appserver.*;
import com.webobjects.foundation.*;
import com.webobjects.eocontrol.*;
import com.webobjects.eoaccess.*;
import java.util.*;

/** WOPayPal is the principal class in the framework.  It gets initialized first, and contains the setup for initializing the framework's functionality
 */
public class WOPayPal {
    public static final String PAYPAL_URL_BASE = "http://www.paypal.com/";
    public static final String PAYPAL_SECURE_URL_BASE = "https://www.paypal.com/";
    public static final Class[] NotificationClassArray = { com.webobjects.foundation.NSNotification.class };
    private static NSMutableSet _retainer = new NSMutableSet();

    /** Constructor.
     */
    public WOPayPal() {
        super();
    }
    
 
    /** ivar to determine whether the framework has been set up or not.
     */
    private static boolean _isInitialized=false;
    /** called implicitly because WOPaypal is the principal class of the framework.
     */
    static {
        if (!_isInitialized) {
            // This is OK to call multiple times as it will only be configured the first time.
            try {

        registerPayPalNotificationListenerObservers();

        _isInitialized=true;
            } catch (Exception e) {
                System.out.println("Caught exception: " + e.getMessage() + " stack: ");
                e.printStackTrace();
            }
        }
    }

    /** registerPayPalNotificationListenerObservers sets up the observer in the PayPalNotificationListener to receive notifications of Instant Payment Notifications from the PayPalAction class' ipnAction method.
     */
    public static void registerPayPalNotificationListenerObservers() {
        Object observer = PayPalNotificationListener.observer();
        _retainer.addObject(observer);

        NSNotificationCenter.defaultCenter().addObserver(observer,
                              new NSSelector("handleDeniedPaymentNotification", NotificationClassArray),
                              PayPalNotificationListener.DeniedPayPalPaymentReceivedNotification,
                              null);
        NSNotificationCenter.defaultCenter().addObserver(observer,
                              new NSSelector("handleFailedPaymentNotification", NotificationClassArray),
                              PayPalNotificationListener.FailedPayPalPaymentReceivedNotification,
                              null);
        NSNotificationCenter.defaultCenter().addObserver(observer,
                              new NSSelector("handleInvalidPaymentNotification", NotificationClassArray),
                              PayPalNotificationListener.InvalidPayPalPaymentReceivedNotification,
                              null);
        NSNotificationCenter.defaultCenter().addObserver(observer,
                              new NSSelector("handlePendingPaymentNotification", NotificationClassArray),
                              PayPalNotificationListener.PendingPayPalPaymentReceivedNotification,
                              null);
        NSNotificationCenter.defaultCenter().addObserver(observer,
                              new NSSelector("handleValidPaymentNotification", NotificationClassArray),
                              PayPalNotificationListener.ValidPayPalPaymentReceivedNotification,
                              null);
    }
    

}
