package er.ajax;

import com.webobjects.appserver.WOAssociation;
import com.webobjects.appserver.WOComponent;
import com.webobjects.foundation.NSArray;
import com.webobjects.foundation.NSDictionary;
import com.webobjects.foundation.NSMutableDictionary;

public class AjaxOption {
  public static AjaxOption.Type DEFAULT = new AjaxOption.Type(0);
  public static AjaxOption.Type STRING = new AjaxOption.Type(1);
  public static AjaxOption.Type SCRIPT = new AjaxOption.Type(2);
  public static AjaxOption.Type NUMBER = new AjaxOption.Type(3);
  public static AjaxOption.Type ARRAY = new AjaxOption.Type(4);
  public static AjaxOption.Type STRING_ARRAY = new AjaxOption.Type(5);
  public static AjaxOption.Type BOOLEAN = new AjaxOption.Type(6);

  public static class Type {
    private int myNumber;

    public Type(int _number) {
      myNumber = _number;
    }
  }

  private String myName;
  private AjaxOption.Type myType;

  public AjaxOption(String _name) {
    this(_name, AjaxOption.DEFAULT);
  }

  public AjaxOption(String _name, AjaxOption.Type _type) {
    myName = _name;
    myType = _type;
  }

  public String name() {
    return myName;
  }

  public AjaxOption.Type type() {
    return myType;
  }

  public String processValue(Object _value) {
    String strValue;
    if (_value == null) {
      strValue = null;
    }
    else if (myType == AjaxOption.STRING) {
      strValue = "'" + _value + "'";
    }
    else if (myType == AjaxOption.ARRAY) {
      if (_value instanceof NSArray) {
        NSArray arrayValue = (NSArray) _value;
        if (arrayValue.count() == 1) {
          strValue = arrayValue.objectAtIndex(0).toString();
        }
        else {
          strValue = "[" + arrayValue.componentsJoinedByString(",") + "]";
        }
      }
      else {
        strValue = _value.toString();
      }
    }
    else if (myType == AjaxOption.STRING_ARRAY) {
      if (_value instanceof NSArray) {
        NSArray arrayValue = (NSArray) _value;
        int count = arrayValue.count();
        if (count == 1) {
          strValue = "'" + arrayValue.objectAtIndex(0).toString() + "'";
        }
        else if (count > 0) {
          strValue = "['" + arrayValue.componentsJoinedByString("','") + "']";
        }
        else {
          strValue = "[]";
        }
      }
      else {
        strValue = _value.toString();
      }
    }
    else {
      strValue = _value.toString();
    }
    return strValue;
  }

  public void addToDictionary(WOComponent _component, NSMutableDictionary _dictionary) {
    addToDictionary(myName, _component, _dictionary);
  }

  public void addToDictionary(String _bindingName, WOComponent _component, NSMutableDictionary _dictionary) {
    Object value = _component.valueForBinding(_bindingName);
    if (value instanceof WOAssociation) {
      WOAssociation association = (WOAssociation) value;
      value = association.valueInComponent(_component);
    }
    String strValue = processValue(value);
    if (strValue != null) {
      _dictionary.setObjectForKey(strValue, myName);
    }
  }

  public void addToDictionary(WOComponent _component, NSDictionary _associations, NSMutableDictionary _dictionary) {
    addToDictionary(myName, _component, _associations, _dictionary);
  }

  public void addToDictionary(String _bindingName, WOComponent _component, NSDictionary _associations, NSMutableDictionary _dictionary) {
    Object value = _associations.objectForKey(_bindingName);
    if (value instanceof WOAssociation) {
      WOAssociation association = (WOAssociation) value;
      value = association.valueInComponent(_component);
    }
    String strValue = processValue(value);
    if (strValue != null) {
      _dictionary.setObjectForKey(strValue, myName);
    }
  }

  public static NSMutableDictionary createAjaxOptionsDictionary(NSArray _ajaxOptions, WOComponent _component) {
    NSMutableDictionary optionsDictionary = new NSMutableDictionary();
    int ajaxOptionCount = _ajaxOptions.count();
    for (int i = 0; i < ajaxOptionCount; i++) {
      AjaxOption ajaxOption = (AjaxOption) _ajaxOptions.objectAtIndex(i);
      ajaxOption.addToDictionary(_component, optionsDictionary);
    }
    return optionsDictionary;
  }

  public static NSMutableDictionary createAjaxOptionsDictionary(NSArray _ajaxOptions, WOComponent _component, NSDictionary _associations) {
    NSMutableDictionary optionsDictionary = new NSMutableDictionary();
    int ajaxOptionCount = _ajaxOptions.count();
    for (int i = 0; i < ajaxOptionCount; i++) {
      AjaxOption ajaxOption = (AjaxOption) _ajaxOptions.objectAtIndex(i);
      ajaxOption.addToDictionary(_component, _associations, optionsDictionary);
    }
    return optionsDictionary;
  }
}
