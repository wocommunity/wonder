//
// CancelledPayPalTransaction.java: Class file for WO Component 'CancelledPayPalTransaction'
// Project WOPayPal
//
// Created by travis on Sat Feb 09 2002
//
package er.wopaypal;

import com.webobjects.appserver.WOComponent;
import com.webobjects.appserver.WOContext;

/** CancelledPayPalTransaction is a very simple component to which the application will return your customers after a cancelled PayPal transaction, if you don't tell it to return a different one.  See cancelAction in the PayPalAction class.
 */
public class CancelledPayPalTransaction extends WOComponent {

    /** Constructor.
     *
     * @param context WOContext
     */
    public CancelledPayPalTransaction(WOContext context) {
        super(context);
    }

}
