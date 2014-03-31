package er.jqm.components.core;

import com.webobjects.appserver.WOContext;
import com.webobjects.foundation.NSMutableArray;

import er.jqm.components.ERQMComponentBase;

public class ERQMContent extends ERQMComponentBase
{
	public ERQMContent(WOContext context)
	{
		super(context);
	}

	@Override
	public void appendCustomTags(StringBuilder sb, NSMutableArray<String> classes, NSMutableArray<String> styles)
	{
		classes.add("ui-content");
		if (booleanValueForBinding("isRoleMain", false))
		{
			sb.append(" role=\"main\"");
		}
	}
}