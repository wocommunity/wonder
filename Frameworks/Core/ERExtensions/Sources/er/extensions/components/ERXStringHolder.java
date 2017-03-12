package er.extensions.components;

import com.webobjects.appserver.WOContext;

/**
 * Use this to return a string from direct actions.
 *
 * @binding value value
 * @binding escapeHTML escape HTML
 *
 * @author ak on Sat Sep 27 2003
 */
public class ERXStringHolder extends ERXStatelessComponent {
	/**
	 * Do I need to update serialVersionUID?
	 * See section 5.6 <cite>Type Changes Affecting Serialization</cite> on page 51 of the 
	 * <a href="http://java.sun.com/j2se/1.4/pdf/serial-spec.pdf">Java Object Serialization Spec</a>
	 */
	private static final long serialVersionUID = 1L;

    protected String _value;
    protected Boolean _escapeHTML = Boolean.FALSE;

    /**
     * Public constructor
     * @param context the context
     */
    public ERXStringHolder(WOContext context) {
        super(context);
    }

    public String value() { return _value; }
    public boolean escapeHTML() { return _escapeHTML.booleanValue(); }
    public void setValue(Object value) {
        _value = (value == null ? "" : value.toString());
    }
    public void setEscapeHTML(boolean value) {
        _escapeHTML = value ? Boolean.TRUE : Boolean.FALSE;
    }

    @Override
    public void reset() {
        _value = null;
        _escapeHTML = Boolean.FALSE;
    }
}
