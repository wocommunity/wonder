package er.jqm.components.core;

import com.webobjects.appserver.WOContext;
import com.webobjects.foundation.NSMutableArray;

import er.jqm.components.ERQMComponentBase;

/**
 * Textarea with autoresize
 * 
 * <pre>
 * data-corners	<strong>true</strong> | false
 * data-clear-btn	true | <strong>false</strong> - Adds a clear button
 * data-clear-btn-text	string - Text for the close button. Default: "<strong>clear text</strong>"
 * data-mini	true | <strong>false</strong> - Compact sized version
 * data-role	none - Prevents auto-enhancement to use native control
 * data-theme	swatch letter (a-z) - Added to the form element
 * 
 * data-disabled	true | <strong>false</strong>
 * label
 * placeholder
 * inset	true | <strong>false</strong>
 * hideLabel	true | <strong>false</strong>
 * 
 * otherTagStringField tag string added to input field attribute list
 * otherTagStringLabel tag string added to label attribute list
 * 
 * </pre>
 */
public class ERQMText extends ERQMComponentBase
{
	public ERQMText(WOContext context)
	{
		super(context);
	}

	public String label()
	{
		return _stringValueForBinding("label", "string", null);
	}

	@Override
	public boolean inset()
	{
		return _booleanValueForBinding("inset", false, null);
	}

	public boolean hideLabel()
	{
		return _booleanValueForBinding("hideLabel", false, null);
	}

	public String otherTagStringLabel()
	{
		String tmp = _stringValueForBinding("otherTagStringLabel", "", null);
		if (hideLabel())
		{
			tmp += " class=\"ui-hidden-accessible\"";
		}
		return (tmp.length() > 0) ? tmp : null;
	}

	@Override
	public void appendCustomTags(StringBuilder sb, NSMutableArray<String> classes, NSMutableArray<String> styles)
	{
		appendStringTag(sb, "data-theme", null, "theme");
		appendStringTag(sb, "data-role", null, null);
		appendBooleanTag(sb, "data-mini", false, "mini");
		appendStringTag(sb, "placeholder", null, null);
		if (appendBooleanTag(sb, "data-clear-btn", false, null))
		{
			appendStringTag(sb, "data-clear-btn-text", null, null);
		}
		appendStringTag(sb, "data-wrapper-class", null, null);
	}
}