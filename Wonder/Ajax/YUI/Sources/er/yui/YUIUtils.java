package er.yui;

import com.webobjects.appserver.WOApplication;
import com.webobjects.appserver.WOContext;
import com.webobjects.appserver.WOResponse;
import com.webobjects.foundation.NSDictionary;

import er.ajax.AjaxUtils;

public class YUIUtils {
 
    public static void addScriptResourceInHead(WOContext context, WOResponse response, String fileName) {
        // auto-discover and switch plain and min versions 
        if(WOApplication.application().isCachingEnabled()) {
            fileName = fileName.replaceFirst("\\.js$", "-min.js");
        } else {
            // debug version is not working in WOLips, probably due to the differences in build
            // fileName = fileName.replaceFirst("\\.js", "-debug.js");
        }
        AjaxUtils.addScriptResourceInHead(context, response, "YUI", fileName);
    }

    public static void addStylesheetResourceInHead(WOContext context, WOResponse response, String fileName) {
        AjaxUtils.addStylesheetResourceInHead(context, response, "YUI", fileName);
  }

  public static String id(String idBindingName, NSDictionary associations, WOContext context) {
    String id = AjaxUtils.stringValueForBinding(idBindingName, associations, context.component());
    if (id == null) {
      id = AjaxUtils.toSafeElementID(context.elementID());
    }
    return id;
  }

  public static String varName(String name, NSDictionary associations, WOContext context) {
    String namespace = AjaxUtils.stringValueForBinding("namespace", associations, context.component());
    return YUIUtils.varName(name, namespace);
  }

  public static String varName(String name, String namespace) {
    String varName = name;
    if (namespace != null) {
      varName = namespace + "." + name;
    }
    return varName;
  }

  public static void appendAttributeValue(WOResponse response, WOContext context, NSDictionary associations, String name) {
    String value = AjaxUtils.stringValueForBinding(name, associations, context.component());
    YUIUtils.appendAttributeValue(response, context, name, value);
  }

  public static void appendAttributeValue(WOResponse response, WOContext context, String name, String value) {
    if (value != null) {
      response._appendTagAttributeAndValue(name, value, true);
    }
  }
}
