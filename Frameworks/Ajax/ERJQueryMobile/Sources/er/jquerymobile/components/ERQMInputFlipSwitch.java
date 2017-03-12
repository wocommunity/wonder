package er.jquerymobile.components;

import com.webobjects.appserver.WOContext;
import com.webobjects.foundation.NSArray;

@SuppressWarnings("serial")
public class ERQMInputFlipSwitch extends ERQMInputBaseComponent {

  //********************************************************************
  //  Constructor
  //********************************************************************

  public ERQMInputFlipSwitch(WOContext aContext) {
    super(aContext);
  }

  //********************************************************************
  //  Methods
  //********************************************************************

  private String on() {
    return stringValueForBinding("stringOn", "On");
  }

  private String off() {
    return stringValueForBinding("stringOff", "Off");
  }

  public NSArray<String> list() {
    return new NSArray<String>(off(), on());
  }

}
