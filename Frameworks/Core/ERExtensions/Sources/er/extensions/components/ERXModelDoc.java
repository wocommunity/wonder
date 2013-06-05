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
	/**
	 * Do I need to update serialVersionUID?
	 * See section 5.6 <cite>Type Changes Affecting Serialization</cite> on page 51 of the 
	 * <a href="http://java.sun.com/j2se/1.4/pdf/serial-spec.pdf">Java Object Serialization Spec</a>
	 */
	private static final long serialVersionUID = 1L;

  public ERXModelDoc(WOContext context) {
    super(context);
  }

  @Override
  public boolean isStateless() {
    return true;
  }
}