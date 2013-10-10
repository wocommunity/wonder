import com.webobjects.appserver.WOComponent;
import com.webobjects.appserver.WOContext;
import com.webobjects.foundation.NSArray;

import er.ajax.AjaxGrid;
import er.extensions.foundation.ERXStringUtilities;

public class AjaxGridExampleFormInputCellComponent extends WOComponent {

	public String value;
	public AjaxGrid grid;
	public NSArray<String> levelList = new NSArray<String>(new String[] { "1", "2", "3", "4", "5", "6", "7", "8", "9", "10" });
	public String aLevel;
	protected String uniqueFunctionName;

	public AjaxGridExampleFormInputCellComponent(WOContext context) {
		super(context);
	}

	/**
	 * Ajax action method for updates to level
	 */
	public void levelChangedUpdated() {
		// There is no form, so we must do this manually
		String levelString = (String) context().request().formValueForKey("level");
		int levelIndex = Integer.parseInt(levelString);

		// If this component was not synchronzing, we would need to change the
		// value in grid.row() instead
		value = levelList.objectAtIndex(levelIndex);
	}

	public String executeUpdateLevel() {
		return uniqueFunctionName() + "('level=' + this.value); return true;";
	}

	/**
	 * This is in a repetition, so we need to be careful to have unique names
	 * 
	 * @return unique updateLevel... name for this element
	 */
	public String uniqueFunctionName() {
		if (uniqueFunctionName == null) {
			uniqueFunctionName = "updateLevel" + ERXStringUtilities.safeIdentifierName(context().elementID());
		}
		return uniqueFunctionName;
	}
}
