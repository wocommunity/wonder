package er.jqm.components.core;

import com.webobjects.appserver.WOContext;
import com.webobjects.foundation.NSMutableArray;

import er.jqm.components.ERQMComponentBase;

/**
 * <pre>
 * id
 * name
 * class
 * checked
 * selection
 * value
 * otherTagString
 * isFlipswitch -> data-role="flipswitch"
 * 
 * data-corners	<strong>true</strong> | false
 * data-mini	true | <strong>false</strong> - Compact sized version
 * data-role	none - Prevents auto-enhancement to use native control
 * data-theme	swatch letter (a-z) - Added to the form element
 * data-disabled	true | <strong>false</strong>
 * data-iconpos	<strong>left</strong> | right
 * data-wrapper-class
 * </pre>
 */
public class ERQMRadioButton extends ERQMComponentBase
{
	public ERQMRadioButton(WOContext aContext)
	{
		super(aContext);
	}

	public boolean hasChecked()
	{
		return hasBinding("checked");
	}

	public String label()
	{
		return _stringValueForBinding("label", null, "string");
	}

	@Override
	public boolean inset()
	{
		return _booleanValueForBinding("inset", false, null);
	}

	@Override
	public void appendCustomTags(StringBuilder sb, NSMutableArray<String> classes, NSMutableArray<String> styles)
	{
		appendStringTag(sb, "data-theme", null, null);
		appendBooleanTag(sb, "data-mini", false, null);
		appendStringTag(sb, "data-role", (booleanValueForBinding("isFlipswitch", false)) ? "flipswitch" : null, null);
		appendStringTag(sb, "data-iconpos", null, null);
		appendStringTag(sb, "data-wrapper-class", null, null);
	}
}