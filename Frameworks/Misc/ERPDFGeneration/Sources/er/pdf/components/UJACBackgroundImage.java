package er.pdf.components;

import com.webobjects.appserver.association.WOAssociation;
import com.webobjects.appserver.WOElement;
import com.webobjects.foundation.NSDictionary;

public class UJACBackgroundImage extends UJACResource {

  public UJACBackgroundImage(String name, NSDictionary<String, WOAssociation> associations, WOElement template) {
    super("background-image", associations, template);
  }

}
