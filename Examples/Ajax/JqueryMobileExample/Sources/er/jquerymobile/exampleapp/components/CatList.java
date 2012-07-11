package er.jquerymobile.exampleapp.components;

import com.webobjects.appserver.WOContext;
import com.webobjects.foundation.NSArray;

import er.jquerymobile.exampleapp.businessLogic.SampleComponentBase;

@SuppressWarnings("serial")
public class CatList extends SampleComponentBase {

  //********************************************************************
  //  Constructor
  //********************************************************************

  public CatList(WOContext aContext) {
    super(aContext);
  }

  //********************************************************************
  //  Methods
  //********************************************************************

  public int _index;

  public String fileName() {
    return "images/cat-0" + _index + ".jpg";
  }

  public NSArray<String> cats() {
    return new NSArray<String>("Bronya", "Lomi 1", "Lomi 2", "Gura 1", "Gura 2");
  }
  public String oneCat;

}
