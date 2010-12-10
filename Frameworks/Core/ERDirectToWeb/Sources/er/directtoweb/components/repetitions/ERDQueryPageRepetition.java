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
 * @project ERDirectToWeb
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
    public boolean synchronizesVariablesWithBindings() { return false; }


    public WODisplayGroup displayGroup() {
        return (WODisplayGroup)valueForBinding("displayGroup");
    }
}
