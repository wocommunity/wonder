package er.jquerymobile.components;

import com.webobjects.appserver.WOContext;

import er.extensions.foundation.ERXStaticResource;

@SuppressWarnings("serial")
public class ERQMListViewIcon extends ERQMInputBaseComponent {

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