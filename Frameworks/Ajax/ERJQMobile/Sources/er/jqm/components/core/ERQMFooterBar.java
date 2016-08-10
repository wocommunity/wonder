package er.jqm.components.core;

import com.webobjects.appserver.WOContext;
import com.webobjects.foundation.NSMutableArray;

import er.jqm.components.ERQMComponentBase;

/**
 * Toolbar Container with data-role="footer"
 * 
 * <pre>
 *   otherTagString tag string added to the container
 *   class
 * 
 *   data-id	string - Unique ID. Required for persistent footers.
 *   data-theme	swatch letter (a-z)
 *   
 *   isNavBar true | <strong>false</strong> - wraps the content in ui-bar
 *   isFixed true | <strong>false</strong> - data-position="fixed"
 *   
 *   Bindings for Fixed toolbar only ( data-position="fixed" )
 * 
 *   data-disable-page-zoom	<strong>true</strong> | false - User-scaling-ability for pages with fixed toolbars
 *   data-fullscreen	true | <strong>false</strong> - Setting toolbars over the page-content
 *   data-tap-toggle	<strong>true</strong> | false - Ability to toggle toolbar-visibility on user tap/click
 *   data-transition	<strong>slide</strong> | fade | none - Show/hide-transition when a tap/click occurs
 *   data-update-page-padding	<strong>true</strong> | false - Have the page top and bottom padding updated on resize, transition, "updatelayout" events (the framework always updates the padding on the "pageshow" event).
 *   data-visible-on-page-show	<strong>true</strong> | false - Toolbar-visibility when parent page is shown
 * </pre>
 */
public class ERQMFooterBar extends ERQMComponentBase
{
	public ERQMFooterBar(WOContext aContext)
	{
		super(aContext);
	}

	@Override
	public void appendCustomTags(StringBuilder sb, NSMutableArray<String> classes, NSMutableArray<String> styles)
	{
		sb.append(" data-role=\"footer\"");
		appendStringTag(sb, "data-id", null, null, false);
		appendStringTag(sb, "data-theme", null, "theme");

		if (!booleanValueForBinding("isNavBar", false))
		{
			classes.add("ui-bar");
		}

		if (booleanValueForBinding("isFixed", false))
		{
			sb.append(" data-position=\"fixed\"");

			appendBooleanTag(sb, "data-disable-page-zoom", true, null);
			appendBooleanTag(sb, "data-fullscreen", false, null);
			appendBooleanTag(sb, "data-tap-toggle", true, null);
			appendStringTag(sb, "data-transition", null, null);
			appendBooleanTag(sb, "data-update-page-padding", true, null);
			appendBooleanTag(sb, "data-visible-on-page-show", true, null);
		}
	}
}
