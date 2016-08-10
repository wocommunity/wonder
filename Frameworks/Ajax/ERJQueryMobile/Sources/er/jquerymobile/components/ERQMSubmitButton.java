package er.jquerymobile.components;

import com.webobjects.appserver.WOContext;

import er.extensions.foundation.ERXStringUtilities;

@SuppressWarnings("serial")
public class ERQMSubmitButton extends ERQMInputBaseComponent {

  //********************************************************************
  //  Constructor
  //********************************************************************

  public ERQMSubmitButton(WOContext aContext) {
    super(aContext);
  }

  //********************************************************************
  //  Methods
  //********************************************************************

  public String cssClass() {
    String result = stringValueForBinding("class", "");

    if(booleanValueForBinding("disabled", false)) {
      result = ERXStringUtilities.stringByAppendingCSSClass(result, "ui-disabled");
    }
    return result;
  }

  public boolean hasActionBinding() {
    return hasBinding("action");
  }

}
