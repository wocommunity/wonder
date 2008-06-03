package er.imadaptor.components;

import com.webobjects.appserver.WOElement;
import com.webobjects.foundation.NSArray;
import com.webobjects.foundation.NSDictionary;

/**
 * IMSearchOptionsAction allows you to search your options for the AIM response that is received. For instance, you can
 * pass in an options dictionary that maps the word "Company XYZ" to the object CompanyXYZ, or the word "Company ABC" to
 * the object CompanyABC. If the word "XYZ" is aimed, it will return the matching CompanyXYZ object as its value.
 * 
 * You can optionally set quicksilver to true if you want string matching to behave like Quicksilver.
 * 
 * @author mschrag
 */
public class IMSearchOptionsAction extends AbstractIMSearchAction {
	public IMSearchOptionsAction(String name, NSDictionary associations, WOElement element) {
		super(name, associations, element);
	}

	@Override
	protected boolean searchInsideMessage() {
		return false;
	}

	public static NSArray selectedValues(NSDictionary options, boolean quicksilver, String message) {
		return AbstractIMSearchAction.selectedValues(options, quicksilver, message, false);
	}

	public static NSArray selectedValues(NSArray options, String optionKeyPath, boolean quicksilver, String message) {
		return AbstractIMSearchAction.selectedValues(options, optionKeyPath, quicksilver, message, false);
	}
}
