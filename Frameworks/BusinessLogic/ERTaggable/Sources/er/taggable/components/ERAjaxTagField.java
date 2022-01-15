package er.taggable.components;

import com.webobjects.appserver.WOContext;

/**
 * ERAjaxTagField is an alternative implementation based on Mike Schrag's original ERTagField.
 * It uses the Insignia tag input JS module via the CCTagEditor wrapper component.
 *  
 * @author fpeters
 * 
 * @binding taggable the ERTaggable to manage
 * @binding class the css class of the text field
 * @binding style the css style of the text field
 * @binding id the dom ID of the text field
 */
public class ERAjaxTagField extends ERTagField {

    /**
	 * Do I need to update serialVersionUID?
	 * See section 5.6 <cite>Type Changes Affecting Serialization</cite> on page 51 of the 
	 * <a href="http://java.sun.com/j2se/1.4/pdf/serial-spec.pdf">Java Object Serialization Spec</a>
	 */
	private static final long serialVersionUID = 1L;

	public ERAjaxTagField(WOContext context) {
	    super(context);
	}

}