package er.imadaptor.components;

import java.util.Enumeration;

import com.webobjects.appserver.WOAssociation;
import com.webobjects.appserver.WOComponent;
import com.webobjects.appserver.WOContext;
import com.webobjects.appserver.WOElement;
import com.webobjects.appserver.WORequest;
import com.webobjects.appserver._private.WODynamicElementCreationException;
import com.webobjects.foundation.NSArray;
import com.webobjects.foundation.NSDictionary;
import com.webobjects.foundation.NSKeyValueCodingAdditions;
import com.webobjects.foundation.NSMutableArray;

import er.extensions.ERXStringUtilities;
import er.imadaptor.InstantMessengerAdaptor;

abstract class AbstractIMSearchAction extends IMAction {
  private WOAssociation myValue;
  private WOAssociation myValues;
  private WOAssociation myQuicksilver;
  private WOAssociation myOptionsDictionary;
  private WOAssociation myOptionsArray;
  private WOAssociation myOptionKeyPath;

  public AbstractIMSearchAction(String _name, NSDictionary _associations, WOElement _element) {
    super(_name, _associations, _element);
    myValue = (WOAssociation) _associations.objectForKey("value");
    myValues = (WOAssociation) _associations.objectForKey("values");
    if (myValue == null && myValues == null) {
      throw new WODynamicElementCreationException("Only one of 'value' or 'values' can be bound at any time.");
    }
    myQuicksilver = (WOAssociation) _associations.objectForKey("quicksilver");
    myOptionsDictionary = (WOAssociation) _associations.objectForKey("optionsDictionary");
    myOptionsArray = (WOAssociation) _associations.objectForKey("optionsArray");
    if (myOptionsArray == null && myOptionsDictionary == null) {
      throw new WODynamicElementCreationException("Only one of 'optionsArray' or 'optionsDictionary' can be bound at any time.");
    }
    if (myOptionsArray != null && myOptionsDictionary != null) {
      throw new WODynamicElementCreationException("Both 'optionsArray' and 'optionsDictionary' cannot be bound at the same time.");
    }
    myOptionKeyPath = (WOAssociation) _associations.objectForKey("optionKeyPath");
    if (myOptionKeyPath != null && myOptionsDictionary != null) {
      throw new WODynamicElementCreationException("Both 'optionKeyPath' and 'optionsDictionary' cannot be bound at the same time.");
    }
  }
  
  protected abstract boolean searchInsideMessage();

  public static NSArray selectedValues(NSDictionary _options, boolean _quicksilver, String _message, boolean _searchInsideMessage) {
    NSMutableArray selectedValues = new NSMutableArray();
    String message = _message.toLowerCase();
    Enumeration keyEnum = _options.keyEnumerator();
    while (keyEnum.hasMoreElements()) {
      String key = (String) keyEnum.nextElement();

      String stringToSearch;
      String stringToSearchFor;
      if (_searchInsideMessage) {
        stringToSearch = _message;
        stringToSearchFor = key;
      }
      else {
        stringToSearch = key;
        stringToSearchFor = _message;
      }

      String selectedKey = null;
      if (_quicksilver && ERXStringUtilities.quicksilverContains(stringToSearch, stringToSearchFor)) {
        selectedKey = key;
      }
      else if (!_quicksilver && stringToSearch.indexOf(stringToSearchFor) != -1) {
        selectedKey = key;
      }
      
      if (selectedKey != null) {
        Object selectedValue = _options.objectForKey(selectedKey);
        selectedValues.addObject(selectedValue);
      }
    }
    return selectedValues;
  }

  public static NSArray selectedValues(NSArray _options, String _optionKeyPath, boolean _quicksilver, String _message, boolean _searchInsideMessage) {
    NSMutableArray selectedValues = new NSMutableArray();
    String message = _message.toLowerCase();
    Enumeration optionEnum = _options.objectEnumerator();
    while (optionEnum.hasMoreElements()) {
      Object option = optionEnum.nextElement();
      String optionStr;
      if (_optionKeyPath == null) {
        optionStr = option.toString();
      }
      else {
        Object optionKeyPathValue = NSKeyValueCodingAdditions.Utility.valueForKeyPath(option, _optionKeyPath);
        if (optionKeyPathValue == null) {
          optionStr = "";
        }
        else {
          optionStr = optionKeyPathValue.toString();
        }
      }

      String stringToSearch;
      String stringToSearchFor;
      if (_searchInsideMessage) {
        stringToSearch = _message;
        stringToSearchFor = optionStr;
      }
      else {
        stringToSearch = optionStr;
        stringToSearchFor = _message;
      }

      if (_quicksilver && ERXStringUtilities.quicksilverContains(stringToSearch, stringToSearchFor)) {
        selectedValues.addObject(option);
      }
      else if (!_quicksilver && stringToSearch.indexOf(stringToSearchFor) != -1) {
        selectedValues.addObject(option);
      }
    }
    return selectedValues;
  }

  protected void actionInvoked(WORequest _request, WOContext _context) {
    WOComponent component = _context.component();
    String message = InstantMessengerAdaptor.message(_request);
    boolean quicksilver = (myQuicksilver != null && ((Boolean) myQuicksilver.valueInComponent(component)).booleanValue());
    NSArray selectedValues;
    if (myOptionsDictionary != null) {
      NSDictionary options = (NSDictionary) myOptionsDictionary.valueInComponent(component);
      selectedValues = AbstractIMSearchAction.selectedValues(options, quicksilver, message, searchInsideMessage());
    }
    else if (myOptionsArray != null) {
      NSArray options = (NSArray) myOptionsArray.valueInComponent(component);
      String optionKeyPath = null;
      if (myOptionKeyPath != null) {
        optionKeyPath = (String) myOptionKeyPath.valueInComponent(component);
      }
      selectedValues = AbstractIMSearchAction.selectedValues(options, optionKeyPath, quicksilver, message, searchInsideMessage());
    }
    else {
      throw new IllegalArgumentException("You must specify either optionsDictionary or optionsArray.");
    }
    if (myValues != null) {
      myValues.setValue(selectedValues, component);
    }
    if (myValue != null) {
      Object selectedValue = null;
      if (selectedValues.count() == 1) {
        selectedValue = selectedValues.objectAtIndex(0);
      }
      myValue.setValue(selectedValue, component);
    }
  }

}
