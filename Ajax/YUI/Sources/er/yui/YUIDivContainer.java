package er.yui;

import com.webobjects.appserver.WOComponent;
import com.webobjects.appserver.WOContext;
import com.webobjects.appserver.WOElement;
import com.webobjects.appserver.WOResponse;
import com.webobjects.appserver._private.WODynamicGroup;
import com.webobjects.foundation.NSDictionary;

public abstract class YUIDivContainer extends WODynamicGroup {
  private NSDictionary _associations;
  
  public YUIDivContainer(String aName, NSDictionary associations, WOElement template) {
    super(aName, associations, template);
    _associations = associations;
  }

  protected NSDictionary associations() {
    return _associations;
  }
  
  protected abstract String divID(WOContext context);

  protected abstract String divClass(WOContext context);

  protected abstract String divStyle(WOContext context);

  public void appendToResponse(WOResponse response, WOContext context) {
    WOComponent component = context.component();
    response.appendContentString("<div");
    YUIUtils.appendAttributeValue(response, context, "class", divClass(context));
    YUIUtils.appendAttributeValue(response, context, "id", divID(context));
    YUIUtils.appendAttributeValue(response, context, "style", divStyle(context));
    response.appendContentString(">");
    super.appendToResponse(response, context);
    response.appendContentString("</div>");
  }
}
