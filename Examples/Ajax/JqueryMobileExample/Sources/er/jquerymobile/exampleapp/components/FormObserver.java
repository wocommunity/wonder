package er.jquerymobile.exampleapp.components;

import com.webobjects.appserver.WOActionResults;
import com.webobjects.appserver.WOContext;
import com.webobjects.foundation.NSArray;

import er.jquerymobile.exampleapp.businessLogic.SampleComponentBase;

public class FormObserver extends SampleComponentBase {

	private static final long serialVersionUID = 1L;

	// ********************************************************************
	// Constructor : コンストラクタ
	// ********************************************************************

	public FormObserver(WOContext aContext) {
		super(aContext);
	}

	// ********************************************************************
	// Methods : メソッド
	// ********************************************************************

	public String _testString = null;
	public String _testFlip = "0";
	public String _testsearch = null;
	public Boolean _testCheckbox = Boolean.FALSE;

	public NSArray<String> allPopUpList1 = new NSArray<String>("Standard: 7 day", "Rush: 3 days", "Express: next day", "Overnight");
	public String testPopUpElement1 = null;
	public String testPopUpElement2 = null;

	public String submitText = "";

	public void setTestCheckbox(Boolean testCheckbox) {
		_testCheckbox = testCheckbox;
	}

	public Boolean testCheckbox() {
		return _testCheckbox;
	}

	// ********************************************************************
	// Actions : アクション
	// ********************************************************************

	public WOActionResults doUpdateAction() {
		submitText = "You use a change request";
		return _doAction("doUpdateAction");
	}

	public WOActionResults doSubmitAction() {
		submitText = "You use the submit button";
		return _doAction("doSubmitAction");
	}

	public WOActionResults doLinkAction() {
		submitText = "You use the link";
		System.err.println("**doLinkAction**");
		return null;
	}

	private WOActionResults _doAction(String name) {
		System.err.println("**" + name + "**");
		System.err.println(" testString = " + _testString);
		System.err.println(" testFlip = " + _testFlip);
		System.err.println(" testsearch = " + _testsearch);
		System.err.println(" testCheckbox = " + testCheckbox());
		System.err.println(" testPopUpElement1 = " + testPopUpElement1);
		System.err.println(" testPopUpElement2 = " + testPopUpElement2);

		return null;
	}
}