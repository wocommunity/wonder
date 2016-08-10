package er.directtoweb.components.repetitions;

import org.apache.log4j.Logger;

import com.webobjects.appserver.WOContext;
import com.webobjects.appserver.WODisplayGroup;

/**
 * Class for DirectToWeb Component ERDQueryPageRepetition.
 *
 * @binding sample sample binding explanation
 * @d2wKey sample sample d2w key
 *
 * @author ak on Mon Sep 01 2003
 * 
 * @d2wKey componentName
 * @d2wKey colSpan
 * @d2wKey rowSpan
 * @d2wKey maxColumns
 * @d2wKey matrixLayoutVertical
 * @d2wKey hidePropertyName
 * @d2wKey useMatrixLayout
 * @d2wKey useTableLayout
 * @d2wKey propertyNameComponentName
 * @d2wKey sectionComponentName
 * @d2wKey useHorizontalLayoutTables
 */
public class ERDQueryPageRepetition extends ERDAttributeRepetition {
	/**
	 * Do I need to update serialVersionUID?
	 * See section 5.6 <cite>Type Changes Affecting Serialization</cite> on page 51 of the 
	 * <a href="http://java.sun.com/j2se/1.4/pdf/serial-spec.pdf">Java Object Serialization Spec</a>
	 */
	private static final long serialVersionUID = 1L;

    /** logging support */
    private static final Logger log = Logger.getLogger(ERDQueryPageRepetition.class);
	
    /**
     * Public constructor
     * @param context the context
     */
    public ERDQueryPageRepetition(WOContext context) {
        super(context);
    }

    /** component does not synchronize it's variables */
    @Override
    public boolean synchronizesVariablesWithBindings() { return false; }

    public WODisplayGroup displayGroup() {
        return (WODisplayGroup)valueForBinding("displayGroup");
    }
}
