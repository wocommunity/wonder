package er.extensions.appserver;

import com.webobjects.appserver._private.WOClassicAdaptor;
import com.webobjects.foundation.NSDictionary;

/**
 * ERXDefaultAdaptor extends WODefaultAdaptor and makes its optional
 * SSL-enabling constructor public.
 * 
 * @author mschrag
 */
public class ERXDefaultAdaptor extends WOClassicAdaptor {
	/**
	 * Constructs an ERXDefaultAdaptor.
	 * 
	 * @param name the name of the adaptor
	 * @param parameters the adaptor parameters (see WODefaultAdaptor's)
	 */
	public ERXDefaultAdaptor(String name, NSDictionary parameters) {
		super(name, parameters);
	}

	/**
	 * Constructs an ERXDefaultAdaptor.
	 * 
	 * @param name the name of the adaptor
	 * @param parameters the adaptor parameters (see WODefaultAdaptor's)
	 * @param secure true = SSL, false = regular
	 */
	public ERXDefaultAdaptor(String name, NSDictionary parameters, boolean secure) {
		super(name, parameters, secure);
	}
}
