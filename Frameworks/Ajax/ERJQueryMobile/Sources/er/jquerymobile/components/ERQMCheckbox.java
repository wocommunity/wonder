package er.jquerymobile.components;

import org.apache.log4j.Logger;

import com.webobjects.appserver.WOContext;

@SuppressWarnings("serial")
public class ERQMCheckbox extends ERQMInputBaseComponent {

  protected static final Logger log = Logger.getLogger(ERQMCheckbox.class);

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
