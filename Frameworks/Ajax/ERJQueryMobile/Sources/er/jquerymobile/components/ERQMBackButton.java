package er.jquerymobile.components;

import org.apache.log4j.Logger;

import com.webobjects.appserver.WOContext;

import er.extensions.components.ERXNonSynchronizingComponent;

@SuppressWarnings("serial")
public class ERQMBackButton extends ERXNonSynchronizingComponent {

  protected static final Logger log = Logger.getLogger(ERQMBackButton.class);

  //********************************************************************
  //  Constructor
  //********************************************************************

  public ERQMBackButton(WOContext aContext) {
    super(aContext);
  }

}
