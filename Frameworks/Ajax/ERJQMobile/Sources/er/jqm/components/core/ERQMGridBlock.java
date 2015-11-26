package er.jqm.components.core;

import com.webobjects.appserver.WOContext;
import com.webobjects.foundation.NSArray;
import com.webobjects.foundation.NSMutableArray;

import er.jqm.components.ERQMComponentBase;

public class ERQMGridBlock extends ERQMComponentBase
{
	public static final NSArray<String> BLOCK_NAME = new NSArray<String>("a", "a", "b", "c", "d", "e"); // 0 element dummy

	public ERQMGridBlock(WOContext aContext)
	{
		super(aContext);
	}

	private int blockNumber()
	{
		int i = intValueForBinding("blockNumber", 1);
		if (i < 1)
		{
			i = 1;
		}
		else if (i > 5)
		{
			i = 5;
		}
		return i;
	}

	public boolean hasTheme()
	{
		return hasNonNullBinding("theme");
	}

	public String themeClass()
	{
		return "ui-bar ui-bar-" + stringValueForBinding("theme");
	}

	@Override
	public void appendCustomTags(StringBuilder sb, NSMutableArray<String> classes, NSMutableArray<String> styles)
	{
		classes.add("ui-block-" + BLOCK_NAME.objectAtIndex(blockNumber()));
	}

}
