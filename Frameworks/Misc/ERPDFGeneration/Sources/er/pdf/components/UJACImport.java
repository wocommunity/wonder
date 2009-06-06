package er.pdf.components;

import com.webobjects.appserver.WOAssociation;
import com.webobjects.appserver.WOElement;
import com.webobjects.foundation.NSDictionary;

public class UJACImport extends UJACResource {

  public UJACImport(String name, NSDictionary<String, WOAssociation> associations, WOElement template) {
    super("import", associations, template);
  }

}
