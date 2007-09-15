package er.seleniumexample;

import com.webobjects.appserver.WOActionResults;
import com.webobjects.appserver.WOContext;
import com.webobjects.appserver.WORequest;
import com.webobjects.appserver.WOResponse;
import er.selenium.SeleniumAction;

public class Selenium extends SeleniumAction {
	
	public Selenium(WORequest request) {
		super(request);
	}
	
	public WOActionResults successfulAction() {
		return success();
	}
	
	public WOActionResults erroneousAction() {
		return fail();
	}
}