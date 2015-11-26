package er.jqm.components.core;

import com.webobjects.appserver.WOContext;
import com.webobjects.foundation.NSData;
import com.webobjects.foundation.NSMutableArray;

import er.extensions.eof.ERXConstant;
import er.extensions.foundation.ERXHyperlinkResource;
import er.extensions.foundation.ERXStaticResource;
import er.extensions.foundation.ERXStringUtilities;
import er.jqm.components.ERQMComponentBase;

/**
 * LI within a listview
 * 
 * <pre>
 * id
 * class
 * otherTagString
 * 
 * one of:
 *    action
 *    href
 *    linkResource @see {@link ERXHyperlinkResource}
 * queryDictionary
 * 
 * inset true | <strong>false</strong>
 * isDivider true | <strong>false</strong>
 * countBubble
 * sideText
 * 
 * imageResource @see {@link ERXStaticResource}
 * imageData
 * imageClass
 * imageStyle
 * imageMax sets width and height to this value
 * imageMaxWidth default: <strong>16</strong> px
 * imageMaxHeight default: <strong>16</strong> px
 * 
 * data-filtertext	string - Filter by this value instead of inner text
 * data-icon	home | delete | plus | arrow-u | arrow-d | carat-l | carat-t | carat-r | carat-b| check | gear | grid | star | custom | arrow-r | arrow-l | minus | refresh | forward | back | alert | info | search | false
 * data-role	list-divider
 * data-theme	swatch letter (a-z)
 * 
 * data-rel	back - To move one step back in history
 *          dialog - To open link styled as dialog, not tracked in history
 *          external - For linking to another domain
 *          popup - For opening a popup
 * 
 * </pre>
 * 
 */
public class ERQMListViewElement extends ERQMComponentBase
{
	public ERQMListViewElement(WOContext aContext)
	{
		super(aContext);
	}

	public boolean hasCountBubble()
	{
		return countBubble() > ERXConstant.MinusOneInteger;
	}

	public Integer countBubble()
	{
		int i = intValueForBinding("countBubble", ERXConstant.MinusOneInteger);
		return Integer.valueOf(i);
	}

	public boolean hasSideText()
	{
		return !ERXStringUtilities.stringIsNullOrEmpty(sideText());
	}

	public String sideText()
	{
		return stringValueForBinding("sideText", null);
	}

	public boolean isDivider()
	{
		return booleanValueForBinding("isDivider", false);
	}

	public boolean hasAnyAction()
	{
		return (hasBinding("action") || hasBinding("linkResource") || hasBinding("href"));
	}

	public String dataRel()
	{
		if (hasBinding("data-rel"))
		{
			return stringValueForBinding("data-rel");
		}
		if (booleanValueForBinding("isDialogCall", false))
		{
			logDeprecated(" 'isDialogCall' binding is deprecated. Use 'data-rel=\"dialog\"' instead. Or set the called page to data-dialog=\"true\".");
			return "dialog";
		}
		return null;
	}

	@Override
	public void appendCustomTags(StringBuilder sb, NSMutableArray<String> classes, NSMutableArray<String> styles)
	{
		if (hasBinding("isInputContainer"))
		{
			logDeprecated(" 'isInputContainer' was moved to ERQMListView -> data-inset");
		}
		if (hasBinding("automaticDivider"))
		{
			logDeprecated(" 'automaticDivider' was moved to ERQMListView -> data-autodividers");
		}

		appendStringTag(sb, "data-filtertext", null, "filtertext");
		appendStringTag(sb, "data-theme", null, "theme");
		appendStringTag(sb, "data-icon", null, "icon");

		if (isDivider())
		{
			sb.append(" data-role=\"list-divider\"");
		}
		else
		{
			appendStringTag(sb, "data-role", null, null);
		}

		if (_booleanValueForBinding("inset", false, "isInputContainer"))
		{
			classes.add("ui-field-contain");
		}

		if (hasImageResource())
		{
			classes.add("ui-li-has-thumb");
		}
	}

	public boolean hasImageResource()
	{
		return hasBinding("imageResource");
	}

	public ERXStaticResource imageResource()
	{
		return new ERXStaticResource(context(), stringValueForBinding("imageResource", null));
	}

	public boolean hasImageData()
	{
		return hasBinding("imageData");
	}

	public NSData imageData()
	{
		return (NSData) valueForBinding("imageData");
	}

	public String imageOtherTagString()
	{
		StringBuilder isb = new StringBuilder();

		if (hasBinding("imageClass"))
		{
			isb.append(" class=\"");
			isb.append(_stringValueForBinding("imageClass", null, null));
			isb.append("\"");
		}

		if (hasBinding("imageStyle"))
		{
			isb.append(" style=\"");
			isb.append(_stringValueForBinding("imageStyle", null, null));
			isb.append("\"");
		}

		String ih;
		String iw;
		if (hasBinding("imageMax"))
		{
			ih = stringValueForBinding("imageMax", "16");
			iw = ih;
		}
		else
		{
			ih = stringValueForBinding("imageMaxHeight", "16");
			iw = stringValueForBinding("imageMaxWidth", "16");
		}

		isb.append(" style=\"max-width:" + iw + "px; max-height:" + ih + "px;\"");

		return isb.toString();
	}
}
