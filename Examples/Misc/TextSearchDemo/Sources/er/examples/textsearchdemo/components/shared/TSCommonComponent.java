package er.examples.textsearchdemo.components.shared;

import com.webobjects.appserver.WOComponent;
import com.webobjects.appserver.WOContext;

import er.extensions.components.ERXComponent;

public class TSCommonComponent extends ERXComponent {

	private WOComponent _backPage;
	
	public TSCommonComponent(WOContext context) {
		super(context);
		_backPage = context().page();
	}

	public WOComponent backPage() {
		return _backPage;
	}
	
	public void setBackPage(WOComponent page) {
		_backPage = page;
	}

}
