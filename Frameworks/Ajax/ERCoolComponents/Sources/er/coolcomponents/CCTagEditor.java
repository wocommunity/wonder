package er.coolcomponents;

import com.webobjects.appserver.WOActionResults;
import com.webobjects.appserver.WOAssociation;
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
 * WebObjects wrapper for Insignia tag editor component and Horsey auto-completion, MIT license.
 * To be used in conjunction with ERAjaxTagField, which contains the ERTaggable-specific code.
 *
 * @binding value, the string representing the list of tags
 * @binding deletion optional, default is <code>true</code>, whether each tag should carry an icon for easy deletion
 * @binding delimiter optional, default is space, the separator between tags
 * @binding render optional, a method that's called whenever a tag should be rendered
 * @binding readTag optional, a method that retrieves the tag from the DOM element
 * @binding parse optional, a method that transforms user input into a tag
 * @binding validate optional, a method that validates the parsed tag value
 * @binding convertOnFocus optional, default is true, whether the tags are being converted 
 *          when the focus event fires on elements other than the tag input
 * 
 * @see <a href="https://github.com/bevacqua/insignia">Insignia</a>
 * @see <a href="https://github.com/bevacqua/horsey">Horsey</a>
 *
 * @author fpeters (WebObjects wrapper only)
 */
public class CCTagEditor extends AjaxDynamicElement {
	
    public CCTagEditor(String name, NSDictionary<String, WOAssociation> associations, WOElement children){
        super(name, associations, children);
    }

    @Override
    protected void addRequiredWebResources(WOResponse response, WOContext context) {
        // Common resources
        addScriptResourceInHead(context, response, "Ajax", "prototype.js");
        // Component specific resources
        addScriptResourceInHead(context, response, "ERCoolComponents", "Insignia/insignia.min.js");
        addStylesheetResourceInHead(context, response, "ERCoolComponents", "Insignia/insignia.min.css");
        // Horsey for auto-completion
        addScriptResourceInHead(context, response, "ERCoolComponents", "Horsey/horsey.min.js");
        addStylesheetResourceInHead(context, response, "ERCoolComponents", "Horsey/horsey.min.css");
    }

    /**
     * Build span, input, and JavaScript into response.
     *
     * @see er.ajax.AjaxDynamicElement#appendToResponse(com.webobjects.appserver.WOResponse, com.webobjects.appserver.WOContext)
     */
    @Override
    public void appendToResponse(WOResponse response, WOContext context) {
        super.appendToResponse(response, context);
        
        // we need to render a span containing an input
        String id = id(context);
        String elementName = (String) valueForBinding("elementName", "span", context.component()); 
        response.appendContentString("<");
        response.appendContentString(elementName);
        response.appendContentString(" ");
        appendTagAttributeToResponse(response, "id", id);
        
        String className = "CCTags";
        if (hasBinding("class")) {
            className += " " + stringValueForBinding("class", context.component());            
        }
        appendTagAttributeToResponse(response, "class", className);

        if (hasBinding("style")) {
            appendTagAttributeToResponse(response, "style", stringValueForBinding("style", context.component()));            
        }
        response.appendContentString(">");

        // add our input field, which will only get accessed client-side, via Insignia
        response.appendContentString("<input ");
        appendTagAttributeToResponse(response, "id", id + "_input");
        appendTagAttributeToResponse(response, "style", "width: 2em;");
        appendTagAttributeToResponse(response, "value",
                valueForBinding("value", context.component()));
        response.appendContentString("/>");

        // close the span wrapping the input
        response.appendContentString("</");
        response.appendContentString(elementName);
        response.appendContentString(">");
            
        // wire up the script to be used with the input
        // TODO this should really be moved to a separate script file,
        // but right now I cannot be bothered – pull requests welcome!
        response.appendContentString("<script type=\"text/javascript\">");
        response.appendContentString("(function(Ajax) {");
        // get our input element
        response.appendContentString("var input = document.querySelector('#");
        response.appendContentString(id);
        response.appendContentString("_input');");
        // initialise an insignia instance and keep track of it
        response.appendContentString("var tags = insignia(");
        response.appendContentString("input, ");
        AjaxOptions.appendToResponse(createOptions(context), response, context);
        response.appendContentString(");");
        // make the fuzzysearch function available to our custom filter
        response.appendContentString("function fuzzysearch (n, h) { ");
        response.appendContentString("var hlen = h.length; ");
        response.appendContentString("var nlen = n.length; ");
        response.appendContentString("if (nlen > hlen) { return false; } ");
        response.appendContentString("if (nlen === hlen) { return n === h; } ");
        response.appendContentString("outer: for (var i = 0, j = 0; i < nlen; i++) { ");
        response.appendContentString("var nch = n.charCodeAt(i); ");
        response.appendContentString("while (j < hlen) { ");
        response.appendContentString("if (h.charCodeAt(j++) === nch) {  continue outer; } ");
        response.appendContentString("} return false; } ");
        response.appendContentString("return true; };");
        // we use a custom filter that'll exclude already selected tags from the list of suggestions
        response.appendContentString("function customFilter (q, suggestion) { ");
        response.appendContentString("if (tags.tags().indexOf(suggestion) != -1) { return false; } ");
        response.appendContentString("else { return fuzzysearch(q, suggestion); } };");
        // initialise horsey auto-completion
        response.appendContentString("horsey(input, { suggestions: ");
        // specify existing tags for auto-completion
        response.appendContentString(availableTags(context));
        response.appendContentString(", filter: customFilter");
        response.appendContentString("}); ");
        // add an event listener to the input
        response.appendContentString("input.addEventListener('insignia-evaluated', changed);");
        // cache the initial tag state
        response.appendContentString("var cachedState = tags.value();");
        // handle the change event
        response.appendContentString("function changed () {");
        // check whether the tags actually changed
        // (this is mostly to avoid creation of an additional Ajax call when
        // leaving the page, which would go w/o a response and hang around)
        response.appendContentString("if (cachedState != tags.value()) {");
        response.appendContentString("cachedState = tags.value();");
        response.appendContentString("var params = {};");
        response.appendContentString("params['");
        response.appendContentString(formValueName(context));
        response.appendContentString("'] = tags.value();");
        // send the current tags value to the server
        response.appendContentString("new Ajax.Request('");
        response.appendContentString(AjaxUtils.ajaxComponentActionUrl(context));
        response.appendContentString("', { parameters : params });");
        response.appendContentString("} }");
        response.appendContentString("})(Ajax)");
        response.appendContentString("</script>");
    }

