package er.jquerymobile.exampleapp.components;

import com.webobjects.appserver.WOContext;

import er.jquerymobile.exampleapp.businessLogic.SampleComponentBase;

public class ThumbnailsList extends SampleComponentBase {

  private static final long serialVersionUID = 1L;

  //********************************************************************
  //  Constructor : コンストラクタ
  //********************************************************************

  public ThumbnailsList(WOContext aContext) {
    super(aContext);
  }

  //********************************************************************
  //  Methods : メソッド
  //********************************************************************

  public int _index;

  public String fileName() {
    return "images/album-0" + _index + ".jpg";
  }
}
