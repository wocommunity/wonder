package er.extensions.components;

import com.webobjects.appserver.WOComponent;
import com.webobjects.appserver.WOContext;

/**
 * ERXModelDoc provides a common renderer for Entity Modeler's
 * Entity, Attribute, and Relationship userInfo._EntityModeler.documentation.  There
 * is a sample stylesheet provided in ERXModelDoc.css.
 *  
 * @author mschrag
 * @binding object the object (entity, relationship, etc) to display documentation for
 */
public class ERXModelDoc extends WOComponent {
  public ERXModelDoc(WOContext context) {
    super(context);
  }

  @Override
  public boolean isStateless() {
    return true;
  }

  @Override
  public boolean synchronizesVariablesWithBindings() {
    return false;
  }
}