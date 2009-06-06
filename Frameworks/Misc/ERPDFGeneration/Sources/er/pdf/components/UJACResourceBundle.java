package er.pdf.components;

import com.webobjects.appserver.WOAssociation;
import com.webobjects.appserver.WOElement;
import com.webobjects.foundation.NSDictionary;

public class UJACResourceBundle extends UJACResource {

  public UJACResourceBundle(String name, NSDictionary<String, WOAssociation> associations, WOElement template) {
    super("resource-bundle", associations, template);
  }

}
