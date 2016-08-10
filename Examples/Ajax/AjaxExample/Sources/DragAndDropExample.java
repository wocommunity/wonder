import com.webobjects.appserver.WOActionResults;
import com.webobjects.appserver.WOComponent;
import com.webobjects.appserver.WOContext;
import com.webobjects.foundation.NSMutableArray;
/**
 * 
 * @author mschrag Original version
 * @author ak repetition enabled, demostrating object bindings (sort of)
 *
 */
public class DragAndDropExample extends WOComponent {
    public String droppedDraggableID;
    public NSMutableArray<Object> objects = new NSMutableArray<Object>();
    public Object o1 = "Object 0";
    public Object item;
    public Object droppedObject;
    
    public DragAndDropExample(WOContext _context) {
        super(_context);
        for (int i = 1; i < 10; i++) {
            objects.addObject("Object " + i);
        }
    }

    public String draggableExample2() {
        String id = "draggableExample_" + context().elementID().replace('.', '_');
        return id;
    }

    public WOActionResults droppedDraggable1() {
        System.out.println("DragAndDropExample.droppedDraggableID: draggable ID '" + droppedDraggableID
                + "' dropped onto 1: " + droppedDraggableID);
        System.out.println("DragAndDropExample.droppedDraggable1: draggable object = " + droppedObject);
        return null;
    }

    public WOActionResults droppedDraggable2() {
        System.out.println("DragAndDropExample.droppedDraggableID: draggable ID '" + droppedDraggableID
                + "' dropped onto 2: " + droppedObject);
        System.out.println("DragAndDropExample.droppedDraggable2: draggable object = " + droppedObject);
        return null;
    }
}
