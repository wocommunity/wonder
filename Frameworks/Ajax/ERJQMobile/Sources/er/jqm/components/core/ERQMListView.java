package er.jqm.components.core;

import java.util.concurrent.atomic.AtomicLong;

import com.webobjects.appserver.WOContext;
import com.webobjects.foundation.NSLog;
import com.webobjects.foundation.NSMutableArray;

import er.extensions.components._private.ERXWOForm;
import er.extensions.foundation.ERXStringUtilities;
import er.jqm.components.ERQMComponentBase;

/**
 * List view (data-role="listview")
 * 
 * <pre>
 * isNumberedList true -> OL
 *                false -> UL
 * 
 * id
 * filterFieldId
 * class
 * otherTagSring
 * 
 * data-autodividers	true | <strong>false</strong>
 * data-count-theme	swatch letter (a-z) - Default "c"
 * data-divider-theme	swatch letter (a-z) - Default "b"
 * data-disabled	true | <strong>false</strong>
 * data-header-theme	swatch letter (a-z)
 * data-icon	home | delete | plus | arrow-u | arrow-d | carat-l | carat-t | carat-r | carat-b| check | gear | grid | star | custom | arrow-r | arrow-l | minus | refresh | forward | back | alert | info | search | false
 * data-inset	true | <strong>false</strong>
 * data-split-icon	home | delete | plus | arrow-u | arrow-d | carat-l | carat-t | carat-r | carat-b| check | gear | grid | star | custom | <strong>arrow-r</strong> | arrow-l | minus | refresh | forward | back | alert | info | search
 * data-split-theme	swatch letter (a-z) - Default "b"
 * data-theme	swatch letter (a-z)
 * 
 * 
 * data-filter	true | <strong>false</strong>
 * data-filter-placeholder	string
 * data-filter-label	string
 * data-filter-hidelabel	true | <strong>false</strong>
 * data-filter-mini	true | <strong>false</strong>
 * data-filter-theme	swatch letter (a-z)
 * </pre>
 * 
 */
public class ERQMListView extends ERQMComponentBase
{
	public String dummyFilterValue;
	private static AtomicLong _filterFieldCounter = new AtomicLong(0);

	public ERQMListView(WOContext aContext)
	{
		super(aContext);
	}

	private boolean isNumberedList()
	{
		return booleanValueForBinding("isNumberedList", false);
	}

	public String elementName()
	{
		return isNumberedList() ? "ol" : "ul";
	}

	@Override
	public void appendCustomTags(StringBuilder sb, NSMutableArray<String> classes, NSMutableArray<String> styles)
	{
		appendBooleanTag(sb, "data-autodividers", false, null);
		appendStringTag(sb, "data-count-theme", null, "countBubble-theme");
		appendStringTag(sb, "data-divider-theme", null, "divider-theme");
		appendStringTag(sb, "data-header-theme", null, null);
		appendStringTag(sb, "data-icon", null, null);
		appendBooleanTag(sb, "data-inset", false, "inset");
		appendStringTag(sb, "data-split-icon", null, "split-icon");
		appendStringTag(sb, "data-split-theme", null, "split-theme");

		if (appendBooleanTag(sb, "data-filter", false, "filter"))
		{
			sb.append(" data-input=\"#");
			sb.append(filterFieldId());
			sb.append("\"");
		}
	}

	public boolean wantsDataFilter()
	{
		return _booleanValueForBinding("data-filter", false, "filter");
	}

	private String _filterFieldId = null;

	public String filterFieldId()
	{
		_checkFormClass();
		if (ERXStringUtilities.stringIsNullOrEmpty(_filterFieldId))
		{
			_filterFieldId = (String) valueForBinding("filterFieldId");
			if (_filterFieldId == null)
			{
				_filterFieldId = "ff_" + _filterFieldCounter.incrementAndGet();
			}
		}
		return _filterFieldId;
	}

	private void _checkFormClass()
	{
		if (context().isInForm())
		{
			if (!context().response().contentString().contains("ui-filterable"))
			{
				String formName = ERXWOForm.formName(context(), "- not specified -");
				NSLog.err.appendln(this.getClass().getName() + " --> Make sure you set class=\"ui-filterable\" in form: " + formName + " in page "
						+ context().page().getClass().getName());
			}
		}
	}
}
