package er.extensions;

import com.webobjects.appserver.WOActionResults;
import com.webobjects.appserver.WOContext;
import com.webobjects.appserver.WOElement;
import com.webobjects.appserver.WORequest;
import com.webobjects.appserver.WOResponse;
import com.webobjects.appserver._private.WOConditional;
import com.webobjects.foundation.NSDictionary;

/**
 * ERXWOConditional behaves just like WOConditional except that it tracks its condition state for use with ERXElse.
 * 
 * @author mschrag
 */
public class ERXWOConditional extends WOConditional {
	public static final String LAST_CONDITION_KEY = "er.extensions.ERXWOConditional.lastCondition";

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
		super(s, nsdictionary, woelement);
	}

	public void takeValuesFromRequest(WORequest worequest, WOContext wocontext) {
		ERXWOConditional.setLastCondition(Boolean.FALSE);
		super.takeValuesFromRequest(worequest, wocontext);
	}

	public void takeChildrenValuesFromRequest(WORequest worequest, WOContext wocontext) {
		ERXWOConditional.setLastCondition(null);
		super.takeChildrenValuesFromRequest(worequest, wocontext);
		ERXWOConditional.setLastCondition(Boolean.TRUE);
	}

	public void appendToResponse(WOResponse woresponse, WOContext wocontext) {
		ERXWOConditional.setLastCondition(Boolean.FALSE);
		super.appendToResponse(woresponse, wocontext);
	}

	public void appendChildrenToResponse(WOResponse woresponse, WOContext wocontext) {
		ERXWOConditional.setLastCondition(null);
		super.appendChildrenToResponse(woresponse, wocontext);
		ERXWOConditional.setLastCondition(Boolean.TRUE);
	}

	public WOActionResults invokeAction(WORequest worequest, WOContext wocontext) {
		ERXWOConditional.setLastCondition(Boolean.FALSE);
		return super.invokeAction(worequest, wocontext);
	}

	public WOActionResults invokeChildrenAction(WORequest worequest, WOContext wocontext) {
		ERXWOConditional.setLastCondition(null);
		WOActionResults results = super.invokeChildrenAction(worequest, wocontext);
		ERXWOConditional.setLastCondition(Boolean.TRUE);
		return results;
	}

}
