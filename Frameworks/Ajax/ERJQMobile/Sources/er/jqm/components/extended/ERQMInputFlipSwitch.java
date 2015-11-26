package er.jqm.components.extended;

import com.webobjects.appserver.WOContext;
import com.webobjects.foundation.NSArray;
import com.webobjects.foundation.NSMutableArray;

import er.jqm.components.ERQMComponentBase;

/**
 * Select with data-role="flipswitch" and two option element
 * 
 * <pre>
 * id
 * class
 * value
 * otherTagString
 * inset
 * 
 * label
 * stringOn
 * stringOff
 * 
 * data-mini	true | <strong>false</strong> - Compact sized version
 * data-role	<strong>flipswitch</strong> | none - Prevents auto-enhancement to use native control
 * data-theme	swatch letter (a-z) - Added to the form element
 * data-track-theme	swatch letter (a-z) - Added to the form element
 * data-corners	<strong>true</strong> | false
 * data-disabled	true | <strong>false</strong>
 * </pre>
 */
public class ERQMInputFlipSwitch extends ERQMComponentBase
{
	public ERQMInputFlipSwitch(WOContext aContext)
	{
		super(aContext);
	}

	public String label()
	{
		return _stringValueForBinding("label", null, "string");
	}

	@Override
	public boolean inset()
	{
		return _booleanValueForBinding("inset", false, null);
	}

	private String on()
	{
		return stringValueForBinding("stringOn", "On");
	}

	private String off()
	{
		return stringValueForBinding("stringOff", "Off");
	}

	public NSArray<String> list()
	{
		return new NSArray<String>(off(), on());
	}

	@Override
	public void appendCustomTags(StringBuilder sb, NSMutableArray<String> classes, NSMutableArray<String> styles)
	{
		appendStringTag(sb, "data-theme", null, null);
		appendStringTag(sb, "data-track-theme", null, null);
		appendBooleanTag(sb, "data-mini", false, null);
		appendStringTag(sb, "data-role", "flipswitch", null,false);
	}

}
