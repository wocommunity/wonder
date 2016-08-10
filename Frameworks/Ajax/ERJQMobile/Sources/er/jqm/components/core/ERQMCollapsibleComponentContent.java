package er.jqm.components.core;

import com.webobjects.appserver.WOContext;
import com.webobjects.foundation.NSMutableArray;

import er.jqm.components.ERQMComponentBase;

/**
 * A number of collapsibles wrapped in a container with the data-role="collapsibleset"
 * 
 * <pre>
 * string
 * heading h1 - h6 default: <strong>h4</strong>
 * 
 * data-corners	<strong>true</strong> | false
 * data-collapsed	<strong>true</strong> | false
 * data-collapsed-icon	home | delete | <strong>plus</strong> | arrow-u | arrow-d | carat-l | carat-t | carat-r | carat-b| check | gear | grid | star | custom | arrow-r | arrow-l | minus | refresh | forward | back | alert | info | search
 * data-content-theme	swatch letter (a-z) - Sets all collapsibles in set
 * data-disabled	true | <strong>false</strong>
 * data-expanded-icon	home | delete | plus | arrow-u | arrow-d | carat-l | carat-t | carat-r | carat-b| check | gear | grid | star | custom | arrow-r | arrow-l | <strong>minus</strong> | refresh | forward | back | alert | info | search
 * data-iconpos	<strong>left</strong> | right | top | bottom | notext
 * data-inset	<strong>true</strong> | false
 * data-mini	true | <strong>false</strong> - Compact sized version
 * data-theme	swatch letter (a-z) - Sets all collapsibles in set
 * data-collapse-cue-text	string - Text used to provide audible feedback for users with screen reader software. Default: " click to collapse contents"
 * data-expand-cue-text	string - Text used to provide audible feedback for users with screen reader software. Default: " click to expand contents"
 * </pre>
 */
public class ERQMCollapsibleComponentContent extends ERQMComponentBase
{
	public ERQMCollapsibleComponentContent(WOContext aContext)
	{
		super(aContext);
	}

	public String heading()
	{
		return _stringValueForBinding("heading", "h4", null);
	}

	@Override
	public void appendCustomTags(StringBuilder sb, NSMutableArray<String> classes, NSMutableArray<String> styles)
	{
		appendBooleanTag(sb, "data-collapsed", true, "collapsed", true);

		appendStringTag(sb, "data-theme", null, "theme");
		appendStringTag(sb, "data-content-theme", null, "content-theme");
		appendBooleanTag(sb, "data-mini", false, "mini");
		appendBooleanTag(sb, "data-inset", false, "inset");

		appendStringTag(sb, "data-collapsed-icon", null, null);
		appendStringTag(sb, "data-expanded-icon", null, null);
		appendStringTag(sb, "data-iconpos", null, null);

		appendStringTag(sb, "data-collapse-cue-text", null, null);
		appendStringTag(sb, "data-expand-cue-text", null, null);
	}
}
