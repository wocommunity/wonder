package er.jquerymobile.components;

import org.apache.log4j.Logger;

import com.webobjects.appserver.WOContext;

@SuppressWarnings("serial")
public class ERQMNavBarElement extends ERQMInputBaseComponent {

  protected static final Logger log = Logger.getLogger(ERQMNavBarElement.class);

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
