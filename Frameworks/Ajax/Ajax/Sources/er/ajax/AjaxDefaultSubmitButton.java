package er.ajax;

import com.webobjects.appserver.WOComponent;
import com.webobjects.appserver.WOContext;
import com.webobjects.appserver.WOElement;
import com.webobjects.appserver.WOResponse;
import com.webobjects.foundation.NSDictionary;
import com.webobjects.foundation.NSMutableDictionary;

import er.extensions.appserver.ERXBrowser;
import er.extensions.appserver.ERXBrowserFactory;
import er.extensions.components._private.ERXWOForm;

/**
 * Invisible form submit button that can be included as the first element in an Ajax submitted form so that hitting
 * enter performs the action bound to this button.
 *
 * @binding name the HTML name of this submit button (optional)
 * @binding value the HTML value of this submit button (optional)
 * @binding action the action to execute when this button is pressed
 * @binding id the HTML ID of this submit button
 * @binding class the HTML class of this submit button
 * @binding onClick arbitrary Javascript to execute when the client clicks the button
 * @binding onClickBefore if the given function returns true, the onClick is executed.  This is to support confirm(..) dialogs.
 * @binding onServerClick if the action defined in the action binding returns null, the value of this binding will be returned as javascript from the server
 * @binding onComplete JavaScript function to evaluate when the request has finished.
 * @binding onSuccess javascript to execute in response to the Ajax onSuccess event
 * @binding onFailure javascript to execute in response to the Ajax onFailure event
 * @binding onLoading javascript to execute when loading
 * @binding evalScripts evaluate scripts on the result
 * @binding updateContainerID the id of the AjaxUpdateContainer to update after performing this action
 * @binding formSerializer the name of the javascript function to call to serialize the form
 * @binding asynchronous boolean defining if the request is sent asynchronously or synchronously, defaults to true
 *
 * // PROTOTYPE EFFECTS
 * @binding effect synonym of afterEffect except it always applies to updateContainerID
 * @binding effectDuration synonym of afterEffectDuration except it always applies to updateContainerID
 * // PROTOTYPE EFFECTS
 * @binding beforeEffect the Scriptaculous effect to apply onSuccess ("highlight", "slideIn", "blindDown", etc);
 * @binding beforeEffectID the ID of the container to apply the "before" effect to (blank = try nearest container, then try updateContainerID)
 * @binding beforeEffectDuration the duration of the effect to apply before
 * // PROTOTYPE EFFECTS
 * @binding afterEffect the Scriptaculous effect to apply onSuccess ("highlight", "slideIn", "blindDown", etc);
 * @binding afterEffectID the ID of the container to apply the "after" effect to (blank = try nearest container, then try updateContainerID)
 * @binding afterEffectDuration the duration of the effect to apply after
 *
 * // PROTOTYPE EFFECTS
 * @binding insertion JavaScript function to evaluate when the update takes place (or effect shortcuts like "Effect.blind", or "Effect.BlindUp")
 * @binding insertionDuration the duration of the before and after insertion animation (if using insertion)
 * @binding beforeInsertionDuration the duration of the before insertion animation (if using insertion)
 * @binding afterInsertionDuration the duration of the after insertion animation (if using insertion)
 *
 * @property er.ajax.formSerializer the default form serializer to use for all ajax submits
 *
 * @author Chuck (with heavy theft from Anjo's AjaxSubmitButton)
 */
public class AjaxDefaultSubmitButton extends AjaxSubmitButton
{

    public AjaxDefaultSubmitButton(String name, NSDictionary associations, WOElement children) {
        super(name, associations, children);
    }

