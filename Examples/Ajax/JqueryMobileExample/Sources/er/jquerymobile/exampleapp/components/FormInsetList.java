package er.jquerymobile.exampleapp.components;

import com.webobjects.appserver.WOActionResults;
import com.webobjects.appserver.WOContext;
import com.webobjects.foundation.NSArray;

import er.jquerymobile.exampleapp.businessLogic.SampleComponentBase;

public class FormInsetList extends SampleComponentBase {

	private static final long serialVersionUID = 1L;

	// ********************************************************************
	// Constructor : コンストラクタ
	// ********************************************************************

	public FormInsetList(WOContext aContext) {
		super(aContext);
	}

	// ********************************************************************
	// Methods : メソッド
	// ********************************************************************

	public String oneStringObject;

	public String _stringField = "String";
	public String _textField = "Text...";
	public String _searchField = "Search";
	public String _flipSwitch;
	public String _range = "20";

	public NSArray<String> allCheckboxList1 = new NSArray<String>("WebObjects", "D2W", "ERest", "WOJqueryMobile");
	public NSArray<String> selectedCheckboxList1 = new NSArray<String>("WOJqueryMobile");

	public NSArray<String> allCheckboxList2 = new NSArray<String>("b", "i", "u");
	public NSArray<String> selectedCheckboxList2 = new NSArray<String>();

	public NSArray<String> allRadioList1 = new NSArray<String>("black", "brown", "red", "blond");
	public String selectedRadioElement1 = "brown";

	public NSArray<String> allRadioList2 = new NSArray<String>("List", "Grid", "Gallery");
	public String selectedRadioElement2 = null;

	public NSArray<String> allPopUpList1 = new NSArray<String>("Standard: 7 day", "Rush: 3 days", "Express: next day", "Overnight");
	public String selectedPopUpElement1 = null;
	public String selectedPopUpElement2 = null;
	public String selectedPopUpElement3 = null;

	// ********************************************************************
	// Actions : アクション
	// ********************************************************************

	public WOActionResults doSaveAction() {
		System.err.println("=== doSaveAction === START ===");

		System.err.println("stringField = " + _stringField);
		System.err.println("textField = " + _textField);
		System.err.println("searchField = " + _searchField);
		System.err.println("range = " + _range);
		System.err.println("_flipSwitch = " + _flipSwitch);
		System.err.println("selectedCheckboxList1 = " + selectedCheckboxList1);
		System.err.println("selectedCheckboxList2 = " + selectedCheckboxList2);
		System.err.println("selectedRadioElement1 = " + selectedRadioElement1);
		System.err.println("selectedRadioElement2 = " + selectedRadioElement2);
		System.err.println("selectedPopUpElement1 = " + selectedPopUpElement1);
		System.err.println("selectedPopUpElement2 = " + selectedPopUpElement2);
		System.err.println("selectedPopUpElement3 = " + selectedPopUpElement3);

		System.err.println("=== doSaveAction === END ===");
		return null;
	}
}
