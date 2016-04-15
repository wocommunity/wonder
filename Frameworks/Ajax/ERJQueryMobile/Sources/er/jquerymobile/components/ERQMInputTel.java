package er.jquerymobile.components;

import com.webobjects.appserver.WOContext;

import er.extensions.appserver.ERXSession;
import er.extensions.foundation.ERXStringUtilities;

@SuppressWarnings("serial")
public class ERQMInputTel extends ERQMInputBaseComponent {

  //********************************************************************
  //  Constructor
  //********************************************************************

  public ERQMInputTel(WOContext aContext) {
    super(aContext);
  }

  //********************************************************************
  //  Methods
  //********************************************************************

  public String value() {
    return stringValueForBinding("value");
  }

  public boolean disabledInvert() {
    // No Tel no Link
    if(ERXStringUtilities.stringIsNullOrEmpty(value())) {
      return true;
    }

    // iPhone ? Link works only on iPhone anyway
    if(ERXSession.session().browser().isIPhone()) {
      return !valueForBooleanBinding("disabled", false);
    }

    return true;
  }

  public String href() {
    StringBuilder sb = new StringBuilder();
    sb.append("tel:");

    String s = countryNumber();
    if(!ERXStringUtilities.stringIsNullOrEmpty(s)) {
      sb.append('+');
      sb.append(s);

      String number = value();
      if(number.startsWith("0")) {
        number = number.substring(1);
      }
      number = ERXStringUtilities.removeCharacters(number, "-/");
      sb.append(number);
    } else {
      sb.append(value()); 
    }

    return sb.toString();
  }

  public String countryNumber() {
    return stringValueForBinding("countryNumber");
  }
}
