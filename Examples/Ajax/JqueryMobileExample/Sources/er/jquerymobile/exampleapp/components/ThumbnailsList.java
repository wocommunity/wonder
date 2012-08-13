package er.jquerymobile.exampleapp.components;

import com.webobjects.appserver.WOContext;

import er.jquerymobile.exampleapp.businessLogic.SampleComponentBase;

@SuppressWarnings("serial")
public class ThumbnailsList extends SampleComponentBase {

  //********************************************************************
  //  Constructor
  //********************************************************************

  public ThumbnailsList(WOContext aContext) {
    super(aContext);
  }

  //********************************************************************
  //  Methods
  //********************************************************************

  public int _index;

  public String fileName() {
    return "images/album-0" + _index + ".jpg";
  }
}
