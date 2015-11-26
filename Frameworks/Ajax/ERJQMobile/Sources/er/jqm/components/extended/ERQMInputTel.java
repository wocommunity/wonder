package er.jqm.components.extended;

import com.webobjects.appserver.WOContext;

import er.jqm.components.core.ERQMTextField;

/**
 * Input type="tel"
 * 
 * <pre>
 * data-clear-btn	true | <strong>false</strong> - Adds a clear button
 * data-clear-btn-text	string - Text for the close button. Default: "<strong>clear text</strong>"
 * data-mini	true | <strong>false</strong> - Compact sized version
 * data-role	none - Prevents auto-enhancement to use native control
 * data-theme	swatch letter (a-z) - Added to the form element
 * 
 * data-disabled	true | <strong>false</strong>
 * label
 * placeholder
 * inset	true | <strong>false</strong>
 * hideLabel	true | <strong>false</strong>
 * 
 * otherTagStringField tag string added to input field attribute list
 * otherTagStringLabel tag string added to label attribute list
 * 
 * </pre>
 */
public class ERQMInputTel extends ERQMTextField
{
	public ERQMInputTel(WOContext context)
	{
		super(context);
	}

	@Override
	public String typeName()
	{
		return "tel";
	}
}
