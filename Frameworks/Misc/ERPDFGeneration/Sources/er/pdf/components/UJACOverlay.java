package er.pdf.components;

import com.webobjects.appserver.association.WOAssociation;
import com.webobjects.appserver.WOElement;
import com.webobjects.foundation.NSDictionary;

public class UJACOverlay extends UJACResource {

  public UJACOverlay(String name, NSDictionary<String, WOAssociation> associations, WOElement template) {
    super("overlay", associations, template);
  }

}
