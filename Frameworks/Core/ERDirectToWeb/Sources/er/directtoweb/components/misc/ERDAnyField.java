//
// ERDAnyField.java: Class file for WO Component 'ERDAnyField'
// Project ERDirectToWeb
//
// Created by giorgio on 07/10/04
//
package er.directtoweb.components.misc;

import com.webobjects.appserver.WOContext;
import com.webobjects.woextensions.WOAnyField;

public class ERDAnyField extends WOAnyField {
	/**
	 * Do I need to update serialVersionUID?
	 * See section 5.6 <cite>Type Changes Affecting Serialization</cite> on page 51 of the 
	 * <a href="http://java.sun.com/j2se/1.4/pdf/serial-spec.pdf">Java Object Serialization Spec</a>
	 */
	private static final long serialVersionUID = 1L;

    public ERDAnyField(WOContext context) {
        super(context);
    }

}
