package er.examples.ui;

import java.util.Arrays;

import com.webobjects.appserver.WOContext;
import com.webobjects.foundation.NSArray;
import com.webobjects.foundation.NSMutableDictionary;

import er.extensions.components.ERXComponent;

public class ERPChartsPage extends ERXComponent {

	public ERPChartsPage(WOContext context) {
		super(context);
	}

	private NSArray _itemSet1;
	
	// Reasonable dataset for LineChart, BarChart, BarChart3D, StackedBarChart, StackedBarChart3D, AreaChart, StackedAreaChart
	//
	public NSArray itemSet1() {
		if (_itemSet1 == null) {
			NSArray keys = new NSArray(Arrays.asList("name", "value", "category"));
			
			NSMutableDictionary oneA = new NSMutableDictionary(new NSArray(Arrays.asList("iPhone", new Integer(100), "01/2011")), keys);
			NSMutableDictionary oneB = new NSMutableDictionary(new NSArray(Arrays.asList("iPhone", new Integer(150), "02/2011")), keys);
			NSMutableDictionary oneC = new NSMutableDictionary(new NSArray(Arrays.asList("iPhone", new Integer(200), "03/2011")), keys);

			NSMutableDictionary twoA = new NSMutableDictionary(new NSArray(Arrays.asList("RIM", new Integer(200), "01/2011")), keys);
			NSMutableDictionary twoB = new NSMutableDictionary(new NSArray(Arrays.asList("RIM", new Integer(210), "02/2011")), keys);
			NSMutableDictionary twoC = new NSMutableDictionary(new NSArray(Arrays.asList("RIM", new Integer(215), "03/2011")), keys);

			NSMutableDictionary threeA = new NSMutableDictionary(new NSArray(Arrays.asList("Android", new Integer(50), "01/2011")), keys);
			NSMutableDictionary threeB = new NSMutableDictionary(new NSArray(Arrays.asList("Android", new Integer(55), "02/2011")), keys);
			NSMutableDictionary threeC = new NSMutableDictionary(new NSArray(Arrays.asList("Android", new Integer(110), "03/2011")), keys);

			_itemSet1 = new NSArray(Arrays.asList(twoA, twoB, twoC, oneA, oneB, oneC, threeA, threeB, threeC));
		}
		//System.out.println("itemSet1 = "+_itemSet1);
		return _itemSet1;
	}

	private NSArray _itemSet1a;
	
	// "Wrong" dataset for AreaChart
	//
	public NSArray itemSet1a() {
		if (_itemSet1a == null) {
			NSArray keys = new NSArray(Arrays.asList("name", "value", "category"));
			
			NSMutableDictionary oneA = new NSMutableDictionary(new NSArray(Arrays.asList("iPhone", new Integer(100), "01/2011")), keys);
			NSMutableDictionary oneB = new NSMutableDictionary(new NSArray(Arrays.asList("iPhone", new Integer(150), "02/2011")), keys);
			NSMutableDictionary oneC = new NSMutableDictionary(new NSArray(Arrays.asList("iPhone", new Integer(200), "03/2011")), keys);

			NSMutableDictionary twoA = new NSMutableDictionary(new NSArray(Arrays.asList("RIM", new Integer(200), "01/2011")), keys);
			NSMutableDictionary twoB = new NSMutableDictionary(new NSArray(Arrays.asList("RIM", new Integer(210), "02/2011")), keys);
			NSMutableDictionary twoC = new NSMutableDictionary(new NSArray(Arrays.asList("RIM", new Integer(215), "03/2011")), keys);

			NSMutableDictionary threeA = new NSMutableDictionary(new NSArray(Arrays.asList("Android", new Integer(50), "01/2011")), keys);
			NSMutableDictionary threeB = new NSMutableDictionary(new NSArray(Arrays.asList("Android", new Integer(55), "02/2011")), keys);
			NSMutableDictionary threeC = new NSMutableDictionary(new NSArray(Arrays.asList("Android", new Integer(110), "03/2011")), keys);

			_itemSet1a = new NSArray(Arrays.asList(oneA, oneB, oneC, twoA, twoB, twoC, threeA, threeB, threeC));
		}
		//System.out.println("itemSet1a = "+_itemSet1a);
		return _itemSet1a;
	}

	// Reasonable dataset for WaterfallChart
	//
	private NSArray _itemSet2;
	
	public NSArray itemSet2() {
		if (_itemSet2 == null) {
			NSArray keys = new NSArray(Arrays.asList("name", "value", "category"));
			
			NSMutableDictionary oneA = new NSMutableDictionary(new NSArray(Arrays.asList("iPhone", new Integer(20), "01/2011")), keys);
			NSMutableDictionary oneB = new NSMutableDictionary(new NSArray(Arrays.asList("iPhone", new Integer(25), "02/2011")), keys);
			NSMutableDictionary oneC = new NSMutableDictionary(new NSArray(Arrays.asList("iPhone", new Integer(30), "03/2011")), keys);
			NSMutableDictionary oneD = new NSMutableDictionary(new NSArray(Arrays.asList("iPhone", new Integer(75), "Total Q1")), keys);

			_itemSet2 = new NSArray(Arrays.asList(oneA, oneB, oneC, oneD));
		}
		//System.out.println("itemSet2 = "+_itemSet2);
		return _itemSet2;
	}
}
