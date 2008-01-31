package er.wolips.components;

import com.webobjects.appserver.WOApplication;
import com.webobjects.appserver.WOAssociation;
import com.webobjects.appserver.WOComponent;
import com.webobjects.appserver.WOContext;
import com.webobjects.appserver.WOElement;
import com.webobjects.appserver.WOResponse;
import com.webobjects.appserver._private.WODynamicGroup;
import com.webobjects.foundation.NSBundle;
import com.webobjects.foundation.NSDictionary;
import com.webobjects.foundation.NSMutableDictionary;

import er.extensions.ERXWOContext;
import er.wolips.WOLipsUtilities;

/**
 * WOLComponentLink generates a link that can open a particular
 * component name in WOLips.
 * 
 * @author mschrag
 */
public class WOLComponentLink extends WODynamicGroup {
  private WOAssociation _app;
  private WOAssociation _component;

  public WOLComponentLink(String name, NSDictionary associations, WOElement template) {
    super(name, associations, template);
    _app = (WOAssociation) associations.objectForKey("app");
    _component = (WOAssociation) associations.objectForKey("component");
  }

  @Override
  public void appendToResponse(WOResponse woresponse, WOContext wocontext) {
    WOLipsUtilities.includePrototype(woresponse, wocontext);
    ERXWOContext.addScriptResourceInHead(wocontext, woresponse, "WOLips", "wolips.js");

    WOComponent component = wocontext.component();
    String componentName = (String) _component.valueInComponent(component);
    String app;
    if (_app != null) {
      app = (String) _app.valueInComponent(component);
    }
    else {
        app = NSBundle.mainBundle().name();
    }
    NSMutableDictionary params = new NSMutableDictionary();
    params.setObjectForKey(app, "app");
    params.setObjectForKey(componentName, "component");
    woresponse.appendContentString("<a href = \"javascript:void(0);\" onclick = \"WOLips.perform('" + WOLipsUtilities.wolipsUrl("openComponent", params) + "')\">");
    super.appendToResponse(woresponse, wocontext);
    woresponse.appendContentString("</a>");
  }
}
