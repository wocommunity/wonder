package er.jquerymobile.components;

import org.apache.log4j.Logger;

import com.webobjects.appserver.WOContext;

import er.extensions.foundation.ERXStringUtilities;

@SuppressWarnings("serial")
public class ERQMSubmitButton extends ERQMInputBaseComponent {

  protected static final Logger log = Logger.getLogger(ERQMSubmitButton.class);

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
