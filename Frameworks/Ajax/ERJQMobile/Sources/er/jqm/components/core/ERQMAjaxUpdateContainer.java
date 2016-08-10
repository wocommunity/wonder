package er.jqm.components.core;

import com.webobjects.appserver.WOContext;
import com.webobjects.foundation.NSMutableArray;

import er.jqm.components.ERQMComponentBase;

/**
 * Container for JQM load()
 * 
 * <pre>
 * elementName <strong>div</strong>
 * 
 * id
 * class
 * otherTagSring
 * 
 * </pre>
 * 
 */
public class ERQMAjaxUpdateContainer extends ERQMComponentBase
{
	public ERQMAjaxUpdateContainer(WOContext context)
	{
		super(context);
	}

	public String elementName()
	{
		return stringValueForBinding("elementName", "div");
	}

	@Override
	public void appendCustomTags(StringBuilder sb, NSMutableArray<String> classes, NSMutableArray<String> styles)
	{
		// TODO Auto-generated method stub

	}
}