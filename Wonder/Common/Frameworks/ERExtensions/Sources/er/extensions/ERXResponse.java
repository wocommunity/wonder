package er.extensions;

import com.webobjects.appserver.WOResponse;

/**
 * ERXResponse provides a place to override methods of WOResponse.  This is returned 
 * by default from ERXApplication.
 * 
 * @author mschrag
 */
public class ERXResponse extends WOResponse {
	/**
	 * The original _appendTagAttributeAndValue would skip null values, but not blank
	 * values, which would produce html like &lt;div style = ""&gt;.  This implementation
	 * also skips blank values.
	 */
	public void _appendTagAttributeAndValue(String name, String value, boolean escape) {
		if (value != null && value.length() > 0) {
			super._appendTagAttributeAndValue(name, value, escape);
		}
	}
}