    @Override
    public void appendToResponse(WOResponse response, WOContext context) {
        WOComponent component = context.component();

        String formName = (String)valueForBinding("formName", component);
        String formReference  = "this.form";
        if (formName != null) {
          formReference = "document." + formName;
        }

        StringBuffer onClickBuffer = new StringBuffer();
        String onClickBefore = (String)valueForBinding("onClickBefore", component);
        if (onClickBefore != null) {
            onClickBuffer.append("if (");
            onClickBuffer.append(onClickBefore);
            onClickBuffer.append(") {");
        }

        // PROTOTYPE EFFECTS
        String updateContainerID = AjaxUpdateContainer.updateContainerID(this, component);
        String beforeEffect = (String) valueForBinding("beforeEffect", component);
        if (beforeEffect != null) {
            onClickBuffer.append("new ");
            onClickBuffer.append(AjaxUpdateLink.fullEffectName(beforeEffect));
            onClickBuffer.append("('");

            String beforeEffectID = (String) valueForBinding("beforeEffectID", component);
            if (beforeEffectID == null) {
                beforeEffectID = AjaxUpdateContainer.currentUpdateContainerID();
                if (beforeEffectID == null) {
                    beforeEffectID = updateContainerID;
                }
            }
            onClickBuffer.append(beforeEffectID);
            onClickBuffer.append("', { ");

            String beforeEffectDuration = (String) valueForBinding("beforeEffectDuration", component);
            if (beforeEffectDuration != null) {
                onClickBuffer.append("duration: ");
                onClickBuffer.append(beforeEffectDuration);
                onClickBuffer.append(", ");
            }

            onClickBuffer.append("queue:'end', afterFinish: function() {");
        }

        if (updateContainerID != null) {
            onClickBuffer.append("ASB.update('" + updateContainerID + "',");
        }
        else {
            onClickBuffer.append("ASB.request(");
        }
        onClickBuffer.append(formReference);
        onClickBuffer.append(",null,");

        NSMutableDictionary options = createAjaxOptions(component);

        AjaxUpdateLink.addEffect(options, (String) valueForBinding("effect", component), updateContainerID, (String) valueForBinding("effectDuration", component));
        String afterEffectID = (String) valueForBinding("afterEffectID", component);
        if (afterEffectID == null) {
            afterEffectID = AjaxUpdateContainer.currentUpdateContainerID();
            if (afterEffectID == null) {
                afterEffectID = updateContainerID;
            }
        }
        AjaxUpdateLink.addEffect(options, (String) valueForBinding("afterEffect", component), afterEffectID, (String) valueForBinding("afterEffectDuration", component));

        AjaxOptions.appendToBuffer(options, onClickBuffer, context);
        onClickBuffer.append(')');
        String onClick = (String) valueForBinding("onClick", component);
        if (onClick != null) {
          onClickBuffer.append(';');
          onClickBuffer.append(onClick);
        }

        if (beforeEffect != null) {
            onClickBuffer.append("}});");
        }

        if (onClickBefore != null) {
            onClickBuffer.append('}');
        }
        onClickBuffer.append("; return false;");
        
        response.appendContentString("<input ");
        appendTagAttributeToResponse(response, "tabindex", "");
        appendTagAttributeToResponse(response, "type", "submit");

        String name = nameInContext(context, component);
        appendTagAttributeToResponse(response, "name", name);
        appendTagAttributeToResponse(response, "value", valueForBinding("value", component));
        appendTagAttributeToResponse(response, "accesskey", valueForBinding("accesskey", component));
        
        // Suppress modal box focus ring if used inside of modal dialog
        if (AjaxModalDialog.isInDialog(context)) {
            StringBuilder sb = new StringBuilder("MB_notFocusable ");
            Object cssClass = valueForBinding("class", component);
            if (cssClass != null) {
            	sb.append(cssClass);
            } 
            appendTagAttributeToResponse(response, "class", sb.toString());
        }
        else {
        	appendTagAttributeToResponse(response, "class", valueForBinding("class", component));
        }
        
        appendTagAttributeToResponse(response, "style", "position:absolute;left:-10000px");
        appendTagAttributeToResponse(response, "id", valueForBinding("id", component));
        appendTagAttributeToResponse(response, "onclick", onClickBuffer.toString());

        response.appendContentString(" />");

        // fix for IE < 9 that deactivates the standard submit routine of the form and
        // triggers the onClick handler of this submit element instead if the return key
        // is pressed within a textfield, radiobutton, checkbox or select
        ERXBrowser browser = ERXBrowserFactory.factory().browserMatchingRequest(context.request());
        if (browser.isIE() && browser.majorVersion().compareTo(Integer.valueOf(9)) < 0) {
            if (!hasBinding("formName")) {
                formName = ERXWOForm.formName(context, "");
            }
            AjaxUtils.appendScriptHeader(response);
            response.appendContentString("\nEvent.observe(document." + formName + ", 'keypress', function(e){");
            response.appendContentString("if(e.keyCode==13){"); // return key
            response.appendContentString("var shouldFire=false;var t=e.target;var tn=t.tagName.toLowerCase();");
            response.appendContentString("if(tn==='select'){shouldFire=true;}");
            response.appendContentString("else if(tn==='input'){var ty=t.type.toLowerCase();");
            response.appendContentString("if(ty==='text' || ty==='radio' || ty==='checkbox'){shouldFire=true;}}");
            response.appendContentString("if(shouldFire){$$('[name=" + name + "]')[0].fireEvent('onClick');e.returnValue=false;}");
            response.appendContentString("}});");
            AjaxUtils.appendScriptFooter(response);
        }
    }
}
