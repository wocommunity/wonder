import com.webobjects.appserver.WOContext;
import com.webobjects.foundation.NSArray;

import er.ajax.AjaxGridNavBar;

public class AjaxGridExampleNavBar extends AjaxGridNavBar {

	public NSArray<String> batchSizes = new NSArray<String>(new String[] { "2", "4", "6", "8", "10" });
	public String batchSize;

	public AjaxGridExampleNavBar(WOContext context) {
		super(context);
	}

	/**
	 * @return value for BatchSizes from application configuration
	 */
	@Override
	public NSArray batchSizes() {
		return batchSizes;
	}
}
