package com.ksroom.components;

import com.ksroom.businessLogic.SampleComponentBase;
import com.webobjects.appserver.WOContext;

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
