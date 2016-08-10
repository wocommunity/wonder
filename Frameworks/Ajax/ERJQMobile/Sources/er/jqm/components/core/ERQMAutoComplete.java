package er.jqm.components.core;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.webobjects.appserver.WOContext;
import com.webobjects.foundation.NSArray;
import com.webobjects.foundation.NSDictionary;
import com.webobjects.foundation.NSMutableDictionary;

/**
 * Input type="<strong>text</strong> | password | number | email | url | tel | time | date | month | week | datetime | datetime-local | color"
 * 
 * <pre>
 * id
 * class
 * value
 * 
 * localValues
 * localMatchFromStart <strong>true</strong> | false
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
 * list-data-icon
 * list-inset	<strong>true</strong> | false
 * jsonAction
 * jsonActionClass
 * jsonDirectActionName
 * jsonQueryDictionary
 * jsonComplex	true | <strong>false</strong>
 * autosubmit	true | <strong>false</strong>
 * minLength <strong>3</strong>
 * 
 * </pre>
 * 
 * More Information unter https://github.com/commadelimited/autoComplete.js and
 */
public class ERQMAutoComplete extends ERQMTextField
{
	private static final Logger log = LoggerFactory.getLogger(ERQMAutoComplete.class);

	public ERQMAutoComplete(WOContext context)
	{
		super(context);
	}

	@Override
	public String wodName()
	{
		return ERQMAutoComplete.class.getName();
	}

	public String listID()
	{
		return "l_" + javaScriptElementID();
	}

	public boolean hasHiddenValue()
	{
		return hasBinding("valueHidden");
	}

	public String hiddenValueId()
	{
		return "h_" + javaScriptElementID();
	}

	public boolean hasLocalValues()
	{
		return hasBinding("localValues");
	}

	public String localValuesAsString()
	{
		String result = null;
		Object obj = _objectValueForBinding("localValues", null, null);
		if (obj != null)
		{
			if (obj instanceof String)
			{
				result = (String) obj;
			}
			else if (obj instanceof NSArray)
			{
				@SuppressWarnings("unchecked")
				NSArray<Object> array = (NSArray<Object>) obj;
				StringBuffer buf = new StringBuffer();
				for (int i = 0; i < array.count(); i++)
				{
					if (i > 0)
					{
						buf.append(", ");
					}
					buf.append("'");
					buf.append(array.get(i).toString());
					buf.append("'");
				}
				result = buf.toString();
			}
		}
		return result;
	}

	public boolean localMatchFromStart()
	{
		return booleanValueForBinding("localMatchFromStart", true);
	}

	public String jsonActionClass()
	{
		return stringValueForBinding("jsonActionClass");
	}

	public String jsonDirectActionName()
	{
		return stringValueForBinding("jsonDirectActionName");
	}

	@SuppressWarnings("unchecked")
	public NSDictionary<String, Object> jsonQueryDictionary()
	{
		NSMutableDictionary tmp = null;

		if (hasBinding("jsonQueryDictionary") && valueForKey("jsonQueryDictionary") != null)
		{
			tmp = new NSMutableDictionary(valueForNSDictionaryBindings("jsonQueryDictionary", new NSDictionary("1", "any")));
		}
		else
		{
			tmp = new NSMutableDictionary("1", "any");
		}

		return tmp.immutableClone();
	}

	public boolean isJsonComplex()
	{
		boolean complex = booleanValueForBinding("jsonComplex", false);
		if (complex && !hasHiddenValue())
		{
			log.error("if jsonComplex = true a hidden value must be set");
		}
		return complex;
	}

	public String minLength()
	{
		return stringValueForBinding("minLength", "3");
	}

	public boolean hasIcon()
	{
		return hasBinding("list-data-icon");
	}

	public boolean isAutosubmit()
	{
		return booleanValueForBinding("autosubmit", false);
	}

	public String otherTagStringList()
	{
		String tmp = _stringValueForBinding("otherTagStringList", "", null);
		if (hasBinding("list-data-icon"))
		{
			tmp = tmp + " data-icon=\"" + stringValueForBinding("list-data-icon") + "\"";
		}
		if (hasBinding("list-inset"))
		{
			tmp = tmp + " data-inset=\"" + stringValueForBinding("list-inset") + "\"";
		}
		else
		{
			tmp = tmp + " data-inset=\"true\"";
		}
		return tmp;
	}

}