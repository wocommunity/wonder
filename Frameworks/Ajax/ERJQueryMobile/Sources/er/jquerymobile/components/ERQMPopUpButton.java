package er.jquerymobile.components;

import com.webobjects.appserver.WOContext;

import er.extensions.foundation.ERXStringUtilities;

@SuppressWarnings("serial")
public class ERQMPopUpButton extends ERQMInputBaseComponent {

  //********************************************************************
  //  Constructor
  //********************************************************************

  public ERQMPopUpButton(WOContext aContext) {
    super(aContext);
  }

  //********************************************************************
  //  Methods
  //********************************************************************

  public String html() {
    StringBuilder sb = new StringBuilder();
    if(booleanValueForBinding("isPopUpWindow", false)) {
      sb.append("data-native-menu=\"false\"");
    }

    String s = miniVersion();
    if(!ERXStringUtilities.stringIsNullOrEmpty(s)) {
      sb.append(" " + s);
    }

    return sb.toString();
  }
}
