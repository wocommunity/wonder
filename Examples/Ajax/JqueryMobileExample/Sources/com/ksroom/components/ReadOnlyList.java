package com.ksroom.components;

import com.ksroom.businessLogic.SampleComponentBase;
import com.webobjects.appserver.WOContext;

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
