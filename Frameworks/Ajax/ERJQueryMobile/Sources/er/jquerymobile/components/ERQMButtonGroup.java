package er.jquerymobile.components;

import com.webobjects.appserver.WOContext;

import er.extensions.components.ERXNonSynchronizingComponent;

@SuppressWarnings("serial")
public class ERQMButtonGroup extends ERXNonSynchronizingComponent {

  //********************************************************************
  //  Constructor
  //********************************************************************

  public ERQMButtonGroup(WOContext aContext) {
    super(aContext);
  }

  //********************************************************************
  //  Methods
  //********************************************************************

  public String horizontal() {
    return booleanValueForBinding("isHorizontal", false) ? "horizontal" : null;
  }
}
