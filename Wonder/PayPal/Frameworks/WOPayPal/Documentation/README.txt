This framework is released under the BSD license.  Do with it what you will.  Blah Blah Blah.

**************************

The WOPayPal framework is meant to give WebObjects developers an easy way to integrate PayPal payments into their applications.

**************************

Setup:

First off, you have to have a PayPal Business or Premiere account for this to be useful.  For it to be more useful, and to use Instant Payment Notifications, you need to enable that option in the preferences area of your account settings on PayPal's site.

Link the framework into your app.  Use the PayPal link components to easily create the links to PayPal's payment site.

If you want to use Instant Payment Notifications:

Create a class that implements the necessary code to deal with the notifications and register it as the delegate for the PayPalNotificationListener.  The observer gets registered to receive notifications, and the Delegate interface defines what it can do with the notifications.  If a delegate is registered and implements some subset of the methods defined in the Delegate interface, PayPalNotificationListener invokes the delegate object's method.  Log them to a file, a database, etc.  Whatever is right for your business logic.  You can find a sample delegate class, PayPalNotificationLogger, that implements all of the methods from the PayPalNotificationListener.Delegate interface and simply logs the messages to NSLog.out.


Set some optional defaults (we use System.getProperty(foo) to read these):

SuccessfulPayPalTransactionComponent - The component that will be returned after a successful PayPal transaction.  A simple component is returned by default.  Its re-entry link returns whatever component is generated in the defaultAction() method on your app's DirectAction class.


CancelledPayPalTransactionComponent - The component that will be returned after a cancelled PayPal transaction. A simple component is returned by default.  Its re-entry link returns whatever component is generated in the defaultAction() method on your app's DirectAction class.


**************************

To Do:
    Include support for other PayPal payment types: subscription.

    WebObjects Palette?
    
    More Testing! - I need help testing Instant Payment Notifications!  I can't pay myself, so it has to be a 2 person job.  I think all the stuff is there to handle ipn's correctly, but it still needs testing.

    Better Documentation.
