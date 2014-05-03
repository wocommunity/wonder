package er.jqm.components.core;

import com.webobjects.appserver.WOContext;

import er.extensions.components.ERXNonSynchronizingComponent;

/**
 * Listen for onChange event
 * 
 * <pre>
 * id
 * class
 * otherTagSring
 *
 * observeFieldID
 * updateContainerID if set, load() is used, otherwise get()
 * fullSubmit not tested yet
 * secure
 * action
 * </pre>
 * 
 */
public class ERQMAjaxObserveField extends ERXNonSynchronizingComponent
{
	public ERQMAjaxObserveField(WOContext context)
	{
		super(context);
	}

	public String observeFieldID()
	{
		return stringValueForBinding("observeFieldID");
	}

	public String updateContainerID()
	{
		return stringValueForBinding("updateContainerID", null);
	}

	public boolean fullSubmit()
	{
		return booleanValueForBinding("fullSubmit", false);
	}

	public String jqObserveFieldID()
	{
		return "#" + observeFieldID();
	}

	public String invSubBtnId()
	{
		return "isb_" + observeFieldID();
	}

	public String jqInvSubBtnId()
	{
		return "#" + invSubBtnId();
	}

	public boolean secure()
	{
		return booleanValueForBinding("secure", context().secureMode());
	}

}