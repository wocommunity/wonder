package er.jquerymobile.components;

import com.webobjects.appserver.WOContext;

@SuppressWarnings("serial")
public class ERQMNavBarElement extends ERQMInputBaseComponent {

  //********************************************************************
  //  Constructor
  //********************************************************************

  public ERQMNavBarElement(WOContext aContext) {
    super(aContext);
  }

  //********************************************************************
  //  Methods
  //********************************************************************

  public String selected() {
    return booleanValueForBinding("isSelected", false) ? "ui-btn-active" : "";
  }

  public boolean hasAction() {
    return hasBinding("action");
  }

}
