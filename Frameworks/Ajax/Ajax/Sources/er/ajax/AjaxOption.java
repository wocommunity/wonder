package er.ajax;

import com.webobjects.appserver.WOAssociation;
import com.webobjects.appserver.WOComponent;
import com.webobjects.foundation.NSArray;
import com.webobjects.foundation.NSDictionary;
import com.webobjects.foundation.NSMutableDictionary;

/**
 * AjaxOption provides a bridge between a binding value in an AjaxComponent or AjaxDynamicElement and one JavaScript formatted
 * key-value pair in a dictionary.  An AjaxOption consists of four values:
 * <ul>
 * 	<li><b>Name</b>: the key value in the dictionary</li>
 * 	<li><b>Binding Name</b>: the name of the binding in an AjaxComponent or AjaxDynamicElement to get the Java value from</li>
 * 	<li><b>Default Value</b>: optional default value if binding name evaluates to a null value</li>
 * 	<li><b>Type</b>: one of the AjaxOption.Type constants in AjaxOption</li>
 * </ul>
 *
 * @see AjaxOptions
 * @see AjaxValue
 */
public class AjaxOption {
  public static final AjaxOption.Type DEFAULT = new AjaxOption.Type(0);
  public static final AjaxOption.Type STRING = new AjaxOption.Type(1);
  public static final AjaxOption.Type SCRIPT = new AjaxOption.Type(2);
  public static final AjaxOption.Type NUMBER = new AjaxOption.Type(3);
  public static final AjaxOption.Type ARRAY = new AjaxOption.Type(4);
  public static final AjaxOption.Type STRING_ARRAY = new AjaxOption.Type(5);
  public static final AjaxOption.Type BOOLEAN = new AjaxOption.Type(6);
  public static final AjaxOption.Type STRING_OR_ARRAY = new AjaxOption.Type(7);
  public static final AjaxOption.Type DICTIONARY = new AjaxOption.Type(8);
  public static final AjaxOption.Type FUNCTION = new AjaxOption.Type(9);	// Function with no args
  public static final AjaxOption.Type FUNCTION_1 = new AjaxOption.Type(9);// Function with one arg
  public static final AjaxOption.Type FUNCTION_2 = new AjaxOption.Type(9);// Function with two args
  
  
  /**
   * AjaxOption.Type is a simple enumeration of the types that AjaxValue can interpret.
   */
  public static class Type {
    private int _number;

	// This class might make more sense on AjaxValue?
    public Type(int number) {
      _number = number;
    }
  }

  private String _name;
  private String _bindingName;
  private Object _defaultValue;
  private AjaxOption.Type _type;

  /*
   * Creates an AjaxOption with:
   * <ul>
   * 	<li>Name: name</li>
   * 	<li>Binding Name: name</li>
   * 	<li>Default Value: none</li>
   * 	<li>Type: AjaxOption.DEFAULT</li>
   * </ul> 
   */
  public AjaxOption(String name) {
    this(name, name, null, AjaxOption.DEFAULT);
  }

  /*
   * Creates an AjaxOption with:
   * <ul>
   * 	<li>Name: name</li>
   * 	<li>Binding Name: name</li>
   * 	<li>Default Value: none</li>
   * 	<li>Type: type</li>
   * </ul> 
   */
  public AjaxOption(String name, AjaxOption.Type type) {
	this(name, name, null, type);
  }

  /*
   * Creates an AjaxOption with:
   * <ul>
   * 	<li>Name: name</li>
   * 	<li>Binding Name: name</li>
   * 	<li>Default Value: defaultValue</li>
   * 	<li>Type: type</li>
   * </ul> 
   */
  public AjaxOption(String name, Object defaultValue, AjaxOption.Type type) {
	this(name, name, defaultValue, type);
  }
  
  /*
   * Creates an AjaxOption with:
   * <ul>
   * 	<li>Name: name</li>
   * 	<li>Binding Name: bindingName</li>
   * 	<li>Default Value: defaultValue</li>
   * 	<li>Type: type</li>
   * </ul> 
   */
  public AjaxOption(String name, String bindingName, Object defaultValue, AjaxOption.Type type) {
    _name = name;
    _bindingName = bindingName;
    _type = type;
    _defaultValue = defaultValue;
  }

