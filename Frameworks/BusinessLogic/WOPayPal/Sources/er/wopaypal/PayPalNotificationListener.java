//
// PayPalNotificationRecorder.java
// Project WOPayPal
//
// Created by travis on Tue Feb 12 2002
//
package er.wopaypal;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;

import com.webobjects.appserver.WORequest;
import com.webobjects.foundation.NSLog;
import com.webobjects.foundation.NSNotification;

/**
 * The PayPalNotificationListener class is a receiver for notifications from PayPalAction's ipnAction method, which broadcasts notifications of different events having to do with "Instant Payment Notification" transaction notifications from PayPal.  The observer gets registered to receive notifications, and the Delegate interface defines what it can do with the notifications.  If a delegate is registered and implements some subset of the methods defined in the PayPalNotificationListener.Delegate interface, PayPalNotificationListener invokes the appropriate method on the delegate object.
 */
public class PayPalNotificationListener {

    /** Notification name when IPN returns DENIED result.
     */
    public final static String DeniedPayPalPaymentReceivedNotification = "DeniedPayPalPaymentReceivedNotification";
    /** Notification name when IPN returns FAILED result.
     */
    public final static String FailedPayPalPaymentReceivedNotification = "FailedPayPalPaymentReceivedNotification";
    /** Notification name when IPN returns INVALID result.
     */
    public final static String InvalidPayPalPaymentReceivedNotification = "InvalidPayPalPaymentReceivedNotification";
    /** Notification name when IPN returns PENDING result.
     */
    public final static String PendingPayPalPaymentReceivedNotification = "PendingPayPalPaymentReceivedNotification";
    /** Notification name when IPN returns VALID result.
     */
    public final static String ValidPayPalPaymentReceivedNotification = "ValidPayPalPaymentReceivedNotification";
    

    /** Constructor.
     */
    public PayPalNotificationListener() {
        super();
    }
    

    /** Private ivar for the default delegate object.
     */
    private static Object _defaultDelegate;
    
    /** Method to return the default delegate object.  Creates a new Object as the delegate if it's not assigned.
     * 
     * @return Object
     */
    protected static Object defaultDelegate() {
        if (_defaultDelegate == null) {
            _defaultDelegate = new Object();
        }
        return _defaultDelegate;
    }

    /** Private ivar for the assigned delegate Object.
     */
    private static volatile Object _delegate;
    
    /** Method to return the assigned delegate object.  If one is not assigned, it returns the defaultDelegate object.
     * 
     * @return Object that is assigned as the class' delegate.
     */
    public static Object delegate() { return _delegate != null ? _delegate : defaultDelegate(); }
    
    /** Method to assign the class's delegate object.  Throws an IllegalArgumentException if it receives a null object to assign as the delegate.
     * 
     * @param obj Object
     */
    public static void setDelegate(Object obj) {
        if (obj != null) {
            _delegate = obj;
        } else {
            throw new IllegalArgumentException("Attempt to assign null delegate");
        }
    }

    /** Interface for the delegate.  Defines the methods which the delegate may implement to be able to process the PayPal IPN status notifications sent from the PayPalAction class' ipnAction.
     *
     *  The delegate you assign to the PayPalNotificationListener must implement a subset of the methods defined in the Delegate interface.  This allows you to define which notifications you're interested in, and how you want to handle them.  Log them to a file, save them in a database, etc.
     */
    public static interface Delegate {
        /** Method name to call on delegate object when Observer recieves a notification with DENIED result.
         */
        static String PROCESS_DENIED_PAYPAL_TRANSACTION = "processDeniedPaypalTransaction";
        /** Method name to call on delegate object when Observer recieves a notification with FAILED result.
         */
        static String PROCESS_FAILED_PAYPAL_TRANSACTION = "processFailedPaypalTransaction";
        /** Method name to call on delegate object when Observer recieves a notification with INVALID result.
         */
        static String PROCESS_INVALID_PAYPAL_TRANSACTION = "processInvalidPaypalTransaction";
        /** Method name to call on delegate object when Observer recieves a notification with PENDING result.
         */
        static String PROCESS_PENDING_PAYPAL_TRANSACTION = "processPendingPaypalTransaction";
        /** Method name to call on delegate object when Observer recieves a notification with VALID result.
         */
        static String PROCESS_VALID_PAYPAL_TRANSACTION = "processValidPaypalTransaction";

        /** method stub for processing Paypal IPN's that come with DENIED status
         * 
         * "@param aRequest
         * 
         * @param aRequest WORequest
         */
        void processDeniedPaypalTransaction(WORequest aRequest);

        /** method stub for processing Paypal IPN's that come with FAILED status
         * 
         * "@param aRequest
         * 
         * @param aRequest WORequest
         */
        void processFailedPaypalTransaction(WORequest aRequest);

        /** method stub for processing Paypal IPN's that come with INVALID status
         * 
         * "@param aRequest
         * 
         * @param aRequest WORequest
         */
        void processInvalidPaypalTransaction(WORequest aRequest);

        /** method stub for processing Paypal IPN's that come with PENDING status
         * 
         * "@param aRequest
         * 
         * @param aRequest WORequest
         */
        void processPendingPaypalTransaction(WORequest aRequest);

        /** method stub for processing Paypal IPN's that come with VALID status
         * 
         * "@param aRequest
         * 
         * @param aRequest WORequest
         */
        void processValidPaypalTransaction(WORequest aRequest);
    }


    /** Private ivar for the Observer object.
     */
    private static Observer _observer;
    
