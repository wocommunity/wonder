package er.jquerymobile.exampleapp;

import com.webobjects.appserver.WOActionResults;
import com.webobjects.appserver.WORequest;

import er.extensions.appserver.ERXDirectAction;
import er.jquerymobile.exampleapp.components.Main;

public class DirectAction extends ERXDirectAction {

	//********************************************************************
	//  Constructor : コンストラクタ
	//********************************************************************

	public DirectAction(WORequest request) {
		super(request);
	}

	//********************************************************************
	//  Actions : アクション
	//********************************************************************

	@Override
	public WOActionResults defaultAction() {
		return pageWithName(Main.class.getName());
	}

}
