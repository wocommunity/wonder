//
// SuccessfulPayPalTransaction.java: Class file for WO Component 'SuccessfulPayPalTransaction'
// Project WOPayPal
//
// Created by travis on Sat Feb 09 2002
//
package er.wopaypal;

import com.webobjects.appserver.WOComponent;
import com.webobjects.appserver.WOContext;

/** SuccessfulPayPalTransaction is a very simple component to which the application will return your customers after a successful PayPal transaction, if you don't tell it to return a different one.  See returnAction in the PayPalAction class.
 */
public class SuccessfulPayPalTransaction extends WOComponent {

    /** Constructor.
     *
     * @param context WOContext
     */
    public SuccessfulPayPalTransaction(WOContext context) {
        super(context);
    }

}