    /** Method to return the observer object.  If one is not assigned, it instantiates and returns a new Observer.
     * 
     * @return Observer
     */
    public static Observer observer() {
        if (_observer == null) {
            _observer = new Observer();
        }
        return _observer;
    }

    /** The Observer is the object which is registered (in WOPayPal) to listen for the NSNotifications broadcast by the PayPalAction's ipnAction method.  When its methods receives a notification, they call handleNotification, passing the notification name and the Notification itself as the parameter to that method call.
     */
    public static class Observer {
        /** Method invoked by the NSNotificationCenter when it receives a DENIED notification from PayPalAction
         * 
         * @param n NSNotification
         */
        public void handleDeniedPaymentNotification(NSNotification n) {
            handleNotification(Delegate.PROCESS_DENIED_PAYPAL_TRANSACTION, n);
        }

        /** Method invoked by the NSNotificationCenter when it receives a FAILED notification from PayPalAction
         * 
         * @param n NSNotification
         */
        public void handleFailedPaymentNotification(NSNotification n) {
            handleNotification(Delegate.PROCESS_FAILED_PAYPAL_TRANSACTION, n);
        }

        /** Method invoked by the NSNotificationCenter when it receives a INVALID notification from PayPalAction
         * 
         * @param n NSNotification
         */
        public void handleInvalidPaymentNotification(NSNotification n) {
            handleNotification(Delegate.PROCESS_INVALID_PAYPAL_TRANSACTION, n);
        }

        /** Method invoked by the NSNotificationCenter when it receives a PENDING notification from PayPalAction
         * 
         * @param n NSNotification
         */
        public void handlePendingPaymentNotification(NSNotification n) {
            handleNotification(Delegate.PROCESS_PENDING_PAYPAL_TRANSACTION, n);
        }

        /** Method invoked by the NSNotificationCenter when it receives a VALID notification from PayPalAction
         * 
         * @param n NSNotification
         */
        public void handleValidPaymentNotification(NSNotification n) {
            handleNotification(Delegate.PROCESS_VALID_PAYPAL_TRANSACTION, n);
        }

        /** handleNotification checks to see if the delegate object can perform the method name passed in as the targetMethodName parameter.  If it can, it tries to invoke that method, passing the notification's object (a WORequest) as the parameter.
         * 
         * @param targetMethodName String
         * @param n NSNotification
         */
        public void handleNotification(String targetMethodName, NSNotification n) {
            Object delegate = delegate();
            Method targetMethod = null;
            Class[] targetParams = new Class[] { WORequest.class };

            if (NSLog.debugLoggingAllowedForLevel(NSLog.DebugLevelInformational)) {
                NSLog.debug.appendln("PayPalNotificationListener -> handleNotification: " + n.name());
                NSLog.debug.appendln("PayPalNotificationListener -> handleNotification: trying to get delegate method:" + targetMethodName + " with params: " + targetParams.getClass().getName());
            }

            try { //try to grab the method on the delegate
                targetMethod = delegate.getClass().getMethod(targetMethodName, targetParams);
            } catch (NoSuchMethodException e) {
                if (NSLog.debugLoggingAllowedForLevel(NSLog.DebugLevelCritical)) {
                    NSLog.debug.appendln("PayPalNotificationListener -> handleNotification: NoSuchMethodException while trying to get method " + targetMethodName + ": " + e);
                }
            }

            if (targetMethod != null) { // we got the delegate's method object
                try { // try to invoke the delegate's method with the targetParams
                    targetMethod.invoke(delegate, new Object[] { n.object() });
                } catch(IllegalAccessException e) {
                    // if the underlying method is inaccessible.
                    if (NSLog.debugLoggingAllowedForLevel(NSLog.DebugLevelCritical)) {
                        NSLog.debug.appendln("PayPalNotificationListener -> handleNotification: IllegalAccessException while trying to invoke method " + targetMethodName + ": " + e);
                    }
                } catch (IllegalArgumentException e) {
                    // if the number of actual and formal parameters differ, or if an unwrapping conversion fails.
                    if (NSLog.debugLoggingAllowedForLevel(NSLog.DebugLevelCritical)) {
                        NSLog.debug.appendln("PayPalNotificationListener -> handleNotification: IllegalArgumentException while trying to invoke method " + targetMethodName + ": " + e);
                    }
                } catch (NullPointerException e) {
                    // if the specified object is null and the method is an instance method.
                    if (NSLog.debugLoggingAllowedForLevel(NSLog.DebugLevelCritical)) {
                        NSLog.debug.appendln("PayPalNotificationListener -> handleNotification: NullPointerException while trying to invoke method " + targetMethodName + ": " + e);
                    }
                } catch (ExceptionInInitializerError e) {
                    // if the initialization provoked by this method fails.
                    if (NSLog.debugLoggingAllowedForLevel(NSLog.DebugLevelCritical)) {
                        NSLog.debug.appendln("PayPalNotificationListener -> handleNotification: ExceptionInInitializerError while trying to invoke method " + targetMethodName + ": " + e);
                    }
                } catch (InvocationTargetException e) {
                    // if the initialization provoked by this method fails.
                    if (NSLog.debugLoggingAllowedForLevel(NSLog.DebugLevelCritical)) {
                        NSLog.debug.appendln("PayPalNotificationListener -> handleNotification: InvocationTargetException while trying to invoke method " + targetMethodName + ": " + e);
                    }
                }
            }

        }

    } // End inner class Observer


}
