package er.jquerymobile.exampleapp.components;

import com.webobjects.appserver.WOContext;

import er.jquerymobile.exampleapp.businessLogic.SampleComponentBase;

@SuppressWarnings("serial")
public class SplitButtonList extends SampleComponentBase {

  //********************************************************************
  //  Constructor
  //********************************************************************

  public SplitButtonList(WOContext aContext) {
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
