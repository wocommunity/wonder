//
// ERD2WQueryToManyField.java: Class file for WO Component 'ERD2WQueryToManyField'
// Project ERDirectToWeb
//
// Created by giorgio on 05/10/04
//

package er.directtoweb.components.relationships;

import com.webobjects.appserver.WOContext;
import com.webobjects.directtoweb.D2WQueryToManyField;

/**
 * @d2wKey name
 */
public class ERD2WQueryToManyField extends D2WQueryToManyField {
	/**
	 * Do I need to update serialVersionUID?
	 * See section 5.6 <cite>Type Changes Affecting Serialization</cite> on page 51 of the 
	 * <a href="http://java.sun.com/j2se/1.4/pdf/serial-spec.pdf">Java Object Serialization Spec</a>
	 */
	private static final long serialVersionUID = 1L;

    public ERD2WQueryToManyField(WOContext context) {
        super(context);
    }
    
}
