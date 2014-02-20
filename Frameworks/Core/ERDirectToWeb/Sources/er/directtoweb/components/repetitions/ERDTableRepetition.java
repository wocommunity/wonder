package er.directtoweb.components.repetitions;

import com.webobjects.appserver.WOContext;

/**
 * @d2wKey object
 */
public class ERDTableRepetition extends ERDListPageRepetition {
	/**
	 * Do I need to update serialVersionUID?
	 * See section 5.6 <cite>Type Changes Affecting Serialization</cite> on page 51 of the 
	 * <a href="http://java.sun.com/j2se/1.4/pdf/serial-spec.pdf">Java Object Serialization Spec</a>
	 */
	private static final long serialVersionUID = 1L;

    public ERDTableRepetition(WOContext context) {
        super(context);
    }
}
