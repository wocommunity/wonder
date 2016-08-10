package er.ajax.mootools.example.components;

import com.webobjects.appserver.WOActionResults;
import com.webobjects.appserver.WOContext;
import com.webobjects.foundation.NSMutableArray;

public class MTAjaxDragDropTestPage extends Main {
    
    public Object o1 = "Object 0";
    public String droppedDraggableID;
    public String droppedArea;
    public NSMutableArray<Object> objects = new NSMutableArray<Object>();
    public Object item;
    public Object droppedObject;	
	
    public String statusMessage;    
	
	public MTAjaxDragDropTestPage(WOContext context) {
        super(context);
        for (int i = 1; i < 10; i++) {
            objects.addObject("Object " + i);
        }
        statusMessage = "Drag one of the green draggable items onto one of the blue droppable items.";
	}

	
    public String draggableExample2() {
        String id = "draggableExample_" + context().elementID().replace('.', '_');
        return id;
    }

    public WOActionResults droppedDraggable1() {
    	statusMessage = "DragAndDropExample.droppedDraggableID: draggable ID '" + droppedObject
                + "' dropped onto : " + droppedArea;
    	/*
    	setTask(new Task());
		task().start();
		do {
			System.out.println(task().getStatus());
		} while(! task().getStatus().equals("Finished"));
		*/
    	return null;
    }

    public WOActionResults droppedDraggable2() {
        System.out.println("DragAndDropExample.droppedDraggableID: draggable ID '" + droppedDraggableID
                + "' dropped onto 2: " + droppedObject);
        System.out.println("DragAndDropExample.droppedDraggable2: draggable object = " + droppedObject);
        return null;
    }	

}