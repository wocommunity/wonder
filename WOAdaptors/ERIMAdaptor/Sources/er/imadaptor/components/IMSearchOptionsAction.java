package er.imadaptor.components;

import com.webobjects.appserver.WOElement;
import com.webobjects.foundation.NSArray;
import com.webobjects.foundation.NSDictionary;

/**
 * IMSearchOptionsAction allows you to search your options for the
 * AIM response that is received.  For instance, you can pass in 
 * an options dictionary that maps the word "Company XYZ" to the object
 * CompanyXYZ, or the word "Company ABC" to the object CompanyABC.  
 * If the word "XYZ" is aimed, it will return the matching CompanyXYZ
 * object as its value.
 * 
 * You can optionally set quicksilver to true if you want string
 * matching to behave like Quicksilver.
 * 
 * @author mschrag
 */
public class IMSearchOptionsAction extends AbstractIMSearchAction {
  public IMSearchOptionsAction(String _name, NSDictionary _associations, WOElement _element) {
    super(_name, _associations, _element);
  }

  protected boolean searchInsideMessage() {
    return false;
  }

  public static NSArray selectedValues(NSDictionary _options, boolean _quicksilver, String _message) {
    return AbstractIMSearchAction.selectedValues(_options, _quicksilver, _message, false);
  }

  public static NSArray selectedValues(NSArray _options, String _optionKeyPath, boolean _quicksilver, String _message) {
    return AbstractIMSearchAction.selectedValues(_options, _optionKeyPath, _quicksilver, _message, false);
  }
}
