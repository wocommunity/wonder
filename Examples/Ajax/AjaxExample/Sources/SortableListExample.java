import com.webobjects.appserver.WOActionResults;
import com.webobjects.appserver.WOComponent;
import com.webobjects.appserver.WOContext;
import com.webobjects.foundation.NSArray;
import com.webobjects.foundation.NSMutableArray;

import er.ajax.example.ExampleDataFactory;
import er.ajax.example.Item;

public class SortableListExample extends WOComponent {
  private NSMutableArray _listA;
  private NSArray _listB;
  public Item _repetitionListItemA;
  public Item _repetitionListItemB;

  public SortableListExample(WOContext context) {
    super(context);
    _listA = ExampleDataFactory.items("A", "A Element #", 10);
    _listB = ExampleDataFactory.items("B", "B Element #", 10).immutableClone();
  }

  public NSMutableArray listA() {
    return _listA;
  }
  
  public NSArray listB() {
    return _listB;
  }
  
  public void setListB(NSArray listB) {
    _listB = listB;
  }
  
  public WOActionResults orderChanged() {
    System.out.println("SortableListExample.orderChanged: A: " + _listA);
    System.out.println("SortableListExample.orderChanged: B: " + _listB);
    return null;
  }
}
