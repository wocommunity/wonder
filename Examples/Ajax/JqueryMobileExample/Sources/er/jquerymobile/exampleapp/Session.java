package er.jquerymobile.exampleapp;

import com.webobjects.appserver.WOActionResults;
import com.webobjects.appserver.WOContext;
import com.webobjects.appserver.WORequest;

import er.extensions.appserver.ERXSession;
import er.jqm.components.ERQMSessionHelper;

public class Session extends ERXSession {

	private static final long serialVersionUID = 1L;

	//********************************************************************
	//  Constructor : コンストラクタ
	//********************************************************************

	private boolean _actAsGuest = true;

	public Session() {
	}

	//********************************************************************
	//  Actions : アクション
	//********************************************************************

	@Override
	public WOActionResults invokeAction(WORequest request, WOContext context) {
		WOActionResults result = super.invokeAction(request, context);
		result = ERQMSessionHelper.checkForUpdateContainer(result, request, context);
		return result;
	}

	public boolean actAsGuest() {
		return _actAsGuest;
	}

	public void setActAsGuest(boolean actAsGuest) {
		_actAsGuest = actAsGuest;
	}

}
