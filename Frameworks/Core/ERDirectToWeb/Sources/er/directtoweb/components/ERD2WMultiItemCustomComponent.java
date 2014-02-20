package er.directtoweb.components;

import com.webobjects.appserver.WOContext;
import com.webobjects.directtoweb.D2WCustomComponent;

/**
 * Displays multiple items in one line. Useful for "firstName lastName" stuff.
 * Rules:
 * 100 : .. => displayPropertyKeys = (..., name, ...)
 * 100 : .. and (propertyKey = "name") => displayPropertyKeys = (firstName, lastName)
 * 100 : .. and (propertyKey = "name") => componentName = ERD2WMultiItemCustomComponent
 * Then all firstName and lastName will end up in one line with a "name" label.
 * @author ak
 * @d2wKey propertyKey
 * @d2wKey displayPropertyKeys
 * @d2wKey displayNameForProperty
 * @d2wKey componentName
 */
public class ERD2WMultiItemCustomComponent extends D2WCustomComponent {
	/**
	 * Do I need to update serialVersionUID?
	 * See section 5.6 <cite>Type Changes Affecting Serialization</cite> on page 51 of the 
	 * <a href="http://java.sun.com/j2se/1.4/pdf/serial-spec.pdf">Java Object Serialization Spec</a>
	 */
	private static final long serialVersionUID = 1L;

    public ERD2WMultiItemCustomComponent(WOContext context) {
        super(context);
    }
}
