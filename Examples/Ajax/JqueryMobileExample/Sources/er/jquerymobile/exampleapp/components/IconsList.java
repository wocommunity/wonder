package er.jquerymobile.exampleapp.components;

import com.webobjects.appserver.WOContext;

import er.jquerymobile.exampleapp.businessLogic.SampleComponentBase;

public class IconsList extends SampleComponentBase {

	private static final long serialVersionUID = 1L;

	//********************************************************************
	//  Constructor : コンストラクタ
	//********************************************************************

	public IconsList(WOContext aContext) {
		super(aContext);
	}

	//********************************************************************
	//  Methods : メソッド
	//********************************************************************

	public int _index;

	public String fileName() {
		return "images/flag-0" + _index + ".png";
	}

}
