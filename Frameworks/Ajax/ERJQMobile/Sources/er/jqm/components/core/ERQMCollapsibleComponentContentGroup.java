package er.jqm.components.core;

import com.webobjects.appserver.WOContext;
import com.webobjects.foundation.NSMutableArray;

import er.jqm.components.ERQMComponentBase;

/**
 * A number of collapsibles wrapped in a container with the data-role="collapsibleset"
 * 
 * <pre>
 * data-corners	<strong>true</strong> | false
 * data-collapsed-icon	home | delete | <strong>plus</strong> | arrow-u | arrow-d | carat-l | carat-t | carat-r | carat-b| check | gear | grid | star | custom | arrow-r | arrow-l | minus | refresh | forward | back | alert | info | search
 * data-content-theme	swatch letter (a-z) - Sets all collapsibles in set
 * data-disabled	true | <strong>false</strong>
 * data-expanded-icon	home | delete | plus | arrow-u | arrow-d | carat-l | carat-t | carat-r | carat-b| check | gear | grid | star | custom | arrow-r | arrow-l | <strong>minus</strong> | refresh | forward | back | alert | info | search
 * data-iconpos	<strong>left</strong> | right | top | bottom | notext
 * data-inset	<strong>true</strong> | false
 * data-mini	true | <strong>false</strong> - Compact sized version
 * data-theme	swatch letter (a-z) - Sets all collapsibles in set
 * </pre>
 */
public class ERQMCollapsibleComponentContentGroup extends ERQMComponentBase
{
	public ERQMCollapsibleComponentContentGroup(WOContext aContext)
	{
		super(aContext);
	}

	@Override
	public void appendCustomTags(StringBuilder sb, NSMutableArray<String> classes, NSMutableArray<String> styles)
	{
		appendStringTag(sb, "data-theme", null, "theme");
		appendStringTag(sb, "data-content-theme", null, "content-theme");
		appendBooleanTag(sb, "data-mini", false, "mini");
		appendBooleanTag(sb, "data-inset", false, "inset");

		appendStringTag(sb, "data-collapsed-icon", null, null);
		appendStringTag(sb, "data-expanded-icon", null, null);
		appendStringTag(sb, "data-iconpos", null, null);
	}
}
