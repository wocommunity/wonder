package er.pdf.components;

import com.webobjects.appserver.WOAssociation;
import com.webobjects.appserver.WOElement;
import com.webobjects.foundation.NSDictionary;

public class UJACImage extends UJACResource {

  public UJACImage(String name, NSDictionary<String, WOAssociation> associations, WOElement template) {
    super("image", associations, template);
  }

}
