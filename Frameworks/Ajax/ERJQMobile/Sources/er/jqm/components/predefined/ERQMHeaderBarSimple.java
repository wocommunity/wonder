package er.jqm.components.predefined;

import com.webobjects.appserver.WOContext;
import com.webobjects.foundation.NSMutableArray;

import er.jqm.components.ERQMComponentBase;

public class ERQMHeaderBarSimple extends ERQMComponentBase
{
	public ERQMHeaderBarSimple(WOContext aContext)
	{
		super(aContext);
	}

	public boolean hasRightButton()
	{
		return hasBinding("rightButton-action")|| hasBinding("rightButton-linkResource");
	}

	public boolean hasBackButtonAction()
	{
		return hasBinding("backButton-action");
	}

	@Override
	public void appendCustomTags(StringBuilder sb, NSMutableArray<String> classes, NSMutableArray<String> styles)
	{
	}
}
