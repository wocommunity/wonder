import com.webobjects.appserver.WOActionResults;
import com.webobjects.appserver.WOComponent;
import com.webobjects.appserver.WOContext;
import com.webobjects.foundation.NSMutableArray;

public class DragAndDropLists extends WOComponent {
  public NSMutableArray<DragItem> _leftList;
  public NSMutableArray<DragItem> _rightList;
  public DragItem _repetitionDragItem;
  public DragItem _droppedObject;

  public DragAndDropLists(WOContext context) {
    super(context);
    _leftList = new NSMutableArray<DragItem>();
    _rightList = new NSMutableArray<DragItem>();
    for (int dragItemNum = 0; dragItemNum < 10; dragItemNum++) {
      _leftList.addObject(new DragItem("Drag Item #" + dragItemNum));
    }
  }

  public WOActionResults droppedOnLeft() {
    System.out.println("DragAndDropLists.droppedOnLeft: Adding " + _droppedObject + " to left list");
    if (_droppedObject != null && !_leftList.containsObject(_droppedObject)) {
      _rightList.removeObject(_droppedObject);
      _leftList.addObject(_droppedObject);
    }
    return null;
  }

  public WOActionResults droppedOnRight() {
    System.out.println("DragAndDropLists.droppedOnLeft: Adding " + _droppedObject + " to right list");
    if (_droppedObject != null && !_rightList.containsObject(_droppedObject)) {
      _leftList.removeObject(_droppedObject);
      _rightList.addObject(_droppedObject);
    }
    return null;
  }

  public static class DragItem {
    private String _name;

    public DragItem(String name) {
      _name = name;
    }

    public String name() {
      return _name;
    }

    @Override
	public String toString() {
      return "[DragItem: " + _name + "]";
    }
  }
}