  public String name() {
    return _name;
  }

  public AjaxOption.Type type() {
    return _type;
  }

  /**
   * @param obj the Object to return an AjaxValue for
   * @return an AjaxValue encapsulating obj with the same type as this AjaxOption
   */
  public AjaxValue valueForObject(Object obj) {
	  return new AjaxValue(_type, obj);
  }

  public Object defaultValue() {
	  return _defaultValue;
  }
  
  /*
   * Bridge to an AjaxComponent.
   */
  protected Object valueInComponent(WOComponent component) {
	Object value = component.valueForBinding(_bindingName);
    if (value instanceof WOAssociation) {
      WOAssociation association = (WOAssociation) value;
      value = association.valueInComponent(component);
    }
	if (value == null) {
		value = _defaultValue;
	}
    return value;
  }
  
  /*
   * Bridge to an AjaxDynamicElement.
   */
  protected Object valueInComponent(WOComponent component, NSDictionary<String, ? extends WOAssociation> associations) {
	Object value = null;
	if (associations != null) {
		value = associations.objectForKey(_bindingName);
		// This is needed for the double step to resolve the value for ^ notation
		if (value instanceof WOAssociation) {
			WOAssociation association = (WOAssociation) value;
			value = association.valueInComponent(component);
		}
	}
	if (value == null) {
		value = _defaultValue;
	}
    return value;
  }
  
  /**
   * Evaluates this AjaxOption on a WOComponent and adds the name and JavaScript formatted value to dictionary.
   * 
   * @param component WOComponent to get binding value from
   * @param dictionary mutable dictionary to add key-value pair to
   */
  public void addToDictionary(WOComponent component, NSMutableDictionary<String, String> dictionary) {
    Object value = valueInComponent(component);
    String strValue = valueForObject(value).javascriptValue();
    if (strValue != null) {
      dictionary.setObjectForKey(strValue, _name);
    }
  }

  /**
   * Evaluates this AjaxOption on a WODynamicElement and adds the name and JavaScript formatted value to dictionary.
   * 
   * @param component WOComponent to get binding value from
   * @param associations dictionary of associations to get WOAssocation providing value from
   * @param dictionary mutable dictionary to add key-value pair to
   */
  protected void addToDictionary(WOComponent component, NSDictionary<String, ? extends WOAssociation> associations, NSMutableDictionary<String, String> dictionary) {
	Object value = valueInComponent(component, associations);
    String strValue = valueForObject(value).javascriptValue();
    if (strValue != null) {
      dictionary.setObjectForKey(strValue, _name);
    }
  }

  /**
   * @param ajaxOptions list of AjaxOption to evaluate on component
   * @param component WOComponent to get binding value from
   *
   * @return dictionary produced by evaluating the array of AjaxOption on a WOComponent and adding the resulting name and JavaScript formatted values
   */
  public static NSMutableDictionary<String, String> createAjaxOptionsDictionary(NSArray<AjaxOption> ajaxOptions, WOComponent component) {
	NSMutableDictionary<String, String> optionsDictionary = new NSMutableDictionary<>();
    for (AjaxOption ajaxOption : ajaxOptions) {
      ajaxOption.addToDictionary(component, optionsDictionary);
    }
    return optionsDictionary;
  }
  
  /**
   * @param ajaxOptions list of AjaxOption to evaluate on component
   * @param component WOComponent to get binding value from
   * @param associations dictionary of associations to get WOAssocation providing value from
   *
   * @return dictionary produced by evaluating the array of AjaxOption on a WOComponent and adding the resulting name and JavaScript formatted values
   */
  public static NSMutableDictionary<String, String> createAjaxOptionsDictionary(NSArray<AjaxOption> ajaxOptions, WOComponent component, NSDictionary<String, ? extends WOAssociation> associations) {
    NSMutableDictionary<String, String> optionsDictionary = new NSMutableDictionary<>();
    for (AjaxOption ajaxOption : ajaxOptions) {
      ajaxOption.addToDictionary(component, associations, optionsDictionary);
    }
    return optionsDictionary;
  }
}
