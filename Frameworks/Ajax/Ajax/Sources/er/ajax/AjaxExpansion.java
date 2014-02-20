package er.ajax;

import com.webobjects.appserver.WOActionResults;
import com.webobjects.appserver.WOContext;
import com.webobjects.appserver.WORequest;
import com.webobjects.appserver.WOResponse;

import er.extensions.appserver.ERXWOContext;
import er.extensions.components.conditionals.ERXWOTemplate;
import er.extensions.foundation.ERXStringUtilities;

/**
 * <p>
 * {@code AjaxExpansion} provides an easy way to make expansion areas that
 * appear and disappear by clicking a link. (For instance, expandable options
 * areas). The simple implementation of an expansion area would include wrapping
 * the toggle link in the {@link AjaxUpdateContainer}. The problem with this
 * approach is that if you want to animate the appearance of the contents, the
 * animation affects the link as well as the contents. {@code AjaxExpansion}
 * instead only updates the contents, and applies an "expanded" class to the
 * link, which you can use to change the expansion icon in a stylesheet. (See
 * AjaxExample2's ToggleDetails example).
 * </p>
 * 
 * <p>
 * You can use an {@code openedLabel} and {@code closedLabel} binding to change
 * the link text. Or, if you want to use something fancier than a string as link
 * label, you can put an {@link ERXWOTemplate} with {@code templateName='label'}
 * inside the component. If present, that will replace the label provided by the
 * {@code string} binding.
 * </p>
 * 
 * <h3>Example</h3>
 * 
 * <pre>
 * <code>&lt;wo:AjaxExpansion id="contact" class="contact" insertion="Effect.blind"
 *                   insertionDuration="0.1" string="Contact" action="$hitMe"&gt;
 *   &lt;dl&gt;
 *     &lt;dt&gt;Phone&lt;/dt&gt;
 *     &lt;dd&gt;804.555.1212&lt;/dd&gt;
 *     &lt;dt&gt;Address&lt;/dt&gt;
 *     &lt;dd&gt;
 *       123 Somewhere Rd.&lt;br /&gt;
 *       Richmond, VA 23233 
 *     &lt;/dd&gt;
 *   &lt;/dl&gt;
 * &lt;/wo:AjaxExpansion&gt;</code>
 * </pre>
 * 
 * @author mschrag
 * @binding id the id of the contents <code>div</code>
 * @binding linkID the id of the toggle link (defaults to "[id]Link")
 * @binding class the class of the contents <code>div</code>
 * @binding linkClass the class of the toggle link (always gets "expansion"
 *          added, and "expanded" when opened)
 * @binding expanded optionally allows controlling the expansion state of the
 *          contents
 * @binding initiallyExpanded optionally allows controlling the initial
 *          expansion state when the "expanded" binding is <em>not</em> used
 * @binding string the string displayed for the link. For something fancier than
 *          a plain string, see above.
 * @binding openedLabel the string to display when expanded. An alternative to
 *          the <code>string</code> binding.
 * @binding closedLabel the string to display when not expanded. An alternative
 *          to the <code>string</code> binding.
 * @binding insertion the insertion effect (see <code>AjaxUpdateLink</code>)
 * @binding insertionDuration the insertion effect duration (see
 *          <code>AjaxUpdateLink</code>)
 * @binding action the action to fire when the hyperlink is clicked (that is, on
 *          expansion <em>and</em> contraction)
 * @binding onLoading JavaScript function to evaluate when the update request
 *          begins
 * @binding onComplete JavaScript function to evaluate when the update request
 *          has finished
 * @binding onSuccess JavaScript function to evaluate when the update request
 *          was successful
 * @binding onFailure JavaScript function to evaluate when the update request
 *          has failed
 * @binding onException JavaScript function to evaluate when the update request
 *          had errors
 * @binding accesskey hot key that should toggle the expansion (optional)
 * @binding onExpansionComplete value for the <code>AjaxUpdateContainer</code>
 *          <code>onRefreshComplete</code> binding when the contents are
 *          expanded
 */
public class AjaxExpansion extends AjaxComponent {
	/**
	 * Do I need to update serialVersionUID?
	 * See section 5.6 <cite>Type Changes Affecting Serialization</cite> on page 51 of the 
	 * <a href="http://java.sun.com/j2se/1.4/pdf/serial-spec.pdf">Java Object Serialization Spec</a>
	 */
	private static final long serialVersionUID = 1L;

	/**
	 * ID for {@link AjaxUpdateContainer}
	 */
	private String _id;

