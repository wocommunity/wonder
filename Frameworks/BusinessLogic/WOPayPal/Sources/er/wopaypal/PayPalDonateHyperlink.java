//
// PayPalDonateHyperlink.java: Class file for WO Component 'PayPalDonateHyperlink'
// Project WOPayPal
//
// Created by travis on Mon May 06 2002
//
package er.wopaypal;

import com.webobjects.foundation.*;
import com.webobjects.appserver.*;
import com.webobjects.eocontrol.*;
import com.webobjects.eoaccess.*;

/** PayPalDonateHyperlink is identical in functionality to PayPalSingleItemHyperlink, except that the amount parameter is optional for donations.  I decided to make a separate WOComponent to make it easier to keep the WOComponent bindings straight.
 */
public class PayPalDonateHyperlink extends PayPalSingleItemHyperlink {
    
    /** Constructor.
     * 
     * @param context WOContext
     */
    public PayPalDonateHyperlink(WOContext context) {
        super(context);
    }

}
