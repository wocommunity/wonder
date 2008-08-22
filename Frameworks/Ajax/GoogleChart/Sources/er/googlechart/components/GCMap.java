package er.googlechart.components;

import java.util.List;

import com.webobjects.appserver.WOAssociation;
import com.webobjects.appserver.WOComponent;
import com.webobjects.appserver.WOContext;
import com.webobjects.appserver.WOElement;
import com.webobjects.appserver.WOResponse;
import com.webobjects.foundation.NSArray;
import com.webobjects.foundation.NSDictionary;

import er.ajax.AjaxUtils;
import er.extensions.foundation.ERXMutableURL;
import er.googlechart.util.GCAbstractEncoding;
import er.googlechart.util.GCTextEncoding;

/**
 * Geographic maps (see http://code.google.com/apis/chart/#maps).
 * 
 * @binding data the array, or array of arrays, of data
 * @binding size "wxh" format chart size ("300x400")
 * @binding width the width of the chart
 * @binding height the height of the chart
 * @binding colors an array of color values (default color followed by gradient colors) 
 * @binding backgroundStyle "solid", "gradient", or "stripes"
 * @binding background the solid color of the background
 * @binding custom custom query string parameters to append
 * @binding id the id of the img tag
 * @binding class the class of the img tag
 * @binding alt the alt text of the img tag
 * @binding encoding the explicit chart encoding to use ("simple", "extended", "text")
 * @binding normalize if true, values will be normalized relative to the max value
 * @binding maxValue if false, normalization is off or set to a number to override the max value 
 * @binding area the geographic area to show ("africa", "asia", "europe", "middle_east", "south_america", "usa", "world")
 * @binding regions the regions to color 
 *  
 * @author mschrag
 */
public class GCMap extends GCAbstractChart {
  protected WOAssociation _area;
  protected WOAssociation _regions;

  public GCMap(String name, NSDictionary associations, WOElement element) {
    super(name, associations, element);
    _area = (WOAssociation) associations.objectForKey("area");
    _regions = (WOAssociation) associations.objectForKey("regions");
  }

  @Override
  protected boolean normalize(WOResponse response, WOContext context) {
    return false;
  }

  @Override
  protected GCAbstractEncoding encoding(List<List<Number>> data, WOResponse response, WOContext context) {
    return new GCTextEncoding();
  }

  @Override
  protected void addQueryParameters(ERXMutableURL chartUrl, WOResponse response, WOContext context) {
    super.addQueryParameters(chartUrl, response, context);

    WOComponent component = context.component();
    chartUrl.setQueryParameter("cht", "t");

    if (_area != null) {
      String area = (String) _area.valueInComponent(component);
      chartUrl.setQueryParameter("chtm", area);
    }

    NSArray<String> regions = AjaxUtils.arrayValueForAssociation(component, _regions);
    if (regions != null) {
      chartUrl.setQueryParameter("chld", regions.componentsJoinedByString(""));
    }
  }
}
