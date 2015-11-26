package er.jqm.components.core;

import com.webobjects.appserver.WOContext;
import com.webobjects.foundation.NSMutableArray;

import er.jqm.components.ERQMPageBase;

/**
 * <pre>
 * id
 * class
 * otherTagString
 * 
 * data-theme	swatch letter (a-z) - Default "a"
 * data-title	string - Title used when page is shown
 * data-url	url - Value for updating the URL, instead of the url used to request the page
 * data-dom-cache	true | <strong>false</strong>
 * </pre>
 * 
 */
public class ERQMHtmlTemplate extends ERQMPageBase
{
	public ERQMHtmlTemplate(WOContext aContext)
	{
		super(aContext);
	}

	@Override
	public void appendCustomTags(StringBuilder sb, NSMutableArray<String> classes, NSMutableArray<String> styles)
	{
		appendStringTag(sb, "data-theme", null, "theme");
		appendStringTag(sb, "data-title", null, "title");

		appendStringTag(sb, "data-url", null, null);
		appendBooleanTag(sb, "data-dom-cache", false, null);
	}

	public Boolean needWonderAjaxFramework()
	{
		return true;
	}
}