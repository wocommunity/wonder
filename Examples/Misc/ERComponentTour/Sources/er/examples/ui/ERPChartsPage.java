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
	
	public NSArray itemSet1() {
		if (_itemSet1 == null) {
			NSArray keys = new NSArray(Arrays.asList("name", "value", "category"));
			
			NSMutableDictionary oneA = new NSMutableDictionary(new NSArray(Arrays.asList("green", new Integer(1), "one")), keys);
			NSMutableDictionary oneB = new NSMutableDictionary(new NSArray(Arrays.asList("green", new Integer(2), "two")), keys);
			NSMutableDictionary oneC = new NSMutableDictionary(new NSArray(Arrays.asList("green", new Integer(3), "three")), keys);

			NSMutableDictionary twoA = new NSMutableDictionary(new NSArray(Arrays.asList("blue", new Integer(2), "one")), keys);
			NSMutableDictionary twoB = new NSMutableDictionary(new NSArray(Arrays.asList("blue", new Integer(3), "two")), keys);
			NSMutableDictionary twoC = new NSMutableDictionary(new NSArray(Arrays.asList("blue", new Integer(4), "three")), keys);

			NSMutableDictionary threeA = new NSMutableDictionary(new NSArray(Arrays.asList("red", new Integer(3), "one")), keys);
			NSMutableDictionary threeB = new NSMutableDictionary(new NSArray(Arrays.asList("red", new Integer(4), "two")), keys);
			NSMutableDictionary threeC = new NSMutableDictionary(new NSArray(Arrays.asList("red", new Integer(5), "three")), keys);

			_itemSet1 = new NSArray(Arrays.asList(oneA, oneB, oneC, twoA, twoB, twoC, threeA, threeB, threeC));
		}
		System.out.println("itemSet1 = "+_itemSet1);
		return _itemSet1;
	}
}