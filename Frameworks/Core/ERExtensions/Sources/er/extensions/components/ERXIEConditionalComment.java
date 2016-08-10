package er.extensions.components;

import com.webobjects.appserver.WOContext;

/**
 * Conditional comments make it easy for developers to take advantage 
 * of the enhanced features offered by Microsoft Internet Explorer 5 and later 
 * versions, while writing pages that downgrade gracefully in less-capable 
 * browsers or display correctly in browsers other than Windows Internet 
 * Explorer.
 * <p>
 * From http://msdn.microsoft.com/en-us/library/ms537512(VS.85).aspx
 * <p>
 * ERXIEConditionalComment is designed to generate IE conditional comments.
 * It supports all conditional comment styles except combination sub-expressions
 * and explicit true/false conditionals.
 * 
 * <h3>Example Usage</h3>
 * In your HTML file an IE Conditional would look something like:
 * <br><code>&lt;webobject name = "IsNotIE" &gt;Content rendered by all browsers except IE version 5+&lt;/webobject&gt;</code>
 * <br><code>&lt;webobject name = "IsNotIE6" &gt;Content only rendered by IE. Includes all versions of IE greater than IE5, not including IE6&lt;/webobject&gt;</code>
 * <br><code>&lt;webobject name = "IsGTEIE7" &gt;Content only rendered by IE 7 and above&lt;/webobject&gt;</code>
 * <p>
 * In your WOD file, those conditionals would be bound like:
 * <br><code>IsNotIE: ERXIEConditionalComment { negate = true; }</code>
 * <br><code>IsNotIE6: ERXIEConditionalComment { negate = true; versionString = "6"; }</code>
 * <br><code>IsGTEIE7: ERXIEConditionalComment { isGreaterThanEqual = true; versionString = "7"; }</code>
 * 
 * @author Ramsey Gurley
 * @binding isGreaterThan boolean binding evaluates the version of IE against the versionString binding
 * @binding isGreaterThanEqual boolean binding evaluates the version of IE against the versionString binding
 * @binding isLessThan boolean binding evaluates the version of IE against the versionString binding
 * @binding isLessThanEqual boolean binding evaluates the version of IE against the versionString binding
 * @binding negate boolean binding indicates the inverse of the version evaluation if versionString binding is bound. Otherwise, this binding escapes the IE conditional comments so that all browser except IE will render the component contents.
 * @binding versionString the version of IE being targeted.  If all versions of IE are targeted, leave versionString unbound. If all browser except IE are targeted, leave versionString unbound and bind negate.
 * 
 */
public class ERXIEConditionalComment extends ERXStatelessComponent {
	/**
	 * Do I need to update serialVersionUID?
	 * See section 5.6 <cite>Type Changes Affecting Serialization</cite> on page 51 of the 
	 * <a href="http://java.sun.com/j2se/1.4/pdf/serial-spec.pdf">Java Object Serialization Spec</a>
	 */
	private static final long serialVersionUID = 1L;

    public ERXIEConditionalComment(WOContext context) {
        super(context);
    }
}
