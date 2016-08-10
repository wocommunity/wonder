//
// PayPalSingleItemHyperlink.java: Class file for WO Component 'PayPalSingleItemHyperlink'
// Project WOPayPal
//
// Created by travis on Sat Feb 09 2002
//
package er.wopaypal;

import com.webobjects.appserver.WOContext;

/** This class is totally identical in functionality to the PayPalSingleItemFormLink class, but the amount is an optional field for donations.  I decided to make a separate WOComponent just to make it easier to keep the WOComponent bindings straight.
 */
public class PayPalDonateFormLink extends PayPalSingleItemFormLink {

    /** Constructor.
     * 
     * @param context WOContext
     */
    public PayPalDonateFormLink(WOContext context) {
        super(context);
    }

}