    public String availableTags(WOContext context) {
        String availableTags = "";
        if (stringValueForBinding("availableTags", context.component()) != null) {
            availableTags = stringValueForBinding("availableTags", context.component());
        }
        return availableTags;
    }
    
    /**
     * Produce dictionary for options object
     *
     * @param context WOContext providing component to resolve bindings in
     * @return binding values converted into Ajax options
     */
    protected NSMutableDictionary<String, String> createOptions(WOContext context) {
        NSMutableArray<AjaxOption> ajaxOptionsArray = new NSMutableArray<AjaxOption>();

        // Insignia options
        ajaxOptionsArray.addObject(new AjaxOption("deletion", true, AjaxOption.BOOLEAN));
        ajaxOptionsArray.addObject(new AjaxOption("delimiter", AjaxOption.STRING));
        ajaxOptionsArray.addObject(new AjaxOption("render", AjaxOption.FUNCTION_2));
        ajaxOptionsArray.addObject(new AjaxOption("readTag", AjaxOption.FUNCTION_1));
        ajaxOptionsArray.addObject(new AjaxOption("parse", AjaxOption.FUNCTION_1));
        ajaxOptionsArray.addObject(new AjaxOption("validate", AjaxOption.FUNCTION_2));
        ajaxOptionsArray.addObject(new AjaxOption("convertOnFocus", AjaxOption.BOOLEAN));

        return AjaxOption.createAjaxOptionsDictionary(ajaxOptionsArray, context.component(), associations());
    }

    /**
     * Handles server action. Sets the value binding and calls the optional action method.
     *
     * @see er.ajax.AjaxDynamicElement#handleRequest(com.webobjects.appserver.WORequest, com.webobjects.appserver.WOContext)
     * @see #takeValuesFromRequest(WORequest, WOContext)
     *
     * @return null, this component returns nothing to the client
     */
    @Override
    public WOActionResults handleRequest(WORequest request, WOContext context) {
        setValueFromFormValue(request, context);

        // Nothing gets returned to the client from the action so we
        // discard any result from firing the action binding
        if (hasBinding("action")) {
            valueForBinding("action", context.component());
        }

        return null;
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
        Object tags = request.formValueForKey(formValueName(context));
        setValueForBinding(tags, "value", context.component());
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

}
