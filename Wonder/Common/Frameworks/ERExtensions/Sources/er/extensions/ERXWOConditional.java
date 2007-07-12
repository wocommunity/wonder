package er.extensions;

import com.webobjects.appserver.WOActionResults;
import com.webobjects.appserver.WOAssociation;
import com.webobjects.appserver.WOContext;
import com.webobjects.appserver.WOElement;
import com.webobjects.appserver.WORequest;
import com.webobjects.appserver.WOResponse;
import com.webobjects.appserver._private.WODynamicElementCreationException;
import com.webobjects.appserver._private.WODynamicGroup;
import com.webobjects.foundation.NSDictionary;

/**
 * ERXWOConditional behaves just like WOConditional except that it tracks its condition state for use with ERXElse.
 * 
 * @author mschrag
 */
public class ERXWOConditional extends WODynamicGroup {
	public static final String LAST_CONDITION_KEY = "er.extensions.ERXWOConditional.lastCondition";

	private WOAssociation _condition;
	private WOAssociation _negate;

	public static void setLastCondition(Boolean lastCondition) {
		if (lastCondition == null) {
			ERXWOContext.contextDictionary().removeObjectForKey(ERXWOConditional.LAST_CONDITION_KEY);
		}
		else {
			ERXWOContext.contextDictionary().setObjectForKey(lastCondition, ERXWOConditional.LAST_CONDITION_KEY);
		}
	}

	public static Boolean lastCondition() {
		return (Boolean) ERXWOContext.contextDictionary().objectForKey(ERXWOConditional.LAST_CONDITION_KEY);
	}

	public ERXWOConditional(String s, NSDictionary nsdictionary, WOElement woelement) {
		super(null, null, woelement);

		_condition = (WOAssociation) nsdictionary.objectForKey("condition");
		_negate = (WOAssociation) nsdictionary.objectForKey("negate");

		if (_condition == null) {
			throw new WODynamicElementCreationException("<" + getClass().getName() + "> Missing 'condition' attribute in initialization.");
		}
	}

	public String toString() {
		return "<WOConditional :  condition: " + (_condition == null ? "null" : _condition.toString()) + " negate: " + (_negate == null ? "null" : _negate.toString()) + ">";
	}

	public void takeValuesFromRequest(WORequest worequest, WOContext wocontext) {
		ERXWOConditional.setLastCondition(Boolean.FALSE);
		com.webobjects.appserver.WOComponent wocomponent = wocontext.component();
		boolean flag = _condition.booleanValueInComponent(wocomponent);

		boolean flag1 = false;
		if (_negate != null) {
			flag1 = _negate.booleanValueInComponent(wocomponent);
		}

		if (flag && !flag1 || !flag && flag1) {
			super.takeValuesFromRequest(worequest, wocontext);
		}
	}

	public void takeChildrenValuesFromRequest(WORequest worequest, WOContext wocontext) {
		ERXWOConditional.setLastCondition(null);
		super.takeChildrenValuesFromRequest(worequest, wocontext);
		ERXWOConditional.setLastCondition(Boolean.TRUE);
	}

	public WOActionResults invokeAction(WORequest worequest, WOContext wocontext) {
		ERXWOConditional.setLastCondition(Boolean.FALSE);
		com.webobjects.appserver.WOComponent wocomponent = wocontext.component();
		boolean flag = _condition.booleanValueInComponent(wocomponent);

		boolean flag1 = false;
		if (_negate != null) {
			flag1 = _negate.booleanValueInComponent(wocomponent);
		}

		if (flag && !flag1 || !flag && flag1) {
			return super.invokeAction(worequest, wocontext);
		}
		else {
			return null;
		}
	}

	public WOActionResults invokeChildrenAction(WORequest worequest, WOContext wocontext) {
		ERXWOConditional.setLastCondition(null);
		WOActionResults results = super.invokeChildrenAction(worequest, wocontext);
		ERXWOConditional.setLastCondition(Boolean.TRUE);
		return results;
	}

	public void appendToResponse(WOResponse woresponse, WOContext wocontext) {
		ERXWOConditional.setLastCondition(Boolean.FALSE);
		com.webobjects.appserver.WOComponent wocomponent = wocontext.component();
		boolean flag = _condition.booleanValueInComponent(wocomponent);

		boolean flag1 = false;
		if (_negate != null) {
			flag1 = _negate.booleanValueInComponent(wocomponent);
		}

		if (flag && !flag1 || !flag && flag1) {
			appendChildrenToResponse(woresponse, wocontext);
		}
	}

	public void appendChildrenToResponse(WOResponse woresponse, WOContext wocontext) {
		ERXWOConditional.setLastCondition(null);
		super.appendChildrenToResponse(woresponse, wocontext);
		ERXWOConditional.setLastCondition(Boolean.TRUE);
	}
}