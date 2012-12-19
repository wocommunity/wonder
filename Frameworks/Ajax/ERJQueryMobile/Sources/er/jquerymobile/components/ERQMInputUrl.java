package er.jquerymobile.components;

import org.apache.log4j.Logger;

import com.webobjects.appserver.WOContext;

import er.extensions.foundation.ERXStringUtilities;

@SuppressWarnings("serial")
public class ERQMInputUrl extends ERQMInputBaseComponent {

  protected static final Logger log = Logger.getLogger(ERQMInputUrl.class);

  //********************************************************************
  //  Constructor
  //********************************************************************

  public ERQMInputUrl(WOContext aContext) {
    super(aContext);
  }

  //********************************************************************
  //  Methods
  //********************************************************************

  public String value() {
    return stringValueForBinding("value");
  }

  public boolean disabledInvert() {
    // No Url no Link
    if(ERXStringUtilities.stringIsNullOrEmpty(value())) {
      return true;
    }

    return !valueForBooleanBinding("disabled", false);
  }

}
