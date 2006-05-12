package er.imadaptor.components;

import com.webobjects.appserver.WOElement;
import com.webobjects.foundation.NSArray;
import com.webobjects.foundation.NSDictionary;

/**
 * IMSearchMessageAction allows you to map substrings that appear
 * in AIM message responses to other objects.  For instance, you can pass in 
 * an options dictionary that maps the word "hi" to the object
 * Greeting, or the word "bug" to the object BugReport.  If the word "hi" 
 * appears in the aim response, it will return the matching object as
 * its value.
 * 
 * You can optionally set quicksilver to true if you want string
 * matching to behave like Quicksilver.
 * 
 * @author mschrag
 */
public class IMSearchMessageAction extends AbstractIMSearchAction {
  public IMSearchMessageAction(String _name, NSDictionary _associations, WOElement _element) {
    super(_name, _associations, _element);
  }

  protected boolean searchInsideMessage() {
    return true;
  }

  public static NSArray selectedValues(NSDictionary _options, boolean _quicksilver, String _message) {
    return AbstractIMSearchAction.selectedValues(_options, _quicksilver, _message, true);
  }

  public static NSArray selectedValues(NSArray _options, String _optionKeyPath, boolean _quicksilver, String _message) {
    return AbstractIMSearchAction.selectedValues(_options, _optionKeyPath, _quicksilver, _message, true);
  }
}
