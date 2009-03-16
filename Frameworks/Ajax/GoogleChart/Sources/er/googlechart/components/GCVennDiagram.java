package er.googlechart.components;

import java.util.List;

import com.webobjects.appserver.WOComponent;
import com.webobjects.appserver.WOContext;
import com.webobjects.appserver.WOElement;
import com.webobjects.appserver.WOResponse;
import com.webobjects.foundation.NSDictionary;

import er.extensions.foundation.ERXMutableURL;

/**
 * A Venn diagram (see http://code.google.com/apis/chart/#venn).  Note that Venn diagrams have very specific requirements
 * for the data set that is passed in.
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
 * 
 * @author mschrag
 */
public class GCVennDiagram extends GCAbstractChart {
  public GCVennDiagram(String name, NSDictionary associations, WOElement element) {
    super(name, associations, element);
  }

  @Override
  protected void addQueryParameters(ERXMutableURL chartUrl, WOResponse response, WOContext context) {
    super.addQueryParameters(chartUrl, response, context);

    WOComponent component = context.component();
    chartUrl.setQueryParameter("cht", "v");
  }

  @Override
  protected void checkData(List<List<Number>> data, WOResponse response, WOContext context) {
    super.checkData(data, response, context);

    if (data.size() != 1 || data.get(0).size() != 7) {
      throw new IllegalArgumentException("Venn diagrams must have exactly seven values. See http://code.google.com/apis/chart/#venn");
    }
  }
}
