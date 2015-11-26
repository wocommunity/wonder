package er.googlechart.components;

import java.net.MalformedURLException;
import java.util.List;

import com.webobjects.appserver.WOAssociation;
import com.webobjects.appserver.WOComponent;
import com.webobjects.appserver.WOContext;
import com.webobjects.appserver.WODynamicElement;
import com.webobjects.appserver.WOElement;
import com.webobjects.appserver.WOMessage;
import com.webobjects.appserver.WOResponse;
import com.webobjects.foundation.NSArray;
import com.webobjects.foundation.NSDictionary;
import com.webobjects.foundation.NSMutableArray;

import er.ajax.AjaxUtils;
import er.extensions.foundation.ERXMutableURL;
import er.extensions.foundation.ERXStringUtilities;
import er.extensions.foundation.ERXValueUtilities;
import er.googlechart.util.GCAbstractEncoding;
import er.googlechart.util.GCEncoding;
import er.googlechart.util.GCTextEncoding;

/**
 * The base class for all charts.  Note that not all bindings are available for all chart types.
 * 
 * @binding data the array, or array of arrays, of data
 * @binding size "wxh" format chart size ("300x400")
 * @binding width the width of the chart
 * @binding height the height of the chart
 * @binding colors an array of color values (for lines, bars, pie slices) 
 * @binding title the title of the chart
 * @binding titleColor the color of the chart title
 * @binding titleSize the size of the chart title
 * @binding backgroundStyle "solid", "gradient", or "stripes"
 * @binding background the solid color of the background
 * @binding chartBackgroundStyle "solid", "gradient", or "stripes"
 * @binding chartBackground the solid color of the chart background 
 * @binding transparency the transparency color of the chart background
 * @binding legend an array of legend values
 * @binding labeledAxes an array of axes that have labels ("x,y,r") (see http://code.google.com/apis/chart/#multiple_axes_labels)
 * @binding axisLabels an array of array of axis labels
 * @binding custom custom query string parameters to append
 * @binding id the id of the img tag
 * @binding class the class of the img tag
 * @binding alt the alt text of the img tag
 * @binding encoding the explicit chart encoding to use ("simple", "extended", "text")
 * @binding normalize if true, values will be normalized relative to the max value
 * @binding maxValue if false, normalization is off or set to a number to override the max value 
 * @binding scaling if true, numbers will be scaled with an automatic min/max, or set to min/max string values (see http://code.google.com/apis/chart/#data_scaling)
 * @binding fillArea the fill area (see http://code.google.com/apis/chart/#fill_area_marker)
 * @binding lineStyles the line styles (see http://code.google.com/apis/chart/#line_styles)
 * @binding rangeMarkers the string that specifies range markers (see http://code.google.com/apis/chart/#hor_line_marker)
 * @binding shapeMarkers the string that specifies shape markers (see http://code.google.com/apis/chart/#shape_markers2)
 * @binding gridLines the override for specifying all gridline values (see http://code.google.com/apis/chart/#grid)
 * @binding gridXStep the number of steps on the x axis between grid lines
 * @binding gridYStep the number of steps on the y axis between grid lines
 * @binding gridLineSize the number of pixels in the line of the dash part of the grid line
 * @binding gridBlankSize the numer of pixels in the spacing between dashes in the grid line
 *  
 * @author mschrag
 */
public abstract class GCAbstractChart extends WODynamicElement {
  protected WOAssociation _data;
  protected WOAssociation _size;
  protected WOAssociation _width;
  protected WOAssociation _height;
  protected WOAssociation _colors;
  protected WOAssociation _title;
  protected WOAssociation _titleColor;
  protected WOAssociation _titleSize;
  protected WOAssociation _backgroundStyle;
  protected WOAssociation _background;
  protected WOAssociation _chartBackgroundStyle;
  protected WOAssociation _chartBackground;
  protected WOAssociation _transparency;
  protected WOAssociation _legend;
  protected WOAssociation _labeledAxes;
  protected WOAssociation _axisLabels;
  protected WOAssociation _custom;
  protected WOAssociation _id;
  protected WOAssociation _class;
  protected WOAssociation _alt;
  protected WOAssociation _encoding;
  protected WOAssociation _normalize;
  protected WOAssociation _maxValue;
  protected WOAssociation _scaling;
  protected WOAssociation _fillArea;
  protected WOAssociation _lineStyles;
  protected WOAssociation _rangeMarkers;
  protected WOAssociation _shapeMarkers;
  protected WOAssociation _gridLines;
  protected WOAssociation _gridXStep;
  protected WOAssociation _gridYStep;
  protected WOAssociation _gridLineSize;
  protected WOAssociation _gridBlankSize;

