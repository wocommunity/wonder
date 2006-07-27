package er.ajax;

import com.webobjects.appserver.WOActionResults;
import com.webobjects.appserver.WOComponent;
import com.webobjects.appserver.WOContext;
import com.webobjects.appserver.WOElement;
import com.webobjects.appserver.WORequest;
import com.webobjects.appserver.WOResponse;
import com.webobjects.foundation.NSDictionary;
import com.webobjects.foundation.NSMutableArray;
import com.webobjects.foundation.NSMutableDictionary;

public class AjaxSubmitButton extends AjaxDynamicElement {

  public AjaxSubmitButton(String name, NSDictionary associations, WOElement children) {
    super(name, associations, children);
  }
  
  public boolean disabledInComponent(WOComponent component) {
	  return false;
  }
  

  public String nameInContext( WOContext context, WOComponent component) {
	  return context.elementID();
  }
  
  public NSDictionary createAjaxOptions(WOComponent component) {
	  String name = (String) valueForBinding("name", component );
	  if(name == null) {
		  name = component.context().elementID();
	  }
	  NSMutableArray ajaxOptionsArray = new NSMutableArray();
	  ajaxOptionsArray.addObject(new AjaxOption("onSuccess", AjaxOption.SCRIPT));
	  ajaxOptionsArray.addObject(new AjaxOption("onFailure", AjaxOption.SCRIPT));
	  NSMutableDictionary options = AjaxOption.createAjaxOptionsDictionary(ajaxOptionsArray, component, associations());
	  options.setObjectForKey("Form.serialize(this.form) + \"&" + name + "=" + valueForBinding("value", component) + "\"", "parameters");
	  return options;
  }
  
  public void appendToResponse(WOResponse response, WOContext context) {
    //    function addComment(e) { 
    //      // submit the form using Ajax 
    //      new Ajax.Request("comment.php", { 
    //        parameters : Form.serialize(this), 
    //        onSuccess : updateComment 
    //      }); 
    //      Event.stop(e); 
    //     } 
	  WOComponent component = context.component();

	  response.appendContentString("<input ");
	  String name = (String) valueForBinding("name", component );
	  if(name == null) {
		  name = context.elementID();
	  }
	  appendTagAttributeToResponse(response, "type", "button");
	  appendTagAttributeToResponse(response, "name", name);
	  appendTagAttributeToResponse(response, "value", valueForBinding("value", component ));
	  appendTagAttributeToResponse(response, "class", valueForBinding("class", component ));
	  appendTagAttributeToResponse(response, "style", valueForBinding("style", component ));
	  appendTagAttributeToResponse(response, "id", valueForBinding("id", component ));
	  StringBuffer sb = new StringBuffer();
	  sb.append("new Ajax.Request(this.form.action,");
	  //sb.append("new Ajax.Request(\"" + context.componentActionURL() + "\",");
	  NSDictionary options = createAjaxOptions(component);
	  AjaxOptions.appendToBuffer(options, sb, context);
	  sb.append(");");
	  appendTagAttributeToResponse(response, "onClick", sb.toString());
	  response.appendContentString(" />");
	  super.appendToResponse(response, context);
  }

  protected void addRequiredWebResources(WOResponse res, WOContext context) {
	    addScriptResourceInHead(context, res, "prototype.js");
	    addScriptResourceInHead(context, res, "scriptaculous.js");
	    addScriptResourceInHead(context, res, "effects.js");
	    addScriptResourceInHead(context, res, "builder.js");
	    addScriptResourceInHead(context, res, "controls.js");
  }

  protected WOActionResults handleRequest(WORequest worequest, WOContext wocontext) {
	  WOComponent wocomponent = wocontext.component();
	  Object obj;
	  if(!disabledInComponent(wocomponent)) {
		  boolean pull = wocontext._isMultipleSubmitForm();
		  pull = (pull && worequest.formValueForKey(nameInContext(wocontext, wocomponent)) != null) || !pull ;
		  if(pull) {
			  wocontext._setActionInvoked(true);
			  obj = valueForBinding("action", wocomponent);
			  if(obj == null)
				  obj = wocontext.page();
		  }
	  }
	  return null;
  }

}
