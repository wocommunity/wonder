import com.webobjects.appserver.WOComponent;
import com.webobjects.appserver.WOContext;

import er.ajax.AjaxGrid;

public class AjaxGridExampleCellComponent extends WOComponent {

	public String value;
	public AjaxGrid grid;

	public AjaxGridExampleCellComponent(WOContext context) {
		super(context);
	}

	public void deleteRow() {
		grid.displayGroup().deleteObjectAtIndex(grid.displayGroup().allObjects().indexOfObject(grid.row()));
	}
}
