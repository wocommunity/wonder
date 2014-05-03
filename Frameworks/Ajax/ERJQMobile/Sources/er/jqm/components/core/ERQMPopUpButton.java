package er.jqm.components.core;

import com.webobjects.appserver.WOContext;
import com.webobjects.appserver.WOResponse;
import com.webobjects.foundation.NSMutableArray;

import er.jqm.components.ERQMComponentBase;
import er.jqm.components.ERQMJavascriptAppender;

/**
 * <pre>
 * 
 * All select form elements are auto-enhanced, no data-role required
 * 
 * id
 * class
 * otherTagString
 * inset
 * label
 * filterable true | <strong>false</strong>
 *            if true data-native-menu is always false
 * 
 * list
 * item
 * displayString
 * selection
 * selectedValue
 * noSelectionString
 * disabled
 * 
 * data-divider-theme	swatch letter (a-z) - Default "b" - Only applicable if optgroup support is used in non-native selects
 * data-icon	home | delete | plus | arrow-u | <strong>arrow-d</strong> | carat-l | carat-t | carat-r | carat-b | check | gear | grid | star | custom | arrow-r | arrow-l | minus | refresh | forward | back | alert | info | search | false
 * data-iconpos	left | <strong>right</strong> | top | bottom | notext
 * data-inline	true | <strong>false</strong>
 * data-mini	true | <strong>false</strong> - Compact sized version
 * data-native-menu	<strong>true</strong> | false
 * data-overlay-theme	swatch letter (a-z) - Overlay theme for non-native selects
 * data-role	none - Prevents auto-enhancement to use native control
 * data-theme	swatch letter (a-z) - Added to the form element
 * </pre>
 * 
 * If filterable is set to true the following classes had to be add to a stylesheet
 * <pre>
 *.ui-selectmenu.ui-dialog .ui-content {
 *    padding-top: 0;
 *}
 *.ui-selectmenu.ui-dialog .ui-selectmenu-list {
 *    margin-top: 0;
 *}
 *.ui-selectmenu.ui-popup .ui-selectmenu-list li.ui-first-child .ui-btn {
 *    border-top-width: 1px;
 *    -webkit-border-radius: 0;
 *    border-radius: 0;
 *}
 *.ui-selectmenu.ui-dialog .ui-header {
 *    border-bottom-width: 1px;
 *}
 * </pre>
 */
public class ERQMPopUpButton extends ERQMComponentBase
{
	public ERQMPopUpButton(WOContext aContext)
	{
		super(aContext);
	}

	@Override
	public void appendToResponse(WOResponse response, WOContext context)
	{
		if (filterable())
		{
			ERQMJavascriptAppender.addScriptResourceAtCurrentPosition(response, context, "ERJQMobile", "min/javascript/jqm.filterPopup.min.js");
		}
		super.appendToResponse(response, context);
	}

	public boolean filterable()
	{
		return booleanValueForBinding("filterable", false);
	}

	public boolean hasSelection()
	{
		return hasBinding("selection");
	}

	public boolean hasLabel()
	{
		return hasBinding("label") || hasBinding("string");
	}

	public String label()
	{
		return _stringValueForBinding("label", null, "string");
	}

	@Override
	public void appendCustomTags(StringBuilder sb, NSMutableArray<String> classes, NSMutableArray<String> styles)
	{
		appendStringTag(sb, "data-divider-theme", null, null);
		appendStringTag(sb, "data-icon", "arrow-d", null);
		appendStringTag(sb, "data-iconpos", "right", null);

		appendBooleanTag(sb, "data-inline", false, null);
		appendBooleanTag(sb, "data-mini", false, "mini");
		if (filterable())
		{
			sb.append(" data-native-menu=\"false\"");
		}
		else
		{
			appendBooleanTag(sb, "data-native-menu", true, "isPopUpWindow");
		}
		appendStringTag(sb, "data-overlay-theme", null, null);
		appendStringTag(sb, "data-role", null, null);
		appendStringTag(sb, "data-theme", null, null);
	}
}
