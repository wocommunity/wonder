package er.jquerymobile.components;

import com.webobjects.appserver.WOContext;

import er.extensions.components.ERXStatelessComponent;

@SuppressWarnings("serial")
public class ERQMDialogLink extends ERXStatelessComponent {

  //********************************************************************
  //  Constructor
  //********************************************************************

  public ERQMDialogLink(WOContext aContext) {
    super(aContext);
  }

  //********************************************************************
  //  Methods
  //********************************************************************

  public boolean hasAction() {
    return hasBinding("action");
  }

  public String isButton() {
    boolean b = booleanValueForBinding("isButton", true);
    return b ? "button" : "";
  }

}
