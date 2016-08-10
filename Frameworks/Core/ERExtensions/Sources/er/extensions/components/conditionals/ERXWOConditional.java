package er.extensions.components.conditionals;

import com.webobjects.appserver.WOActionResults;
import com.webobjects.appserver.WOAssociation;
import com.webobjects.appserver.WOComponent;
import com.webobjects.appserver.WOContext;
import com.webobjects.appserver.WOElement;
import com.webobjects.appserver.WORequest;
import com.webobjects.appserver.WOResponse;
import com.webobjects.appserver._private.WODynamicElementCreationException;
import com.webobjects.appserver._private.WODynamicGroup;
import com.webobjects.foundation.NSDictionary;

import er.extensions.appserver.ERXWOContext;

/**
 * ERXWOConditional behaves just like WOConditional except that it tracks its condition state for use with ERXElse.
 * Also makes it easier to override by implementing {@link #meetsConditionInComponent(WOComponent)} and {@link #pullAssociations(NSDictionary)}.
 * @author mschrag
 * @author ak
 * @binding condition
 * @binding negate
 */
public class ERXWOConditional extends WODynamicGroup {
	public static final String LAST_CONDITION_KEY = "er.extensions.ERXWOConditional.lastCondition";

	protected WOAssociation _condition;
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

	/**
	 * Override this to return true when your condition is met.
	 */
	protected boolean conditionInComponent(WOComponent wocomponent) {   
		boolean condition = _condition.booleanValueInComponent(wocomponent);
		return condition;
	}

	protected final boolean meetsConditionInComponent(WOComponent wocomponent) {   
		boolean condition = conditionInComponent(wocomponent);

		boolean negate = false;
		if (_negate != null) {
			negate = _negate.booleanValueInComponent(wocomponent);
		}

		return condition && !negate || !condition && negate;
	}

	/**
	 * Override to pull the associations for your condition. The
	 * <code>negate</code> has already been pulled, so don't call super, as you
	 * will get an IllegalStateException because <code>condition</code> isn't
	 * bound. 
	 */
	protected void pullAssociations(NSDictionary<String, ? extends WOAssociation> nsdictionary) {

		_condition =nsdictionary.objectForKey("condition");

		if (_condition == null && getClass() == ERXWOConditional.class) {
			throw new WODynamicElementCreationException("<" + getClass().getName() + "> Missing 'condition' attribute in initialization.");
		}

	}

	public ERXWOConditional(String name, NSDictionary dict, WOElement element) {
		super(null, null, element);
		_negate = (WOAssociation) dict.objectForKey("negate");
		pullAssociations(dict);
	}

	@Override
	public String toString() {
		return "<WOConditional :  condition: " + (_condition == null ? "null" : _condition.toString()) + " negate: " + (_negate == null ? "null" : _negate.toString()) + ">";
	}

	@Override
	public void takeValuesFromRequest(WORequest worequest, WOContext wocontext) {
		ERXWOConditional.setLastCondition(Boolean.FALSE);
		if (meetsConditionInComponent(wocontext.component())) {
			super.takeValuesFromRequest(worequest, wocontext);
		}
	}

	@Override
	public void takeChildrenValuesFromRequest(WORequest worequest, WOContext wocontext) {
		ERXWOConditional.setLastCondition(null);
		super.takeChildrenValuesFromRequest(worequest, wocontext);
		ERXWOConditional.setLastCondition(Boolean.TRUE);
	}

	@Override
	public WOActionResults invokeAction(WORequest worequest, WOContext wocontext) {
		ERXWOConditional.setLastCondition(Boolean.FALSE);
		if (meetsConditionInComponent(wocontext.component())) {
			return super.invokeAction(worequest, wocontext);
		}
		return null;
	}

	@Override
	public WOActionResults invokeChildrenAction(WORequest worequest, WOContext wocontext) {
		ERXWOConditional.setLastCondition(null);
		WOActionResults results = super.invokeChildrenAction(worequest, wocontext);
		ERXWOConditional.setLastCondition(Boolean.TRUE);
		return results;
	}

	@Override
	public void appendToResponse(WOResponse woresponse, WOContext wocontext) {
		ERXWOConditional.setLastCondition(Boolean.FALSE);
		if (meetsConditionInComponent(wocontext.component())) {
			appendChildrenToResponse(woresponse, wocontext);
		}
	}

	@Override
	public void appendChildrenToResponse(WOResponse woresponse, WOContext wocontext) {
		ERXWOConditional.setLastCondition(null);
		super.appendChildrenToResponse(woresponse, wocontext);
		ERXWOConditional.setLastCondition(Boolean.TRUE);
	}
}
