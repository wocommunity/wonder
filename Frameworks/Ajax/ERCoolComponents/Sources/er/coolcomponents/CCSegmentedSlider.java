package er.coolcomponents;

import com.webobjects.appserver.WOActionResults;
import com.webobjects.appserver.WOContext;
import com.webobjects.appserver.WORequest;
import com.webobjects.appserver.WOResponse;
import com.webobjects.foundation.NSDictionary;
import com.webobjects.foundation.NSMutableDictionary;

import er.extensions.ERXExtensions;
import er.extensions.appserver.ERXResponseRewriter;
import er.extensions.components.ERXComponent;
import er.extensions.foundation.ERXStringUtilities;

/**
 * CCSegmentedSlider provides a picker for an enumerated type inspired by the iPhone on/off slider. The bindings are similar
 * to a WOPopUpButton. For a bunch of example uses and example CSS modifications, check out 
 * <a href="http://mschrag.github.com/segmented_slider/example/">the SegmentedSlider example page<a>.
 * 
 * @binding id the id of the segmented slider (or one will be generated)
 * @binding list the list of options
 * @binding item the repetition item for options
 * @binding selection the currently selected object
 * @binding displayString the string to show on the segment (defaults to the item toString)
 * @binding initialSelection if false, there will be no default selection on the control
 * @binding toggleSelection if true, selections can be toggled on and off
 * @binding value the value of the current selection (optional)
 * 
 * @author mschrag
 */
public class CCSegmentedSlider extends ERXComponent {
	private String _id;
	public String _radioButtonGroupName;
	private Object _selection;

	public CCSegmentedSlider(WOContext context) {
		super(context);
	}

	@Override
	public boolean synchronizesVariablesWithBindings() {
		return false;
	}

	public String id() {
		if (_id == null) {
			_id = stringValueForBinding("id", ERXStringUtilities.safeIdentifierName(_radioButtonGroupName, "cc_"));
		}
		return _id;
	}

	public String displayString() {
		String displayName;
		if (hasBinding("displayString")) {
			displayName = stringValueForBinding("displayString");
		} else {
			displayName = String.valueOf(valueForBinding("item"));
		}
		return displayName;
	}

	public NSDictionary<String, Object> options() {
		NSMutableDictionary<String, Object> options = new NSMutableDictionary<String, Object>();
		if (hasBinding("initialSelection")) {
			options.setObjectForKey(Boolean.valueOf(booleanValueForBinding("initialSelection", true)), "initialSelection");
		}
		if (hasBinding("toggleSelection")) {
			options.setObjectForKey(Boolean.valueOf(booleanValueForBinding("toggleSelection", false)), "toggleSelection");
		}
		return options;
	}

	public String value() {
		String value;
		if (hasBinding("value")) {
			value = stringValueForBinding("value");
		} else {
			value = displayString();
		}
		return value;
	}

	public boolean isChecked() {
		Object selection = valueForBinding("selection");
		Object item = valueForBinding("item");
		return ERXExtensions.safeEquals(selection, item);
	}

	public void setChecked(boolean checked) {
		if (checked) {
			_selection = valueForBinding("item");
		}
	}

	@Override
	public void takeValuesFromRequest(WORequest request, WOContext context) {
		_radioButtonGroupName = context.elementID();
		_selection = null;
		super.takeValuesFromRequest(request, context);
		setValueForBinding(_selection, "selection");
	}

	@Override
	public WOActionResults invokeAction(WORequest request, WOContext context) {
		_radioButtonGroupName = context.elementID();
		return super.invokeAction(request, context);
	}

	@Override
	public void appendToResponse(WOResponse response, WOContext context) {
		_radioButtonGroupName = context.elementID();
		ERXResponseRewriter.addScriptResourceInHead(response, context, "Ajax", "prototype.js");
		ERXResponseRewriter.addScriptResourceInHead(response, context, "Ajax", "effects.js");
		ERXResponseRewriter.addScriptResourceInHead(response, context, "ERCoolComponents", "SegmentedSlider/SegmentedSlider.js");
		ERXResponseRewriter.addStylesheetResourceInHead(response, context, "ERCoolComponents", "SegmentedSlider/SegmentedSlider.css");
		super.appendToResponse(response, context);
	}
}