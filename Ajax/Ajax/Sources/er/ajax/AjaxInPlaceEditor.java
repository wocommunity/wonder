package er.ajax;

import java.math.BigDecimal;
import java.text.Format;
import java.text.ParseException;

import com.webobjects.appserver.WOActionResults;
import com.webobjects.appserver.WOAssociation;
import com.webobjects.appserver.WOComponent;
import com.webobjects.appserver.WOContext;
import com.webobjects.appserver.WOElement;
import com.webobjects.appserver.WORequest;
import com.webobjects.appserver.WOResponse;
import com.webobjects.appserver._private.WOConstantValueAssociation;
import com.webobjects.appserver._private.WODynamicElementCreationException;
import com.webobjects.appserver._private.WOFormatterRepository;
import com.webobjects.foundation.NSDictionary;
import com.webobjects.foundation.NSLog;
import com.webobjects.foundation.NSMutableArray;
import com.webobjects.foundation.NSMutableDictionary;
import com.webobjects.foundation.NSValidation.ValidationException;

public class AjaxInPlaceEditor extends AjaxDynamicElement {
  private WOAssociation _idAssociation;
  private WOAssociation _elementNameAssociation;
  private WOAssociation _classAssociation;
  private WOAssociation _valueAssociation;
  private WOAssociation _formatter;
  private WOAssociation _dateFormat;
  private WOAssociation _numberFormat;
  private WOAssociation _useDecimalNumber;
  
  public AjaxInPlaceEditor(String name, NSDictionary associations, WOElement children) {
    super(name, associations, children);
    _idAssociation = (WOAssociation) associations.objectForKey("id");
    _elementNameAssociation = (WOAssociation) associations.objectForKey("elementName");
    if (_elementNameAssociation == null) {
      _elementNameAssociation = new WOConstantValueAssociation("div");
    }
    _classAssociation = (WOAssociation) associations.objectForKey("class");
    _valueAssociation = (WOAssociation) associations.objectForKey("value");
    _formatter = (WOAssociation) associations.objectForKey("formatter");
    _dateFormat = (WOAssociation) associations.objectForKey("dateformat");
    _numberFormat = (WOAssociation) associations.objectForKey("numberformat");
    _useDecimalNumber = (WOAssociation) associations.objectForKey("useDecimalNumber");
    if (_dateFormat != null && _numberFormat != null) {
      throw new WODynamicElementCreationException("<" + getClass().getName() + "> Cannot have 'dateFormat' and 'numberFormat' attributes at the same time.");
    }
  }

  public NSDictionary createAjaxOptions(WOComponent component) {
    NSMutableArray ajaxOptionsArray = new NSMutableArray();
    ajaxOptionsArray.addObject(new AjaxOption("okButton", AjaxOption.BOOLEAN));
    ajaxOptionsArray.addObject(new AjaxOption("okText", AjaxOption.STRING));
    ajaxOptionsArray.addObject(new AjaxOption("cancelLink", AjaxOption.BOOLEAN));
    ajaxOptionsArray.addObject(new AjaxOption("cancelText", AjaxOption.STRING));
    ajaxOptionsArray.addObject(new AjaxOption("savingText", AjaxOption.STRING));
    ajaxOptionsArray.addObject(new AjaxOption("clickToEditText", AjaxOption.STRING));
    ajaxOptionsArray.addObject(new AjaxOption("formId", AjaxOption.STRING));
    ajaxOptionsArray.addObject(new AjaxOption("externalControl", AjaxOption.STRING));
    ajaxOptionsArray.addObject(new AjaxOption("rows", AjaxOption.NUMBER));
    ajaxOptionsArray.addObject(new AjaxOption("onComplete", AjaxOption.SCRIPT));
    ajaxOptionsArray.addObject(new AjaxOption("onFailure", AjaxOption.SCRIPT));
    ajaxOptionsArray.addObject(new AjaxOption("cols", AjaxOption.NUMBER));
    ajaxOptionsArray.addObject(new AjaxOption("size", AjaxOption.NUMBER));
    ajaxOptionsArray.addObject(new AjaxOption("highlightcolor", AjaxOption.STRING));
    ajaxOptionsArray.addObject(new AjaxOption("highlightendcolor", AjaxOption.STRING));
    ajaxOptionsArray.addObject(new AjaxOption("savingClassName", AjaxOption.STRING));
    ajaxOptionsArray.addObject(new AjaxOption("formClassName", AjaxOption.STRING));
    ajaxOptionsArray.addObject(new AjaxOption("loadTextURL", AjaxOption.STRING));
    ajaxOptionsArray.addObject(new AjaxOption("loadingText", AjaxOption.STRING));
    ajaxOptionsArray.addObject(new AjaxOption("callback", AjaxOption.SCRIPT));
    ajaxOptionsArray.addObject(new AjaxOption("submitOnBlur", AjaxOption.BOOLEAN));
    ajaxOptionsArray.addObject(new AjaxOption("valueWhenEmpty", AjaxOption.STRING));
    //ajaxOptionsArray.addObject(new AjaxOption("ajaxOptions", AjaxOption.SCRIPT));
    NSMutableDictionary options = AjaxOption.createAjaxOptionsDictionary(ajaxOptionsArray, component, associations());
    return options;
  }

