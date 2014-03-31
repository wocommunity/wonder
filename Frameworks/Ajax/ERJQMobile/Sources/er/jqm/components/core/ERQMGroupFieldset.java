package er.jqm.components.core;

import com.webobjects.appserver.WOContext;
import com.webobjects.foundation.NSMutableArray;

import er.jqm.components.ERQMComponentBase;

/**
 * <pre>
 * otherTagString tag string added to the container
 * class
 * isHorizontal
 * isDiv	<strong>true</strong> | false - if false a fieldset is used instead of an div
 * inset	true | <strong>false</strong> - surround all with div class = "ui-field-contain"
 * legend	text for legend
 *   
 * data-corners	<strong>true</strong> | false
 * data-exclude-invisible	<strong>true</strong> | false - Sets whether to exclude invisible children in the assignment of rounded corners
 * data-mini	true | <strong>false</<strong> - Compact sized version for all items in the controlgroup
 * data-theme	swatch letter (a-z)
 * data-type	horizontal | <strong>vertical</strong> - For horizontal or vertical item alignment
 * </pre>
 */
public class ERQMGroupFieldset extends ERQMComponentBase
{
	public ERQMGroupFieldset(WOContext aContext)
	{
		super(aContext);
	}

	public boolean hasLegend()
	{
		return (hasBinding("legend") || hasBinding("string"));
	}

	public String legend()
	{
		return _stringValueForBinding("legend", null, "string");
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
		if (booleanValueForBinding("isHorizontal", false))
		{
			sb.append(" data-type=\"horizontal\"");
		}
		else
		{
			appendStringTag(sb, "data-type", "vertical", null, true);
		}

		appendBooleanTag(sb, "data-exclude-invisible", true, null);
		appendBooleanTag(sb, "data-mini", false, null);
	}

}
