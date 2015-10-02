package er.coolcomponents;

import com.webobjects.appserver.WOActionResults;
import com.webobjects.appserver.WOContext;
import com.webobjects.appserver.WOElement;
import com.webobjects.appserver.WORequest;
import com.webobjects.appserver.WOResponse;
import com.webobjects.foundation.NSDictionary;
import com.webobjects.foundation.NSMutableArray;
import com.webobjects.foundation.NSMutableDictionary;

import er.ajax.AjaxConstantOption;
import er.ajax.AjaxDynamicElement;
import er.ajax.AjaxOption;
import er.ajax.AjaxOptions;
import er.ajax.AjaxUtils;
import er.extensions.appserver.ERXWOContext;


/**
 * WebObjects wrapper for LivePipe Rating component, MIT license.
 * <p>
 * CCRating is a fully customizable CSS based ratings widget. By default it acts as an input.  Alternatively, it can notify the
 * server when the rating is changed, update the bound value, and call an action method.
 * <p>
 * It uses four (customizable) CSS class names to determine each link's state:
 * <ul>
 * <li>rating_off</li>
 * <li>rating_half</li>
 * <li>rating_on</li>
 * <li>rating_selected</li>
 * </ul>
 *
 * <h3>Example Usages</h3>
 *
 * <pre>
 * Rating: CCRating {
 *     value = rating;
 * }
 * </pre>
 * <pre>
 * Rating: CCRating {
 *     value = rating;
 *     actAsInput = false;
 * }
 * </pre>
 * <pre>
 * Rating: CCRating {
 *     value = rating;
 *     actAsInput = false;
 *     action = rated;
 * }
 * </pre>
 * <pre>
 * Rating: CCRating {
 *     value = rating;
 *     min = 1;
 *     max = 10;
 *     multiple = true;
 * }
 * </pre>
 *
 * @binding value the value to show in the ratings widget and the value set when the user selects a different rating
 * @binding actAsInput optional, default is <code>true</code>, if false updates the value binding when clicked and optionally calls action  method
 * @binding action optional, action method to fire when rating changed.  Ignored if actAsInput is <code>true</code> or unbound
 * @binding min optional, the value sent to the server when the lowest rating is selected, indirectly controls the number of rating points displayed
 * @binding max optional, the value sent to the server when the highest rating is selected, indirectly controls the number of rating points displayed
 * @binding multiple optional, <code>true</code> if the user can change a previous rating
 * @binding id optional, HTML ID for the div and Control.Rating widget
 * @binding capture optional, stops the click event on each rating from propagating
 * @binding style optional CSS style for container element
 * @binding class optional CSS class for container element in addition to the standard rating_container class
 * @binding classNames optional, dictionary of state names and CSS class names with state names of: off, half, on, selected
 * @binding rated optional, <code>true</code> if this has already been rated
 * @binding reverse optional, <code>true</code> if the links should be shown in reverse order
 * @binding updateOptions highly optional, Ajax Options for the request
 * @binding formValueName optional, the name of the form value that will contain the value
 * @binding elementName optional, defaults to div, the name of the HTML element to use to hold the rating UI
 * @binding afterChange, optional, script to run client side after a change e.g. afterChange = "alert(v)";.  Receives one
 *              parameter, v, the new value selected
 * 
 * @see <a href="http://livepipe.net/control/rating">Control.Rating</a>
 *
 * @author chill (WebObjects wrapper only, not LivePipe Rating)
 */
public class CCRating extends AjaxDynamicElement {
	
    public CCRating(String name, NSDictionary associations, WOElement children){
        super(name, associations, children);
    }

    @Override
    protected void addRequiredWebResources(WOResponse response, WOContext context) {
        // Common resources
        addScriptResourceInHead(context, response, "Ajax", "prototype.js");
        addScriptResourceInHead(context, response, "Ajax", "effects.js");
        addScriptResourceInHead(context, response, "Ajax", "controls.js");

        // Library specific resources
        addScriptResourceInHead(context, response, "ERCoolComponents", "Rating/livepipe.js");

        // Component specific resources
        addScriptResourceInHead(context, response, "ERCoolComponents", "Rating/rating.js");
        addStylesheetResourceInHead(context, response, "ERCoolComponents", "Rating/rating.css");
    }


    /**
     * Build div, optional input, and JavaScript into response.
     *
     * @see er.ajax.AjaxDynamicElement#appendToResponse(com.webobjects.appserver.WOResponse, com.webobjects.appserver.WOContext)
     */
    @Override
    public void appendToResponse(WOResponse response, WOContext context) {
        // We don't contain anything, but we need to call super so it calls addRequiredWebResources(WOResponse, WOContext)
        super.appendToResponse(response, context);

        // Build container element like <div id="e_1_0_0_1_3_7" class="rating_container"></div>
        
        String id = id(context);
        String elementName = (String) valueForBinding("elementName", "div", context.component()); 
        response.appendContentString("<");
        response.appendContentString(elementName);
        response.appendContentString(" ");
        appendTagAttributeToResponse(response, "id", id);
        
        String className = "rating_container";
        if (hasBinding("class")) {
            className += " " + stringValueForBinding("class", context.component());            
        }
        appendTagAttributeToResponse(response, "class", className);

        if (hasBinding("style")) {
            appendTagAttributeToResponse(response, "style", stringValueForBinding("style", context.component()));            
        }
        response.appendContentString("></");
        response.appendContentString(elementName);
        response.appendContentString(">");

        // Build optional input like <input id="e_1_0_0_1_3_7_input" name="e_1_0_0_1_3_7_value" value="5" type="hidden"/>
        if (actAsInput(context)) {
            response.appendContentString("<input ");
            appendTagAttributeToResponse(response, "id", id + "_input");
            appendTagAttributeToResponse(response, "name", formValueName(context));
            appendTagAttributeToResponse(response, "value", valueForBinding("value", context.component()));
            appendTagAttributeToResponse(response, "type", "hidden");
            response.appendContentString("/>");
        }

        // Build script like
        // <script type="text/javascript">
        //     var e_1_0_0_1_3_7 = new Control.Rating('e_1_0_0_1_3_7',
        //         {multiple:true, value:5, min:2, max:8, rated:false, input:'e_1_0_0_1_3_7_input', updateParameterName:'e_1_0_0_1_3_7_value'});
        // </script>
        response.appendContentString("<script type=\"text/javascript\">");
        response.appendContentString("var ");
        response.appendContentString(id);
        response.appendContentString(" = new Control.Rating('");
        response.appendContentString(id);
        response.appendContentString("', ");
        AjaxOptions.appendToResponse(createOptions(context), response, context);
        response.appendContentString("); </script>");
    }

