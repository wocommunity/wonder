package er.r2d2w.components.misc;

import com.webobjects.appserver.WOContext;
import com.webobjects.foundation.NSArray;
import com.webobjects.foundation.NSKeyValueCoding;

import er.extensions.appserver.ERXWOContext;
import er.extensions.appserver.navigation.ERXNavigationItem;
import er.extensions.appserver.navigation.ERXNavigationMenu;
import er.r2d2w.components.buttons.R2DDefaultButtonContent;

public class R2DNavigationMenu extends ERXNavigationMenu {
	/**
	 * Do I need to update serialVersionUID? See section 5.6 <cite>Type Changes
	 * Affecting Serialization</cite> on page 51 of the <a
	 * href="http://java.sun.com/j2se/1.4/pdf/serial-spec.pdf">Java Object
	 * Serialization Spec</a>
	 */
	private static final long serialVersionUID = 1L;
	private String checkboxID;

	public R2DNavigationMenu(WOContext context) {
		super(context);
	}

	public void reset() {
		super.reset();
		checkboxID = null;
	}

	public ERXNavigationItem navigationItem() {
		return aNavigationItem;
	}

	public void setNavigationItem(ERXNavigationItem navigationItem) {
		aNavigationItem = navigationItem;
		navigationContext().takeValueForKey(navigationItem, "navigationItem");
	}

	public String navigationButtonContentComponentName() {
		String name = (String) navigationContext().valueForKey("navigationButtonContentComponentName");
		if (name == null) {
			name = R2DDefaultButtonContent.class.getSimpleName();
		}
		return name;
	}

	public NSKeyValueCoding navContext() {
		return navigationContext();
	}

	public boolean isSelected() {
		NSArray<?> state = navigationState().state();
		boolean isSelected = !navigationState().isDisabled() && state != null
				&& state.containsObject(navigationItem().name());
		return isSelected;
	}

	/**
	 * @return the checkboxID
	 */
	public String checkboxID() {
		if (checkboxID == null) {
			checkboxID = ERXWOContext.safeIdentifierName(context(), false);
		}
		return checkboxID;
	}

	public boolean omitListItem() {
		return !itemMeetsDisplayConditions();
	}

	public boolean itemMeetsDisplayConditions() {
		return navigationItem().meetsDisplayConditionsInComponent(this);
	}

	public String listItemClass() {
		return isSelected()?"selected":null;
	}
}