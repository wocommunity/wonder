package er.ajax;

import java.util.Enumeration;

import com.webobjects.appserver.WOAssociation;
import com.webobjects.appserver.WOComponent;
import com.webobjects.appserver.WOContext;
import com.webobjects.appserver.WODynamicElement;
import com.webobjects.appserver.WOElement;
import com.webobjects.appserver.WOResponse;
import com.webobjects.foundation.NSDictionary;
import com.webobjects.foundation.NSMutableDictionary;

/**
 * AjaxOptions provides a mechanism to produce a JSON formatted dictionary from binding names and / or a dictionary. 
 * This is intended to support the options parameter in Prototype, e.g. the second parameter here: 
 * <pre>
 * new Ajax.Request('/some_url', {
 *     method: 'get',
 *     parameters: {company: 'example', limit: 12}
 *   });
 * </pre>
 * However, the functionality is not specific to Prototype and can be used anywhere a JSON formatted dictionary is needed.
 *
 * <p>
 * AjaxOptions can be used either in a HTML/WOD setting, e.g.
 * <pre>
 * AjaxOptions : AjaxOptions {
 *  method = "get";
 *	options = createAjaxOptions;
 * }
 * </pre>
 * with options bound to a dictionary produced by AjaxOption, or it can be used directly in Java, e.g.
 * <pre>
 *  AjaxOptions.appendToResponse(createAjaxOptions(), context.response(), context);
 * </pre>
 * 
 * <p>
 * Example (and fictitious) usage code:
 * <pre><code>
 * public NSDictionary createAjaxOptions(WOComponent component) {
 *     NSMutableArray&lt;AjaxOption&gt; ajaxOptionsArray = new NSMutableArray&lt;AjaxOption&gt;();
 *     ajaxOptionsArray.addObject(new AjaxOption("frequency", AjaxOption.NUMBER));
 *     ajaxOptionsArray.addObject(new AjaxOption("onLoading", AjaxOption.SCRIPT));
 *     ajaxOptionsArray.addObject(new AjaxOption("evalScripts", Boolean.TRUE, AjaxOption.BOOLEAN));
 *     ajaxOptionsArray.addObject(new AjaxOption("method", "get", AjaxOption.STRING));
 *     ...
 *     NSMutableDictionary&lt;String, String&gt; options = AjaxOption.createAjaxOptionsDictionary(ajaxOptionsArray, component, associations());
 *     return options;
 * }
 *
 * response.appendContentString("AUC.registerPeriodic('" + id + "'," + canStop + "," + stopped + ",");
 * AjaxOptions.appendToResponse(options, response, context);
 * response.appendContentString(");");
 * </code></pre>
 * 
 * @see AjaxOption
 * @see AjaxValue
 */
public class AjaxOptions extends WODynamicElement {
  private NSMutableDictionary<String, WOAssociation> _associations;
  private WOElement _children;

  public AjaxOptions(String name, NSDictionary<String, WOAssociation> bindings, WOElement children) {
    super(name, bindings, children);
    _associations = bindings.mutableClone();
    _children = children;
  }

  @Override
  public void appendToResponse(WOResponse response, WOContext context) {
    response.appendContentCharacter('{');
    NSMutableDictionary options = _associations;
    WOAssociation optionsBinding = _associations.objectForKey("options");
    if (optionsBinding != null) {
      NSDictionary passedInOptions = (NSDictionary) optionsBinding.valueInComponent(context.component());
      if (passedInOptions != null) {
        options = passedInOptions.mutableClone();
        options.addEntriesFromDictionary(_associations);
      }
    }
    AjaxOptions._appendToResponse(options, response, context);
    if (_children != null) {
      _children.appendToResponse(response, context);
    }
    response.appendContentCharacter('}');
  }

  /**
   * Adds JSON formatted key-value pairs from options to end of response content.  Does not adds the surrounding "{" and "}" signifying a dictionary / object. 
   *
   * @param options dictionary of key-value pairs, intended to have come from AjaxOption
   * @param response WOResponse to add JSON formatted key-value pairs to
   * @param context WOContext to provide WOComponent to resolve binding values in
   */
  public static void _appendToResponse(NSDictionary options, WOResponse response, WOContext context) {
    StringBuffer sb = new StringBuffer();
    AjaxOptions._appendToBuffer(options, sb, context);
    response.appendContentString(sb.toString());
  }
  
  /**
   * Adds JSON formatted key-value pairs from options to end of response content.  Does not adds the surrounding "{" and "}" signifying a dictionary / object. 
   *
   * @param options dictionary of key-value pairs, intended to have come from AjaxOption
   * @param stringBuffer StringBuffer to add JSON formatted key-value pairs to
   * @param context WOContext to provide WOComponent to resolve binding values in
   */
  public static void _appendToBuffer(NSDictionary options, StringBuffer stringBuffer, WOContext context) {
    if (options != null) {
      WOComponent component = context.component();
      boolean hasPreviousOptions = false;
      Enumeration bindingsEnum = options.keyEnumerator();
      while (bindingsEnum.hasMoreElements()) {
        String bindingName = (String) bindingsEnum.nextElement();
        if (!"options".equals(bindingName)) {
          Object bindingValue = options.objectForKey(bindingName);
  		  // This is needed for the double step to resolve the value for ^ notation
          if (bindingValue instanceof WOAssociation) {
            WOAssociation association = (WOAssociation) bindingValue;
            bindingValue = association.valueInComponent(component);
          }
          if (bindingValue != null) {
            if (hasPreviousOptions) {
              stringBuffer.append(", ");
            }
            stringBuffer.append(bindingName);
            stringBuffer.append(':');
            stringBuffer.append(bindingValue.toString());
            hasPreviousOptions = true;
          }
        }
      }
    }
  }

  /**
   * Adds JSON formatted key-value pairs from options to end of response content.  Adds the surrounding "{" and "}" signifying a dictionary / object. 
   *
   * @param options dictionary of key-value pairs, intended to have come from AjaxOption
   * @param stringBuffer StringBuffer to add JSON formatted key-value pairs to
   * @param context WOContext to provide WOComponent to resolve binding values in
   */
  public static void appendToBuffer(NSDictionary options, StringBuffer stringBuffer, WOContext context) {
    stringBuffer.append('{');
    AjaxOptions._appendToBuffer(options, stringBuffer, context);
    stringBuffer.append('}');
  }

  /**
   * Adds JSON formatted key-value pairs from options to end of response content.  Adds the surrounding "{" and "}" signifying a dictionary / object. 
   *
   * @param options dictionary of key-value pairs, intended to have come from AjaxOption
   * @param response WOResponse to add JSON formatted key-value pairs to
   * @param context WOContext to provide WOComponent to resolve binding values in
   */
  public static void appendToResponse(NSDictionary options, WOResponse response, WOContext context) {
    response.appendContentCharacter('{');
    AjaxOptions._appendToResponse(options, response, context);
    response.appendContentCharacter('}');
  }
}