    /**
     * Produce dictionary for options object for Control.Rating.
     *
     * @param context WOContext providing component to resolve bindings in
     * @return binding values converted into Ajax options
     */
    protected NSMutableDictionary createOptions(WOContext context) {
        NSMutableArray ajaxOptionsArray = new NSMutableArray();

        // Standard options from Control.Rating
        ajaxOptionsArray.addObject(new AjaxOption("min", AjaxOption.NUMBER));
        ajaxOptionsArray.addObject(new AjaxOption("max", AjaxOption.NUMBER));
        ajaxOptionsArray.addObject(new AjaxOption("value", AjaxOption.NUMBER));
        ajaxOptionsArray.addObject(new AjaxOption("capture", AjaxOption.BOOLEAN));
        ajaxOptionsArray.addObject(new AjaxOption("classNames", AjaxOption.DICTIONARY));
        ajaxOptionsArray.addObject(new AjaxOption("multiple", AjaxOption.BOOLEAN));
        ajaxOptionsArray.addObject(new AjaxOption("rated", AjaxOption.BOOLEAN));
        ajaxOptionsArray.addObject(new AjaxOption("reverse", AjaxOption.BOOLEAN));
        ajaxOptionsArray.addObject(new AjaxOption("afterChange", AjaxOption.FUNCTION_1));
        
        // updateParameterName is renamed to formValueName to be more WO like
        ajaxOptionsArray.addObject(new AjaxConstantOption("updateParameterName", "formValueName", formValueName(context), AjaxOption.STRING));

        // These parameters are mutually exclusive at present, but dataUpdateUrl could be used with an input if there is a reason for it.
        // I can't think of one right now
        if ( ! actAsInput(context)) {
            ajaxOptionsArray.addObject(new AjaxConstantOption("dataUpdateUrl", AjaxUtils.ajaxComponentActionUrl(context), AjaxOption.STRING));
            ajaxOptionsArray.addObject(new AjaxOption("updateOptions", AjaxOption.DICTIONARY));
        }
        else {
            ajaxOptionsArray.addObject(new AjaxConstantOption("input", id(context) + "_input", AjaxOption.STRING));
        }

        return AjaxOption.createAjaxOptionsDictionary(ajaxOptionsArray, context.component(), associations());
    }

    /**
     * Handles server action if this is not being use as an input.  Sets the value binding and calls the optional action method.
     *
     * @see er.ajax.AjaxDynamicElement#handleRequest(com.webobjects.appserver.WORequest, com.webobjects.appserver.WOContext)
     * @see #takeValuesFromRequest(WORequest, WOContext)
     *
     * @return null, this component returns nothing to the client
     */
    @Override
    public WOActionResults handleRequest(WORequest request, WOContext context) {
        setValueFromFormValue(request, context);

       // Nothing gets returned to the client from  the CCRating action so we discard any result from firing the action binding
       if (hasBinding("action")) {
           valueForBinding("action", context.component());
       }

        return null;
    }

    /**
     * Sets value binding if this is being used as an input.
     *
     * @see er.ajax.AjaxDynamicElement#takeValuesFromRequest(com.webobjects.appserver.WORequest, com.webobjects.appserver.WOContext)
     * @see #handleRequest(WORequest, WOContext)
     */
    @Override
    public void takeValuesFromRequest(WORequest request, WOContext context) {
        if (actAsInput(context)) {
            setValueFromFormValue(request, context);
        }
        super.takeValuesFromRequest(request, context);
    }

    /**
     * Sets the value binding based on the form value.
     *
     * @see #takeValuesFromRequest(WORequest, WOContext)
     * @see #handleRequest(WORequest, WOContext)
     *
     * @param request the WORequest to get the form values from
     * @param context WOContext used to determine component used in
     */
    protected void setValueFromFormValue(WORequest request, WOContext context) {
        Object ratingValue = request.formValueForKey(formValueName(context));
        if (ratingValue instanceof String) {
            ratingValue = Integer.valueOf((String)ratingValue);
        }
        setValueForBinding(ratingValue, "value", context.component());
    }

    /**
     * @param context WOContext used to determine component used in
     * @return optional value for formValueName, or calculated value if unbound
     */
    protected String formValueName(WOContext context) {
        return (String)valueForBinding("formValueName", id(context) + "_value", context.component());
    }

    /**
     * @param context WOContext used to determine component used in
     * @return optional value for id, or calculated value if unbound
     */
    @Override
    public String id(WOContext context) {
        return (String) valueForBinding("id", ERXWOContext.safeIdentifierName(context, false), context.component());
    }

    /**
     * @param context WOContext used to determine component used in
     * @return optional value for actAsInput, or <code>true</code> if unbound
     */
    protected boolean actAsInput(WOContext context) {
        return booleanValueForBinding("actAsInput", true, context.component());
    }
}
