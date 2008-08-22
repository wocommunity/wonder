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
 * A Pie Chart (see http://code.google.com/apis/chart/#pie_charts).
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
 * @binding custom custom query string parameters to append
 * @binding id the id of the img tag
 * @binding class the class of the img tag
 * @binding alt the alt text of the img tag
 * @binding encoding the explicit chart encoding to use ("simple", "extended", "text")
 * @binding normalize if true, values will be normalized relative to the max value
 * @binding maxValue if false, normalization is off or set to a number to override the max value 
 * @binding scaling if true, numbers will be scaled with an automatic min/max, or set to min/max string values (see http://code.google.com/apis/chart/#data_scaling)
 * @binding labels an array of labels for the pie slices
 *  
 * @author mschrag
 */
public class GCPieChart extends GCAbstractChart {
  protected WOAssociation _labels;

  public GCPieChart(String name, NSDictionary associations, WOElement element) {
    super(name, associations, element);
    _labels = (WOAssociation) associations.objectForKey("labels");
  }

  @Override
  protected void addQueryParameters(ERXMutableURL chartUrl, WOResponse response, WOContext context) {
    super.addQueryParameters(chartUrl, response, context);

    WOComponent component = context.component();
    chartUrl.setQueryParameter("cht", "p");
    
    NSArray<String> labels = AjaxUtils.arrayValueForAssociation(component, _labels);
    if (labels != null) {
      chartUrl.setQueryParameter("chl", labels.componentsJoinedByString("|"));
    }
  }
}
