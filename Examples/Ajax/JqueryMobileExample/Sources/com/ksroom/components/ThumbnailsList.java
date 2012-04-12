package com.ksroom.components;

import com.ksroom.businessLogic.SampleComponentBase;
import com.webobjects.appserver.WOContext;

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
