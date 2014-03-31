package er.jqm.components.core;

import com.webobjects.appserver.WOContext;
import com.webobjects.foundation.NSMutableArray;

import er.jqm.components.ERQMComponentBase;

/**
 * <pre>
 * class
 * id
 * otherTagString
 * 
 * data-iconpos	left | right | <strong>top</strong> | bottom | notext
 * </pre>
 */
public class ERQMNavBar extends ERQMComponentBase
{
	public ERQMNavBar(WOContext aContext)
	{
		super(aContext);
	}

	@Override
	public void appendCustomTags(StringBuilder sb, NSMutableArray<String> classes, NSMutableArray<String> styles)
	{
		appendStringTag(sb, "data-iconpos", null, null, false);
	}
}
