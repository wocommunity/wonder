package er.uber.components;

import com.webobjects.appserver.WOContext;
import com.webobjects.foundation.NSArray;
import com.webobjects.foundation.NSMutableArray;

public class GoogleCharts extends UberComponent {
  public GoogleCharts(WOContext context) {
    super(context);
  }

  public NSArray<Integer> someArrayOfData() {
    return new NSArray<Integer>(new Integer[] { 0, 5, 2, 10, 3 });
  }

  public NSArray<NSArray<Integer>> someArrayOfData2() {
    NSMutableArray<NSArray<Integer>> data = new NSMutableArray<NSArray<Integer>>();
    data.addObject(new NSArray<Integer>(new Integer[] { 0, 5, 2, 10, 3 }));
    data.addObject(new NSArray<Integer>(new Integer[] { 0, 5, 2, 10, 3 }));
    return data;
  }

  public NSArray<String> someArrayOfColors() {
    return new NSArray<String>(new String[] { "FF0000", "00FF00" });
  }
}