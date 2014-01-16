package er.googlechart.components;

import com.webobjects.appserver.WOAssociation;
import com.webobjects.appserver.WOComponent;
import com.webobjects.appserver.WOContext;
import com.webobjects.appserver.WOElement;
import com.webobjects.appserver.WOResponse;
import com.webobjects.foundation.NSArray;
import com.webobjects.foundation.NSDictionary;

import er.ajax.AjaxUtils;
import er.extensions.foundation.ERXMutableURL;

/**
 * A bar chart (see http://code.google.com/apis/chart/#bar_charts).
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
 * @binding orientation "horizontal" or "vertical" orientation
 * @binding barWidth the width of the bars of the graph
 * @binding spacing the spacing between bars
 * @binding groupSpacing the spacing between groupds of bars
 * @binding zeroLine an array of zeroLines for the data sets
 *  
 * @author mschrag
 */
public class GCBarChart extends GCAbstractChart {
  protected WOAssociation _orientation;
  protected WOAssociation _stacked;
  protected WOAssociation _barWidth;
  protected WOAssociation _spacing;
  protected WOAssociation _groupSpacing;
  protected WOAssociation _zeroLine;

  public GCBarChart(String name, NSDictionary associations, WOElement element) {
    super(name, associations, element);
    _orientation = (WOAssociation) associations.objectForKey("orientation");
    _stacked = (WOAssociation) associations.objectForKey("stacked");
    _barWidth = (WOAssociation) associations.objectForKey("barWidth");
    _spacing = (WOAssociation) associations.objectForKey("spacing");
    _groupSpacing = (WOAssociation) associations.objectForKey("groupSpacing");
    _zeroLine = (WOAssociation) associations.objectForKey("zeroLine");
  }

  @Override
  protected void addQueryParameters(ERXMutableURL chartUrl, WOResponse response, WOContext context) {
    super.addQueryParameters(chartUrl, response, context);

    WOComponent component = context.component();
    String orientation = "vertical";
    if (_orientation != null) {
      orientation = (String) _orientation.valueInComponent(component);
    }

    boolean stacked = true;
    if (_stacked != null) {
      stacked = _stacked.booleanValueInComponent(component);
    }

    if (!"horizontal".equals(orientation) && !"vertical".equals(orientation)) {
      throw new IllegalArgumentException("Unknown orientation '" + orientation + "'.");
    }

    String orientationKey = orientation.substring(0, 1);
    String stackedKey = (stacked) ? "s" : "g";
    chartUrl.setQueryParameter("cht", "b" + orientationKey + stackedKey);

    StringBuilder chbh = new StringBuilder();
    String barWidth = "23";
    String spacing = "4";
    if (_barWidth != null) {
      barWidth = (String) _barWidth.valueInComponent(component);
      chbh.append(barWidth);
    }
    
    if (_spacing != null) {
      if (_barWidth == null) {
        chbh.append(barWidth);
      }
      chbh.append(',');

      spacing = (String) _spacing.valueInComponent(component);
      chbh.append(spacing);
    }

    if (_groupSpacing != null) {
      if (_barWidth == null) {
        chbh.append(barWidth);
        chbh.append(',');
      }
      if (_spacing == null) {
        chbh.append(spacing);
      }
      chbh.append(',');

      String groupSpacing = (String) _groupSpacing.valueInComponent(component);
      chbh.append(groupSpacing);
    }
    if (chbh.length() > 0) {
      chartUrl.setQueryParameter("chbh", chbh.toString());
    }
    
    NSArray<Object> zeroLine = AjaxUtils.arrayValueForAssociation(component, _zeroLine);
    if (zeroLine != null) {
      chartUrl.setQueryParameter("chp", zeroLine.componentsJoinedByString(","));
    }
  }
}
