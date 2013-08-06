package er.ajax;

/*
 * Aaron Rosenzweig Sept. 18, 2012 
 */

import com.webobjects.appserver.WOComponent;
import com.webobjects.appserver.WOContext;
import com.webobjects.appserver.WOResponse;
import com.webobjects.foundation.NSArray;

import er.extensions.foundation.ERXStringUtilities;

/**
 * @binding hoverableWidth num of pixels for hoverable width (200 as default) (String)
 * @binding additionalClassHoverArea
 * @binding additionalClassToolTip
 * @binding showHoverable
 * @binding additionalStyleHoverArea
 * @binding useJavascriptForHoverEffect
 * @binding useJavascriptOffsetX num of pixels without a 'px' on the end. Just the raw number.
 * @binding useJavascriptOffsetY num of pixels without a 'px' on the end. Just the raw number.
 * @binding toolTipWidth
 * @binding additionalStyleToolTip
 * @binding toolTipHeight
 * @binding toolTipAutoScroll
 * @binding toolTipDropBelow
 * @binding toolTipDropAbove
 * @binding toolTipDropTopRight
 * @binding toolTipDropTopLeft
 * @binding toolTipDropBottomRight
 * @binding toolTipDropBottomLeft
 * @binding advancedToolTipLeft
 * @binding advancedToolTipRight
 * @binding advancedToolTipTop
 * @binding advancedToolTipBottom
 */

public class AjaxHoverable extends WOComponent {
	protected static final String AJAX_FRAMEWORK_NAME = "Ajax";
	protected static final String LOCAL_CSS_FILE = "ajaxHoverable.css";
	protected static final String LOCAL_JS_FILE = "ajaxHoverable.js";
	
	protected String _idStr = null;

	/**
	 * Do I need to update serialVersionUID?
	 * See section 5.6 <cite>Type Changes Affecting Serialization</cite> on page 51 of the 
	 * <a href="http://java.sun.com/j2se/1.4/pdf/serial-spec.pdf">Java Object Serialization Spec</a>
	 */
	private static final long serialVersionUID = 1L;

	public AjaxHoverable(WOContext context) {
        super(context);
    }

	@Override
	public boolean synchronizesVariablesWithBindings() {
        return false;
	}
	
	public static void addWebResourcesInHead(WOResponse response, WOContext context) {
		AjaxUtils.addStylesheetResourceInHead(context, response, AJAX_FRAMEWORK_NAME, LOCAL_CSS_FILE);
		AjaxUtils.addScriptResourceInHead(context, response, AJAX_FRAMEWORK_NAME, LOCAL_JS_FILE);
	}

	@Override
	public void appendToResponse(WOResponse response, WOContext context) {
		addWebResourcesInHead(response, context);
		super.appendToResponse(response, context);
	}	
	
	public String hoverAreaClasses() {
		String classes = "erxHoverArea";
		String userDefined = (String) valueForBinding("additionalClassHoverArea");
		if (ERXStringUtilities.isNotBlank(userDefined)) {
			classes += " " + userDefined;
		}
		
		if (showHoverable()) {
			classes += " showHoverable";
		}
		return classes;
	}
	
	public String toolTipClasses() {
		String classes = "erxToolTip";
		if (useJavascriptForHoverEffect()) {
			classes = "erxToolTipJS";
		}
		String userDefined = (String) valueForBinding("additionalClassToolTip");
		if (ERXStringUtilities.isNotBlank(userDefined)) {
			classes += " " + userDefined;
		}
		return classes;
	}
    
    public boolean showHoverable() {
        boolean showHoverable = true;
        if (valueForBinding("showHoverable") != null) {
            Object showHoverableBinding = valueForBinding("showHoverable");
            
            if (showHoverableBinding instanceof Boolean) {
                showHoverable = ((Boolean)showHoverableBinding).booleanValue();
            } else if (showHoverableBinding instanceof NSArray) {
                NSArray tempArray = (NSArray)showHoverableBinding;
                if (tempArray.count() == 0) {
                    showHoverable = false;
                } else {
                    showHoverable = true;
                }
            } else if (showHoverableBinding instanceof Number) {
                Number tempNumber = (Number)showHoverableBinding;
                if (tempNumber.intValue() == 0) {
                    showHoverable = false;
                } else {
                    showHoverable = true;
                }
            }
            
        }
        return showHoverable;
    }
	
	public String hoverAreaInlineStyle() {
		String userDefined = (String) valueForBinding("additionalStyleHoverArea");
		
		return userDefined;
	}
	
	public boolean useJavascriptForHoverEffect() {
		Boolean useJavascriptForHoverEffect = (Boolean) valueForBinding("useJavascriptForHoverEffect");
		
		if (useJavascriptForHoverEffect == null) {
			useJavascriptForHoverEffect = Boolean.TRUE;
		}
		
		return useJavascriptForHoverEffect.booleanValue();
	}	
	
	public String toolTipWidth() {
		String toolTipWidth = (String) valueForBinding("toolTipWidth");
		return toolTipWidth;
	}
	
