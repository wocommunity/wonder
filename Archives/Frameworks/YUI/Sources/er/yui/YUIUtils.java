package er.yui;

import com.webobjects.appserver.WOApplication;
import com.webobjects.appserver.WOContext;
import com.webobjects.appserver.WOResponse;
import com.webobjects.foundation.NSDictionary;

import er.ajax.AjaxUtils;
import er.extensions.components.ERXComponentUtilities;
import er.extensions.foundation.ERXProperties;
import er.extensions.foundation.ERXStringUtilities;

/**
 *
 * @property er.yui.base
 */
public class YUIUtils {
    
    /**
     * Holds the external URL for the YUI libs via the property <code>er.yui.base</code>.
     */
    private static String yuiBase;

    /**
     * Returns an external URL for the YUI libs to prepend via the property <code>er.yui.base</code> or the empty String. MUST end with "/" if set.
     * @return
     */
    private static String yuiBase() {
        if(yuiBase == null) {
            yuiBase = ERXProperties.stringForKeyWithDefault("er.yui.base","");
        }
        return yuiBase;
    }
    
    /**
     * 
     * @param fileName
     * @return
     */
    private static String actualFileName(String fileName) {
        if(fileName.startsWith("ext") || fileName.startsWith("wonder")) {
            return fileName;
        }
        return yuiBase() + fileName;
    }
    
    public static void addScriptResourceInHead(WOContext context, WOResponse response, String fileName) {
        // auto-discover and switch plain and min versions 
        if(WOApplication.application().isCachingEnabled()) {
            fileName = fileName.replaceFirst("\\.js$", "-min.js");
        } else {
            // debug version is not working in WOLips, probably due to the differences in build
            // fileName = fileName.replaceFirst("\\.js", "-debug.js");
        }
        AjaxUtils.addScriptResourceInHead(context, response, "YUI", actualFileName(fileName));
    }

    public static void addStylesheetResourceInHead(WOContext context, WOResponse response, String fileName) {
        AjaxUtils.addStylesheetResourceInHead(context, response, "YUI", actualFileName(fileName));
  }

  public static String id(String idBindingName, NSDictionary associations, WOContext context) {
    String id = ERXComponentUtilities.stringValueForBinding(idBindingName, associations, context.component());
    if (id == null) {
      id = ERXStringUtilities.safeIdentifierName(context.elementID());
    }
    return id;
  }

  public static String varName(String name, NSDictionary associations, WOContext context) {
    String namespace = ERXComponentUtilities.stringValueForBinding("namespace", associations, context.component());
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
    String value = ERXComponentUtilities.stringValueForBinding(name, associations, context.component());
    YUIUtils.appendAttributeValue(response, context, name, value);
  }

  public static void appendAttributeValue(WOResponse response, WOContext context, String name, String value) {
    if (value != null) {
      response._appendTagAttributeAndValue(name, value, true);
    }
  }
}
