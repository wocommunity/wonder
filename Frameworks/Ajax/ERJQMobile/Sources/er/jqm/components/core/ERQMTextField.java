package er.jqm.components.core;

import com.webobjects.appserver.WOApplication;
import com.webobjects.appserver.WOContext;
import com.webobjects.appserver._private.WOComponentDefinition;
import com.webobjects.foundation.NSArray;
import com.webobjects.foundation.NSMutableArray;

import er.jqm.components.ERQMComponentBase;

/**
 * Input type="<strong>text</strong> | password | number | email | url | tel | time | date | month | week | datetime | datetime-local | color"
 * 
 * <pre>
 * id
 * class
 * value
 * 
 * data-corners	<strong>true</strong> | false
 * data-clear-btn	true | <strong>false</strong> - Adds a clear button
 * data-clear-btn-text	string - Text for the close button. Default: "<strong>clear text</strong>"
 * data-mini	true | <strong>false</strong> - Compact sized version
 * data-role	none - Prevents auto-enhancement to use native control
 * data-theme	swatch letter (a-z) - Added to the form element
 * data-wrapper-class
 * 
 * data-disabled	true | <strong>false</strong>
 * label
 * placeholder
 * inset	true | <strong>false</strong> - surround all with div class = "ui-field-contain"
 * hideLabel	true | <strong>false</strong>
 * 
 * otherTagString tag string added to input field attribute list
 * otherTagStringLabel tag string added to label attribute list
 * 
 * </pre>
 */
public class ERQMTextField extends ERQMComponentBase
{
	public ERQMTextField(WOContext context)
	{
		super(context);
	}

	private transient WOComponentDefinition _overridenComponentDefinition;

	public String wodName()
	{
		return ERQMTextField.class.getName();
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

	public String typeName()
	{
		return "text";
	}

	public String type()
	{
		return _stringValueForBinding("type", typeName(), null);
	}

	public String label()
	{
		return _stringValueForBinding("label", "string", null);
	}

	@Override
	public boolean inset()
	{
		return _booleanValueForBinding("inset", false, null);
	}

	public boolean hideLabel()
	{
		return _booleanValueForBinding("hideLabel", false, null);
	}

	public String otherTagStringLabel()
	{
		String tmp = _stringValueForBinding("otherTagStringLabel", "", null);
		if (hideLabel())
		{
			tmp += " class=\"ui-hidden-accessible\"";
		}
		return (tmp.length() > 0) ? tmp : null;
	}

	@Override
	public void appendCustomTags(StringBuilder sb, NSMutableArray<String> classes, NSMutableArray<String> styles)
	{
		appendStringTag(sb, "data-theme", null, "theme");
		appendStringTag(sb, "data-role", null, null);
		appendBooleanTag(sb, "data-mini", false, "mini");
		appendStringTag(sb, "placeholder", null, null);
		if (appendBooleanTag(sb, "data-clear-btn", false, null))
		{
			appendStringTag(sb, "data-clear-btn-text", null, null);
		}
		appendStringTag(sb, "data-wrapper-class", null, null);
	}

}