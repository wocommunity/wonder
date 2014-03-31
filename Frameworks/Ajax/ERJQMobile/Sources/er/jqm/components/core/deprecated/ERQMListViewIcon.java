package er.jqm.components.core.deprecated;

import com.webobjects.appserver.WOContext;
import com.webobjects.foundation.NSMutableArray;

import er.extensions.foundation.ERXStaticResource;
import er.jqm.components.ERQMComponentBase;

@Deprecated
public class ERQMListViewIcon extends ERQMComponentBase
{
	public ERQMListViewIcon(WOContext aContext)
	{
		super(aContext);
	}

	public ERXStaticResource imageResource()
	{
		return new ERXStaticResource(context(), stringValueForBinding("imageResource", null));
	}

	@Override
	public void appendCustomTags(StringBuilder sb, NSMutableArray<String> classes, NSMutableArray<String> styles)
	{
		logDeprecated(this.getClass().getCanonicalName() + " is depreceated. Use imageResource binding in ERQMListViewItem.");
		appendStringTag(sb, "alt", null, null);
	}

}