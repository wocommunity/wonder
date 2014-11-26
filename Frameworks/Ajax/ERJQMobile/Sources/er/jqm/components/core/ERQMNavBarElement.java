package er.jqm.components.core;

import com.webobjects.appserver.WOContext;
import com.webobjects.foundation.NSMutableArray;

import er.extensions.foundation.ERXHyperlinkResource;
import er.jqm.components.ERQMComponentBase;

/**
 * Button Links with data-role="button". Links in toolbars are auto-enhanced, no data-role required.
 * 
 * <pre>
 * one of:
 *    action
 *    href
 *    linkResource @see {@link ERXHyperlinkResource}
 * queryDictionary
 * 
 * string
 * id
 * 
 * otherTagString tag string added to the container
 * class
 *   
 * data-icon	home | delete | plus | arrow-u | arrow-d | carat-l | carat-t | carat-r | carat-b | check | gear | grid | star | custom | arrow-r | arrow-l | minus | refresh | forward | back | alert | info | search
 * data-ajax	<strong>true</strong> | false
 * data-transition	<strong>fade</strong> | flip | flow | pop | slide | slidedown | slidefade | slideup | turn | none
 * 
 * </pre>
 */
public class ERQMNavBarElement extends ERQMComponentBase
{
	public ERQMNavBarElement(WOContext aContext)
	{
		super(aContext);
	}

	public boolean hasAction()
	{
		return hasBinding("action");
	}

	public boolean hasLinkResource()
	{
		return hasBinding("linkResource");
	}

	public boolean hasHref()
	{
		return hasBinding("href");
	}

	public String href()
	{
		return stringValueForBinding("href");
	}

	@Override
	public void appendCustomTags(StringBuilder sb, NSMutableArray<String> classes, NSMutableArray<String> styles)
	{
		if (_booleanValueForBinding("selected", false, "isSelected"))
		{
			classes.add("ui-btn-active");
		}
		appendStringTag(sb, "data-icon", null, "icon");
	}
}