  public void appendToResponse(WOResponse response, WOContext context) {
    WOComponent component = context.component();
    String id;
    if (_idAssociation == null) {
      id = AjaxUtils.toSafeElementID(context.elementID());
    }
    else {
      id = (String) _idAssociation.valueInComponent(component);
    }
    String elementName = (String) _elementNameAssociation.valueInComponent(component);
    String actionUrl = AjaxUtils.ajaxComponentActionUrl(context);
    super.appendToResponse(response, context);
    response.appendContentString("<");
    response.appendContentString(elementName);
    response.appendContentString(" id = \"");
    response.appendContentString(id);
    response.appendContentString("\"");
    if (_classAssociation != null) {
      String className = (String) _classAssociation.valueInComponent(component);
      response.appendContentString(" class = \"");
      response.appendContentString(className);
      response.appendContentString("\"");
    }
    response.appendContentString(">");

    _appendValueAttributeToResponse(response, context);

    response.appendContentString("</");
    response.appendContentString(elementName);
    response.appendContentString(">");
    AjaxUtils.appendScriptHeader(response);
    response.appendContentString("new Ajax.InPlaceEditor('");
    response.appendContentString(id);
    response.appendContentString("', '");
    response.appendContentString(actionUrl);
    response.appendContentString("',");
    NSDictionary options = createAjaxOptions(component);
    AjaxOptions.appendToResponse(options, response, context);
    response.appendContentString(");");
    AjaxUtils.appendScriptFooter(response);
  }

  protected void addRequiredWebResources(WOResponse response, WOContext context) {
    AjaxUtils.addScriptResourceInHead(context, response, "prototype.js");
    AjaxUtils.addScriptResourceInHead(context, response, "scriptaculous.js");
    AjaxUtils.addScriptResourceInHead(context, response, "wonder.js");
  }

  // Formatting/Parsing method "inspired by" WOTextField
  public WOActionResults handleRequest(WORequest request, WOContext context) {
    WOComponent component = context.component();
    String strValue = request.stringFormValueForKey("value");
    Object objValue = strValue;
    if (strValue != null) {
      Format format = null;
      if (strValue.length() != 0) {
        format = WOFormatterRepository.formatterForComponent(component, _dateFormat, _numberFormat, _formatter);
      }
      if (format != null) {
        try {
          Object parsedValue = format.parseObject(strValue);
          String formattedValue = format.format(parsedValue);
          objValue = format.parseObject(formattedValue);
        }
        catch (ParseException parseexception) {
          String valueKeyPath = _valueAssociation.keyPath();
          ValidationException validationexception = new ValidationException(parseexception.getMessage(), strValue, valueKeyPath);
          component.validationFailedWithException(validationexception, strValue, valueKeyPath);
          return null;
        }
        if (objValue != null && _useDecimalNumber != null && _useDecimalNumber.booleanValueInComponent(component)) {
          objValue = new BigDecimal(objValue.toString());
        }
      }
      else if (objValue.toString().length() == 0) {
        objValue = null;
      }
    }

    _valueAssociation.setValue(objValue, component);

    // just executing action, ignoring result
    valueForBinding("action", component);

    WOResponse response = AjaxUtils.createResponse(request, context);
    _appendValueAttributeToResponse(response, context);

    return response;
  }

  protected void _appendValueAttributeToResponse(WOResponse response, WOContext context) {
    WOComponent component = context.component();
    Object objValue = _valueAssociation.valueInComponent(component);
    if (objValue != null) {
      String strValue = null;
      Format format = WOFormatterRepository.formatterForInstance(objValue, component, _dateFormat, _numberFormat, _formatter);
      if (format != null) {
        try {
          String formattedStrValue = format.format(objValue);
          Object parsedValue = format.parseObject(formattedStrValue);
          strValue = format.format(parsedValue);
        }
        catch (IllegalArgumentException illegalargumentexception) {
          NSLog._conditionallyLogPrivateException(illegalargumentexception);
          strValue = null;
        }
        catch (ParseException parseexception) {
          NSLog._conditionallyLogPrivateException(parseexception);
          strValue = null;
        }
      }
      if (strValue == null) {
        strValue = objValue.toString();
      }
      response.appendContentString(strValue);
    }
  }

}
