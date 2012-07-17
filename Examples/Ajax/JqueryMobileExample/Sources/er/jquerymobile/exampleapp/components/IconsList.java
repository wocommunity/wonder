package er.jquerymobile.exampleapp.components;

import com.webobjects.appserver.WOContext;

import er.jquerymobile.exampleapp.businessLogic.SampleComponentBase;

@SuppressWarnings("serial")
public class IconsList extends SampleComponentBase {

  //********************************************************************
  //  Constructor
  //********************************************************************

  public IconsList(WOContext aContext) {
    super(aContext);
  }

  //********************************************************************
  //  Methods
  //********************************************************************

  public int _index;

  public String fileName() {
    return "images/flag-0" + _index + ".png";
  }

}
