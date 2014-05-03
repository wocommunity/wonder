package er.jqm.components;

import org.apache.log4j.Logger;

import com.webobjects.appserver.WOComponent;
import com.webobjects.appserver.WOContext;
import com.webobjects.foundation.NSArray;
import com.webobjects.foundation.NSMutableArray;

import er.extensions.appserver.ERXWOContext;
import er.extensions.components.ERXNonSynchronizingComponent;
import er.extensions.foundation.ERXStringUtilities;
import er.extensions.foundation.ERXValueUtilities;

/**
 * Base for all JQueryMobile Components.<br />
 * <br />
 * The following bindings are not present for all UI elements. See JQueryMobile documentation for a specific widget.
 * 
 * <pre>
 * data-corners	<strong>true</strong> | false
 * data-disabled	true | <strong>false</strong>
 * data-enhanced	true | <strong>false</strong>
 * </pre>
 */
abstract public class ERQMComponentBase extends ERXNonSynchronizingComponent
{
	protected static final Logger log = Logger.getLogger(ERQMComponentBase.class);
	private String _elementID = null;
	private static Boolean _useShortNamesInLog = Boolean.FALSE;

	public ERQMComponentBase(WOContext context)
	{
		super(context);
	}

	public static Boolean getUseShortNamesInLog()
	{
		return _useShortNamesInLog;
	}

	public static void setUseShortNamesInLog(Boolean useShortNamesInLog)
	{
		ERQMComponentBase._useShortNamesInLog = useShortNamesInLog;
	}

	public void logDeprecated(String message)
	{
		NSMutableArray<String> componentStack = new NSMutableArray<String>();
		componentStack.add(componentName());
		WOComponent next = parent();
		while (next != null)
		{
			if (_useShortNamesInLog)
			{
				componentStack.add("(" + next.getClass().getSimpleName() + ".java:0)");
			}
			else
			{
				componentStack.add("(" + next.getClass().getName() + ".java:0)");
			}
			next = next.parent();
		}
		System.err.println(componentStack.componentsJoinedByString(" -> ") + " : " + message);
	}

	public String javaScriptElementID()
	{
		if (ERXStringUtilities.stringIsNullOrEmpty(_elementID))
		{
			_elementID = (String) valueForBinding("id");
			if (_elementID == null)
			{
				_elementID = ERXWOContext.safeIdentifierName(context(), false);
			}
		}
		return _elementID;
	}

	public boolean hasNonNullBinding(String key)
	{
		if (hasBinding(key))
		{
			Object v = valueForBinding(key);
			if (v != null)
			{
				if (v instanceof String)
				{
					return (((String) v).length() > 0);
				}
				return true;
			}
		}
		return false;
	}

	public Object _objectValueForBinding(String key, Object defaultValue, String deprecatedKey)
	{
		Object tmp = valueForBinding(key);
		if (tmp == null && deprecatedKey != null)
		{
			tmp = valueForBinding(deprecatedKey);
			if (tmp != null)
			{
				logDeprecated(" '" + deprecatedKey + "' binding is deprecated. Use '" + key + "' instead.");
			}
		}
		return (tmp != null) ? tmp : defaultValue;
	}

	public String _stringValueForBinding(String key, String defaultValue, String deprecatedkey)
	{
		Object tmp = _objectValueForBinding(key, defaultValue, deprecatedkey);
		return (tmp != null) ? tmp.toString() : defaultValue;
	}

	public boolean _booleanValueForBinding(String key, boolean defaultValue, String deprecatedkey)
	{
		Object value = _objectValueForBinding(key, (defaultValue) ? "true" : "false", deprecatedkey);
		return ERXValueUtilities.booleanValueWithDefault(value, false);
	}

	public boolean appendStringTag(StringBuilder b, String key, String defaultValue, String deprecatedkey)
	{
		return appendStringTag(b, key, defaultValue, deprecatedkey, true);
	}

	public boolean appendStringTag(StringBuilder b, String key, String defaultValue, String deprecatedkey, boolean omitDefault)
	{
		String value = _stringValueForBinding(key, defaultValue, deprecatedkey);
		if (value != null && (!value.equals(defaultValue) || !omitDefault))
		{
			b.append(" ");
			b.append(key);
			b.append("=\"");
			b.append(value);
			b.append("\"");

			return true;
		}
		return false;
	}

	public boolean appendBooleanTag(StringBuilder b, String key, boolean defaultValue, String deprecatedkey)
	{
		return appendBooleanTag(b, key, defaultValue, deprecatedkey, true);
	}

	public boolean appendBooleanTag(StringBuilder b, String key, boolean defaultValue, String deprecatedkey, boolean omitDefault)
	{
		boolean value = _booleanValueForBinding(key, defaultValue, deprecatedkey);
		if (value != defaultValue || !omitDefault)
		{
			b.append(" ");
			b.append(key);
			b.append("=\"");
			b.append(value);
			b.append("\"");

			return true;
		}
		return false;
	}

	private boolean _appendClasses(StringBuilder b, NSArray<String> classes)
	{
		StringBuilder tmp = new StringBuilder();

		if (hasBinding("class"))
		{
			tmp.append(_stringValueForBinding("class", null, null));
		}

		if (classes != null && classes.count() > 0)
		{
			if (tmp.length() > 0)
			{
				tmp.append(' ');
			}
			tmp.append(classes.componentsJoinedByString(" "));
		}
		if (tmp.length() > 0)
		{
			b.append(" class=\"");
			b.append(tmp.toString());
			b.append("\"");
			return true;
		}
		return false;
	}

	private boolean _appendStyles(StringBuilder b, NSArray<String> styles)
	{
		StringBuilder tmp = new StringBuilder();

		if (hasBinding("style"))
		{
			tmp.append(_stringValueForBinding("style", null, null));
		}

		if (styles != null && styles.count() > 0)
		{
			if (tmp.length() > 0)
			{
				tmp.append(';');
			}
			tmp.append(styles.componentsJoinedByString(";"));
		}
		if (tmp.length() > 0)
		{
			b.append(" style=\"");
			b.append(tmp.toString().replaceAll(";;", ";"));
			b.append("\"");
			return true;
		}
		return false;
	}

	public String otherTagString()
	{
		NSMutableArray<String> additionalClasses = new NSMutableArray<String>();
		NSMutableArray<String> additionalStyles = new NSMutableArray<String>();
		StringBuilder tags = new StringBuilder();

		if (hasBinding("otherTagString"))
		{
			tags.append(_stringValueForBinding("otherTagString", null, null));
		}

		if (needJavascriptId())
		{
			tags.append(" id=\"");
			tags.append(javaScriptElementID());
			tags.append("\"");
		}

		appendCustomTags(tags, additionalClasses, additionalStyles);

		appendBooleanTag(tags, "data-corners", true, null);
		appendBooleanTag(tags, "data-disabled", false, null);

		_appendClasses(tags, additionalClasses);
		_appendStyles(tags, additionalStyles);

		return (tags.length() > 0) ? tags.toString() : null;
	}

	public boolean needJavascriptId()
	{
		return true;
	}

	public boolean inset()
	{
		return booleanValueForBinding("inset", false);
	}

	public abstract void appendCustomTags(StringBuilder sb, NSMutableArray<String> classes, NSMutableArray<String> styles);
}
