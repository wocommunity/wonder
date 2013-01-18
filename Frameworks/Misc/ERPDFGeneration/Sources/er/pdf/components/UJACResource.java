package er.pdf.components;

import com.webobjects.appserver.WOApplication;
import com.webobjects.appserver.WOAssociation;
import com.webobjects.appserver.WOComponent;
import com.webobjects.appserver.WOContext;
import com.webobjects.appserver.WOElement;
import com.webobjects.appserver.WOResourceManager;
import com.webobjects.appserver.WOResponse;
import com.webobjects.appserver._private.WODynamicElementCreationException;
import com.webobjects.appserver._private.WOHTMLDynamicElement;
import com.webobjects.foundation.NSDictionary;

/**
 * UJAC resource component with resource manager support
 * @binding href href to the icon
 * @binding filename filename of the icon
 * @binding framework framework of the icon
 * @author q
 */
public abstract class UJACResource extends WOHTMLDynamicElement {

  protected WOAssociation _source;
  protected WOAssociation _framework;

  public UJACResource(String name, NSDictionary<String, WOAssociation> associations, WOElement template) {
    super(name, associations, template);
    _source = _associations.removeObjectForKey("source");
    _framework = _associations.removeObjectForKey("framework");
    if(_source == null) {
      throw new WODynamicElementCreationException("'source' must be bound: " + this);
    }
  }

  @Override
  public void appendAttributesToResponse(WOResponse response, WOContext context) {
    WOComponent component = context.component();
    String href = (String)_source.valueInComponent(component);

    String framework = "app";
    if(_framework != null) {
      Object val = _framework.valueInComponent(component);
      if(val != null) {
        framework = val.toString();
      }
    }
    
    WOResourceManager rs = WOApplication.application().resourceManager();
    if (!href.startsWith("http://") && !href.startsWith("https://"))
      href = rs.urlForResourceNamed(href, framework, null, context.request());
    response._appendTagAttributeAndValue("source", href, false);
    super.appendAttributesToResponse(response, context);
  }
  
}