	public Number useJavascriptOffsetX() {
		Number returnVal = Integer.valueOf(0);
		Number offsetValue = (Number) valueForBinding("useJavascriptOffsetX");
		if (offsetValue != null) {
			returnVal = offsetValue;
		}
		
		return returnVal;
	}

	public Number useJavascriptOffsetY() {
		Number returnVal = Integer.valueOf(0);
		Number offsetValue = (Number) valueForBinding("useJavascriptOffsetY");
		if (offsetValue != null) {
			returnVal = offsetValue;
		}
		
		return returnVal;
	}
	
	public String toolTipInlineStyle() {
		String userDefined = (String) valueForBinding("additionalStyleToolTip");
		String inlineStyle = "width: " + toolTipWidth() + "; ";
		
		String toolTipHeight = (String) valueForBinding("toolTipHeight");
		if (ERXStringUtilities.isNotBlank(toolTipHeight)) {
			inlineStyle += "height: " + toolTipHeight + "; ";
		}
		
		boolean toolTipAutoScroll = valueForBinding("toolTipAutoScroll") != null && ((Boolean) valueForBinding("toolTipAutoScroll")).booleanValue();
		if (toolTipAutoScroll) {
			inlineStyle += "overflow: auto; ";
		}
		
		/*
		*	The following bindings are only "guesses". They make assumptions on how big the hoverArea container is.
		*	If they do not work correctly, you'll have to forget about them and use the advanced options.
		*/
		boolean toolTipDropBelow = valueForBinding("toolTipDropBelow") != null && ((Boolean) valueForBinding("toolTipDropBelow")).booleanValue();
		boolean toolTipDropAbove = valueForBinding("toolTipDropAbove") != null && ((Boolean) valueForBinding("toolTipDropAbove")).booleanValue();
		boolean toolTipDropTopRight = valueForBinding("toolTipDropTopRight") != null && ((Boolean) valueForBinding("toolTipDropTopRight")).booleanValue();
		boolean toolTipDropTopLeft = valueForBinding("toolTipDropTopLeft") != null && ((Boolean) valueForBinding("toolTipDropTopLeft")).booleanValue();
		boolean toolTipDropBottomRight = valueForBinding("toolTipDropBottomRight") != null && ((Boolean) valueForBinding("toolTipDropBottomRight")).booleanValue();
		boolean toolTipDropBottomLeft = valueForBinding("toolTipDropBottomLeft") != null && ((Boolean) valueForBinding("toolTipDropBottomLeft")).booleanValue();
		
		/*
		*	You should only use these bindings if the "drop" bindings don't suit your needs. These set the top/bot/left/right properties
		*	of the toolTip div that shows on hover. The "drop" bindings make guesses for these values on your behalf.
		*/
		String advancedToolTipLeft = (String) valueForBinding("advancedToolTipLeft");
		String advancedToolTipRight = (String) valueForBinding("advancedToolTipRight");
		String advancedToolTipTop = (String) valueForBinding("advancedToolTipTop");
		String advancedToolTipBottom = (String) valueForBinding("advancedToolTipBottom");
		
		if (ERXStringUtilities.isBlank(advancedToolTipLeft) && ERXStringUtilities.isBlank(advancedToolTipRight) &&
			ERXStringUtilities.isBlank(advancedToolTipTop) && ERXStringUtilities.isBlank(advancedToolTipBottom)) {
			if (toolTipDropAbove) {
				inlineStyle += "bottom: 1.5em; ";
			} else if (toolTipDropTopRight) {
				inlineStyle += "bottom: 1.5em; left: 30px; ";
			} else if (toolTipDropTopLeft) {
				inlineStyle += "bottom: 1.5em; right: 30px; ";
			} else if (toolTipDropBottomRight) {
				inlineStyle += "left: 30px; ";
			} else if (toolTipDropBottomLeft) {
				inlineStyle += "right: 30px; ";
			}
		} else {
			if (ERXStringUtilities.isNotBlank(advancedToolTipLeft)) {
				inlineStyle += "left: " + advancedToolTipLeft + "; ";
			}
			
			if (ERXStringUtilities.isNotBlank(advancedToolTipRight)) {
				inlineStyle += "right: " + advancedToolTipRight + "; ";
			}
			
			if (ERXStringUtilities.isNotBlank(advancedToolTipTop)) {
				inlineStyle += "top: " + advancedToolTipTop + "; ";
			}
			
			if (ERXStringUtilities.isNotBlank(advancedToolTipBottom)) {
				inlineStyle += "bottom: " + advancedToolTipBottom + "; ";
			}
		}
		
		if (userDefined == null) {
			userDefined = "";
		}
		return inlineStyle + userDefined;
	}
	
	public String idStr() {
		if (ERXStringUtilities.isBlank(_idStr) && useJavascriptForHoverEffect()) {
			_idStr = "hoverable_" + ERXStringUtilities.safeIdentifierName(context().elementID());
		}
		return _idStr;
	}
	
	public boolean isAjaxRequest() {
		return AjaxUtils.isAjaxRequest(context().request());
	}
}
