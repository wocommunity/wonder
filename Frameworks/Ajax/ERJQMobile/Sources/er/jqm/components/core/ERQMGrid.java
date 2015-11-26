package er.jqm.components.core;

import com.webobjects.appserver.WOContext;
import com.webobjects.foundation.NSArray;
import com.webobjects.foundation.NSMutableArray;

import er.jqm.components.ERQMComponentBase;

/**
 * <pre>
 * id
 * class
 * style
 * otherTagString
 * responsive
 * 
 * columnCount
 * </pre>
 */
public class ERQMGrid extends ERQMComponentBase
{
	public static final NSArray<String> COLUMN_NAME = new NSArray<String>("a", "a", "a", "b", "c", "d"); // 0 + 1 element dummy

	public ERQMGrid(WOContext aContext)
	{
		super(aContext);
	}

	public String columnIdentifer()
	{
		return "ui-grid-" + COLUMN_NAME.objectAtIndex(columnCount());
	}

	public int columnCount()
	{
		int i = intValueForBinding("columnCount", 2);
		if (i < 2)
		{
			i = 2;
		}
		else if (i > 5)
		{
			i = 5;
		}
		return i;
	}

	@Override
	public void appendCustomTags(StringBuilder sb, NSMutableArray<String> classes, NSMutableArray<String> styles)
	{
		classes.add(columnIdentifer());
		if (booleanValueForBinding("responsive", false))
		{
			classes.add("ui-responsive");
		}
	}


}
