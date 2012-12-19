package er.jquerymobile.components;

import org.apache.log4j.Logger;

import com.webobjects.appserver.WOContext;
import com.webobjects.foundation.NSArray;

@SuppressWarnings("serial")
public class ERQMInputFlipSwitch extends ERQMInputBaseComponent {

  protected static final Logger log = Logger.getLogger(ERQMInputFlipSwitch.class);

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
