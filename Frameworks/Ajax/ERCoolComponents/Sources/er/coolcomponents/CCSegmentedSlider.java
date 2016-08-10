package er.coolcomponents;

import java.util.Objects;

import com.webobjects.appserver.WOActionResults;
import com.webobjects.appserver.WOContext;
import com.webobjects.appserver.WORequest;
import com.webobjects.appserver.WOResponse;
import com.webobjects.foundation.NSDictionary;
import com.webobjects.foundation.NSMutableDictionary;

import er.ajax.AjaxUtils;
import er.extensions.appserver.ERXResponseRewriter;
import er.extensions.components.ERXComponent;
import er.extensions.foundation.ERXStringUtilities;

/**
 * CCSegmentedSlider provides a picker for an enumerated type inspired by the iPhone on/off slider. The bindings are
 * similar to a WOPopUpButton. For a bunch of example uses and example CSS modifications, check out <a
 * href="http://mschrag.github.com/segmented_slider/example/">the SegmentedSlider example page</a>.
 * 
 * @binding id the id of the segmented slider (or one will be generated)
 * @binding list the list of options
 * @binding item the repetition item for options
 * @binding selection the currently selected object
 * @binding displayString the string to show on the segment (defaults to the item toString)
 * @binding initialSelection if false, there will be no default selection on the control
 * @binding toggleSelection if true, selections can be toggled on and off
 * @binding enableDragSupport if true, the selector is draggable
 * @binding value the value of the current selection (optional)
 * 
 * @author mschrag
 */
public class CCSegmentedSlider extends ERXComponent {
	/**
	 * Do I need to update serialVersionUID?
	 * See section 5.6 <cite>Type Changes Affecting Serialization</cite> on page 51 of the 
	 * <a href="http://java.sun.com/j2se/1.4/pdf/serial-spec.pdf">Java Object Serialization Spec</a>
	 */
	private static final long serialVersionUID = 1L;

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
			options.setObjectForKey(Boolean.valueOf(booleanValueForBinding("initialSelection", true)),
					"initialSelection");
		}
		if (hasBinding("toggleSelection")) {
			options.setObjectForKey(Boolean.valueOf(booleanValueForBinding("toggleSelection", false)),
					"toggleSelection");
		}
		if (hasBinding("enableDragSupport")) {
			options.setObjectForKey(Boolean.valueOf(booleanValueForBinding("enableDragSupport", false)),
					"enableDragSupport");
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
		return Objects.equals(selection, item);
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
		ERXResponseRewriter.addScriptResourceInHead(response, context, "ERCoolComponents",
				"SegmentedSlider/SegmentedSlider.js");
		ERXResponseRewriter.addStylesheetResourceInHead(response, context, "ERCoolComponents",
				"SegmentedSlider/SegmentedSlider.css");

		if (AjaxUtils.isAjaxRequest(context.request()))
			response.appendContentString("<script>new SegmentedSlider($('" + id() + "'), '" + _radioButtonGroupName
					+ "', '')</script>");
		else
			response.appendContentString("<script>Event.observe(window, 'load', function() { new SegmentedSlider($('"
					+ id() + "'), '" + _radioButtonGroupName + "', '') })</script>");

		super.appendToResponse(response, context);
	}
}
