package er.seleniumexample;

import com.webobjects.appserver.WOContext;
import com.webobjects.appserver.WOResponse;
import er.selenium.SeleniumDefaultSetupActions;

public class SetupActions extends SeleniumDefaultSetupActions {
	public static void successfulAction(WOResponse response, WOContext context) {
		/* Some useful code */
	}
	
	public static void erroneousAction(WOResponse response, WOContext context) {
		throw new RuntimeException("42");
	}
}