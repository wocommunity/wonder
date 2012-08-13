package er.jquerymobile.components;

import org.apache.log4j.Logger;

import com.webobjects.appserver.WOContext;

import er.extensions.components.ERXNonSynchronizingComponent;

@SuppressWarnings("serial")
public class ERQMButtonGroup extends ERXNonSynchronizingComponent {

  protected static final Logger log = Logger.getLogger(ERQMButtonGroup.class);

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
