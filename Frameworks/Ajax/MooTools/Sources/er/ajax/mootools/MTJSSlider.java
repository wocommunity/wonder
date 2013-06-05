package er.ajax.mootools;

import com.webobjects.appserver.WOActionResults;
import com.webobjects.appserver.WOAssociation;
import com.webobjects.appserver.WOComponent;
import com.webobjects.appserver.WOContext;
import com.webobjects.appserver.WOElement;
import com.webobjects.appserver.WORequest;
import com.webobjects.appserver.WOResponse;
import com.webobjects.foundation.NSDictionary;
import com.webobjects.foundation.NSMutableArray;
import com.webobjects.foundation.NSMutableDictionary;

import er.ajax.AjaxDynamicElement;
import er.ajax.AjaxOption;
import er.ajax.AjaxOptions;
import er.ajax.AjaxUtils;
import er.extensions.appserver.ERXWOContext;

/**
 * Creates a JavaScript slider
 * see the MooTools documentation here: http://mootools.net/docs/more/Drag/Slider
 * 
 * The HTML for the slider will be written like this:
 *  &gt;div id="_1Element" class="element"&lt;
 *		&gt;div id="_2Knob" class="knob">&gt;/div&lt;
 *	&gt;/div&lt;
 * 
 * @binding id - the identifer for the slider's JS var.
 * @binding element the identifier to be used for the slider's container
 * @binding knob the identifier to be used for the knob's container.
 * @binding elementClass the CSS class to be used on the slider's container.
 * @binding element - (element) The container element for the slider.
 * @binding knob - (element) The handle element for the slider.
 * OPTIONS
 * @binding snap - (boolean: defaults to false) True if you want the knob to snap to the nearest value.
 * @binding offset - (number: defaults to 0) Relative offset for knob position at start.
 * @binding range - (mixed: defaults to false) Array of numbers or false. The minimum and maximum limits values the slider will use.
 * @binding wheel - (boolean: defaults to false) True if you want the ability to move the knob by mousewheeling.
 * @binding steps - (number: defaults to 100) The number of steps the Slider should move/tick.
 * @binding mode - (string: defaults to horizontal) The type of Slider can be either 'horizontal' or 'vertical' in movement.
 * @binding initialStep - (number: defaults to 0) The step the slider will start at. 
 * 
 * OBSERVER
 * @binding the id of the element who will update the slider's value with it's value.
 * @binding the event to observer on defaults to "change"
 * 
 * This is the layout of the HTML that the 
 * 
 * 
 * @author johnnymiller
 *
 */


public class MTJSSlider extends AjaxDynamicElement {
	
    public MTJSSlider(String name, NSDictionary<String, WOAssociation> associations, WOElement children) {
		super(name, associations, children);
	}

	@Override
	public void appendToResponse(WOResponse response, WOContext context) {

		WOComponent component = context.component();
		String sliderID = (String)valueForBinding("id", ERXWOContext.safeIdentifierName(context, true) + "Slider", component);
		String elementID = (String)valueForBinding("element", ERXWOContext.safeIdentifierName(context, true) + "Element", component);
		String knobID = (String)valueForBinding("knob", ERXWOContext.safeIdentifierName(context, true) + "Knob", component);
		response.appendContentString("\n<div");
		appendTagAttributeToResponse(response, "id", elementID);
		appendTagAttributeToResponse(response, "class", valueForBinding("elementClass", component));
		response.appendContentString(">");
		response.appendContentString("\n\t<div");
		appendTagAttributeToResponse(response, "id", knobID);
		appendTagAttributeToResponse(response, "class", valueForBinding("knobClass", component));
		response.appendContentString("></div>");
		response.appendContentString("\n</div>\n");

		AjaxUtils.appendScriptHeader(response);
		response.appendContentString("var ");
		response.appendContentString(sliderID);
		response.appendContentString(";");
		response.appendContentString("\nwindow.addEvent('domready', function() {");
		response.appendContentString("\n\t");
		response.appendContentString(sliderID);
		response.appendContentString(" = new Slider('");
		response.appendContentString(elementID);
		response.appendContentString("', '");
		response.appendContentString(knobID);
		response.appendContentString("', {");
		AjaxOptions._appendToResponse(createOptions(component), response, context);
		response.appendContentString("});");
		
		String observer = stringValueForBinding("observer", component);
		if(observer != null) {
			String observerEvent = (String)valueForBinding("observerEvent", "change", component);
			response.appendContentString("\n\t$('");
			response.appendContentString(observer);
			response.appendContentString("').addEvent('");
			response.appendContentString(observerEvent);
			response.appendContentString("', function(e) { ");
			response.appendContentString("this.set(e.target.value);");
			response.appendContentString("}.bind(");
			response.appendContentString(sliderID);
			response.appendContentString("));");
		}
		
		response.appendContentString("\n});\n");
		AjaxUtils.appendScriptFooter(response);
		super.appendToResponse(response, context);

	}	
	
	@SuppressWarnings({ "rawtypes", "unchecked" })
	protected NSMutableDictionary createOptions(WOComponent component) {
	    
		NSMutableArray ajaxOptionsArray = new NSMutableArray();
	    ajaxOptionsArray.addObject(new AjaxOption("snap", AjaxOption.BOOLEAN));
	    ajaxOptionsArray.addObject(new AjaxOption("offset", AjaxOption.NUMBER));
	    ajaxOptionsArray.addObject(new AjaxOption("range", AjaxOption.ARRAY));
	    ajaxOptionsArray.addObject(new AjaxOption("wheel", AjaxOption.BOOLEAN));
	    ajaxOptionsArray.addObject(new AjaxOption("steps", AjaxOption.NUMBER));
	    ajaxOptionsArray.addObject(new AjaxOption("mode", AjaxOption.STRING));
	    ajaxOptionsArray.addObject(new AjaxOption("onChange", AjaxOption.SCRIPT));
	    ajaxOptionsArray.addObject(new AjaxOption("onComplete", AjaxOption.SCRIPT));
	    ajaxOptionsArray.addObject(new AjaxOption("onTick", AjaxOption.SCRIPT));
		NSMutableDictionary options = AjaxOption.createAjaxOptionsDictionary(ajaxOptionsArray, component, associations());
		return options;

	}	

	@Override
	protected void addRequiredWebResources(WOResponse response,
			WOContext context) {
		MTAjaxUtils.addScriptResourceInHead(context, context.response(), "MooTools", MTAjaxUtils.MOOTOOLS_CORE_JS);
		MTAjaxUtils.addScriptResourceInHead(context, context.response(), "MooTools", MTAjaxUtils.MOOTOOLS_MORE_JS);
	}

	@Override
	public WOActionResults handleRequest(WORequest request, WOContext context) {
		// TODO Auto-generated method stub
		return null;
	}

    

}