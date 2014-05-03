package er.jqm.components.core;

import com.webobjects.appserver.WOContext;
import com.webobjects.foundation.NSMutableArray;

import er.jqm.components.ERQMComponentBase;

/**
 * <pre>
 * otherTagString tag string added to the container
 * class
 * isHorizontal
 *   
 * data-corners	<strong>true</strong> | false
 * data-exclude-invisible	<strong>true</strong> | false - Sets whether to exclude invisible children in the assignment of rounded corners
 * data-mini	true | <strong>false</<strong> - Compact sized version for all items in the controlgroup
 * data-theme	swatch letter (a-z)
 * data-type	horizontal | <strong>vertical</strong> - For horizontal or vertical item alignment
 * </pre>
 */
public class ERQMGroupDiv extends ERQMComponentBase
{
	public ERQMGroupDiv(WOContext aContext)
	{
		super(aContext);
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

		if (booleanValueForBinding("inset", false))
		{
			classes.add("ui-field-contain");
		}
	}
}
