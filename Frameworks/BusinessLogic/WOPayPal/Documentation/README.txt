This framework is released under the BSD license.  Do with it what you will.  Blah Blah Blah.

**************************

The WOPayPal framework is meant to give WebObjects developers an easy way to integrate PayPal payments into their applications.

**************************

Setup:

First off, you have to have a PayPal Business or Premiere account for this to be useful.  For it to be more useful, and to use Instant Payment Notifications, you need to enable that option in the preferences area of your account settings on PayPal's site.

Link the framework into your app.  Use the PayPal link components to easily create the links to PayPal's payment site.

If you want to use Instant Payment Notifications:

Create a class that implements the necessary code to deal with the notifications and register it as the delegate for the PayPalNotificationListener.  The observer gets registered to receive notifications, and the Delegate interface defines what it can do with the notifications.  If a delegate is registered and implements some subset of the methods defined in the Delegate interface, PayPalNotificationListener invokes the delegate object's method.  Log them to a file, a database, etc.  Whatever is right for your business logic.  You can find a sample delegate class, PayPalNotificationLogger, that implements all of the methods from the PayPalNotificationListener.Delegate interface and simply logs the messages to NSLog.out.

As an important caveat, you should check the amount (price) returned by the IPN against the amount you set for your item before considering any transaction complete.  This is to assure that a user can't change the values in the initial request parameters sent to PayPal.  Currently the framework does NOT do this check for you, since the prices will be very dependent on your business.  You should do it before.

One more thing about PayPal transactions is that PayPal is fairly persistent with their notifications.  They repeatedly resend them in an increasing time interval for a few days if they haven't received a response from you.  While this is good if something goes temporarily wrong on your end, you need to log the txn_id parameter of the IPN requests with whatever you do to process them in order to make sure you don't process duplicate transactions.  The framework does NOT currently do this check for you.

Here's some sample code that sets the delegate to use the included PayPalNotificationLogger:

In your application class, just set the delegate like so: 

public Application() {
        super();
        System.out.println("Welcome to " + this.name() + "!");
        
        /* ** Put your application initialization code here ** */

        if (NSLog.debugLoggingAllowedForLevel(NSLog.DebugLevelInformational)) {
            NSLog.debug.appendln("Registering delegate for Paypal ipn...");
        }
        PayPalNotificationListener.setDelegate(new PayPalNotificationLogger());
    }

The good news: Yes, it's really that easy!  The bad news: the PayPalNotificationLogger isn't all that useful, so you'll need to do a little work to create your own class to deal with IPNs.


Set some optional defaults (we use System.getProperty(foo) to read these):

SuccessfulPayPalTransactionComponent - The component that will be returned after a successful PayPal transaction.  A simple component is returned by default.  Its re-entry link returns whatever component is generated in the defaultAction() method on your app's DirectAction class.


CancelledPayPalTransactionComponent - The component that will be returned after a cancelled PayPal transaction. A simple component is returned by default.  Its re-entry link returns whatever component is generated in the defaultAction() method on your app's DirectAction class.


**************************

To Do:
    Include support for other PayPal payment types: subscription.

    WebObjects Palette?

    Better Documentation.