  public GCAbstractChart(String name, NSDictionary associations, WOElement element) {
    super(name, associations, element);
    _data = (WOAssociation) associations.objectForKey("data");
    _size = (WOAssociation) associations.objectForKey("size");
    _width = (WOAssociation) associations.objectForKey("width");
    _height = (WOAssociation) associations.objectForKey("height");
    _colors = (WOAssociation) associations.objectForKey("colors");
    _title = (WOAssociation) associations.objectForKey("title");
    _titleColor = (WOAssociation) associations.objectForKey("titleColor");
    _titleSize = (WOAssociation) associations.objectForKey("titleSize");
    _backgroundStyle = (WOAssociation) associations.objectForKey("backgroundStyle");
    _background = (WOAssociation) associations.objectForKey("background");
    _chartBackgroundStyle = (WOAssociation) associations.objectForKey("chartBackgroundStyle");
    _chartBackground = (WOAssociation) associations.objectForKey("chartBackground");
    _transparency = (WOAssociation) associations.objectForKey("transparency");
    _legend = (WOAssociation) associations.objectForKey("legend");
    _labeledAxes = (WOAssociation) associations.objectForKey("labeledAxes");
    _axisLabels = (WOAssociation) associations.objectForKey("axisLabels");
    _custom = (WOAssociation) associations.objectForKey("custom");
    _id = (WOAssociation) associations.objectForKey("id");
    _class = (WOAssociation) associations.objectForKey("class");
    _alt = (WOAssociation) associations.objectForKey("alt");
    _encoding = (WOAssociation) associations.objectForKey("encoding");
    _normalize = (WOAssociation) associations.objectForKey("normalize");
    _maxValue = (WOAssociation) associations.objectForKey("maxValue");
    _scaling = (WOAssociation) associations.objectForKey("scaling");
    _fillArea = (WOAssociation) associations.objectForKey("fillArea");
    _lineStyles = (WOAssociation) associations.objectForKey("lineStyles");
    _rangeMarkers = (WOAssociation) associations.objectForKey("rangeMarkers");
    _shapeMarkers = (WOAssociation) associations.objectForKey("shapeMarkers");
    _gridLines = (WOAssociation) associations.objectForKey("gridLines");
    _gridXStep = (WOAssociation) associations.objectForKey("gridXStep");
    _gridYStep = (WOAssociation) associations.objectForKey("gridYStep");
    _gridLineSize = (WOAssociation) associations.objectForKey("gridLineSize");
    _gridBlankSize = (WOAssociation) associations.objectForKey("gridBlankSize");
  }

  protected void addQueryParameters(ERXMutableURL chartUrl, WOResponse response, WOContext context) {
    // DO NOTHING
  }

  protected void checkData(List<List<Number>> data, WOResponse response, WOContext context) {
    // DO NOTHING
  }

  protected Number maxValue(WOResponse response, WOContext context) {
    return _maxValue == null ? null : GCEncoding.numberFromObject(_maxValue.valueInComponent(context.component()));
  }

  protected boolean normalize(WOResponse response, WOContext context) {
    WOComponent component = context.component();
    boolean normalize = true;
    if (_normalize == null) {
      if (_maxValue != null) {
        Object maxValue = _maxValue.valueInComponent(component);
        if (maxValue instanceof Boolean) {
          normalize = ((Boolean) maxValue).booleanValue();
        }
        else if (maxValue == null) {
          normalize = true;
        }
      }
    }
    else {
      normalize = _normalize.booleanValueInComponent(component);
    }
    return normalize;
  }

  protected Object scaling(WOResponse response, WOContext context) {
    Object scaling = _scaling == null ? null : _scaling.valueInComponent(context.component());
    return scaling;
  }

  protected GCAbstractEncoding encoding(List<List<Number>> data, WOResponse response, WOContext context) {
    GCAbstractEncoding encoding;
    if (_encoding != null) {
      String encodingName = (String) _encoding.valueInComponent(context.component());
      try {
        encoding = Class.forName("GC" + ERXStringUtilities.capitalize(encodingName) + "Encoding").asSubclass(GCAbstractEncoding.class).newInstance();
      }
      catch (Exception e) {
        throw new IllegalArgumentException("Unable to create the encoding named '" + encodingName + "'.");
      }
    }
    else {
      Object scaling = scaling(response, context);
      if ((scaling instanceof Boolean && ((Boolean) scaling).booleanValue()) || scaling != null) {
        encoding = new GCTextEncoding();
      }
      else {
        Number maxValue = maxValue(response, context);
        if (maxValue != null) {
          encoding = GCEncoding.recommendedEncoding(maxValue, data);
        }
        else {
          encoding = GCEncoding.recommendedEncoding(normalize(response, context), data);
        }
      }
    }
    return encoding;
  }

