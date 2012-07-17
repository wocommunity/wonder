package er.ajax;

import com.webobjects.appserver.WOContext;

import er.extensions.components.ERXStatelessComponent;

// PROTOTYPE FUNCTIONS (component uses PeriodicUpdater)
/**
 * Simple Ajax long response handler that stays on the same page. The parent
 * must start the actual task and call fooUpdate to trigger the refresh.
 * 
 * @author ak
 * @binding id ID of the AjaxUpdate (required)
 * @binding isRunning true if the task is running and the update should get
 *          triggered (required)
 * @binding elementName elementName of the AjaxUpdate, defaults to DIV
 * @binding frequency frequency of the AjaxUpdate, defaults to 1 (sec)
 * @binding stopped start out in stopped mode. You need to start it by yourself via JS if you use this.
 */
public class AjaxLongResponse extends ERXStatelessComponent {
	/**
	 * Do I need to update serialVersionUID?
	 * See section 5.6 <cite>Type Changes Affecting Serialization</cite> on page 51 of the 
	 * <a href="http://java.sun.com/j2se/1.4/pdf/serial-spec.pdf">Java Object Serialization Spec</a>
	 */
	private static final long serialVersionUID = 1L;

	public AjaxLongResponse(WOContext context) {
		super(context);
	}

	public String elementName() {
		return stringValueForBinding("elementName", "div");
	}

	public int frequency() {
		return intValueForBinding("frequency", 1);
	}
}