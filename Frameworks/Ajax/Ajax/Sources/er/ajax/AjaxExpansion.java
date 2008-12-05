package er.ajax;

import com.webobjects.appserver.WOActionResults;
import com.webobjects.appserver.WOContext;
import com.webobjects.appserver.WORequest;
import com.webobjects.appserver.WOResponse;

import er.extensions.appserver.ERXWOContext;

/**
 * AjaxExpansion provides an easy way to make expansion areas that
 * appear and disappear by clicking a link (for instance, expandable
 * options areas).  The simple implementation of an expansion area would
 * include wrapping the toggle link in the AjaxUpdateContainer.  The problem with
 * this approach is that if you want to animate the appearance of the contents,
 * the animation effects the link as well as the contents.  AjaxExpansion instead
 * only updates the contents and applies an "expanded" class to the link, which
 * you can use to change the expansion icon in a stylesheet (see AjaxExample2's
 * ToggleDetails example).
 * 
 * If you want to use something fancier than a string as link label, you can put
 * a ERXWOTemplate with templateName='label' inside the component. If present,
 * that will replace the label provided by the 'string' binding.
 *  
 * @author mschrag
 * @binding id the id of the contents div 
 * @binding linkID the id of the toggle link (defaults to "[id]Link") 
 * @binding class the class of the contents div
 * @binding linkClass the class of the toggle link (always gets "expansion" added, and "expanded" when opened)
 * @binding expanded optionally allows controlling the expansion state of the contents
 * @binding initiallyExpanded optionally allows controlling the initial expansion state when the "expanded" binding is NOT used
 * @binding string the string displayed for the link. For something fancier than a plain string, see above.
 * @binding insertion the insertion effect (see AjaxUpdateLink)
 * @binding insertionDuration the insertion effect duration (see AjaxUpdateLink)
 * @binding action the action to fire when the contents are expanded
 * @binding accesskey hot key that should toggle the expansion (optional)
 */
public class AjaxExpansion extends AjaxComponent {
	private String _id;
	private Boolean _expanded;

	public AjaxExpansion(WOContext context) {
		super(context);
	}

	public boolean synchronizesVariablesWithBindings() {
		return false;
	}

	public String linkID() {
		String linkID = (String) valueForBinding("linkID");
		if (linkID == null) {
			linkID = id() + "Link";
		}
		return linkID;
	}

	public String linkClass() {
		String linkClass = (String) valueForBinding("linkClass");
		StringBuffer linkClassBuffer = new StringBuffer();
		linkClassBuffer.append("expansion");
		if (isExpanded()) {
			linkClassBuffer.append(" expanded");
		}
		if (linkClass != null) {
			linkClassBuffer.append(" ");
			linkClassBuffer.append(linkClass);
		}
		return linkClassBuffer.toString();
	}

	public String id() {
		if (_id == null) {
			_id = (String) valueForBinding("id");
			if (_id == null) {
				_id = ERXWOContext.safeIdentifierName(context(), true);
			}
		}
		return _id;
	}

	public String string() {
		String string = (String) valueForBinding("string", "Options");
		return string;
	}

	protected void addRequiredWebResources(WOResponse response) {
		addScriptResourceInHead(response, "prototype.js");
		addScriptResourceInHead(response, "effects.js");
		addScriptResourceInHead(response, "wonder.js");
	}

	public WOActionResults handleRequest(WORequest request, WOContext context) {
		return null;
	}

	public boolean isExpanded() {
		boolean expanded;
		if (hasBinding("expanded")) {
			expanded = ((Boolean) valueForBinding("expanded")).booleanValue();
		}
		else {
			if (_expanded == null) {
				_expanded = Boolean.valueOf(booleanValueForBinding("initiallyExpanded", false));
			}
			expanded = _expanded.booleanValue();
		}
		return expanded;
	}

	public void setExpanded(boolean expanded) {
		Boolean e = Boolean.valueOf(expanded);
		if (hasBinding("expanded")) {
			setValueForBinding(e, "expanded");
		}
		else {
			_expanded = e;
		}
	}

	public WOActionResults toggle() {
		setExpanded(!isExpanded());
		if (hasBinding("action")) {
			valueForBinding("action");
		}
		return null;
	}
}