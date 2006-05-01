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
  private NSDictionary myAssociations;
  private WOAssociation myIDAssociation;
  private WOAssociation myElementNameAssociation;
  private WOAssociation myClassAssociation;
  private WOAssociation myValueAssociation;
  private WOAssociation myFormatter;
  private WOAssociation myDateFormat;
  private WOAssociation myNumberFormat;
  private WOAssociation myUseDecimalNumber;

  public AjaxInPlaceEditor(String _name, NSDictionary _associations, WOElement _children) {
    super(_name, _associations, _children);
    myAssociations = _associations;
    myIDAssociation = (WOAssociation) _associations.objectForKey("id");
    myElementNameAssociation = (WOAssociation) _associations.objectForKey("elementName");
    if (myElementNameAssociation == null) {
      myElementNameAssociation = new WOConstantValueAssociation("div");
    }
    myClassAssociation = (WOAssociation) _associations.objectForKey("class");
    myValueAssociation = (WOAssociation) _associations.objectForKey("value");
    myFormatter = (WOAssociation) _associations.objectForKey("formatter");
    myDateFormat = (WOAssociation) _associations.objectForKey("dateformat");
    myNumberFormat = (WOAssociation) _associations.objectForKey("numberformat");
    myUseDecimalNumber = (WOAssociation) _associations.objectForKey("useDecimalNumber");
    if (myDateFormat != null && myNumberFormat != null) {
      throw new WODynamicElementCreationException("<" + getClass().getName() + "> Cannot have 'dateFormat' and 'numberFormat' attributes at the same time.");
    }
  }

  public NSDictionary createAjaxOptions(WOComponent _component) {
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
    //ajaxOptionsArray.addObject(new AjaxOption("ajaxOptions", AjaxOption.SCRIPT));
    NSMutableDictionary options = AjaxOption.createAjaxOptionsDictionary(ajaxOptionsArray, _component, myAssociations);
    return options;
  }

  public void appendToResponse(WOResponse _response, WOContext _context) {
    WOComponent component = _context.component();
    String id;
    if (myIDAssociation == null) {
      id = AjaxUtils.toSafeElementID(_context.elementID());
    }
    else {
      id = (String) myIDAssociation.valueInComponent(component);
    }
    String elementName = (String) myElementNameAssociation.valueInComponent(component);
    String actionUrl = _context.componentActionURL();
    super.appendToResponse(_response, _context);
    _response.appendContentString("<");
    _response.appendContentString(elementName);
    _response.appendContentString(" id = \"");
    _response.appendContentString(id);
    _response.appendContentString("\"");
    if (myClassAssociation != null) {
      String className = (String) myClassAssociation.valueInComponent(component);
      _response.appendContentString(" class = \"");
      _response.appendContentString(id);
      _response.appendContentString("\"");
    }
    _response.appendContentString(">");

    _appendValueAttributeToResponse(_response, _context);

    _response.appendContentString("</");
    _response.appendContentString(elementName);
    _response.appendContentString(">");
    _response.appendContentString("<script type = \"text/javascript\"><!--\n");
    _response.appendContentString("new Ajax.InPlaceEditor('");
    _response.appendContentString(id);
    _response.appendContentString("', '");
    _response.appendContentString(actionUrl);
    _response.appendContentString("',");
    NSDictionary options = createAjaxOptions(component);
    AjaxOptions.appendToResponse(options, _response, _context);
    _response.appendContentString(");");
    _response.appendContentString("\n//--></script>");
  }

  protected void addRequiredWebResources(WOResponse _response, WOContext _context) {
    AjaxUtils.addScriptResourceInHead(_context, _response, "prototype.js");
    AjaxUtils.addScriptResourceInHead(_context, _response, "scriptaculous.js");
    AjaxUtils.addScriptResourceInHead(_context, _response, "effects.js");
    AjaxUtils.addScriptResourceInHead(_context, _response, "builder.js");
    AjaxUtils.addScriptResourceInHead(_context, _response, "dragdrop.js");
    AjaxUtils.addScriptResourceInHead(_context, _response, "controls.js");
  }

  // Formatting/Parsing method "inspired by" WOTextField
  protected WOActionResults handleRequest(WORequest _request, WOContext _context) {
    WOComponent component = _context.component();
    String strValue = _request.stringFormValueForKey("value");
    Object objValue = strValue;
    if (strValue != null) {
      Format format = null;
      if (strValue.length() != 0) {
        format = WOFormatterRepository.formatterForComponent(component, myDateFormat, myNumberFormat, myFormatter);
      }
      if (format != null) {
        try {
          Object parsedValue = format.parseObject(strValue);
          String formattedValue = format.format(parsedValue);
          objValue = format.parseObject(formattedValue);
        }
        catch (ParseException parseexception) {
          String valueKeyPath = myValueAssociation.keyPath();
          ValidationException validationexception = new ValidationException(parseexception.getMessage(), strValue, valueKeyPath);
          component.validationFailedWithException(validationexception, strValue, valueKeyPath);
          return null;
        }
        if (objValue != null && myUseDecimalNumber != null && myUseDecimalNumber.booleanValueInComponent(component)) {
          objValue = new BigDecimal(objValue.toString());
        }
      }
      else if (objValue.toString().length() == 0) {
        objValue = null;
      }
    }

    myValueAssociation.setValue(objValue, component);

    WOResponse response = AjaxUtils.createResponse(_context);
    _appendValueAttributeToResponse(response, _context);

    return response;
  }

  protected void _appendValueAttributeToResponse(WOResponse _response, WOContext _context) {
    WOComponent component = _context.component();
    Object objValue = myValueAssociation.valueInComponent(component);
    if (objValue != null) {
      String strValue = null;
      Format format = WOFormatterRepository.formatterForInstance(objValue, component, myDateFormat, myNumberFormat, myFormatter);
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
      _response.appendContentString(strValue);
    }
  }

}
