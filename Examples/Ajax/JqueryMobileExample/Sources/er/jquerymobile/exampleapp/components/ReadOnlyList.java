package er.jquerymobile.exampleapp.components;

import com.webobjects.appserver.WOContext;

import er.jquerymobile.exampleapp.businessLogic.SampleComponentBase;

@SuppressWarnings("serial")
public class ReadOnlyList extends SampleComponentBase {

  //********************************************************************
  //  Constructor
  //********************************************************************

  public ReadOnlyList(WOContext aContext) {
    super(aContext);
  }

  //********************************************************************
  //  Methods
  //********************************************************************

  public int _index;

  public String iconName() {
    return "images/flag-0" + _index + ".png";
  }

  public String fileName() {
    return "images/album-0" + _index + ".jpg";
  }

}