  protected String encode(List<List<Number>> data, WOResponse response, WOContext context) {
    GCAbstractEncoding encoding = encoding(data, response, context);
    String encodedValue;
    Number maxValue = maxValue(response, context);
    if (maxValue != null) {
      encodedValue = encoding.encode(maxValue, data);
    }
    else {
      encodedValue = encoding.encode(normalize(response, context), data);
    }
    return encodedValue;
  }

  protected String styleKey(String styleName) {
    String styleKey;
    if ("solid".equals(styleName)) {
      styleKey = "s";
    }
    else if ("stripes".equals(styleName)) {
      styleKey = "ls";
    }
    else if ("gradient".equals(styleName)) {
      styleKey = "lg";
    }
    else {
      throw new IllegalArgumentException("Unknown style name '" + styleName + "'.");
    }
    return styleKey;
  }

  @Override
  public void appendToResponse(WOResponse response, WOContext context) {
    ERXMutableURL chartUrl = new ERXMutableURL();
    chartUrl.setProtocol("http");
    chartUrl.setHost("chart.apis.google.com");
    chartUrl.setPath("/chart");

    WOComponent component = context.component();

    int width = 300;
    int height = 200;
    if (_size != null) {
      String sizeStr = (String) _size.valueInComponent(component);
      if (sizeStr != null) {
        String[] sizeStrs = sizeStr.split("x");
        width = Integer.parseInt(sizeStrs[0]);
        height = Integer.parseInt(sizeStrs[1]);
      }
    }
    else {
      if (_width != null) {
        width = ERXValueUtilities.intValueWithDefault(_width.valueInComponent(component), width);
      }
      if (_height != null) {
        height = ERXValueUtilities.intValueWithDefault(_height.valueInComponent(component), height);
      }
    }
    chartUrl.setQueryParameter("chs", width + "x" + height);

    NSArray<String> colors = AjaxUtils.arrayValueForAssociation(component, _colors);
    if (colors != null) {
      chartUrl.setQueryParameter("chco", colors.componentsJoinedByString(","));
    }

    List<List<Number>> data = null;
    if (_data != null) {
      Object dataValue = _data.valueInComponent(component);
      if (dataValue instanceof String && ((String) dataValue).length() >= 2 && ((String) dataValue).charAt(1) == ':') {
        chartUrl.setQueryParameter("chd", (String) dataValue);
      }
      else {
        data = GCEncoding.convertToNumberLists(AjaxUtils.arrayValueForAssociation(component, _data));
        chartUrl.setQueryParameter("chd", encode(data, response, context));
      }
    }

    Object scaling = scaling(response, context);
    if (scaling instanceof Boolean && ((Boolean) scaling).booleanValue()) {
      if (data != null) {
        NSMutableArray<String> scaleNumbers = new NSMutableArray<String>();
        for (List<Number> innerList : data) {
          Float minValue = Float.valueOf(GCEncoding.minValueInList(innerList));
          scaleNumbers.addObject(String.format("%1$.1f", minValue));

          Number maxValue = maxValue(response, context);
          if (maxValue == null) {
            maxValue = Float.valueOf(GCEncoding.minValueInList(innerList));
          }
          scaleNumbers.addObject(String.format("%1$.1f", maxValue));
        }

        chartUrl.setQueryParameter("chds", scaleNumbers.componentsJoinedByString(","));
      }
    }
    else {
      chartUrl.setQueryParameter("chds", (String) scaling);
    }

    if (_title != null) {
      String title = (String) _title.valueInComponent(component);
      if (title != null) {
        chartUrl.setQueryParameter("chtt", title);
      }
    }

    if (_titleColor != null || _titleSize != null) {
      String titleColor = "454545";
      if (_titleColor != null) {
        titleColor = (String) _titleColor.valueInComponent(component);
      }
      if (_titleSize != null) {
        Object titleSize = _titleSize.valueInComponent(component);
        chartUrl.setQueryParameter("chts", titleColor + "," + titleSize);
      }
      else {
        chartUrl.setQueryParameter("chts", titleColor);
      }
    }

    if (_lineStyles != null) {
      chartUrl.setQueryParameter("chls", (String) _lineStyles.valueInComponent(component));
    }

    if (_fillArea != null) {
      chartUrl.setQueryParameter("chm", (String) _fillArea.valueInComponent(component));
    }

    if (_rangeMarkers != null) {
      chartUrl.setQueryParameter("chm", (String) _rangeMarkers.valueInComponent(component));
    }

    if (_shapeMarkers != null) {
      chartUrl.setQueryParameter("chm", (String) _shapeMarkers.valueInComponent(component));
    }

    if (_gridLines != null) {
      chartUrl.setQueryParameter("chg", (String) _gridLines.valueInComponent(component));
    }
    else if (_gridXStep != null || _gridYStep != null || _gridLineSize != null || _gridBlankSize != null) {
      StringBuilder chg = new StringBuilder();
      if (_gridXStep != null && _gridYStep != null) {
        chg.append(_gridXStep.valueInComponent(component));
        chg.append(',');
        chg.append(_gridYStep.valueInComponent(component));
      }

      if (_gridLineSize != null || _gridBlankSize != null) {
        if (chg.length() == 0) {
          chg.append("20,50");
        }

        if (_gridLineSize != null) {
          chg.append(',');
          chg.append(_gridLineSize.valueInComponent(component));
        }

        if (_gridBlankSize != null) {
          if (_gridLineSize == null) {
            chg.append(",5");
          }
          chg.append(',');
          chg.append(_gridBlankSize.valueInComponent(component));
        }
      }
      chartUrl.setQueryParameter("chg", chg.toString());
    }

    StringBuilder fill = new StringBuilder();

    String backgroundStyle = "solid";
    if (_backgroundStyle != null) {
      backgroundStyle = (String) _backgroundStyle.valueInComponent(component);
    }
    if (_background != null) {
      fill.append("bg,");
      fill.append(styleKey(backgroundStyle));
      fill.append(',');
      fill.append(_background.valueInComponent(component));
    }

    String chartBackgroundStyle = "solid";
    if (_chartBackgroundStyle != null) {
      chartBackgroundStyle = (String) _chartBackgroundStyle.valueInComponent(component);
    }
    if (_chartBackground != null) {
      if (fill.length() > 0) {
        fill.append('|');
      }
      fill.append("c,");
      fill.append(styleKey(chartBackgroundStyle));
      fill.append(',');
      fill.append(_chartBackground.valueInComponent(component));
    }

    if (_chartBackground != null || _transparency != null) {
      if (_transparency != null) {
        if (fill.length() > 0) {
          fill.append('|');
        }
        fill.append("a,s,");
        fill.append(_transparency.valueInComponent(component));
      }
    }

    if (fill.length() > 0) {
      chartUrl.setQueryParameter("chf", fill.toString());
    }

    NSArray<String> legend = AjaxUtils.arrayValueForAssociation(component, _legend);
    if (legend != null) {
      chartUrl.setQueryParameter("chdl", legend.componentsJoinedByString("|"));
    }

    NSArray<String> labeledAxes = AjaxUtils.arrayValueForAssociation(component, _labeledAxes);
    if (labeledAxes != null) {
      chartUrl.setQueryParameter("chxt", labeledAxes.componentsJoinedByString(","));
    }

    NSArray<Object> axisLabels = AjaxUtils.arrayValueForAssociation(component, _axisLabels);
    if (axisLabels != null) {
      StringBuilder axisLabelsStr = new StringBuilder();
      for (int i = 0; i < axisLabels.count(); i++) {
        Object singleAxisLabels = axisLabels.objectAtIndex(i);
        if (i > 0) {
          axisLabelsStr.append('|');
        }
        axisLabelsStr.append(i + ":|");
        if (singleAxisLabels instanceof Object[]) {
          axisLabelsStr.append(new NSArray<Object>((Object[]) singleAxisLabels).componentsJoinedByString("|"));
        }
        else {
          axisLabelsStr.append(singleAxisLabels);
        }
      }
      chartUrl.setQueryParameter("chxl", axisLabelsStr.toString());
    }

    if (_custom != null) {
      String custom = (String) _custom.valueInComponent(component);
      if (custom != null) {
        try {
          chartUrl.addQueryParameters(custom);
        }
        catch (MalformedURLException e) {
          throw new IllegalArgumentException("Failed to add the query parameters '" + custom + "'.", e);
        }
      }
    }
    addQueryParameters(chartUrl, response, context);

    response.appendContentString("<img");
    if (_id != null) {
      response._appendTagAttributeAndValue("id", (String) _id.valueInComponent(component), true);
    }
    if (_class != null) {
      response._appendTagAttributeAndValue("class", (String) _class.valueInComponent(component), true);
    }
    if (_alt != null) {
      response._appendTagAttributeAndValue("alt", (String) _alt.valueInComponent(component), true);
    }
    String chartSrc = WOMessage.stringByEscapingHTMLAttributeValue(chartUrl.toExternalForm());
    response._appendTagAttributeAndValue("src", chartSrc, false);
    response._appendTagAttributeAndValue("width", String.valueOf(width), false);
    response._appendTagAttributeAndValue("height", String.valueOf(height), false);
    response.appendContentString("/>");
  }
}
