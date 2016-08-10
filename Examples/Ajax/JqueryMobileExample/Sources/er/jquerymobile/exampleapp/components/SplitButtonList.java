package er.jquerymobile.exampleapp.components;

import com.webobjects.appserver.WOContext;

import er.jquerymobile.exampleapp.businessLogic.SampleComponentBase;

public class SplitButtonList extends SampleComponentBase {

  private static final long serialVersionUID = 1L;

  public int _index;

  //********************************************************************
  //  Constructor : コンストラクタ
  //********************************************************************

  public SplitButtonList(WOContext aContext) {
    super(aContext);
  }

  //********************************************************************
  //  Methods : メソッド
  //********************************************************************

  public String fileName() {
    return "images/album-0" + _index + ".jpg";
  }

}
