package er.pdf.components;

import com.webobjects.appserver.association.WOAssociation;
import com.webobjects.appserver.WOElement;
import com.webobjects.foundation.NSDictionary;

public class UJACInsertDocument extends UJACResource {

  public UJACInsertDocument(String name, NSDictionary<String, WOAssociation> associations, WOElement template) {
    super("insert-document", associations, template);
  }

}
