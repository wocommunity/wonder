package er.extensions.components.conditionals;

import com.webobjects.appserver.WOActionResults;
import com.webobjects.appserver.WOContext;
import com.webobjects.appserver.WOElement;
import com.webobjects.appserver.WORequest;
import com.webobjects.appserver.WOResponse;
import com.webobjects.appserver._private.WODynamicGroup;
import com.webobjects.foundation.NSDictionary;

/**
 * ERXElse can be used like a Java "else" after a preceding conditional.
 * 
 * @author mschrag
 */
public class ERXElse extends WODynamicGroup {
	public ERXElse(String s, NSDictionary nsdictionary, WOElement woelement) {
		super(s, nsdictionary, woelement);
	}

	@Override
	public String toString() {
		return "<ERXElse>";
	}

	public static boolean lastConditionChecked() {
		Boolean lastCondition = ERXWOConditional.lastCondition();
		if (lastCondition == null) {
			throw new IllegalStateException("You attempted to use an ERXElse without a preceding conditional.");
		}
		return lastCondition.booleanValue();
	}

	@Override
	public void takeValuesFromRequest(WORequest worequest, WOContext wocontext) {
		if (!ERXElse.lastConditionChecked()) {
			super.takeValuesFromRequest(worequest, wocontext);
			ERXWOConditional.setLastCondition(null);
		}
	}

	@Override
	public WOActionResults invokeAction(WORequest worequest, WOContext wocontext) {
		WOActionResults results = null;
		if (!ERXElse.lastConditionChecked()) {
			results = super.invokeAction(worequest, wocontext);
			ERXWOConditional.setLastCondition(null);
		}
		return results;
	}

	@Override
	public void appendToResponse(WOResponse woresponse, WOContext wocontext) {
		if (!ERXElse.lastConditionChecked()) {
			super.appendToResponse(woresponse, wocontext);
			ERXWOConditional.setLastCondition(null);
		}
	}
}
