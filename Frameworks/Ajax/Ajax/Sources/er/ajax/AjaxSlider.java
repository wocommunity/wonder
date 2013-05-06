package er.ajax;

import org.apache.log4j.Logger;

import com.webobjects.appserver.WOActionResults;
import com.webobjects.appserver.WOContext;
import com.webobjects.appserver.WORequest;
import com.webobjects.appserver.WOResponse;
import com.webobjects.foundation.NSMutableDictionary;
import com.webobjects.foundation.NSNumberFormatter;

import er.extensions.components.ERXComponentUtilities;

// PROTOTYPE FUNCTIONS (WRAPPER)
/**
 * Simple Ajax slider.
 * @author ak
 *
 * @binding minimum the minimum value of this slider
 * @binding maximum the maximum value of this slider
 * @binding snap if true, and min/max is set, this will set "values" to be the list of integer values
 */
public class AjaxSlider extends AjaxComponent {
	/**
	 * Do I need to update serialVersionUID?
	 * See section 5.6 <cite>Type Changes Affecting Serialization</cite> on page 51 of the 
	 * <a href="http://java.sun.com/j2se/1.4/pdf/serial-spec.pdf">Java Object Serialization Spec</a>
	 */
	private static final long serialVersionUID = 1L;

	private static final Logger log = Logger.getLogger(AjaxSlider.class);

    private String _trackerId;
    private String _handleId;

    public AjaxSlider(WOContext context) {
        super(context);
    }

    /**
     * Overridden because the component is stateless
     */
    @Override
    public boolean isStateless() {
        return true;
    }

    /**
     * Overridden to add the initialization javascript for the auto completer.
     */
    @Override
    public void appendToResponse(WOResponse res, WOContext ctx) {
        super.appendToResponse(res, ctx);
        _trackerId = safeElementID() + "_tracker";
        _handleId = safeElementID() + "_handle";
        
        NSMutableDictionary options = new NSMutableDictionary();
        new AjaxOption("axis", "orientation", null, AjaxOption.STRING).addToDictionary(this, options);
        new AjaxOption("sliderValue", "value", null, AjaxOption.NUMBER).addToDictionary(this, options);
        new AjaxOption("values", "possibleValues", null, AjaxOption.ARRAY).addToDictionary(this, options);
        new AjaxOption("alignX", AjaxOption.NUMBER).addToDictionary(this, options);
        new AjaxOption("alignY", AjaxOption.NUMBER).addToDictionary(this, options);
        new AjaxOption("disabled", AjaxOption.BOOLEAN).addToDictionary(this, options);
        new AjaxOption("handleImage", AjaxOption.STRING).addToDictionary(this, options);
        new AjaxOption("handleDisabled", AjaxOption.STRING).addToDictionary(this, options);
        new AjaxOption("increment", AjaxOption.NUMBER).addToDictionary(this, options);
        new AjaxOption("restricted", AjaxOption.BOOLEAN).addToDictionary(this, options);
        new AjaxOption("step", AjaxOption.NUMBER).addToDictionary(this, options);

        if(hasBinding("onChangeServer")) {
        	String parent = (String) valueForBinding("onChange");
        	options.setObjectForKey("function(v) {new Ajax.Request('"+ AjaxUtils.ajaxComponentActionUrl(context())
        			+"', {parameters: '"+context().elementID()+"=' + v + '&ajaxSlideTrigger=onChange'})"
        			+(parent != null ? "; var parentFunction = " + parent + "; parentFunction(v);" : "")
        			+"}", "onChange");
        } else {
        	new AjaxOption("onChange", AjaxOption.SCRIPT).addToDictionary(this, options);
        }
        if(hasBinding("onSlideServer")) {
        	String parent = (String) valueForBinding("onSlide");
        	options.setObjectForKey("function(v) {new Ajax.Request('"+ AjaxUtils.ajaxComponentActionUrl(context())
        			+"', {parameters: '"+context().elementID()+"=' + v + '&ajaxSlideTrigger=onSlide'})"
        			+(parent != null ? "; var parentFunction = " + parent + "; parentFunction(v);" : "")
        			+"}", "onSlide");
        } else {
        	new AjaxOption("onSlide", AjaxOption.SCRIPT).addToDictionary(this, options);
         }
        Number min = (Number)valueForBinding("minimum", Integer.valueOf(0));
        Number max = (Number)valueForBinding("maximum", Integer.valueOf(100));
        options.setObjectForKey("$R(" + min + "," + max + ")", "range");

        if (min != null && max != null && ERXComponentUtilities.booleanValueForBinding(this, "snap")) {
          StringBuilder valuesBuffer = new StringBuilder();
          valuesBuffer.append("[");
          for (int i = min.intValue(); i <= max.intValue(); i ++ ) {
            valuesBuffer.append(i);
            if (i < max.intValue()) {
              valuesBuffer.append(",");
            }
          }
          valuesBuffer.append("]");
        	options.setObjectForKey(valuesBuffer.toString(), "values");
        }

        res.appendContentString("<div class=\"tracker\" id=\""+
                _trackerId+"\"><div class=\"handle\" id=\""+
                _handleId+"\"></div></div>");
        AjaxUtils.appendScriptHeader(res);
        if (hasBinding("id")) {
          res.appendContentString((String)valueForBinding("id") + " = ");
        }
        res.appendContentString("new Control.Slider('"+_handleId+"', '"+_trackerId+"', ");
        AjaxOptions.appendToResponse(options, res, ctx);
        res.appendContentString(");");
        AjaxUtils.appendScriptFooter(res);
    }

    /**
     * Adds all required resources.
     */
    @Override
    protected void addRequiredWebResources(WOResponse res) {
        addScriptResourceInHead(res, "prototype.js");
    	addScriptResourceInHead(res, "effects.js");
    	addScriptResourceInHead(res, "controls.js");
    	addScriptResourceInHead(res, "slider.js");
    }

    @Override
    public void takeValuesFromRequest(WORequest worequest, WOContext wocontext) {
    	try {
	    		String format = (String) valueForBinding("numberformat", "0");
	    		Number num = worequest.numericFormValueForKey(wocontext.elementID(), new NSNumberFormatter(format));
	    		if(num != null) {
	    			setValueForBinding(num, "value");
	    		}
    	} catch(NumberFormatException ex) {
    		log.error(ex);
    	}
		super.takeValuesFromRequest(worequest, wocontext);
	}

    @Override
    public WOActionResults handleRequest(WORequest worequest, WOContext wocontext) {
    	WOResponse result = AjaxUtils.createResponse(worequest, wocontext);
    	String mode = worequest.stringFormValueForKey("ajaxSlideTrigger");
    	if(mode != null) {
    		result.setHeader("text/javascript", "content-type");
    		result.setContent((String)valueForBinding(mode+"Server", ""));
    	}
    	return result;
    }
}
