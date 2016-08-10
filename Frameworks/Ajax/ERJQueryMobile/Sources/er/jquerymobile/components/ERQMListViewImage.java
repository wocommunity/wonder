package er.jquerymobile.components;

import com.webobjects.appserver.WOContext;

import er.extensions.foundation.ERXStaticResource;

@SuppressWarnings("serial")
public class ERQMListViewImage extends ERQMInputBaseComponent {

  //********************************************************************
  //  Constructor
  //********************************************************************

  public ERQMListViewImage(WOContext aContext) {
    super(aContext);
  }

  //********************************************************************
  //  Methods
  //********************************************************************

  public ERXStaticResource imageResource() {
    return new ERXStaticResource(context(), stringValueForBinding("imageResource", null));
  }
}
