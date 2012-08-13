package er.jquerymobile.components;

import org.apache.log4j.Logger;

import com.webobjects.appserver.WOContext;

import er.extensions.components.ERXStatelessComponent;

@SuppressWarnings("serial")
public class ERQMDialogLink extends ERXStatelessComponent {

  protected static final Logger log = Logger.getLogger(ERQMDialogLink.class);

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
