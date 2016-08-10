package er.jqm.components.core;

import com.webobjects.appserver.WOContext;
import com.webobjects.foundation.NSMutableArray;

import er.jqm.components.ERQMPageBase;

/**
 * Page with data-dialog = true
 * 
 * <pre>
 * id
 * class
 * otherTagString
 * heading h1 - h6 default: <strong>h4</strong>
 * 
 * data-theme	swatch letter (a-z) - Default "a"
 * data-title	string - Title used when page is shown
 * data-url	url - Value for updating the URL, instead of the url used to request the page
 * data-dom-cache	true | <strong>false</strong>
 * data-overlay-theme	swatch letter (a-z) - Overlay theme when the page is opened as a dialog
 * data-corners	<strong>true</strong> | false
 * data-close-btn <strong>left</strong> | right | none
 * data-close-btn-text	string - Text for the close button, dialog only. Default: "close"
 * data-disabled	true | <strong>false</strong>
 * </pre>
 * 
 */
public class ERQMDialog extends ERQMPageBase
{
	public ERQMDialog(WOContext aContext)
	{
		super(aContext);
	}

	public String heading()
	{
		return _stringValueForBinding("heading", "h4", null);
	}

	@Override
	public void appendCustomTags(StringBuilder sb, NSMutableArray<String> classes, NSMutableArray<String> styles)
	{
		appendStringTag(sb, "data-theme", null, "theme");
		appendStringTag(sb, "data-title", null, "title");

		appendStringTag(sb, "data-url", null, null);
		appendBooleanTag(sb, "data-dom-cache", false, null);

		// Dialogs only
		appendStringTag(sb, "data-overlay-theme", null, null);
		appendBooleanTag(sb, "data-dialog", false, null);
		appendStringTag(sb, "data-close-btn", "left", null);
		appendStringTag(sb, "data-close-btn-text", null, null);
	}
}
