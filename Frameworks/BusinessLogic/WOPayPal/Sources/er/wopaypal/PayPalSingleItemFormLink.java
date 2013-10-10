//
// PayPalSingleItemHyperlink.java: Class file for WO Component 'PayPalSingleItemHyperlink'
// Project WOPayPal
//
// Created by travis on Sat Feb 09 2002
//
package er.wopaypal;

import java.text.DecimalFormat;
import java.util.Enumeration;

import com.webobjects.appserver.WOActionResults;
import com.webobjects.appserver.WOContext;
import com.webobjects.appserver.WORequest;
import com.webobjects.appserver.WOResponse;
import com.webobjects.foundation.NSArray;
import com.webobjects.foundation.NSDictionary;
import com.webobjects.foundation.NSMutableArray;

/**
 * PayPalSingleItemFormLink is a WOComponent that implements a form to submit a
 * PayPal single item purchase.
 */
public class PayPalSingleItemFormLink extends
        PayPalSingleItemLinkBase {

    /**
     * Used in the WORepetition in the WOComponent
     */
    public NSDictionary aDict;
    /**
     * Used in the WOConditional on the WOComponent
     */
    public Boolean useImageButton;

    /**
     * Constructor.
     * 
     * @param context
     *            WOContext
     */
    public PayPalSingleItemFormLink(WOContext context) {
        super(context);
    }

    /**
     * Makes the component stateless.
     * 
     * @return boolean
     */
    @Override
    public boolean isStateless() {
        return true;
    }

    /**
     * NSArray of binding values presented in the way PayPal expects them. This
     * array will be used in the WOComponent (in a WORepetition) to pass values
     * to a series of hidden fields in the form that gets submitted to PayPal.
     * 
     * @return NSArray of NSDIctionary objects
     */
    public NSArray encodedBindings() {
        DecimalFormat dollarFormatter = new DecimalFormat("##0.00");
        NSMutableArray boundValues = new NSMutableArray();
        NSMutableArray allBindings = new NSMutableArray();
        allBindings.addObjectsFromArray(baseBindingList());
        allBindings.addObjectsFromArray(additionalBindingList());

        Enumeration enumeration = allBindings.objectEnumerator();

        while (enumeration.hasMoreElements()) {
            String key = (String) enumeration.nextElement();
            if (valueForKey(key) != null) {
                if (key.equals("payPalBusinessName")) {
                    boundValues.addObject(new NSDictionary(new Object[] { payPalBusinessName, "business"},
                            new Object[] {"value", "key"}));
                } else if (key.equals("userDefinableQuantity")) {
                    boundValues.addObject(new NSDictionary(new Object[] {
                            (userDefinableQuantity.booleanValue() ? "1" : "0"), "undefined_quantity"}, 
                            new Object[] {"value", "key"}));
                } else if (key.equals("itemName")) {
                    boundValues.addObject(new NSDictionary(new Object[] { itemName, "item_name"}, 
                            new Object[] {"value", "key"}));
                } else if (key.equals("itemNumber")) {
                    boundValues.addObject(new NSDictionary(new Object[] { itemNumber, "item_number"}, 
                            new Object[] {"value", "key"}));
                } else if (key.equals("custom")) {
                    boundValues.addObject(new NSDictionary(new Object[] { custom, "custom"}, 
                            new Object[] {"value", "key"}));
                } else if (key.equals("amount")) {
                    boundValues.addObject(new NSDictionary(new Object[] {
                            dollarFormatter.format(Double.valueOf(amount)), "amount"}, 
                            new Object[] {"value", "key"}));
                } else if (key.equals("currencyCode")) {
                    boundValues.addObject(new NSDictionary(new Object[] { currencyCode, "currency_code"}, 
                            new Object[] {"value", "key"}));
                } else if (key.equals("collectShippingAddress")) {
                    boundValues.addObject(new NSDictionary(new Object[] {
                            (collectShippingAddress.booleanValue() ? "0" : "1"), "no_shipping"}, 
                            new Object[] {"value", "key"}));
                } else if (key.equals("allowCustomerNote")) {
                    boundValues.addObject(new NSDictionary(new Object[] { (allowCustomerNote.booleanValue() ? "0" : "1"), "no_note"}, 
                            new Object[] { "value", "key"}));
                } else if (key.equals("logoURL")) {
                    boundValues.addObject(new NSDictionary(new Object[] { logoURL, "image_url"}, 
                            new Object[] {"value", "key"}));
                } else if (key.equals("returnURL")) {
                    boundValues.addObject(new NSDictionary(new Object[] { returnURL, "return"}, 
                            new Object[] { "value", "key"}));
                } else if (key.equals("cancelURL")) {
                    boundValues.addObject(new NSDictionary(new Object[] { cancelURL, "cancel_return"}, 
                            new Object[] {"value", "key"}));
                } else if (key.equals("notify_url") && useIPN.booleanValue()) {
                    boundValues.addObject(new NSDictionary(new Object[] { notifyURL, "notify_url"}, 
                            new Object[] {"value", "key"}));
                } else if (key.equals("tax_rate")) {
                    boundValues.addObject(new NSDictionary(new Object[] {
                            dollarFormatter.format(Double.valueOf(tax_rate)), "tax_rate"}, 
                            new Object[] {"value", "key"}));
                } else {
                    // received some other binding value somehow
                }

            }
        }

        return boundValues;
    }

    /**
     * Simple utility method that checks to see if the WOComponent should render
     * the image button or the submit button. Defaults to false, which means it
     * should show the submit button.
     * 
     * @return boolean
     */
    public boolean useImageButtonAsSmallBBoolean() { // default to false
        return (useImageButton != null) ? useImageButton.booleanValue() : false;
    }

    /**
     * additionalBindingList is a NSArray of bindings to pull when we
     * synchronize our values with the WOComponent's binding settings. It's a
     * simple way to customize the bindings that should be pulled, in addition
     * to the superclass' base list of bindings that it cares about
     * (baseBindingList()).
     * 
     * @return NSArray
     */
    @Override
    protected NSArray additionalBindingList() {
        NSMutableArray bindingsArray = new NSMutableArray();
        bindingsArray.addObjectsFromArray(new NSArray(new Object[] { "useImageButton"}));

        return bindingsArray;
    }

    /**
     * Manually synchronizes the values from the WOComponent. It does this by
     * enumerating first through the baseBindingList() and then the
     * additionalBindingList()
     */
    protected void pullBindings() {
        Enumeration enumeration = baseBindingList().objectEnumerator();

        while (enumeration.hasMoreElements()) {
            String key = (String) enumeration.nextElement();
            takeValueForKey(valueForBinding(key), key);
        }

        enumeration = additionalBindingList().objectEnumerator();

        while (enumeration.hasMoreElements()) {
            String key = (String) enumeration.nextElement();
            takeValueForKey(valueForBinding(key), key);
        }
    }

    /**
     * Resets the values pulled from the WOComponent to null.
     */
    @Override
    public void reset() {
        Enumeration enumeration = baseBindingList().objectEnumerator();

        while (enumeration.hasMoreElements()) {
            String key = (String) enumeration.nextElement();
            takeValueForKey(null, key);
        }

        enumeration = additionalBindingList().objectEnumerator();

        while (enumeration.hasMoreElements()) {
            String key = (String) enumeration.nextElement();
            takeValueForKey(null, key);
        }
        super.reset();
    }

    /**
     * Overrides the default behavior and tells the Component to synchronize its
     * ivar values with those bound to the WOComponent's bindings by calling
     * pullBindings()
     * 
     * @param r
     *            WOResponse
     * @param c
     *            WOContext
     */
    @Override
    public void appendToResponse(WOResponse r, WOContext c) {
        pullBindings();
        super.appendToResponse(r, c);
    }

    /**
     * Overrides the default behavior and tells the Component to synchronize its
     * ivar values with those bound to the WOComponent's bindings by calling
     * pullBindings()
     * 
     * @param r
     *            WORequest
     * @param c
     *            WOContext
     */
    @Override
    public void takeValuesFromRequest(WORequest r, WOContext c) {
        pullBindings();
        super.takeValuesFromRequest(r, c);
    }

    /**
     * Overrides the default behavior and tells the Component to synchronize its
     * ivar values with those bound to the WOComponent's bindings by calling
     * pullBindings()
     * 
     * @param r
     *            WOResponse
     * @param c
     *            WOContext
     * @return WOActionResults
     */
    @Override
    public WOActionResults invokeAction(WORequest r, WOContext c) {
        pullBindings();
        return super.invokeAction(r, c);
    }

}
