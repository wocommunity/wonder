package er.jqm.components.extended;

import com.webobjects.appserver.WOContext;

import er.extensions.foundation.ERXHyperlinkResource;
import er.jqm.components.core.ERQMButton;

/**
 * Button Links with data-role="button" and data-rel = "dialog".
 * 
 * <pre>
 * one of:
 *    action
 *    href
 *    linkResource @see {@link ERXHyperlinkResource}
 * queryDictionary
 * 
 * isButton	<strong>true</strong> | false
 * string
 * id
 * disabled
 * 
 * otherTagString tag string added to the container
 * class
 *   
 * data-corners	<strong>true</strong> | false
 * data-icon	home | delete | plus | arrow-u | arrow-d | carat-l | carat-t | carat-r | carat-b | check | gear | grid | star | custom | arrow-r | arrow-l | minus | refresh | forward | back | alert | info | search
 * data-iconpos	<strong>lef</strong>t | right | top | bottom | notext
 * data-iconshadow	true | <strong>false</strong>
 * data-inline	true | <strong>false</strong>
 * data-mini	true | <strong>false</strong> - Compact sized version
 * data-shadow	<strong>true</strong> | false
 * data-theme	swatch letter (a-z)
 * 
 * Links: including those with a data-role="button" share these attributes
 * 
 * data-ajax	<strong>true</strong> | false
 * data-direction	reverse - Reverse transition animation (only for page or dialog)
 * data-dom-cache	true | <strong>false</strong>
 * data-prefetch	true | <strong>false</strong>
 * data-transition	<strong>fade</strong> | flip | flow | pop | slide | slidedown | slidefade | slideup | turn | none
 * data-position-to	<strong>origin</strong> - Centers the popup over the link that opens it
 *                  jQuery selector - Centers the popup over the specified element
 *                  window - Centers the popup in the window
 *                  Note: option only available when used with popups. See also: options.
 * 
 * </pre>
 */
public class ERQMButtonDialog extends ERQMButton
{

	public ERQMButtonDialog(WOContext aContext)
	{
		super(aContext);
	}

	@Override
	public String dataRelDefault()
	{
		return "external";
	}
}
