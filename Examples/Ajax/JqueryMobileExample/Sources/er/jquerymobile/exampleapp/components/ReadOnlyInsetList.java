package er.jquerymobile.exampleapp.components;

import com.webobjects.appserver.WOContext;

import er.jquerymobile.exampleapp.businessLogic.SampleComponentBase;

public class ReadOnlyInsetList extends SampleComponentBase {

	private static final long serialVersionUID = 1L;

	//********************************************************************
	//  Constructor : コンストラクタ
	//********************************************************************

	public ReadOnlyInsetList(WOContext aContext) {
		super(aContext);
	}

	//********************************************************************
	//  Methods : メソッド
	//********************************************************************

	public int _index;

	public String iconName() {
		return "images/flag-0" + _index + ".png";
	}

	public String fileName() {
		return "images/album-0" + _index + ".jpg";
	}

}
