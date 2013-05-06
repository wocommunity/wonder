package er.jquerymobile.components;

import org.apache.log4j.Logger;

import com.webobjects.appserver.WOContext;

import er.extensions.foundation.ERXStaticResource;

@SuppressWarnings("serial")
public class ERQMListViewIcon extends ERQMInputBaseComponent {

  protected static final Logger log = Logger.getLogger(ERQMListViewIcon.class);

  //********************************************************************
  //  Constructor
  //********************************************************************

  public ERQMListViewIcon(WOContext aContext) {
    super(aContext);
  }

  //********************************************************************
  //  Methods
  //********************************************************************

  public ERXStaticResource imageResource() {
    return new ERXStaticResource(context(), stringValueForBinding("imageResource", null));
  }
}