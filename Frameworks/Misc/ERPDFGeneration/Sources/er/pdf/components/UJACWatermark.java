package er.pdf.components;

import com.webobjects.appserver.association.WOAssociation;
import com.webobjects.appserver.WOElement;
import com.webobjects.foundation.NSDictionary;

public class UJACWatermark extends UJACResource {

  public UJACWatermark(String name, NSDictionary<String, WOAssociation> associations, WOElement template) {
    super("watermark", associations, template);
  }

}
