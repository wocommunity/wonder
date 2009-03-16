package er.ajax.example2.components;

import com.webobjects.appserver.WOContext;
import com.webobjects.appserver.WOResponse;

import er.ajax.AjaxUtils;

public class AjaxHeaderFooter extends AjaxWOWODCComponent {
  public AjaxHeaderFooter(WOContext context) {
    super(context);
  }
  
  @Override
  public void appendToResponse(WOResponse response, WOContext context) {
    super.appendToResponse(response, context);
    AjaxUtils.addScriptResourceInHead(context, response, "Ajax", "prototype.js");
  }
  
  @Override
  protected boolean useDefaultComponentCSS() {
    return true;
  }

  @Override
  public boolean synchronizesVariablesWithBindings() {
    return false;
  }
}