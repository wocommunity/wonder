package er.ajax;

import com.webobjects.appserver.WOActionResults;
import com.webobjects.appserver.WOAssociation;
import com.webobjects.appserver.WOComponent;
import com.webobjects.appserver.WOContext;
import com.webobjects.appserver.WOElement;
import com.webobjects.appserver.WORequest;
import com.webobjects.appserver.WOResponse;
import com.webobjects.appserver._private.WODynamicElementCreationException;
import com.webobjects.foundation.NSDictionary;

/**
 * AjaxIncludeScript provides an easy way to add an Ajax script dependency.  If you use
 * any of the Wonder Ajax components, they will automatically include their own 
 * dependencies.  However, if you want to do custom Ajax javascript, you may want to
 * include the same library versions that ship in Ajax.framework.
 * 
 * @binding name The name of the javascript file to include ("prototype.js", "effects.js", etc)
 * @binding framework The framework the javascript file resides in, defaults to "Ajax"
 */
public class AjaxIncludeScript extends AjaxDynamicElement {
  private WOAssociation _name;
  private WOAssociation _framework;
  
  public AjaxIncludeScript(String name, NSDictionary associations, WOElement children) {
    super(name, associations, children);
    _name = (WOAssociation)associations.objectForKey("name");
    if (_name == null) {
      throw new WODynamicElementCreationException("'name' is a required binding for AjaxIncludeScript.");
    }
    _framework = (WOAssociation)associations.objectForKey("framework");
  }

  @Override
  protected void addRequiredWebResources(WOResponse res, WOContext context) {
    WOComponent component = context.component();
    String name = (String)_name.valueInComponent(component);
    String framework = "Ajax";
    if(_framework!=null) {
      String value = (String)_framework.valueInComponent(component);
      if (value!=null)
        framework = value;
	}
    addScriptResourceInHead(context, res, framework, name);
  }

  @Override
  public WOActionResults handleRequest(WORequest request, WOContext context) {
      return null;
  }
}