	/**
	 * Current expansion state
	 */
	private Boolean _expanded;

	/**
	 * Constructor
	 * 
	 * @param context a {@link WOContext}
	 */
	public AjaxExpansion(WOContext context) {
		super(context);
	}

	@Override
	public boolean synchronizesVariablesWithBindings() {
		return false;
	}

	/**
	 * Returns ID for {@link AjaxUpdateLink}.
	 * 
	 * @return value of {@code linkID} binding if set, otherwise value of
	 *         {@link #id()} with "Link" appended
	 */
	public String linkID() {
		String linkID = (String) valueForBinding("linkID");
		if (linkID == null) {
			linkID = id() + "Link";
		}
		return linkID;
	}

	/**
	 * Returns CSS {@code class} attribute value applied to the
	 * {@link AjaxUpdateLink}, and the corresponding HTML hyperlink element.
	 * 
	 * @return CSS class
	 */
	public String linkClass() {
		String linkClass = (String) valueForBinding("linkClass");
		StringBuilder linkClassBuffer = new StringBuilder();
		linkClassBuffer.append("expansion");
		if (isExpanded()) {
			linkClassBuffer.append(" expanded");
		}
		if (linkClass != null) {
			linkClassBuffer.append(' ');
			linkClassBuffer.append(linkClass);
		}
		return linkClassBuffer.toString();
	}

	/**
	 * Returns ID for {@link AjaxUpdateContainer}.
	 * 
	 * @return ID value
	 */
	public String id() {
		if (_id == null) {
			_id = (String) valueForBinding("id");
			if (_id == null) {
				_id = ERXWOContext.safeIdentifierName(context(), true);
			}
		}
		return _id;
	}

	/**
	 * Returns the label to render for the hyperlink, based on the
	 * {@code string} or {@code openedLabel} and {@code closedLabel} bindings.
	 * 
	 * @return label to render for the hyperlink
	 */
	public String string() {
        String string = (String) valueForBinding("string");
        if (null == string) {
            if (isExpanded()) {
                string = (String)valueForBinding("openedLabel");
            } else {
                string = (String)valueForBinding("closedLabel");
            }
        }
        return string;
    }

	/**
	 * Is this request an Ajax request?
	 * 
	 * @return {@code true} if this request is an Ajax request, otherwise
	 *         {@code false}
	 */
	public boolean isAjaxRequest() {
		return AjaxUtils.isAjaxRequest(context().request());
	}

	/**
	 * Returns an escaped version of {@link #string()} using
	 * {@link er.extensions.foundation.ERXStringUtilities#escapeJavascriptApostrophes(String)}.
	 * 
	 * @return escaped string
	 */
    public String jsEscapedString() {
        return ERXStringUtilities.escapeJavascriptApostrophes(string());
    }

    /**
     * Adds required resources for this component.
     */
	@Override
	protected void addRequiredWebResources(WOResponse response) {
		addScriptResourceInHead(response, "prototype.js");
		addScriptResourceInHead(response, "effects.js");
		addScriptResourceInHead(response, "wonder.js");
	}

	/**
	 * Returns {@code null}.
	 * 
	 * @return {@code null}
	 */
	@Override
	public WOActionResults handleRequest(WORequest request, WOContext context) {
		return null;
	}

	/**
	 * Is the component currently expanded?
	 * 
	 * @return {@code true} if the component is currently expanded, otherwise
	 *         {@code false}
	 */
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

	/**
	 * Sets the current expansion state of the component to {@code expanded}.
	 * 
	 * @param expanded
	 *            desired current expansion state
	 */
	public void setExpanded(boolean expanded) {
		Boolean e = Boolean.valueOf(expanded);
		if (hasBinding("expanded")) {
			setValueForBinding(e, "expanded");
		}
		else {
			_expanded = e;
		}
	}

	/**
	 * Toggles the current state of the component.
	 * 
	 * @return {@code null}
	 */
	public WOActionResults toggle() {
		setExpanded(!isExpanded());
		if (hasBinding("action")) {
			valueForBinding("action");
		}
		return null;
	}

	/**
	 * Returns value of {@code onExpansionComplete} binding, or {@code null} if
	 * the component is not currently expanded.
	 * 
	 * @return value of {@code onExpansionComplete} binding, or {@code null}
	 */
	public String onExpansionComplete() {
		if (hasBinding("onExpansionComplete") && isExpanded()) {
			return (String) valueForBinding("onExpansionComplete");
		}
		return null;
	}
}
