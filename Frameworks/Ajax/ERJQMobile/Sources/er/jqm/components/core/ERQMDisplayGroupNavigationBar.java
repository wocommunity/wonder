package er.jqm.components.core;

import com.webobjects.appserver.WOActionResults;
import com.webobjects.appserver.WOContext;
import com.webobjects.appserver.WODisplayGroup;
import com.webobjects.foundation.NSMutableArray;

import er.jqm.components.ERQMComponentBase;

/**
 * Don't forget to add the stylesheet "dgnavbar/icon-pack-custom.css" to your page wrapper.<br />
 * <br />
 * displayGroup
 * 
 */
public class ERQMDisplayGroupNavigationBar extends ERQMComponentBase
{
	public ERQMDisplayGroupNavigationBar(WOContext context)
	{
		super(context);
	}

	public WODisplayGroup displayGroup()
	{
		return (WODisplayGroup) objectValueForBinding("displayGroup");
	}

	public boolean isMultiBatch()
	{
		return (displayGroup().batchCount() > 1);
	}

	public boolean isFirstBatch()
	{
		return (displayGroup().currentBatchIndex() == 1);
	}

	public boolean showFastBack()
	{
		return (displayGroup().currentBatchIndex() > 2);
	}

	public boolean isLastBatch()
	{
		return (displayGroup().currentBatchIndex() >= displayGroup().batchCount());
	}

	public WOActionResults displayFirstBatch()
	{
		displayGroup().setCurrentBatchIndex(1);
		return null;
	}

	@Override
	public void appendCustomTags(StringBuilder sb, NSMutableArray<String> classes, NSMutableArray<String> styles)
	{
		// TODO Auto-generated method stub

	}
}