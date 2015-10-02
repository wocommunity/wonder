package er.extensions.components.javascript;

import com.webobjects.appserver.WOContext;

import er.extensions.components.ERXStatelessComponent;

/**
 * This component will display is content (as in WOComponentContent) if
 * it detects that popup windows are blocked.  This can be useful for displaying
 * messages to the user that they should allow popup windows or to allow alternate
 * access to functionality normally delivered in popup windows.
 * <p>
 * The content is used as a JavaScript string literal, so it must all be on single line.
 * Any line breaks will result in invalid JavaScript.
 * 
 * @binding negate 
 */
public class ERXJSPopupBlockerConditional extends ERXStatelessComponent {
	/**
	 * Do I need to update serialVersionUID?
	 * See section 5.6 <cite>Type Changes Affecting Serialization</cite> on page 51 of the 
	 * <a href="http://java.sun.com/j2se/1.4/pdf/serial-spec.pdf">Java Object Serialization Spec</a>
	 */
	private static final long serialVersionUID = 1L;

    /**
     * Public constructor
     * @param aContext a context
     */
    public ERXJSPopupBlockerConditional(WOContext aContext) {
        super(aContext);
    }

    /**
     * First part of the javascript to check if popups are blocked.
	 *
     * @return first part of the js string
     */
    public String string1() {
        return "<script LANGUAGE=\"JavaScript\">\n"+
        "<!--\n"+
        "var popupBlockerDetectWindow = window.open('','','width=1,height=1,top=10000,left=10000,scrollbars=no,location=no,menubar=no,toolbar=no,titlebar=no,resizable=no');\n"+
        "if(popupBlockerDetectWindow) {\n"+
        "   popupBlockerDetectWindow.close();\n"+
        "   var popUpsBlocked = false\n"+
        "}\n" +
        "else\n"+
        "   var popUpsBlocked = true;\n"+
        "if (" + ( negate() ? "! popUpsBlocked" : "popUpsBlocked") + " ) {\n" +
        "  document.write(\'";
    }

    private boolean negate() {
		return booleanValueForBinding("negate");
	}

	/**
     * Second part of the js string
     * @return second part of the js string.
     */
    public String string2() {
        return "')\n"+
            "}\n"+
            "//-->\n"+
            "</script>\n";
    }
}