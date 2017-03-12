package er.jquerymobile.components;

import com.webobjects.appserver.WOContext;

@SuppressWarnings("serial")
public class ERQMCheckbox extends ERQMInputBaseComponent {

  //********************************************************************
  //  Constructor
  //********************************************************************

  public ERQMCheckbox(WOContext aContext) {
    super(aContext);
  }

  //********************************************************************
  //  Methods
  //********************************************************************

  public boolean isInset() {
    return booleanValueForBinding("inset", false);
  }

}
