package er.iui.components;

import com.webobjects.appserver.WOContext;

import er.extensions.components.ERXComponent;
import er.extensions.foundation.ERXStringUtilities;

/**
 *
 * @binding checked (required)
 * @binding id
 * @binding offString
 * @binding onchange
 * @binding onString
 */
public class ERIUIToggleButton extends ERXComponent {
  private String _id;

  public ERIUIToggleButton(WOContext context) {
    super(context);
  }

  @Override
  public boolean synchronizesVariablesWithBindings() {
    return false;
  }

  public String divID() {
    return id() + "_div";
  }
  
  public String id() {
    String id = _id;
    if (id == null) {
      id = stringValueForBinding("id");
      if (id == null) {
        id = ERXStringUtilities.safeIdentifierName(context().elementID());
      }
      _id = id;
    }
    return id;
  }

  public String toggleClass() {
    return "toggle toggled_" + value();
  }
  
  public void setValue(String value) {
    setValueForBinding(Boolean.valueOf("on".equals(value)), "checked");
  }

  public String value() {
    return booleanValueForBinding("checked") ? "on" : "off";
  }

  public String onclick() {
    return "toggleButtonToggled('" + id() + "')";
  }
}
