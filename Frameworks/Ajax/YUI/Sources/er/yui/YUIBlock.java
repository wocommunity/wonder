package er.yui;

import com.webobjects.appserver.WOContext;
import com.webobjects.appserver.WOElement;
import com.webobjects.appserver.WOResponse;
import com.webobjects.foundation.NSDictionary;

import er.ajax.AjaxUtils;
import er.yui.YUIDivContainer;
import er.yui.YUIUtils;

@SuppressWarnings("unused")
public class YUIBlock extends YUIDivContainer {
  public YUIBlock(String aName, NSDictionary<String, Object> associations, WOElement template) {
    super(aName, associations, template);
  }

  protected String divID(WOContext context) {
    return AjaxUtils.stringValueForBinding("id", associations(), context.component());
  }

  protected String divClass(WOContext context) {
    return "yui-b";
  }

  protected String divStyle(WOContext context) {
    return AjaxUtils.stringValueForBinding("style", associations(), context.component());
  }

}
