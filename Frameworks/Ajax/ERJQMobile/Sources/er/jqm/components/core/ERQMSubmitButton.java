package er.jqm.components.core;

import com.webobjects.appserver.WOContext;
import com.webobjects.foundation.NSMutableArray;

import er.jqm.components.ERQMComponentBase;

/**
 * Submit-Button with data-role="button". Button elements are auto-enhanced, no data-role required.
 * 
 * <pre>
 * one of:
 *    action
 *    or
 *    actionClass + directActionName
 * value
 * id
 * 
 * otherTagString tag string added to the container
 * class
 * 
 * data-disabled	true | <strong>false</strong>
 * data-corners	<strong>true</strong> | false
 * data-icon	home | delete | plus | arrow-u | arrow-d | carat-l | carat-t | carat-r | carat-b | check | gear | grid | star | custom | arrow-r | arrow-l | minus | refresh | forward | back | alert | info | search
 * data-iconpos	<strong>lef</strong>t | right | top | bottom | notext
 * data-iconshadow	true | <strong>false</strong>
 * data-inline	true | <strong>false</strong>
 * data-mini	true | <strong>false</strong> - Compact sized version
 * data-shadow	<strong>true</strong> | false
 * data-theme	swatch letter (a-z)
 * 
 * Links: including form submit buttons share these attributes
 * 
 * data-ajax	true | <strong>false</strong>
 * data-direction	reverse - Reverse transition animation (only for page or dialog)
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
public class ERQMSubmitButton extends ERQMComponentBase
{

	public ERQMSubmitButton(WOContext aContext)
	{
		super(aContext);
	}

	public boolean hasAction()
	{
		return hasBinding("action");
	}

	public String dataRelDefault()
	{
		return null;
	}

	@Override
	public void appendCustomTags(StringBuilder sb, NSMutableArray<String> classes, NSMutableArray<String> styles)
	{
		// sb.append(" data-role=\"button\"");
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

		appendBooleanTag(sb, "data-ajax", false, null, false);
		appendStringTag(sb, "data-direction", null, null);
		appendBooleanTag(sb, "data-dom-cache", false, null);
		appendBooleanTag(sb, "data-prefetch", false, null);
		if (!appendStringTag(sb, "data-rel", dataRelDefault(), null))
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
