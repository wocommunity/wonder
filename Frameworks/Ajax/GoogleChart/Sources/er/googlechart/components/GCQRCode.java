package er.googlechart.components;

import com.webobjects.appserver.WOAssociation;
import com.webobjects.appserver.WOComponent;
import com.webobjects.appserver.WOContext;
import com.webobjects.appserver.WOElement;
import com.webobjects.appserver.WOResponse;
import com.webobjects.foundation.NSDictionary;

import er.extensions.foundation.ERXMutableURL;

/**
 * QR Codes (see http://code.google.com/apis/chart/#qrcodes).
 * 
 * @binding text the text to encode
 * @binding outputEncoding the output encoding to use (defaults to UTF-8)
 * @binding ecLevel the error correction level ("L", "M", "Q", or "H")
 * @binding margin the margin around the code
 * @binding size "wxh" format chart size ("300x400")
 * @binding width the width of the chart
 * @binding height the height of the chart
 * @binding custom custom query string parameters to append
 * @binding id the id of the img tag
 * @binding class the class of the img tag
 * @binding alt the alt text of the img tag
 *  
 * @author mschrag
 */
public class GCQRCode extends GCAbstractChart {
  protected WOAssociation _text;
  protected WOAssociation _outputEncoding;
  protected WOAssociation _ecLevel;
  protected WOAssociation _margin;

  public GCQRCode(String name, NSDictionary associations, WOElement element) {
    super(name, associations, element);
    _text = (WOAssociation) associations.objectForKey("text");
    _outputEncoding = (WOAssociation) associations.objectForKey("outputEncoding");
  }

  @Override
  protected void addQueryParameters(ERXMutableURL chartUrl, WOResponse response, WOContext context) {
    super.addQueryParameters(chartUrl, response, context);

    WOComponent component = context.component();
    chartUrl.setQueryParameter("cht", "qr");

    String text = null;
    if (_text != null) {
      text = (String) _text.valueInComponent(component);
    }
    if (text != null) {
      chartUrl.setQueryParameter("chl", text);
    }

    String outputEncoding = null;
    if (_outputEncoding != null) {
      outputEncoding = (String) _outputEncoding.valueInComponent(component);
    }
    if (outputEncoding != null) {
      chartUrl.setQueryParameter("choe", outputEncoding);
    }

    if (_ecLevel != null || _margin != null) {
      StringBuilder chld = new StringBuilder();
      if (_ecLevel != null) {
        String ecLevel = (String) _ecLevel.valueInComponent(component);
        chld.append(ecLevel);
      }
      if (_margin != null) {
        if (chld.length() > 0) {
          chld.append('|');
        }
        String margin = (String) _margin.valueInComponent(component);
        chld.append(margin);
      }
      chartUrl.setQueryParameter("chld", chld.toString());
    }
  }
}
