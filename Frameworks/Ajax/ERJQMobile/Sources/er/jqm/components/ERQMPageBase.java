package er.jqm.components;

import java.util.Enumeration;

import com.webobjects.appserver.WOContext;
import com.webobjects.appserver.WOElement;
import com.webobjects.appserver._private.WODynamicGroup;

import er.extensions.components.conditionals.ERXWOTemplate;
import er.extensions.foundation.ERXHyperlinkResource;
import er.extensions.foundation.ERXProperties;

/**
 * Builds url names for jQueryMobile css and js files
 *
 */
public abstract class ERQMPageBase extends ERQMComponentBase
{

	public ERQMPageBase(WOContext context)
	{
		super(context);
	}

	private String _uriForKey(String key)
	{
		StringBuilder propertyKey = new StringBuilder(key);
		propertyKey.append('.');
		propertyKey.append(ERXProperties.stringForKeyWithDefault(key, "odn"));
		propertyKey.append(".location");

		String uri = ERXProperties.stringForKey(propertyKey.toString());
		return uri;
	}

	public String themeStyleSheetUrl()
	{
		String uri = _uriForKey("er.jqm.css-theme");
		return ERXHyperlinkResource.urlForHyperlinkResource(context(), uri);
	}

	public String iconsStyleSheetUrl()
	{
		String uri = _uriForKey("er.jqm.css-icons");
		return ERXHyperlinkResource.urlForHyperlinkResource(context(), uri);
	}

	public String structurceStyleSheetUrl()
	{
		String uri = _uriForKey("er.jqm.css-structure");
		return ERXHyperlinkResource.urlForHyperlinkResource(context(), uri);
	}

	public String jQueryUrl()
	{
		String uri = _uriForKey("er.jqm.jquery");
		return ERXHyperlinkResource.urlForHyperlinkResource(context(), uri);
	}

	public String javascriptUrl()
	{
		String uri = _uriForKey("er.jqm.javascript");
		return ERXHyperlinkResource.urlForHyperlinkResource(context(), uri);
	}

	public String autocompleteUrl()
	{
		String uri = _uriForKey("er.jqm.autocomplete");
		return ERXHyperlinkResource.urlForHyperlinkResource(context(), uri);
	}

	public boolean hasTemplateInComponent()
	{
		boolean result = false;

		WOElement content = _childTemplate();
		if (content instanceof WODynamicGroup)
		{
			WODynamicGroup group = (WODynamicGroup) content;
			for (Enumeration<WOElement> e = group.childrenElements().objectEnumerator(); e.hasMoreElements() && !result;)
			{
				WOElement current = e.nextElement();
				if (current instanceof ERXWOTemplate)
				{
					result = true;
				}
			}
		}
		else if (content instanceof ERXWOTemplate)
		{
			result = true;
		}
		return result;
	}

}
