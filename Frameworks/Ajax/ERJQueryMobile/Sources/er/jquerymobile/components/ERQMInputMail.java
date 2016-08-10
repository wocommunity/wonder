package er.jquerymobile.components;

import com.webobjects.appserver.WOContext;

import er.extensions.foundation.ERXStringUtilities;

@SuppressWarnings("serial")
public class ERQMInputMail extends ERQMInputBaseComponent {

  //********************************************************************
  //  Constructor
  //********************************************************************

  public ERQMInputMail(WOContext aContext) {
    super(aContext);
  }

  //********************************************************************
  //  Methods
  //********************************************************************

  public String value() {
    return stringValueForBinding("value");
  }

  public boolean disabledInvert() {
    // No Mail no Link
    if(ERXStringUtilities.stringIsNullOrEmpty(value())) {
      return true;
    }

    return !valueForBooleanBinding("disabled", false);
  }

  public String href() {
    StringBuilder sb = new StringBuilder();
    sb.append("mailto:");
    sb.append(value()); 

    return sb.toString();
  }
}
