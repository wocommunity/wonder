package er.googlechart.components;

import java.util.List;

import com.webobjects.appserver.WOComponent;
import com.webobjects.appserver.WOContext;
import com.webobjects.appserver.WOElement;
import com.webobjects.appserver.WOResponse;
import com.webobjects.foundation.NSDictionary;

import er.extensions.foundation.ERXMutableURL;

/**
 * A scatter plot (see http://code.google.com/apis/chart/#scatter_plot).
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
public class GCScatterPlot extends GCAbstractChart {
  public GCScatterPlot(String name, NSDictionary associations, WOElement element) {
    super(name, associations, element);
  }

  @Override
  protected void addQueryParameters(ERXMutableURL chartUrl, WOResponse response, WOContext context) {
    super.addQueryParameters(chartUrl, response, context);

    WOComponent component = context.component();
    chartUrl.setQueryParameter("cht", "s");
  }

  @Override
  protected void checkData(List<List<Number>> data, WOResponse response, WOContext context) {
    super.checkData(data, response, context);

    if (data.size() < 2 || data.size() > 3) {
      throw new IllegalArgumentException("Scatter plots require at least 2 data sets, but no more than 3 data sets. See http://code.google.com/apis/chart/#scatter_plot");
    }

    if (data.get(0).size() != data.get(1).size()) {
      throw new IllegalArgumentException("Scatter plots must have the same number of items in each data set");
    }

    if (data.size() == 3 && data.get(0).size() != data.get(2).size()) {
      throw new IllegalArgumentException("Scatter plots must have the same number of items in each data set");
    }
  }
}
