package er.jqm.components.core;

import com.webobjects.appserver.WOApplication;
import com.webobjects.appserver.WOContext;
import com.webobjects.appserver._private.WOComponentDefinition;
import com.webobjects.foundation.NSArray;
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
 * isButton	<strong>true</strong> | false
 * string
 * id
 * 
 * otherTagString tag string added to the container
 * class
 *   
 * data-corners	<strong>true</strong> | false
 * data-icon	home | delete | plus | arrow-u | arrow-d | carat-l | carat-t | carat-r | carat-b | check | gear | grid | star | custom | arrow-r | arrow-l | minus | refresh | forward | back | alert | info | search
 * data-iconpos	<strong>lef</strong>t | right | top | bottom | notext
 * data-iconshadow	true | <strong>false</strong>
 * data-inline	true | <strong>false</strong>
 * data-mini	true | <strong>false</strong> - Compact sized version
 * data-shadow	<strong>true</strong> | false
 * data-theme	swatch letter (a-z)
 * 
 * Links: including those with a data-role="button" share these attributes
 * 
 * data-ajax	<strong>true</strong> | false
 * data-direction	reverse - Reverse transition animation (only for page or dialog)
 * data-disabled	true | <strong>false</strong>
 * data-dom-cache	true | <strong>false</strong>
 * data-prefetch	true | <strong>false</strong>
 * data-rel	back - To move one step back in history
 *          dialog - To open link styled as dialog, not tracked in history
 *          external - For linking to another domain
 *          popup - For opening a popup
 * data-transition	<strong>fade</strong> | flip | flow | pop | slide | slidedown | slidefade | slideup | turn | none
 * data-position-to	<strong>origin</strong> - Centers the popup over the link that opens it
 *                  jQuery selector - Centers the popup over the specified element
 *                  window - Centers the popup in the window
 *                  Note: option only available when used with popups. See also: options.
 * data-wrapper-class
 * 
 * </pre>
 */
public class ERQMButton extends ERQMComponentBase
{
	public ERQMButton(WOContext aContext)
	{
		super(aContext);
	}

	private WOComponentDefinition _overridenComponentDefinition;

	public String wodName()
	{
		return ERQMButton.class.getName();
	}

	@Override
	public WOComponentDefinition _componentDefinition()
	{
		WOComponentDefinition aComponentDefinition = null;
		if (_overridenComponentDefinition != null)
		{
			aComponentDefinition = _overridenComponentDefinition;
		}
		else
		{
			NSArray<String> languages = null;
			if (context() != null)
			{
				languages = context()._languages();
			}
			aComponentDefinition = WOApplication.application()._componentDefinition(wodName(), languages);
			if (isCachingEnabled())
			{
				_overridenComponentDefinition = aComponentDefinition;
			}
		}
		return aComponentDefinition;
	}

	public String dataRelDefault()
	{
		return null;
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
		if (booleanValueForBinding("isButton", true))
		{
			sb.append(" data-role=\"button\"");
			appendStringTag(sb, "data-icon", null, "icon");
			appendStringTag(sb, "data-iconpos", "left", "iconpos");
			appendBooleanTag(sb, "data-inline", false, "inline");
			appendBooleanTag(sb, "data-mini", false, "mini");
			appendBooleanTag(sb, "data-shadow", true, null);
			appendStringTag(sb, "data-theme", null, "theme");

			if (_booleanValueForBinding("data-iconshadow", false, null))
			{
				classes.add("ui-shadow-icon");
			}
		}
		appendBooleanTag(sb, "data-ajax", true, null);
		appendStringTag(sb, "data-direction", null, null);
		appendBooleanTag(sb, "data-dom-cache", false, null);
		appendBooleanTag(sb, "data-prefetch", false, null);
		if (!appendStringTag(sb, "data-rel", dataRelDefault(), null, false))
		{
			if (booleanValueForBinding("externalLink", false))
			{
				log.debug(getClass().getName() + " 'externalLink' binding is deprecated. Use 'data-rel=\"external\"' instead.");
			}
		}
		appendStringTag(sb, "data-transition", null, "transition");
		appendStringTag(sb, "data-position-to", "origin", null);
		appendStringTag(sb, "data-wrapper-class", null, null);
	}

}
