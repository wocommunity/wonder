package er.imadaptor.components;

import com.webobjects.appserver.WOElement;
import com.webobjects.foundation.NSArray;
import com.webobjects.foundation.NSDictionary;

/**
 * IMSearchMessageAction allows you to map substrings that appear in AIM message responses to other objects. For
 * instance, you can pass in an options dictionary that maps the word "hi" to the object Greeting, or the word "bug" to
 * the object BugReport. If the word "hi" appears in the aim response, it will return the matching object as its value.
 * 
 * You can optionally set quicksilver to true if you want string matching to behave like Quicksilver.
 * 
 * @author mschrag
 */
public class IMSearchMessageAction extends AbstractIMSearchAction {
	public IMSearchMessageAction(String name, NSDictionary associations, WOElement element) {
		super(name, associations, element);
	}

	@Override
	protected boolean searchInsideMessage() {
		return true;
	}

	public static NSArray selectedValues(NSDictionary options, boolean quicksilver, String message) {
		return AbstractIMSearchAction.selectedValues(options, quicksilver, message, true);
	}

	public static NSArray selectedValues(NSArray options, String optionKeyPath, boolean quicksilver, String message) {
		return AbstractIMSearchAction.selectedValues(options, optionKeyPath, quicksilver, message, true);
	}
}
