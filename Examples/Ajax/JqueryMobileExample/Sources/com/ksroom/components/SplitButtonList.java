package com.ksroom.components;

import com.ksroom.businessLogic.SampleComponentBase;
import com.webobjects.appserver.WOContext;

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
