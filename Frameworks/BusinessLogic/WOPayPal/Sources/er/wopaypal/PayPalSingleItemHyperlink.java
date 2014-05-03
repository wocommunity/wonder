package er.wopaypal;

import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;
import java.text.DecimalFormat;
import java.text.DecimalFormatSymbols;
import java.util.Enumeration;
import java.util.Locale;

import org.apache.commons.lang.CharEncoding;

import com.webobjects.appserver.WOActionResults;
import com.webobjects.appserver.WOContext;
import com.webobjects.appserver.WORequest;
import com.webobjects.appserver.WOResponse;
import com.webobjects.foundation.NSArray;
import com.webobjects.foundation.NSLog;
import com.webobjects.foundation.NSMutableArray;

public class PayPalSingleItemHyperlink extends PayPalSingleItemLinkBase {

    /** The cgi command we're calling on PayPal's server.
     */
    protected static String PAYPAL_CGI_COMMAND = "?cmd=_xclick";

    /** Constructor
     *
     * @param context WOContext
     */
    public PayPalSingleItemHyperlink(WOContext context) {
        super(context);
    }

    
    /** Makes the component stateless.
     *
     * @return boolean
     */
    @Override
    public boolean isStateless() { return true; }

    /** Assembles the url to send to PayPal for the single item purchase
     *
     *  @return String
     */
    public String payPalPurchaseHref() {
        StringBuffer sb = WOPayPal.baseUrl();
        sb.append(PayPalSingleItemLinkBase.PAYPAL_CGI_NAME);
        sb.append(PAYPAL_CGI_COMMAND);
        sb.append(payPalUrlParams());

        return sb.toString();
    }

    /** Assembles the string of parameters for the payPalPurchaseHref.  It returns the binding values encoded into the format PayPal expects in order to successfully process the url params.
     *
     *  @return String
     */
    public String payPalUrlParams() { // this should probably have much more robust error handling
        DecimalFormat currencyFormatter = new DecimalFormat("##0.00", new DecimalFormatSymbols(Locale.US));
        DecimalFormat taxFormatter = new DecimalFormat("##0.000", new DecimalFormatSymbols(Locale.US));

        StringBuilder sb = new StringBuilder();
        sb.append("&business=" + urlEncode(payPalBusinessName) ); // required!!!
        if (userDefinableQuantity!=null) {
            sb.append("&undefined_quantity=" + (userDefinableQuantity.booleanValue() ? "1" : "0") );  
        }
        if (itemName != null) {
            sb.append("&item_name=" + urlEncode(itemName) );
        }
        if (itemNumber != null) {
            sb.append("&item_number=" + urlEncode(itemNumber) );
        }
        if (custom != null) {
            sb.append("&custom=" + urlEncode(custom) );
        }
        if (amount != null) {
          sb.append("&amount=" + currencyFormatter.format(Double.valueOf(amount)) );  
        }
        if (currencyCode != null) {
            sb.append("&currency_code=" + urlEncode(currencyCode) );
        }
        if (collectShippingAddress != null) {
            sb.append("&no_shipping=" + (collectShippingAddress.booleanValue() ? "0" : "1") );
        }
        if (allowCustomerNote != null) {
           sb.append("&no_note=" + (allowCustomerNote.booleanValue() ? "0" : "1") ); 
        }
        if (logoURL != null) {
            sb.append("&image_url=" + PayPalEmailURLUTF8Encoder.encode(logoURL) );
        }
        if (returnURL != null) {
           sb.append("&return=" + PayPalEmailURLUTF8Encoder.encode(returnURL) );
        }
        if (cancelURL != null) {
           sb.append("&cancel_return=" + PayPalEmailURLUTF8Encoder.encode(cancelURL) );
        }
        if (notifyURL != null) {
            sb.append("&notify_url=" + PayPalEmailURLUTF8Encoder.encode(notifyURL) );
        } else {
            if (useIPN != null && useIPN.booleanValue()) {
                sb.append("&notify_url=" + PayPalEmailURLUTF8Encoder.encode(defaultNotificationURL()) );
            }  
        }
        if (tax_rate != null) {
        	sb.append("&tax_rate=" + taxFormatter.format(Double.valueOf(tax_rate)) );  
        }   

        return sb.toString();
    }
    
    /**
     * URL encodes the input string.
     * @param input to encode
     * @return the encoded input string or null if the encoding fails
     */
    private String urlEncode(String input) {
        String output = null;
        if (input != null) {
          try {
              output = URLEncoder.encode(input, CharEncoding.UTF_8);
          } catch (UnsupportedEncodingException uee) {
              NSLog.err.appendln("Could not URL encode input string.  Error: " + uee.getMessage());
          }
        }
        
        return output;
    }

    /** additionalBindingList is a NSArray of bindings to pull when we synchronize our values with the WOComponent's binding settings.  It's a simple way to customize the bindings that should be pulled, in addition to the superclass' base list of bindings that it cares about (baseBindingList()).
     *
     * @return NSArray
     */
    @Override
    protected NSArray additionalBindingList() {
        NSMutableArray bindingArray = new NSMutableArray(); // super.bindingList();
        bindingArray.addObjectsFromArray(new NSArray(new Object[] {}));
        return bindingArray;
    }

    /** Manually synchronizes the values from the WOComponent.  It does this by enumerating first through the baseBindingList() and then the additionalBindingList()
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

    /** Resets the values pulled from the WOComponent to null.
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

    /** Overrides the default behavior and tells the Component to synchronize its ivar values with those bound to the WOComponent's bindings by calling pullBindings()
     *
     * @param r WOResponse
     * @param c WOContext
     */
    @Override
    public void appendToResponse(WOResponse r, WOContext c) {
        pullBindings();
        super.appendToResponse(r,c);
    }

    /** Overrides the default behavior and tells the Component to synchronize its ivar values with those bound to the WOComponent's bindings by calling pullBindings()
     *
     * @param r WORequest
     * @param c WOContext
     */
    @Override
    public void takeValuesFromRequest(WORequest r, WOContext c) {
        pullBindings();
        super.takeValuesFromRequest(r,c);
    }

    /** Overrides the default behavior and tells the Component to synchronize its ivar values with those bound to the WOComponent's bindings by calling pullBindings()
     *
     * @param r WOResponse
     * @param c WOContext
     * @return WOActionResults
     */
    @Override
    public WOActionResults invokeAction(WORequest r, WOContext c) {
        pullBindings();
        return super.invokeAction(r,c);
    }

}
